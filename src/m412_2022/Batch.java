package m412_2022;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class Batch {
	public static void main(String[] args) throws InterruptedException {
		var batch = new BatchSystem<String>();

		for (int i = 0; i < 100; ++i) {
			System.out.println("injecting jobs");
			batch.in.put(() -> {
				// some long computation that takes 1s
				try {
					long duration = (long) (Math.random() * 10000);
					Thread.sleep(duration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return "salut";
			});
		}

		while (true) {
			var j = batch.results.take();
			System.out.println("new result: " + j);
		}
	}

	static class BatchSystem<R> {
		BlockingQueue<Callable<R>> in = new ArrayBlockingQueue(1000);
		BlockingQueue<String> results = new ArrayBlockingQueue(1000);

		public BatchSystem() {
			List<Cell> cells = new ArrayList<>();

			System.out.println("creating cells");
			while (cells.size() < 10) {
				cells.add(new Cell(in, results));
			}

			cells.forEach(c -> c.start());

		}
	}

	static class Cell<R> extends Thread {
		final BlockingQueue<Callable<R>> in;
		final BlockingQueue<R> out;

		Cell(BlockingQueue<Callable<R>> in, BlockingQueue<R> out) {
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {
			while (true) {
				try {
					var job = in.take();
					R r = job.call();
					out.put(r);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
