package m412;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

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

	public Set<PeerInfo> loadFromFile(File f) throws IOException
	{
		Set<PeerInfo> s = new HashSet<>();
		BufferedReader r = new BufferedReader(new FileReader(f));

		while (true)
		{
			String[] line = r.readLine().split("[ \\t]+");

			if (line == null)
				break;

			InetAddress ip = InetAddress.getByName(line[0]);
			int port = Integer.valueOf(line[1]);
			PeerInfo p = new PeerInfo(ip, port);
			s.add(p);
		}

		r.close();
		return s;
	}

	public void toFile(Set<PeerInfo> peerInfos, File outputFile) throws IOException
	{
		PrintWriter writer = new PrintWriter(new FileWriter(outputFile));

		for (PeerInfo peer : peerInfos)
		{
			writer.println(peer.ip.getHostName() + "\t" + peer.port);
		}

		writer.close();
	}

}
