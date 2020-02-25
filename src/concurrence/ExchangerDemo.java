package concurrence;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import concurretnce.Test;
import net.mindview.util.BasicGenerator;
import net.mindview.util.Generator;

class ExchangerProducer<T> implements Runnable{
	private Generator<T> generator;
	private Exchanger<List<T>> exchanger;
	private List<T> holder;
	ExchangerProducer(Exchanger<List<T>> exchg,Generator<T> gen,List<T> holder){
		exchanger = exchg;
		generator = gen;
		this.holder = holder;
	}
	public void run() {
		while(!Thread.interrupted()){
			try {
				for(int i = 0; i < ExchangerDemo.size; i++){
					holder.add(generator.next());
					holder = exchanger.exchange(holder);
				}
			} catch (InterruptedException e) {
			}
		}
	}
}
class ExchangerConsumer<T> implements Runnable{
	private Exchanger<List<T>> exchanger;
	private List<T> holder;
	private volatile T value;
	ExchangerConsumer(Exchanger<List<T>> exchg,List<T> holder){
		exchanger = exchg;
		this.holder = holder;
	}
	public void run() {
		while(!Thread.interrupted()){
			try {
				holder = exchanger.exchange(holder);
				for(T x : holder){
					value = x;
					holder.remove(x);
				}
			} catch (InterruptedException e) {
			}
			System.out.println("Final value: " + value);
		}
	}
}
public class ExchangerDemo {
	static int size = 10;
	static int delay = 5;
	public static void main(String[] args) throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool();
		Exchanger<List<Test>> xc = new Exchanger<List<Test>>();
		List<Test> producerList = new CopyOnWriteArrayList<Test>();
		List<Test> consumerList = new CopyOnWriteArrayList<Test>();
		exec.execute(new ExchangerProducer<Test>(xc,BasicGenerator.create(Test.class),producerList));
		exec.execute(new ExchangerConsumer<Test>(xc,consumerList));
		TimeUnit.SECONDS.sleep(5);
		exec.shutdownNow();
	}

}

























