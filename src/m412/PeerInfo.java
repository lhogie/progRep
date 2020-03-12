package m412;

import java.net.InetAddress;

public class PeerInfo
{
	public final InetAddress ip;
	public final int port;
	public String username;

	public PeerInfo(InetAddress ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}

	@Override
	public int hashCode()
	{
		return String.valueOf(ip.hashCode() + port).hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		return o.getClass() == PeerInfo.class && o.hashCode() == hashCode();
	}

	@Override
	public String toString()
	{
		return username + "@" + ip.getHostName() + ":" + port;
	}

}
