package m412;

import java.io.IOException;
import java.util.Scanner;

public class CmdLine
{
	public static void main(String[] args) throws IOException
	{
		Scanner sc = new Scanner(System.in);

		String defaultUsername = System.getProperty("user.name");
		System.out.println("What is your username? (default is " + defaultUsername + ")");
		String username = sc.nextLine();

		if (username == null || username == null)
			username = defaultUsername;

		int defaultPort = Node.DEFAULT_PORT;
		System.out.println("Which port? (default is " + defaultPort + ")");
		String port = sc.nextLine();

		if (port == null || port.isEmpty())
			port = String.valueOf(defaultPort);

		Node node = new Node(username, Integer.valueOf(port));

		for (Peer p : node.peers)
		{
			System.out.println(p);
		}

		node.chat = msg -> System.out.println(msg);

		while (true)
		{
			String line = sc.nextLine();
			node.broadcast(new Message(line));
		}
	}
}
