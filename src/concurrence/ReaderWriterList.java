package concurrence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class ReaderWriterList<T> {

	private ArrayList<T> lockedList;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	public ReaderWriterList(int size,T initialValue){
		lockedList = new ArrayList<T>(Collections.nCopies(size, initialValue));
	}
	public T set(int index,T element){
		WriteLock wlock = lock.writeLock();
		wlock.lock();
		try{
			return lockedList.set(index, element);
		}finally{
			wlock.unlock();
		}
	}
	public T get(int index){
		ReadLock rlock = lock.readLock();
		rlock.lock();
		try{
			if(lock.getReadHoldCount() > 1)
				System.out.println(lock.getReadHoldCount());
			return lockedList.get(index);
		}finally{
			rlock.unlock();
		}
	}
	public static void main(String[] args) {
		new ReaderWriterListTest(30,1);
	}
}
class ReaderWriterListTest{
	ExecutorService exec = Executors.newCachedThreadPool();
	private final static int SIZE = 100;
	private static Random rand = new Random(47);
	private ReaderWriterList<Integer> list = new ReaderWriterList<Integer>(SIZE,0);
	private class Writer implements Runnable{
		public void run() {
			try{
				for(int i = 0; i < 20; i++){
					list.set(i, rand.nextInt());
					TimeUnit.MILLISECONDS.sleep(100);
				}
			}catch(InterruptedException e){

			}
			System.out.println("Writer finished, shutting down");
			exec.shutdownNow();
		}
	}
	private class Reader implements Runnable{
		public void run() {
			try{
				while(!Thread.interrupted()){
					for(int i = 0; i < 20; i++){
						list.set(i, rand.nextInt());
						TimeUnit.MILLISECONDS.sleep(100);
					}
				}
			}catch(InterruptedException e){

			}
			System.out.println("Reader finished, shutting down");
			exec.shutdownNow();
		}
	}
	public ReaderWriterListTest(int readers,int writers){
		for(int i = 0; i < readers; i++)
			exec.execute(new Reader());
		for(int i = 0; i < writers; i++)
			exec.execute(new Writer());
	}
}
