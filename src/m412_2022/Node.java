package m412_2022;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Node {
	private final DatagramSocket socket;
	private final ServerSocket tcpServer;
	private final Set<InetAddress> candidateAddresses = new HashSet<>();
	private final Set<InetAddress> peersAddress = new HashSet<>();
	private final Map<String, InetAddress> name2address = new HashMap<>();
	private final int port;
	private final Set<Long> alreadyReceivedMessages = new HashSet<>();
	private final String nickname;
	static final Random random = new Random();
	Serializer serializer = new FSTSerializer();
	File directory = new File(System.getProperty("user.home") + "/" + "m412");

	public Node(int port, String nickname) throws IOException {
		this.socket = new DatagramSocket(port);
		this.tcpServer = new ServerSocket(port);
		this.port = port;
		this.nickname = nickname;

		if (!directory.exists()) {
			System.out.println("creating directory " + directory.getAbsolutePath());
			directory.mkdirs();
		}

		final String prefix = "192.168.136.";

		for (var suffix : new int[] { 56, 137, 36, 28, 29, 54, 129, 167, 34, 79, 164, 75, 4, 230, 112 }) {
			candidateAddresses.add(InetAddress.getByName(prefix + suffix));
		}

		// choose random peers
		while (peersAddress.size() < 4) {
			var p = candidateAddresses.toArray(new InetAddress[0])[random.nextInt(candidateAddresses.size())];
			peersAddress.add(p);
			System.out.println("peers: " + peersAddress);
		}

		var filename2ownerName = new HashMap<String, Set<String>>();

		// user input thread
		new Thread(() -> {
			final var userInput = new Scanner(System.in);

			while (true) {
				try {
					System.out.println("Please type a message: ");
					var line = userInput.nextLine();

					if (line.startsWith("/")) {
						line = line.substring(1);
						var cmdScanner = new Scanner(line);
						var cmd = cmdScanner.next();

						if (cmd == null) {
							System.err.println("missing command");
						} else if (cmd.equals("peers")) {
							System.out.println(peersAddress);
						} else if (cmd.equals("list")) {
							System.out.println("sending list request");
							sendToAllPeers(new FileListRequest());
						} else if (cmd.equals("tcpget")) {
							cmdScanner.useDelimiter("$");
							var filename = cmdScanner.next();
							System.out.println("searching for " + filename);

							if (filename == null) {
								System.err.println("missing filename");
							} else {
								filename = filename.trim();
								var ownersName = filename2ownerName.get(filename);

								if (ownersName == null || ownersName.isEmpty()) {
									System.err.println("none is known to have this file");
								} else {
									var fromName = ownersName.iterator().next();
									var ip = name2address.get(fromName);

									if (ip == null) {
										System.err.println("can't get the IP address of " + fromName);
									} else {
										System.out.println("downloading file from " + fromName + " at " + ip);
										var socket = new Socket(ip, port);
										var dos = new DataOutputStream(socket.getOutputStream());
										dos.writeUTF(filename);
										socket.getOutputStream().write(filename.getBytes());
										var file = new File(directory, filename);
										var fos = new FileOutputStream(file);
										var gzipIn = new GZIPInputStream(socket.getInputStream());
										gzipIn.transferTo(fos);
										socket.close();
										System.out.println("file received!");
									}
								}
							}
						} else {
							System.err.println("Unknown command " + cmd);
						}
					} else {
						var msg = new TextMessage();
						msg.text = line;
						sendToAllPeers(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

		// broadcasting thread
		new Thread(() -> {
			final byte[] buf = new byte[1000000];

			while (true) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);

				try {
					socket.receive(packet);

					if (!peersAddress.contains(packet.getAddress())) {
						System.out.println("adding new peer " + packet.getAddress());
						peersAddress.add(packet.getAddress());
					}

					var data = Arrays.copyOf(packet.getData(), packet.getLength());
					var gzipIn = new GZIPInputStream(new ByteArrayInputStream(data));
					data = gzipIn.readAllBytes();

					var msg = serializer.deserializeMessage(data);

					var neighbor = msg.route.get(msg.route.size() - 1);
					name2address.put(neighbor, packet.getAddress());

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
							System.out.println("Request received from " + msg.route);
							var r = new FileListResponse();
							r.filenames = Arrays.asList(directory.list());
							sendToAllPeers(r);
						} else if (msg instanceof FileListResponse) {
							System.out.println("list response received from " + msg.route);
							var r = (FileListResponse) msg;

							if (!r.filenames.isEmpty()) {
								System.out.println("files shared by " + msg.route.get(0) + "> " + r.filenames);

								// registers the file location into the local node
								for (var filename : r.filenames) {
									var owners = filename2ownerName.get(filename);

									if (owners == null) {
										filename2ownerName.put(filename, owners = new HashSet<>());
									}

									owners.add(r.route.get(0));
								}
							}
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

		// TCP server thread
		new Thread(() -> {
			while (true) {
				new Thread(() -> {
					try {
						var socket = tcpServer.accept();
						var ios = new DataInputStream(socket.getInputStream());
						var filename = ios.readUTF();
						System.out.println("sending file  " + filename);
						var file = new File(directory, filename);

						if (file.exists()) {
							System.err.println("you already have this file");
						} else {
							var fis = new FileInputStream(file);
							var gzipOut = new GZIPOutputStream(socket.getOutputStream());
							fis.transferTo(gzipOut);
							gzipOut.close();
							fis.close();
						}

						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			}
		}).start();
	}

	public synchronized void sendToAllPeers(Message msg) throws IOException {
		alreadyReceivedMessages.add(msg.id);
		msg.route.add(nickname);
		var buf = serializer.serialize(msg);

		var o = new ByteArrayOutputStream();
		var gzipO = new GZIPOutputStream(o);
		gzipO.write(buf);
		gzipO.close();
		buf = o.toByteArray();

		// System.out.println("sending " + new String(buf));

		// and send it to all my peers
		for (var ip : peersAddress) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, port);

			try {
				socket.send(packet);
			} catch (Exception e) {
				System.err.println("error: " + e.getMessage());
				System.err.println("peer: " + ip);
			}
			packet = new DatagramPacket(buf, buf.length);
		}
	}
}
