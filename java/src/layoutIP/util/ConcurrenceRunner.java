package layoutIP.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrenceRunner {
	private static final ExecutorService exec;
	public static final int cpuNum;
	static {
		cpuNum = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of CPU: " + cpuNum);
		exec = Executors.newFixedThreadPool(cpuNum);
	}

	public static void run(Runnable task) {
		exec.execute(task);
	}

	public static void stop() {
		exec.shutdown();
	}
}
