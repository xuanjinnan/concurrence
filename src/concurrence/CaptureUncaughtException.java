package concurrence;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

class simpleExceptionRunnable implements Runnable{

	@Override
	public void run() {
		throw new RuntimeException();
	}
	
}
class HandlerThreadFactory implements ThreadFactory{

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setUncaughtExceptionHandler(new MyuncaughtExceptionHandler());
		return t;
	}
	
}
class MyuncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.out.println("caught " + e);
	}
	
}
public class CaptureUncaughtException {
	public static void main(String[] args) {
		
		ExecutorService exec = Executors.newCachedThreadPool(new HandlerThreadFactory());
		exec.execute(new simpleExceptionRunnable());
	}

}
