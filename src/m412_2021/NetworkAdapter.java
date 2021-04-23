package m412_2021;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NetworkAdapter {
	Set<PeerInfo> peers = new HashSet<>();
	private final DatagramSocket udpServer = new DatagramSocket(5567);
	public AbstractApplication client;

	public NetworkAdapter() throws IOException {
		System.out.println("UDP server listening on port " + udpServer.getLocalPort());
		var buf = new byte[64000];

		new Thread(() -> {
			while (true) {
				var p = new DatagramPacket(buf, buf.length);
				try {
					udpServer.receive(p);
					var from = lookupPeer(p.getAddress(), p.getPort());

					if (from == null) {
						peers.add(from = new PeerInfo(p.getAddress(), p.getPort()));
					}

					client.process(from, p);
					broadcast(from, fromBytes(p.getData()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public PeerInfo lookupPeer(InetAddress ip, int port) {
		for (var peer : peers) {
			if (peer.ip.equals(ip) && peer.port == port) {
				return peer;
			}
		}

		return null;
	}

	public Set<InetAddress> ips() {
		return peers.stream().map(c -> c.ip).collect(Collectors.toSet());
	}

	public void broadcast(PeerInfo from, Object o) {
		var buf = toBytes(o);
		var p = new DatagramPacket(buf, buf.length);

		for (var peer : peers) {
			if(!peer.equals(from)) {
				p.setAddress(peer.ip);
				p.setPort(peer.port);

				try {
					udpServer.send(p);
				} catch (IOException e) {
					System.err.println("error sending to " + peer);
				}
			}
		}
	}

	public static byte[] toBytes(Object o) {
		try {
			var bos = new ByteArrayOutputStream();
			var oos = new ObjectOutputStream(bos);
			oos.writeObject(o);
			oos.close();
			return bos.toByteArray();
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	public static Object fromBytes(byte[] bytes) {
		try {
			var in = new ObjectInputStream(new ByteArrayInputStream(bytes));
			var object = in.readObject();
			in.close();
			return object;
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}
}
