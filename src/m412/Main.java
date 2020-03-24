package m412;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		String username = JOptionPane.showInputDialog(null, "What is your username:",
				System.getProperty("user.name"));
		int port = Integer
				.valueOf(JOptionPane.showInputDialog(null, "Which port:", Node.DEFAULT_PORT));

		Node node = new Node(username, port);

		// creates a Swing component for that node
		NodeComponent gui = new NodeComponent(node);

		// display this component in a Swing frame
		JFrame frame = new JFrame();
		frame.setTitle(node.createPeerInfo().toString());
		frame.setContentPane(gui);
		frame.setSize(600, 800);
		frame.setVisible(true);
	}
}
