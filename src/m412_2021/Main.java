package m412_2021;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws InterruptedException, IOException {
		var network = new NetworkAdapter();
		new ChatApplication(network);
	}
}
