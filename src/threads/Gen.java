package threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Gen {
	public static void main(String[] args) throws InterruptedException {

		System.out.println(new String(createText_Nthreads(10, 1)));
	}

	public static byte[] createText_Nthreads(int len, int nbThreads)
			throws InterruptedException {
		byte[] b = new byte[len];
		int segmentSize = len / nbThreads;
		List<Thread> threads = new ArrayList<>();
		for (int t = 0; t < nbThreads; ++t) {
			int start = t * segmentSize;
			int end = t == nbThreads - 1 ? b.length : (t + 1) * segmentSize;
			threads.add(new Thread(new GenerationParallelCode(b, start, end)));
		}
		for (Thread t : threads) {
			t.start();
		}
		for (Thread t : threads) {
			t.join();
		}
		return b;
	}

	static class GenerationParallelCode implements Runnable {
		final int start, end;
		final byte[] b;
		private static byte[] allowedChars = "abcdefghijklmnopqrstuvwxyz ".getBytes();

		GenerationParallelCode(byte[] b, int start, int end) {
			this.start = start;
			this.end = end;
			this.b = b;
		}

		@Override
		public void run() {
			for (int i = start; i < end; ++i) {
				int pos = ThreadLocalRandom.current().nextInt(allowedChars.length);
				byte c = allowedChars[pos];
				b[i] = c;
			}
		}
	}
}
