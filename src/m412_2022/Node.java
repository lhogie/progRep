package m412_2022;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.nustaq.serialization.FSTConfiguration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Node {
	private final DatagramSocket socket;
	private final Set<InetAddress> candidateAddresses = new HashSet<>();
	private final Set<InetAddress> peers = new HashSet<>();
	private final int port;
	private final Set<Long> alreadyReceivedMessages = new HashSet<>();
	private final String nickname;
	private static final Random random = new Random();
	Serializer serializer = new FSTSerializer();

	// the base abstraction of a message does not define any content or role
	public static class Message implements Serializable {
		// just an randomly chosen ID
		public long id = random.nextLong();

		// and a list of node that relayed the message
		public List<String> route = new ArrayList<>();
	}

	// a specific type of message that carries a string
	public static class TextMessage extends Message {
		public String text;

		@Override
		public String toString() {
			return route + "> " + text;
		}
	}

	// a specific type of message that is an ACK of an other message
	// whose it carries the ID
	public static class ACKMessage extends Message {
		// the ID of the message we are interested in
		public long originalMessageID;

		// the list of nodes that had received that message
		public List<String> actualRecipients = new ArrayList<>();

		@Override
		public String toString() {
			return "ACK received from  " + route.get(0) + " who informs us the message reached " + actualRecipients;
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Java version: " + System.getProperty("java.version"));
		new Node(6665, System.getProperty("user.name"));
	}

	public Node(int port, String nickname) throws SocketException, UnknownHostException {
		this.socket = new DatagramSocket(port);
		this.port = port;
		this.nickname = nickname;

		final String prefix = "192.168.225.";

		for (var suffix : new int[] { 56, 137, 36, 28, 54, 129, 167, 34, 79, 164, 75, 4, 230, 112 }) {
			candidateAddresses.add(InetAddress.getByName(prefix + suffix));
		}

		// choose random peers
		while (peers.size() < 4) {
			var p = candidateAddresses.toArray(new InetAddress[0])[random.nextInt(candidateAddresses.size())];
			peers.add(p);
			System.out.println("peers: " + peers);
		}

		// user input thread
		new Thread(() -> {
			final Scanner userInput = new Scanner(System.in);

			try {
				while (true) {
					System.out.println("Please type a message: ");
					var msg = new TextMessage();
					msg.text = userInput.nextLine();
					sendToAllPeers(msg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		// broadcasting thread
		new Thread(() -> {
			final byte[] buf = new byte[1000000];

			while (true) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);

				try {
					socket.receive(packet);
					var data = Arrays.copyOf(packet.getData(), packet.getLength());
					var msg = serializer.deserializeMessage(data);

					// if the message was never received
					if (!alreadyReceivedMessages.contains(msg.id)) {
						alreadyReceivedMessages.add(msg.id);

						if (msg instanceof TextMessage) {
							var ack = new ACKMessage();
							ack.originalMessageID = msg.id;
							ack.actualRecipients = msg.route;
							sendToAllPeers(ack);
							System.out.println(msg);
						} else if (msg instanceof ACKMessage) {
							System.out.println(msg);
						} else {
							throw new IllegalStateException("unknown message type:  " + msg.getClass().getName());
						}

						sendToAllPeers(msg);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void sendToAllPeers(Message msg) throws IOException {
		alreadyReceivedMessages.add(msg.id);
		msg.route.add(nickname);
		var buf = serializer.serialize(msg);
		// System.out.println("sending " + new String(buf));

		// and send it to all my peers
		for (var peer : peers) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length, peer, port);
			socket.send(packet);
			packet = new DatagramPacket(buf, buf.length);
		}
	}

	public static interface Serializer {
		Message deserializeMessage(byte[] bytes) throws IOException;

		byte[] serialize(Message msg) throws IOException;
	}

	public static class JavaSerializer implements Serializer {

		@Override
		public Message deserializeMessage(byte[] bytes) throws IOException {
			var bis = new ByteArrayInputStream(bytes);
			var ois = new ObjectInputStream(bis);

			try {
				var m = (Message) ois.readObject();
				ois.close();
				return m;
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}

		@Override
		public byte[] serialize(Message msg) throws IOException {
			var bos = new ByteArrayOutputStream();
			var oos = new ObjectOutputStream(bos);
			oos.writeObject(msg);
			return bos.toByteArray();
		}
	}

	public static class JSONSerializer implements Serializer {
		static final Gson gson;

		static {
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			gson = builder.create();
		}

		@Override
		public Message deserializeMessage(byte[] bytes) throws IOException {
			String received = new String(bytes);

			// deserialize the UDP packet to a Message object
			return gson.fromJson(received, Message.class);
		}

		@Override
		public byte[] serialize(Message msg) throws IOException {
			return gson.toJson(msg).getBytes();
		}
	}

	public static class FSTSerializer implements Serializer {
		static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

		@Override
		public Message deserializeMessage(byte[] bytes) throws IOException {
			return (Message) conf.asObject(bytes);
		}

		@Override
		public byte[] serialize(Message msg) throws IOException {
			return conf.asByteArray(msg);
		}
	}
}
