package concurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

class PrioritizedTask implements Runnable,Comparable<PrioritizedTask>{
	private Random rand = new Random(47);
	private static int counter = 0;
	private final int id = counter++;
	private final int priority;
	protected static List<PrioritizedTask> sequence = new ArrayList<PrioritizedTask>();
	public PrioritizedTask(int priority){
		this.priority = priority;
		sequence.add(this);
	}
	@Override
	public int compareTo(PrioritizedTask o) {
		return this.priority < o.priority ? 1 : this.priority > o.priority ? -1 : 0;
	}

	@Override
	public void run() {
		try {
			TimeUnit.MILLISECONDS.sleep(rand.nextInt(250));
		} catch (InterruptedException e) {
			// Acceptable way to exit
		}
		System.out.println(this);
	}
	public String summary(){
		return "(" + id + ":" + priority + ")";
	}
	public String toString(){
		return String.format("[%1$-3d]", priority) + " Task" + id;
	}
	public static class EndSentinel extends PrioritizedTask{
		private ExecutorService exec;
		public EndSentinel(ExecutorService e) {
			super(-1); // Lowest priority in this program
			exec = e;
		}
		@Override
		public void run() {
			int count = 0;
			for (PrioritizedTask pt : sequence) {
				System.out.print(pt.summary());
				if(++count % 5 == 0)
					System.out.println();
			}
			System.out.println();
			System.out.println(this + " Calling shutdownNow()");
			exec.shutdownNow();
		}
	}

}
class PrioritizedTaskProducer implements Runnable{
	private Random rand = new Random(47);
	private Queue<Runnable> queue;
	private ExecutorService exec;
	public PrioritizedTaskProducer(Queue<Runnable> queue, ExecutorService exec) {
		this.queue = queue;
		this.exec = exec; // Used for EndSentinel
	}
	@Override
	public void run() {
		// Unbounded queue; never blocks.
		// Fill it up fast with random priorities:
		for(int i = 0; i < 20; i++){
			PrioritizedTask task = new PrioritizedTask(rand.nextInt(10));
			System.out.println("producing for cycle one!" + task);
			queue.add(task);
			Thread.yield();
		}
		// Trickle in highest-priority jobs:
		System.out.println(" produce again!");
		try {
			for(int i = 0; i < 10; i++){
				TimeUnit.MILLISECONDS.sleep(250);
				PrioritizedTask task = new PrioritizedTask(10);
				System.out.println("producing for cycle two!" + task);
				queue.add(task);
			}
			// Add jobs, lowest priority first:
			for(int i = 0; i < 10; i++){
				PrioritizedTask task = new PrioritizedTask(i);
				System.out.println("producing for cycle three!" + task);
				queue.add(task);
			}
			// A sentinel to stop all the tasksL
			queue.add(new PrioritizedTask.EndSentinel(exec));
		} catch (InterruptedException e) {
			// Acceptalbe way to exit
		}
		System.out.println("Finished PrioritizedTaskProducer");
	}
}
class PrioritizedTaskConsumer implements Runnable{
	private PriorityBlockingQueue<Runnable> q;
	public PrioritizedTaskConsumer(PriorityBlockingQueue<Runnable> q) {
		this.q = q;
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted()){
				Runnable take = q.take();
				take.run();
				System.out.println("consumering! " + take);
			}
		} catch (InterruptedException e) {
			// Acceptable way to exit
		}
		System.out.println("Finished PrioritizedTaskConsumer");
	}

}
public class PriorityBlockingQueueDemo {

	public static void main(String[] args) {
		Random rand = new Random (47);
		ExecutorService exec = Executors.newCachedThreadPool();
		PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>();
		exec.execute(new PrioritizedTaskProducer(queue,exec));
		exec.execute(new PrioritizedTaskConsumer(queue));
	}
}



























