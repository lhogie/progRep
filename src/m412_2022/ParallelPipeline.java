package m412_2022;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelPipeline {
	public static void main(String[] args) throws InterruptedException {
		List<Cell> cells = new ArrayList<>();
		cells.add(new Cell());

		System.out.println("creating cells");
		while (cells.size() < 10) {
			var c = new Cell();
			cells.get(cells.size() - 1).next = c;
			cells.add(c);
		}

		cells.forEach(c -> c.start());

		for (int i = 0; i < 1; ++i) {
			System.out.println("injecting data");
			cells.get(0).q.put("");
		}

		for (var c : cells)
			c.join();
	}

	static class Cell extends Thread {
		final BlockingQueue<String> q = new ArrayBlockingQueue<>(10);
		Cell next;

		@Override
		public void run() {
			while (true) {
				try {
					var o = q.take();

					if (next == null) {
						System.out.println("Data reached the end: " + o);
					} else {
						next.q.add(o + ".");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
