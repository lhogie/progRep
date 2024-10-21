package y2021;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class A {

	public static void main(String[] args) {
		byte[] text = generateText(1000000000);
		System.out.println("text length: " + text.length + " chars");

		{
			System.out.println("running search_string");
			List<Integer> positions = new ArrayList<>();
			long startDateNs = System.nanoTime();
			searchTextThreads("a".getBytes(), text, positions);
			long endDateNs = System.nanoTime();
			long durationNs = endDateNs - startDateNs;
			System.out.println(
					"found " + positions.size() + " time(s) in " + (durationNs / 1000000) + "ms: " + positions);
		}
	}

	public static byte[] generateText(int len) {
		final var chars = "abcdefghijklmnopqrstuvwxyz 0123456789".getBytes();
		var text = new byte[len];

		var prng = ThreadLocalRandom.current();

		for (int i = 0; i < text.length; ++i) {
			text[i] = chars[prng.nextInt(chars.length)];
		}

		return text;
	}

	private static void searchText(T t, byte[] s, byte[] text, int start, int end, List<Integer> positions) {
		var wordIndex = 0;
		for (var textIndex = start; textIndex < end; textIndex++) {
			if (s[wordIndex] == text[textIndex]) {
				wordIndex++;
				if (wordIndex == s.length) {
					wordIndex = 0;
					//synchronized (positions) {
						t.n++;
					//}
				}
			} else if (wordIndex > 0) {
				textIndex--;
				wordIndex = 0;
			}
		}
	}

	static  class T extends Thread {
		int n = 0;
		
		
	}

	public static void searchTextThreads(byte[] s, byte[] text, List<Integer> positions) {
		final var cores = Runtime.getRuntime().availableProcessors();
		final var t = new T[cores];
		final int SIZE = text.length;

		for (var i = 0; i < cores; i++) {
			var start = i == 0 ? 0 : i * (SIZE / cores) - (s.length); // Overlapping sub arrays to avoid cut words. We
																		// take 0 for the first subarray to make sure
																		// the whole byte array is used.
			var end = i == cores - 1 ? SIZE : (i + 1) * (SIZE / cores);
			/*
			 * end equals to SIZE for the last subarray to avoid out of bound. For the other
			 * subarray, we take the length of the byte array divided by the number of cores
			 * (which is the size of the subarray). We then multiply that number by the
			 * index of the current subarray.
			 */


			t[i] = new T() {
				@Override
				public void run() {
					searchText(this, s, text, start, end, positions);
				}
			};
			t[i].setName("thread-" + i);
		}

		for (var i = 0; i < cores; i++) {
			t[i].start();
		}

		try {
			for (var i = 0; i < cores; i++) {
				t[i].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int sum = 0;

		for (var r : t) {
			sum = r.n;
		}
		
		System.out.println("sum: " + sum);
	}
}
