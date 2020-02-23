package concurrence;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import concurrence.GreenhouseScheduler.DataPoint;

import static java.util.concurrent.TimeUnit.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.DelayQueue;
abstract class DelayedGreenhouseTask implements Delayed, Runnable {
	protected long delayTime;
	public long trigger;
	public DelayedGreenhouseTask(){
		delayTime = 0;
		trigger = System.nanoTime();
	}
	public DelayedGreenhouseTask(long d){
		delayTime = d;
		trigger = System.nanoTime() + NANOSECONDS.convert(delayTime,MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		DelayedGreenhouseTask that = (DelayedGreenhouseTask) o;
		if(trigger < that.trigger) return -1;
		if(trigger > that.trigger) return 1;
		return 0;
	}
	public long getDelay(TimeUnit unit) {
		return unit.convert(trigger-System.nanoTime(), NANOSECONDS);
	}
	abstract public DelayedGreenhouseTask create(long d);
	abstract public void run();

}
class GreenhouseController33{
	private volatile boolean light = false;
	private volatile boolean water = false;
	private String thermostat = "Day";
	DelayQueue<DelayedGreenhouseTask> tasks = new DelayQueue<DelayedGreenhouseTask>();
	public synchronized String getThermostat(){
		return thermostat;
	}
	public synchronized void setThermostat(String value){
		thermostat = value;
	}
	class LightOn33 extends DelayedGreenhouseTask{
		public LightOn33(){
			super();
		}
		public LightOn33(long delayTime){
			super(delayTime);
		}
		public DelayedGreenhouseTask create(long d) {
			return new LightOn33(d);
		}
		public void run() {
			if(!light){
				System.out.println("Turning on lights");
				light = true;
			}
		}
	}
	class LightOff33 extends DelayedGreenhouseTask{
		public LightOff33(){
			super();
		}
		public LightOff33(long delayTime){
			super(delayTime);
		}
		public DelayedGreenhouseTask create(long d) {
			return new LightOn33(d);
		}
		public void run() {
			if(light){
				System.out.println("Turning off lights");
				light = false;
			}
		}
	}
	class WaterOn33 extends DelayedGreenhouseTask{
		public WaterOn33(){
			super();
		}
		public WaterOn33(long delayTime){
			super(delayTime);
		}
		public DelayedGreenhouseTask create(long d) {
			return new LightOn33(d);
		}
		public void run() {
			System.out.println("Turning greenhuse water on");
			water = true;
		}
	}
	class WaterOff33 extends DelayedGreenhouseTask{
		public WaterOff33(){
			super();
		}
		public WaterOff33(long delayTime){
			super(delayTime);
		}
		public DelayedGreenhouseTask create(long d) {
			return new LightOn33(d);
		}
		public void run() {
			System.out.println("Turning greenhuse water off");
			water = false;
		}
	}
	class ThermostatNight33 extends DelayedGreenhouseTask{
		public ThermostatNight33(){
			super();
		}
		public ThermostatNight33(long delayTime){
			super(delayTime);
		}
		public DelayedGreenhouseTask create(long d) {
			return new ThermostatNight33(d);
		}
		public void run() {
			System.out.println("Thermostat to night setting");
			setThermostat("Night");
		}
	}
	class ThermostatDay33 extends DelayedGreenhouseTask{
		public ThermostatDay33(){
			super();
		}
		public ThermostatDay33(long delayTime){
			super(delayTime);
		}
		public DelayedGreenhouseTask create(long d) {
			return new ThermostatDay33(d);
		}
		public void run() {
			System.out.println("Thermostat to day setting");
			setThermostat("Day");
		}
	}
	class Bell33 extends DelayedGreenhouseTask{
		public Bell33(){
			super();
		}
		public Bell33(long delayTime){
			super(delayTime);
		}
		public DelayedGreenhouseTask create(long d) {
			return new Bell33(d);
		}
		public void run() {
			System.out.println("Bing!");
		}
	}
	static class DataPoint33{
		final Calendar time;
		final float temperature;
		final float humidity;
		public DataPoint33(Calendar d,float temp,float hum){
			this.time = d;
			this.temperature = temp;
			this.humidity = hum;
		}
		public String toString(){
			return time.getTime() + String.format(" temperature: %1$.1f humidity: %2$.2f", temperature,humidity);
		}
	}
	private Calendar lastTime = Calendar.getInstance();
	{
		lastTime.set(Calendar.MINUTE, 30);
		lastTime.set(Calendar.SECOND, 00);
	}
	private float lastTemp = 65.0f;
	private int tempDirection = +1;
	private float lastHumidity = 50.0f;
	private int humidityDirection  = +1;
	private Random rand = new Random(47);
	List<DataPoint33> data = Collections.synchronizedList(new ArrayList<DataPoint33>());
	class CollectData33 extends DelayedGreenhouseTask{
		public CollectData33(){
			super();
		}
		public CollectData33(long delayTime){
			super(delayTime);
		}
		public CollectData33 create(long d) {
			return new CollectData33(d) ;
		}
		public void run() {
			System.out.println("Collecting data");
			synchronized(GreenhouseController33.this){
				lastTime.set(Calendar.MINUTE, lastTime.get(Calendar.MINUTE) + 30);
				if(rand.nextInt(5) == 4)
					tempDirection = -tempDirection;
				lastTemp = lastTemp + tempDirection * (1.0f + rand.nextFloat());
				if(rand.nextInt(5) == 4)
					humidityDirection = -humidityDirection;
				lastHumidity = lastHumidity + humidityDirection * rand.nextFloat();
				data.add(new DataPoint33((Calendar) lastTime.clone(),lastTemp,lastHumidity));
			}
		}
	}
	public class StopController extends DelayedGreenhouseTask{
		private ExecutorService exec;
		public StopController(long delay,ExecutorService e){
			super(delay);
			exec = e;
		}
		public DelayedGreenhouseTask create(long d) {
			return new StopController(d,Executors.newCachedThreadPool());
		}
		public void run() {
			System.out.println("Calling shutdownNow()");
			exec.shutdownNow();
			new Thread(){
				public void run() {
					for(DataPoint33 d : data)
						System.out.println(d);
				};
			}.start();
		}
	}
	public static class GreenhouseGo implements Runnable{
		private DelayQueue<DelayedGreenhouseTask> q;
		public GreenhouseGo(DelayQueue<DelayedGreenhouseTask> q){
			this.q = q;
		}
		public void run() {
			try {
				while(!Thread.interrupted()){
					q.take().run();
				}
			} catch (InterruptedException e) {
			}
			System.out.println("Finished GreenhouseGo");
		}
	}
}
public class GreenhouseScheduler33{
	public static void repeat(GreenhouseController33 c,DelayedGreenhouseTask task,long interval,long duration){
		if(interval <= duration){
			for(int i = 0; i < duration/interval; i++){
				DelayedGreenhouseTask t = task.create(interval * (i + 1));
				c.tasks.put(t);
			}
		}
	}
	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		GreenhouseController33 ghc = new GreenhouseController33();
		repeat(ghc,ghc.new Bell33(),1000,4000);
		repeat(ghc,ghc.new ThermostatNight33(),2000,4000);
		repeat(ghc,ghc.new LightOn33(),200,4000);
		repeat(ghc,ghc.new LightOff33(),400,4000);
		repeat(ghc,ghc.new WaterOn33(),600,4000);
		repeat(ghc,ghc.new WaterOff33(),800,4000);
		repeat(ghc,ghc.new ThermostatDay33(),1400,4000);
		repeat(ghc,ghc.new CollectData33(),500,4000);
		ghc.tasks.put(ghc.new StopController(5000,exec));
		exec.execute(new GreenhouseController33.GreenhouseGo(ghc.tasks));
	}
}