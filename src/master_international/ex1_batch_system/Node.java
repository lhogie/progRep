package master_international.ex1_batch_system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class Node {
	public static void main(String[] args) throws IOException {
		new Node();
	}

	public interface Command {
		void process(List<String> elements) throws IOException;
	}

	public abstract class Service {
		abstract String getName();

		abstract void query(JsonNode receivedMsg) throws IOException;

		abstract void response(JsonNode receivedMsg) throws IOException;

		ObjectNode createResponseMsg(Map<String, String> keys) {
			keys.put("type", getName());
			keys.put("mode", "response");
			return createMsgNode(keys);
		}
	}

	public class ResultHandler<R> {
		long creationDate;
		R accumulator;
		int nbResults;

		public ResultHandler(R init) {
			this.accumulator = init;
		}
	}

	List<InetAddress> neighbors = new ArrayList<>();
	List<String> senders = new ArrayList<>();
	Path sharedDirectory = new File("/Users/lhogie/shared").toPath();
	DatagramSocket udpServer = new DatagramSocket(12345);
	ObjectMapper mapper = new ObjectMapper();
	Set<String> alreadySent = new HashSet<>();
	Map<String, Command> userCmds = new HashMap<>();
	Set<Service> services = new HashSet<>();
	Map<String, ResultHandler<?>> accumulators = new HashMap<>();
	String nickname = System.getProperty("user.name");
	final int chunkSize = 64000;

	public Node() throws IOException {
//		neighbors.add(InetAddress.getByName("192.168.64.202"));
//		neighbors.add(InetAddress.getByName("192.168.64.215"));
//		neighbors.add(InetAddress.getByName("192.168.64.164"));

		userCmds.put("ls", args -> {
			var msg = createMsgNode(Map.of("type", "ls", "mode", "query"));
			var id = msg.get("id").asText();
			accumulators.put(id, new ResultHandler<>(new HashSet<String>()));
			send(msg);
			System.out.println("results in " + id);
		});

		userCmds.put("a", args -> {
			accumulators.entrySet().forEach(e -> System.out
					.println(e.getKey() + "\t" + e.getValue().nbResults + "\t" + e.getValue().accumulator));
		});
		
		userCmds.put("ca", args -> accumulators.clear());
		userCmds.put("who", args -> neighbors.forEach(p -> System.out.println(p)));
		userCmds.put("senders", args -> senders.forEach(p -> System.out.println(p)));
		userCmds.put("help", args -> System.out.println(userCmds.keySet()));
		userCmds.put("exit", args -> System.exit(0));

		services.add(new Service() {

			@Override
			public String getName() {
				return "ls";
			}

			@Override
			public void query(JsonNode msg) throws IOException {
				var array = new ArrayNode(null);
				Files.list(sharedDirectory).forEach(f -> array.add(new TextNode(f.toString())));
				ObjectNode n = createResponseMsg(Map.of());
				n.set("content", array);
				send(n);
			}

			@Override
			public void response(JsonNode msg) throws IOException {
				var reqID = msg.get("requestID").asText();
				var a = (ResultHandler<Set<String>>) accumulators.get(reqID);
				((ArrayNode) msg.get("content")).forEach(n -> a.accumulator.add(n.asText()));
				a.nbResults++;
			}
		});

		services.add(new Service() {
			@Override
			public String getName() {
				return "get";
			}

			@Override
			public void query(JsonNode msg) throws IOException {
				var nameNode = msg.get("filename").asText();
				var file = new File(sharedDirectory.toFile(), nameNode);

				if (file.exists()) {
					var fis = new FileInputStream(file);

					for (int c = 0;; c++) {
						byte[] chunk = fis.readNBytes(chunkSize);

						if (chunk.length == 0) { // EOF
							send(createResponseMsg(Map.of("chunckIndex", "-1")));
							fis.close();
							return;
						} else {
							send(createResponseMsg(Map.of("chunckIndex", "" + c, "content",
									new String(Base64.getEncoder().encode(chunk)))));
						}
					}
				}
			}

			@Override
			public void response(JsonNode msg) throws IOException {
				var blockIndex = msg.get("chunkIndex").asInt();
				var bytes = Base64.getDecoder().decode(msg.get("content").asText());
				var file = new File(sharedDirectory.toFile(), msg.get("filename").asText());
				var h = new RandomAccessFile(file, "w");
				h.setLength(Math.max(h.length(), blockIndex * chunkSize));
				h.skipBytes(blockIndex * chunkSize);
				h.write(bytes);
				h.close();
			}
		});

		new Thread(() -> {
			final var buf = new byte[64000];

			while (true) {
				try {
					var p = new DatagramPacket(buf, buf.length);
					udpServer.receive(p);
					neighbors.add(p.getAddress());
					var bytes = Arrays.copyOf(p.getData(), p.getLength());
					System.out.println("received: " + new String(bytes));
					var msg = (ObjectNode) mapper.readTree(bytes);

					if (!alreadySent.contains(msg.get("id").asText()))
						send(msg);

					((ArrayNode) msg.get("route")).forEach(n -> senders.add(n.asText()));
					var type = msg.get("type").asText();
					var cmd = services.stream().filter(q -> q.getName().equals(type)).findAny().get();
					var mode = msg.get("mode").asText();
					cmd.getClass().getMethod(mode, ObjectNode.class).invoke(cmd, msg);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
		}).start();

		while (true) {
			try {
				var line = System.console().readLine("> ");

				if (line == null) {
					break;
				} else if (line.startsWith("/")) {
					var elements = Arrays.asList(line.trim().split(" +"));
					var cmdName = elements.getFirst().substring(1);
					var cmd = userCmds.get(cmdName);

					if (cmd == null) {
						System.err.println("unknow command: " + cmdName);
					} else {
						cmd.process(elements.subList(1, elements.size()));
					}
				} else {
					send(createMsgNode(Map.of("type", "msg", "content", line)));
				}
			} catch (Exception err) {
				err.printStackTrace();
			}
		}
	}

	private ObjectNode createMsgNode(Map<String, String> keys) {
		var n = mapper.createObjectNode();
		keys.entrySet().forEach(e -> n.set(e.getKey(), new TextNode(e.getValue())));
		n.set("id", new TextNode("" + Long.toHexString(new Random().nextLong())));
		n.set("route", new ArrayNode(null));
		return n;
	}

	private void send(ObjectNode node) throws IOException {
		alreadySent.add(node.get("id").asText());
		((ArrayNode) node.get("route")).add(new TextNode(nickname));
		var data = node.toString().getBytes();
		var p = new DatagramPacket(data, data.length);
		p.setPort(12345);

		for (var to : neighbors) {
			p.setAddress(to);
			udpServer.send(p);
			// System.out.println("=> " + to + " : " + new String(p.getData()));
		}
	}

}
