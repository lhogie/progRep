package m412_2021;
import java.io.Serializable;
import java.util.Random;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	public long ID = new Random().nextLong();
}
