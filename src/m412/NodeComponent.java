package m412;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class NodeComponent extends JComponent
{
	public NodeComponent(Node node)
	{
		JTextField tf = new JTextField();
		tf.setBorder(new TitledBorder("Type your message/command here:"));

		JTextArea ta = new JTextArea();
		ta.setBorder(new TitledBorder("Received messages"));

		JList<Peer> peerList = new JList<>();
		peerList.setBorder(new TitledBorder("Peers"));

		ta.setEditable(false);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(BorderLayout.CENTER, new JScrollPane(ta));
		rightPanel.add(BorderLayout.SOUTH, tf);

		setLayout(new GridLayout(1, 1));
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(peerList),
				rightPanel));

		tf.addActionListener(e -> {
			node.broadcast(new Message(tf.getText()));
			tf.setText("");
		});

		node.messageHandler = msg -> ta.append(msg.toString() + "\n");

		// periodically updates peers list
		new Thread(() -> {
			while (true)
			{
				try
				{
					Thread.sleep(1000);
					peerList.setListData(new Vector(node.peers));
				}
				catch (InterruptedException e)
				{
				}
			}
		}).start();
	}

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
