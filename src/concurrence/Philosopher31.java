package concurrence;
import java.util.concurrent.*;
import java.util.*;
import static net.mindview.util.Print.*;

public class Philosopher31 implements Runnable {
	private Chopstick left;
	private Chopstick right;
	private LinkedBlockingQueue<Chopstick> bin;
	private final int id;
	private final int ponderFactor;
	private Random rand = new Random(47);
	private void pause() throws InterruptedException {
		if(ponderFactor == 0) return;
		TimeUnit.MILLISECONDS.sleep(rand.nextInt(ponderFactor * 250));
	}
	public Philosopher31(Chopstick left, Chopstick right, 
		LinkedBlockingQueue<Chopstick> bin, int ident, int ponder) {
		this.left = left;
		this.right = right;
		this.bin = bin;
		id = ident;
		ponderFactor = ponder;
	}
	public void run() {
		try {
			while(!Thread.interrupted()) {
				print(this + " " + "thinking");
				pause();
				// Philosopher becomes hungry
				print(this + " taking first, right chopstick");
				right = bin.take();
				print(this + " taking second, left chopstick");
				left = bin.take();
				print(this + " eating");
				pause();
				print(this + " returning chopsticks");
				bin.put(right);
				bin.put(left);
			}
		} catch(InterruptedException e) {
			print(this + " " + "exiting via interrupt");
		}
	}
	public String toString() { return "Philosopher " + id; }
}