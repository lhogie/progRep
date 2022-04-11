package m412_2022;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Node {
	private final DatagramSocket socket;
	private final Set<InetAddress> candidateAddresses = new HashSet<>();
	private final Set<InetAddress> peers = new HashSet<>();
	private final int port;
	private final Set<Long> alreadyReceivedMessages = new HashSet<>();
	private final String nickname;
	static final Random random = new Random();
	Serializer serializer = new FSTSerializer();
	File directory = new File(System.getProperty("user.home") + "/" + "m412");
	
	

	public Node(int port, String nickname) throws SocketException, UnknownHostException {
		this.socket = new DatagramSocket(port);
		this.port = port;
		this.nickname = nickname;

		if (!directory.exists()) {
			System.out.println("creating directory " + directory.getAbsolutePath());
			directory.mkdirs();
		}
		
		
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
					var line = userInput.nextLine();

					if (line.startsWith("/")) {
						line = line.substring(1);
						var tokens = line.split(" +");

						if (tokens.length == 0) {
							System.err.println("command not specified");
						} else {
							var cmd = tokens[0];

							if (cmd.equals("list")) {
								sendToAllPeers(new FileListRequest());
							} else {
								System.err.println("Unknown command " + cmd);
							}
						}
					} else {
						var msg = new TextMessage();
						msg.text = line;
						sendToAllPeers(msg);
					}
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
						} else if (msg instanceof FileListRequest) {
							var r = new FileListResponse();
							r.filenames = Arrays.asList(directory.list());
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
}
