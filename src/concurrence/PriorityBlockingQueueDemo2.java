package concurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

class PrioritizedTask2 implements Runnable,Comparable<PrioritizedTask2>{
	private Random rand = new Random(47);
	private static int counter = 0;
	private final int id = counter++;
	private final int priority;
	protected static List<PrioritizedTask2> sequence = new ArrayList<PrioritizedTask2>();
	public PrioritizedTask2(int priority){
		this.priority = priority;
		sequence.add(this);
	}
	public int compareTo(PrioritizedTask2 o) {
		return priority < o.priority ? 1 : (priority > o.priority ? -1 :0);
	}
	public void run() {
		try {
			TimeUnit.MILLISECONDS.sleep(rand.nextInt(250));
		} catch (InterruptedException e) {
		}
		System.out.println(this);
	}
	public String toString(){
		return String.format("[%1$-3d]", priority) + " Task " + id;
	}
	public String summary(){
		return "(" + id + ":" + priority + ")";
	}
	public static class EndSentinel extends PrioritizedTask2	{
		private ExecutorService exec;
		public EndSentinel(ExecutorService exec) {
			super(-1);
			this.exec = exec;
		}
		public void run(){
			int count = 0;
			for(PrioritizedTask2 pt : sequence){
				System.out.println(pt.summary());
				if(++count % 5 == 0)
					System.out.println();
			}
			System.out.println();
			System.out.println(this + " Calling shutdownNow()");
			exec.shutdownNow();
		}
	}
}
class PrioritizedTaskProducer2 implements Runnable{
	private Random rand = new Random(47);
	private Queue<Runnable> queue;
	private ExecutorService exec;
	public PrioritizedTaskProducer2(Queue<Runnable> queue,ExecutorService exec){
		this.queue = queue;
		this.exec = exec;
	}
	public void run() {
		for(int i = 0; i < 20; i++){
			queue.add(new PrioritizedTask2(rand.nextInt(10)));
			Thread.yield();
		}
		try {
			for(int i = 0; i < 10; i++){
				TimeUnit.MILLISECONDS.sleep(250);
				queue.add(new PrioritizedTask2(10));
			}
			for(int i = 0; i < 10; i++){
				queue.add(new PrioritizedTask2(i));
			}
			queue.add(new PrioritizedTask2.EndSentinel(exec));
			System.out.println(queue);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Finished PrioritizedTaskProducer");
	}
}
class PrioritizedTaskConsumer2 implements Runnable{
	private PriorityBlockingQueue<Runnable> q;
	public PrioritizedTaskConsumer2(PriorityBlockingQueue<Runnable> q){
		this.q = q;
	}
	public void run() {
		try {
			while(!Thread.interrupted()){
				q.take().run();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Finished PrioritizedTaskConsumer");
	}

}
public class PriorityBlockingQueueDemo2 {

	public static void main(String[] args) throws InterruptedException {
		Random rand = new Random(47);
		ExecutorService exec = Executors.newCachedThreadPool();
		PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>();
		exec.execute(new PrioritizedTaskProducer2(queue,exec));
		Thread.sleep(10000);
		exec.execute(new PrioritizedTaskConsumer2(queue));
	}
}

































