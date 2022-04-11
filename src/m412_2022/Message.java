package m412_2022;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// the base abstraction of a message does not define any content or role
public class Message implements Serializable {
	// just an randomly chosen ID
	public long id = Node.random.nextLong();

	// and a list of node that relayed the message
	public List<String> route = new ArrayList<>();
}