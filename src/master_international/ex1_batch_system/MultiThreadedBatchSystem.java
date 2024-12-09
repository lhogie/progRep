package master_international.ex1_batch_system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MultiThreadedBatchSystem extends BatchSystem {
	BlockingQueue<Job> sharedQueue = new ArrayBlockingQueue<Job>(10);
	Set<Thread> workers = new HashSet<>();
	File inputDirectory;
	File beingProcessedDirectory;

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

		while (true) {
			synchronized (beingProcessedDirectory) {
				File newFile = pickupOneFile();

				// move the file to the "processing" directory
				// inputFile.
				try {
					Files.move(newFile.toPath(), beingProcessedDirectory.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}

				sharedQueue.offer(file2job(newFile));
			}
		}
	}

	private Job file2job(File inputFile) {
		return null;
	}

	/*
	 * This is a comment
	 * 
	 * @return
	 */
	private synchronized File pickupOneFile() {
		File[] files = inputDirectory.listFiles();

		// if there is at least one file to proceed
		if (files.length > 0) {
			return files[0];
		} else {
			// it is empty
			return null;
		}
	}

	@Override
	public void accept(Job newJob) {
		sharedQueue.offer(newJob);
	}
}
