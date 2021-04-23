package m412_2021;
import java.net.InetAddress;

class PeerInfo {
	final InetAddress ip;
	final int port;
	long lastSeen;

	public PeerInfo(InetAddress address, int port) {
		this.ip = address;
		this.port = port;
	}

	@Override
	public int hashCode() {
		return (ip.toString() + port).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		var c = (PeerInfo) obj;
		return c.ip.equals(ip) && c.port == port;
	}

	@Override
	public String toString() {
		return ip.getHostName() + ":" + port;
	}
}