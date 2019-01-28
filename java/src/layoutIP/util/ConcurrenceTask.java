package layoutIP.util;

import java.util.concurrent.CountDownLatch;

public abstract class ConcurrenceTask {
	int length;

	public ConcurrenceTask(int length_) {
		length = length_;
	}

	public void start() {
		int taskNum = Math.min(length, ConcurrenceRunner.cpuNum);
		int fragmentLength = length / taskNum;
		int fragmentRemainder = length % taskNum;

		final CountDownLatch gate = new CountDownLatch(taskNum);
		for (int i = 0; i < taskNum; i++) {
			final int start = i * fragmentLength;
			int rem = (i == taskNum - 1) ? fragmentRemainder : 0;
			final int end = (i + 1) * fragmentLength + rem;

			Runnable task = new Runnable() {
				@Override
				public void run() {
					process(start, end);
					gate.countDown();
				}

			};
			ConcurrenceRunner.run(task);
		}

		try {
			/*
			 * wait for all threads finish
			 */
			gate.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected abstract void process(int start, int end);
}
