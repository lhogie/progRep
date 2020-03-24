package m412;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Node
{
	public static final int DEFAULT_PORT = 23457;

	// this will be used for both sending and receiving of messages
	final DatagramSocket udp;

	// the port on which the UDP server will listen to
	final int port;

	// the username for this node. Can be whatever string.
	private final String username;

	// this consumer sets what to do when a message is received
	// by default the message is simply printed on to stdout
	public Consumer<Message> messageHandler = msg -> System.out.println(msg);

	// the peers known be this node
	final Set<Peer> peers;

	// a set of IDs for all messages received in the past
	final Set<Long> receivedMessages = new HashSet<>();

	public Node(String username, int port) throws IOException
	{
		this.username = username;
		this.port = port;
		System.out.println("opening UDP server on port " + port);
		this.udp = new DatagramSocket(port);

		String filename = System.getProperty("user.home") + "/" + username + "-peers.txt";
		System.out.println("reading " + filename);

		File f = new File(filename);

		if ( ! f.exists())
		{
			String luchogiehome = "91.166.171.231\t" + DEFAULT_PORT;
			Files.write(f.toPath(), luchogiehome.getBytes());
		}

		peers = Peer.loadFromFile(f);

		// starts the server
		new Thread(() -> {
			try
			{
				byte[] buf = new byte[10000];

				while (true)
				{
					// creates a new packet for the reception of a new message
					DatagramPacket p = new DatagramPacket(buf, buf.length);

					// blocks until a packet arrives
					udp.receive(p);

					try
					{
						// decodes the message object out of the bytes received
						Message msg = Message.fromBytes(buf);

						// registers the sender as a new neighbors for this node
						Peer peer = ensurePeerKnown(p.getAddress(), p.getPort());

						if ( ! msg.senders.isEmpty())
						{
							String lastRelay = msg.senders.get(msg.senders.size() - 1);
							peer.username = lastRelay;
						}

						// if the message was not already received in the past
						if ( ! receivedMessages.contains(msg.ID))
						{
							// registers this message to as to never process it
							receivedMessages.add(msg.ID);

							// notify of the incoming message
							messageHandler.accept(msg);

							// forwards the message to all my neighbors
							broadcast(msg);
						}
					}
					catch (Exception e)
					{
						System.err.println(
								"Error processing from " + p.getAddress().getHostName());
						e.printStackTrace(System.out);
					}
				}
			}
			catch (Exception e)
			{
				// some I/O error. Let's see what happened
				e.printStackTrace();
			}
		}).start();
	}

	private Peer ensurePeerKnown(InetAddress ip, int port)
	{
		Peer peer = findPeerInfo(ip, port);

		// this peer was unknown so far
		if (peer == null)
			// create a new entry from him
			peers.add(peer = new Peer(ip, port));

		return peer;
	}

	private Peer findPeerInfo(InetAddress ip, int port)
	{
		for (Peer n : peers)
			if (n.ip.equals(ip) && n.port == port)
				return n;

		return null;
	}

	/**
	 * Sends the given message to all known nodes.
	 */
	public void broadcast(Message msg)
	{
		for (Peer n : peers)
		{
			send(msg, n);
		}
	}

	/*
	 * Sends the given message to the specified recipient node.
	 */
	public boolean send(Message msg, Peer recipient)
	{
		// make sure the message knows who's sending it
		msg.makeSureImDeclaredAsTheSender(username);

		// defines that streamed bytes will be stored in an array
		byte[] buf = msg.toBytes();

		// then send it through an UDP datagram
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		p.setAddress(recipient.ip);
		p.setPort(recipient.port);

		try
		{
			udp.send(p);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public Peer createPeerInfo() throws UnknownHostException
	{
		Peer i = new Peer(InetAddress.getLocalHost(), port);
		i.username = username;
		return i;
	}

}
