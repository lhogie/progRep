package m412;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NodeComponent extends JComponent
{

	private final Node node;

	public NodeComponent(Node node)
	{
		this.node = node;
		JTextField tf = new JTextField();
		JTextArea ta = new JTextArea();
		JList<PeerInfo> peerList = new JList<>();
		ta.setEditable(false);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(BorderLayout.CENTER, ta);
		rightPanel.add(BorderLayout.SOUTH, tf);

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(peerList), rightPanel);
		add(sp);
		setLayout(new GridLayout(1, 1));

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

}
