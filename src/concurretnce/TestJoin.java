package concurretnce;

class A extends Thread{
	public A() {
		start();
	}
	@Override
	public void run() {
		try {
			sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("A.run()");
	}
}
class B extends Thread{
	private A a;

	public B(A a) {
		this.a = a;
		start();
	}
	@Override
	public void run() {
		try {
			a.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("B.run()");
	}
}
public class TestJoin {

	public static void main(String[] args) {
		A a = new A();
		new B(a);
	}
}
