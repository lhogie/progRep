package m412_2020;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class Peer implements Serializable
{
	public final InetAddress ip;
	public final int port;
	public String username;

	public Peer(InetAddress ip, int port)
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
		return o.getClass() == Peer.class && o.hashCode() == hashCode();
	}

	@Override
	public String toString()
	{
		String s = ip.getHostName() + ":" + port;

		if (username != null)
			s = username + "@" + s;

		return s;
	}

	public static Set<Peer> loadFromFile(File f) throws IOException
	{
		System.out.println(f.getAbsolutePath());
		Set<Peer> s = new HashSet<>();

		if (f.exists())
		{
			BufferedReader r = new BufferedReader(new FileReader(f));

			while (true)
			{
				String line = r.readLine();

				if (line == null)
					break;

				String[] elements = line.split("[ \\t]+");
				InetAddress ip = InetAddress.getByName(elements[0]);
				int port = Integer.valueOf(elements[1]);
				Peer p = new Peer(ip, port);
				s.add(p);
			}

			r.close();
		}

		return s;
	}

	public static void toFile(Set<Peer> peers, File outputFile) throws IOException
	{
		PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
		peers.forEach(peer -> writer.println(peer.ip.getHostName() + "\t" + peer.port));
		writer.close();
	}



}
