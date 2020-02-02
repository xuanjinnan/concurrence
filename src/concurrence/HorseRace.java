package concurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Horse implements Runnable{
	private static int counter = 0;
	private final int id = counter++;
	private int strides = 0;
	private static Random rand = new Random(47);
	private static CyclicBarrier barrier;
	public Horse(CyclicBarrier b){barrier = b;}
	public synchronized int getStrides(){return strides;}
	public void run(){
		try {
			while(!Thread.interrupted()){
				synchronized(this){
					strides += rand.nextInt(3);
				}
				barrier.await();
			}
		} catch (InterruptedException e) {
			// A legitimate way to exit
		} catch (BrokenBarrierException e) {
			// This one we want to know about
			throw new RuntimeException(e);
		}
	}
	public String toString(){return "Horse " + id + " ";}
	public String tracks(){
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < getStrides(); i++)
			s.append("*");
		s.append(id);
		return s.toString();
	}
}
public class HorseRace {
	static final int FINISH_LINE = 75;
	private List<Horse> horses = new ArrayList<Horse>();
	private ExecutorService exec = Executors.newCachedThreadPool();
	private CyclicBarrier barrier;
	public HorseRace(int nHorses, final int pause){
		barrier = new CyclicBarrier(nHorses,new Runnable(){
			@Override
			public void run() {
				StringBuilder s = new StringBuilder();
				for(int i = 0; i < FINISH_LINE; i++)
					s.append("=");
				System.out.println(s);
				for (Horse horse : horses) {
					System.out.println(horse.tracks());
				}
				for (Horse horse2 : horses) {
					if(horse2.getStrides() >= FINISH_LINE){
						System.out.println(horse2 + "won!");
						exec.shutdownNow();
						return;
					}
				}
				try {
					TimeUnit.MILLISECONDS.sleep(pause);
				} catch (InterruptedException e) {
					System.out.println("barrier-action sleep interrupted");
				}
			}
		});
		for(int i = 0; i < nHorses; i++){
			Horse horse = new Horse(barrier);
			horses.add(horse);
			exec.execute(horse);
		}
	}
	public static void main(String[] args) {
		new HorseRace(7,200);
	}
}













