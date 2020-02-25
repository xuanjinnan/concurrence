package concurrence;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class Car2{
	private final int id;
	private boolean engine = false,driveTrain = false,wheels = false;
	public Car2(int idn){id = idn;}
	//Empty Car2 object
	public Car2(){id= -1;}
	public synchronized int getId(){return id;}
	public synchronized void addEngine(){engine = true;}
	public synchronized void addDriveTrain(){driveTrain = true;}
	public synchronized void addWheels(){wheels = true;}
	public synchronized String toString(){
		return "Car2 " + id + " [" + " engine: " + engine + " driveTrain: " + driveTrain
				+ " wheels: " + wheels + "]";
	}
}
class Car2Queue extends LinkedBlockingQueue<Car2>{}
class ChassisBuilder implements Runnable{
	private Car2Queue carQueue;
	private int counter = 0;
	public ChassisBuilder(Car2Queue cq){carQueue = cq;}
	public void run() {
		try {
			while(!Thread.interrupted()){
				TimeUnit.MICROSECONDS.sleep(500);
				Car2 c = new Car2(counter++);
				//System.out.println("ChassisBuiler created " + c);
				carQueue.put(c);
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted: Chassisbuilder");
		}
		System.out.println("ChassisBuilder off");
	}

}
class Assembler implements Runnable{
	private Car2Queue chassisQueue,finishingQueue;
	private Car2 car;
	private CyclicBarrier barrier = new CyclicBarrier(4);
	private RobotPool robotPool;
	public Assembler(Car2Queue chassisQueue, Car2Queue finishingQueue,RobotPool robotPool) {
		this.chassisQueue = chassisQueue;
		this.finishingQueue = finishingQueue;
		this.robotPool = robotPool;
	}
	public Car2 car(){return car;}
	public CyclicBarrier barrier(){return barrier;}
	public void run() {
		try {
			while(!Thread.interrupted()){
				car = chassisQueue.take();
				System.out.println(car);
				robotPool.hire(EngineRobot.class,this);
				robotPool.hire(DriveTrainRobot.class,this);
				robotPool.hire(WheelRobot.class,this);
				barrier.await();
				finishingQueue.put(car);
			}
		} catch (InterruptedException e) {
			System.out.println("Exiting Assembler via interrupt");
		} catch (BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
	}

}
class Reporter implements Runnable{
	private Car2Queue carQueue;
	public Reporter(Car2Queue cq){carQueue = cq;}
	public void run() {
		try {
			while(!Thread.interrupted()){
				System.out.println(carQueue.take());
			}
		} catch (InterruptedException e) {
			System.out.println("Exiting Reporter via interrupt");
		}
		System.out.println("Reporter off");
	}
}
abstract class Robot implements Runnable{
	private RobotPool pool;
	public Robot(RobotPool p){pool = p;}
	protected Assembler assembler;
	public Robot assignAssembler(Assembler assembler){
		this.assembler = assembler;
		return this;
	}
	private boolean engage = false;
	public synchronized void engage(){
		engage = true;
		notifyAll();
	}
	abstract protected void performService();
	public void run(){
		try {
			powerDown();
			while(!Thread.interrupted()){
				performService();
				assembler.barrier().await();
				powerDown();
			}
		} catch (InterruptedException e) {
			System.out.println("Exiting " + this + "via interrupt");
		} catch (BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
		System.out.println(this + " off");
	}
	private synchronized void powerDown() throws InterruptedException{
		engage = false;
		assembler = null;
		pool.release(this);
		while(engage = false)
			wait();
	}
	public String toString(){return getClass().getName();}
}
class EngineRobot extends Robot{
	public EngineRobot(RobotPool p) {
		super(p);
	}
	protected void performService() {
		System.out.println(this + " installing engine");
		assembler.car().addEngine();
	}
}
class DriveTrainRobot extends Robot{
	public DriveTrainRobot(RobotPool p) {
		super(p);
	}
	protected void performService() {
		System.out.println(this + " installing DriveTrain");
		assembler.car().addDriveTrain();
	}
}
class WheelRobot extends Robot{
	public WheelRobot(RobotPool p) {
		super(p);
	}
	protected void performService() {
		System.out.println(this + " installing Wheels");
		assembler.car().addWheels();
	}
	
}
class RobotPool{
	private Set<Robot> pool = new HashSet<Robot>();
	public synchronized void add(Robot r){
		pool.add(r);
		notifyAll();
	}
	public synchronized void hire(Class<?> class1, Assembler assembler) throws InterruptedException {
		for(Robot r : pool){
			if(r.getClass().equals(class1)){
				pool.remove(r);
				r.assignAssembler(assembler);
				r.engage();
				return;
			}
		}
		wait();
		hire(class1,assembler);
	}
	public synchronized void release(Robot robot) {
		add(robot);
	}

}
public class Car2Builder {
	public static void main(String[] args) throws InterruptedException {
		Car2Queue chassisQueue = new Car2Queue(),finishingQueue = new Car2Queue();
		ExecutorService exec = Executors.newCachedThreadPool();
		RobotPool robotPool = new RobotPool();
		exec.execute(new EngineRobot(robotPool));
		exec.execute(new DriveTrainRobot(robotPool));
		exec.execute(new WheelRobot(robotPool));
		exec.execute(new Assembler(chassisQueue,finishingQueue,robotPool));
		exec.execute(new Reporter(finishingQueue));
		exec.execute(new ChassisBuilder(chassisQueue));
		//TimeUnit.SECONDS.sleep(7);
		//exec.shutdownNow();
	}

}
