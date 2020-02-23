package concurrence;

import java.util.concurrent.*;

public class DeadlockingDiningPhilosophers31 {
	public static void main(String[] args) throws Exception {
		int ponder = 0;
		if(args.length > 0)
			ponder = Integer.parseInt(args[0]);
		int size = 5;
		if(args.length > 1)
			size = Integer.parseInt(args[1]);
		ExecutorService exec = Executors.newCachedThreadPool();
		// chopstick bin:
		LinkedBlockingQueue<Chopstick> bin = new LinkedBlockingQueue<Chopstick>();
		Chopstick[] sticks = new Chopstick[size];
		for(int i = 0; i < size; i++) {
			sticks[i] = new Chopstick();
			bin.put(sticks[i]);
		}	
		for(int i = 0; i < size; i++)	
			exec.execute(new Philosopher31(sticks[i], sticks[(i + 1) % size], bin, i, ponder));
		if(args.length == 3 && args[2].equals("timeout"))
			TimeUnit.SECONDS.sleep(5);
		else {
			System.out.println("Press 'Enter' to quit");
			System.in.read();
		}
		exec.shutdownNow();
	}
}