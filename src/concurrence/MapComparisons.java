package concurrence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import concurrence.ListTest.Reader;
import concurrence.ListTest.Writer;
import net.mindview.util.CountingGenerator;
import net.mindview.util.MapData;

abstract class MapTest extends Tester<Map<Integer,Integer>>{
	MapTest(String testId, int nReaders, int nWriters) {
		super(testId, nReaders, nWriters);
	}
	class Reader extends TestTask{
		long result = 0;
		void test(){
			for(int i = 0; i < testCycles; i++){
				for(int index = 0; index < containerSize; index ++){
					result += testContainer.get(index);
				}
			}
		}
		void putResults() {
			readResult = result;
			readTime += duration;
		}
	}
	class Writer extends TestTask{
		void test() {
			for(int i = 0; i < testCycles; i++){
				for(int index = 0; index < containerSize; index++){
					testContainer.put(index, writeData[index]);
				}
			}
		}
		void putResults() {
			writeTime += duration;
		}

	}
	void startReadersAndWriters(){
		for(int i = 0; i < nReaders; i++){
			exec.execute(new Reader());
		}
		for(int i = 0; i < nWriters; i++)
			exec.execute(new Writer());
	}
}
class SynchronizedHashMapTest extends MapTest{
	SynchronizedHashMapTest(int nReaders, int nWriters) {
		super("Synched HashMap", nReaders, nWriters);
	}
	@Override
	Map<Integer, Integer> containerInitializer() {
		return Collections.synchronizedMap(
				new HashMap<Integer,Integer>(
						MapData.map(
								new CountingGenerator.Integer(),
								new CountingGenerator.Integer(),
								containerSize)));
	}
}
class ConcurrentHashMapTest extends MapTest{
	ConcurrentHashMapTest(int nReaders, int nWriters) {
		super("ConcurrentHashMap", nReaders, nWriters);
	}
	@Override
	Map<Integer, Integer> containerInitializer() {
		return new ConcurrentHashMap<Integer,Integer>(
						MapData.map(
								new CountingGenerator.Integer(),
								new CountingGenerator.Integer(),
								containerSize));
	}
}
public class MapComparisons {
	public static void main(String[] args) {
		args = new String[]{"1","10","10"};
		Tester.initMain(args);
		new SynchronizedHashMapTest(10,0);
		new SynchronizedHashMapTest(9,1);
		new SynchronizedHashMapTest(5,5);
		new ConcurrentHashMapTest(10,0);
		new ConcurrentHashMapTest(9,1);
		new ConcurrentHashMapTest(5,5);
		Tester.exec.shutdown();
	}

}
