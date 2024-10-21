package threads;

import java.util.ArrayList;

public class SharedInteger {
	static class SharedData {
		long value;
	}

	static class MyRunnable implements Runnable {
		SharedData sharedData;

		@Override
		public void run() {
			while (sharedData.value < 100000000) {
				sharedData.value++;
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		int nbCores = Runtime.getRuntime().availableProcessors();
		int maxNbThreads = nbCores;

		for (int nbThreads = 1; nbThreads < maxNbThreads; nbThreads++) {
			var durations = new ArrayList<Long>();

			for (int run = 0; run < 30; ++run) {
				var sharedData = new SharedData();
				var threads = new ArrayList<Thread>();

				for (int ti = 0; ti < nbThreads; ++ti) {
					var r = new MyRunnable();
					r.sharedData = sharedData;
					threads.add(new Thread(r));
				}

				long startDate = System.nanoTime();

				for (var t : threads) {
					t.start();
				}

				for (var t : threads) {
					t.join();
				}

				long endDate = System.nanoTime();
				durations.add(endDate - startDate);
			}

			long avgDuration = durations.stream().mapToLong(e -> e).sum() / durations.size();
			System.out.print("{\"nbThreads\": " + nbThreads + ", \"time\": " + avgDuration + "}");

			if (nbThreads < maxNbThreads - 1) {
				System.out.println(",");
			} else {
				System.out.println();
			}
		}
	}
}
