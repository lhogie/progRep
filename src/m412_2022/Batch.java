package m412_2022;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class Batch {
	public static void main(String[] args) throws InterruptedException {
		// create a new batch system of 10 threads that will generate string results
		var batch = new BatchSystem<String>(10);

		// send 100 jobs into the system
		for (int i = 0; i < 100; ++i) {
			System.out.println("injecting jobs");
			batch.in.put(() -> {
				try {
					// some long computation that takes between 0 and 10s
					long duration = (long) (Math.random() * 10000);
					Thread.sleep(duration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return "salut";
			});
		}

		// prints results as soon as they are available
		while (true) {
			var j = batch.results.take();
			System.out.println("new result: " + j);
		}
	}

	static class BatchSystem<R> {
		// where submitted jobs will wait
		BlockingQueue<Callable<R>> in = new ArrayBlockingQueue(1000);
		
		// where results will be put after processing
		BlockingQueue<String> results = new ArrayBlockingQueue(1000);

		public BatchSystem(int nbThreads) {
			List<Cell> cells = new ArrayList<>();

			System.out.println("creating cells");
			while (cells.size() < nbThreads) {
				cells.add(new Cell(in, results));
			}

			cells.forEach(c -> c.start());

		}
	}

	static class Cell<R> extends Thread {
		final BlockingQueue<Callable<R>> incomingJobs;
		final BlockingQueue<R> results;

		Cell(BlockingQueue<Callable<R>> in, BlockingQueue<R> out) {
			this.incomingJobs = in;
			this.results = out;
		}

		@Override
		public void run() {
			while (true) {
				try {
					// takes a jobs, waits if necessary
					var job = incomingJobs.take();
					
					// process it
					R r = job.call();
					
					// put the result in the result queue
					results.put(r);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
