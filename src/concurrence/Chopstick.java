package concurrence;

public class Chopstick {
	private boolean taken = false;

	public synchronized void take() throws Exception{
		while(taken)
			wait();
		taken = true;
	}
	public synchronized void drop(){
		taken = false;
		notifyAll();
	}
}
