package concurretnce;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
public class Test {

	public static void main(String[] args) {
		
		long delta = 1000;
		long convert = NANOSECONDS.convert(delta ,MILLISECONDS);
		System.out.println(convert);
		long trigger = 1000;
		long nanoTime = System.nanoTime();
		System.out.println(nanoTime);
		long convert2 = NANOSECONDS.convert(trigger - nanoTime, NANOSECONDS);
		System.out.println(convert2);
		long convert3 = MILLISECONDS.convert(trigger - nanoTime, NANOSECONDS);
		System.out.println(convert3);
	}
}
