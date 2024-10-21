package threads;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Grep {
	public static byte[] alphabet = "abcdefghijklmnopqrstuvwxyz 0123456789".getBytes();

	public static byte[] generateText(int len, byte[] alphabet) {
		var text = new byte[len];
		var prng = ThreadLocalRandom.current();

		for (int i = 0; i < text.length; ++i) {
			text[i] = alphabet[prng.nextInt(alphabet.length)];
		}

		return text;
	}

	public static String generateText(int len, String alphabet) {
		var text = "";
		Random prng = new Random();

		for (int i = 0; i < len; ++i) {
			text += alphabet.charAt(prng.nextInt(alphabet.length()));
		}

		return text;
	}
}
