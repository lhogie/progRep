package m412_2022;

import java.util.ArrayList;
import java.util.List;

// a specific type of message that is an ACK of an other message
// whose it carries the ID
public class ACKMessage extends Message {
	// the ID of the message we are interested in
	public long originalMessageID;

	// the list of nodes that had received that message
	public List<String> actualRecipients = new ArrayList<>();

	@Override
	public String toString() {
		return "ACK received from  " + route.get(0) + " who informs us the message reached " + actualRecipients;
	}
}