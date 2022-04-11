package m412_2022;

// a specific type of message that carries a string
public class TextMessage extends Message {
	public String text;

	@Override
	public String toString() {
		return route + "> " + text;
	}
}