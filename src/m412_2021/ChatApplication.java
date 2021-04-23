package m412_2021;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatApplication extends AbstractApplication {

	JTextField tf = new JTextField();
	JTextArea ta = new JTextArea();

	public ChatApplication(NetworkAdapter network) {
		super(network);		
		JFrame f = new JFrame("client");
		f.setSize(600, 800);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(BorderLayout.CENTER, ta);
		f.getContentPane().add(BorderLayout.SOUTH, tf);
		f.setVisible(true);

		tf.addActionListener(e -> {
			server.broadcast(null, tf.getText());
		});
	}

	@Override
	public void process(PeerInfo from, Object o) {
		System.out.println(from + ": " + o);
		ta.setText(ta.getText() + "\n" + o);
	}
}
