package master_international.ex1_batch_system;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MultiThreadedBatchSystem extends BatchSystem {
	BlockingQueue<Job> sharedQueue = new ArrayBlockingQueue<Job>(10);
	Set<Thread> workers = new HashSet<>();
	File inputDirectory;

	public MultiThreadedBatchSystem(int nbWorkers) {
		for (int i = 0; i < nbWorkers; ++i) {
			workers.add(new Thread(() -> {
				while (true) {
					Job j = null;

					try {
						j = sharedQueue.take();
						j.compute();
					} catch (Exception e) {
						e.printStackTrace();
						sharedQueue.offer(j);
					}
				}
			}));
		}
	}

	public void start() {
		workers.forEach(thread -> thread.start());

		new Thread(() -> {
			while (true) {
				for (var inputFile : inputDirectory.listFiles()) {
					var j = new Text2SizeJob();
					j.inputFile = inputFile;
//					j.outputFile = somewhere else...
				}
			}
		});
	}

	@Override
	public void accept(Job newJob) {
		sharedQueue.offer(newJob);
	}
}
