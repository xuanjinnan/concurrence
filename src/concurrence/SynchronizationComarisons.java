package concurrence;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


// 普通方法/Synchronized内置锁/Lock显示锁/Atomic操作的性能对比 单位(纳秒)
abstract class Accumulator {
    public static long cycles = 50000l;
    private static final int N = 4;
    public static ExecutorService exec = Executors.newFixedThreadPool(N * 2);
    private static CyclicBarrier barrier =
            new CyclicBarrier(N*2 + 1);
    protected volatile int index = 0;
    protected volatile long value = 0;
    protected long duration = 0;
    protected String id = "error";
    protected final static int SIZE = 100000;
    protected static int[] preLoaded = new int[SIZE];

    static  {
        Random rand = new Random(47);
        for (int i = 0; i < SIZE; i++) {
            preLoaded[i] = rand.nextInt();
        }

    }

    public abstract void accumulate();
    public abstract long read();
    private class Modifier implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < cycles; i++) {
                accumulate();
            }
            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class Reader implements Runnable {
        private volatile long value;

        @Override
        public void run() {
            for (int i = 0; i < cycles; i++) {
                value = read();
            }
            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void timedTest() {
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            exec.execute(new Modifier());
            exec.execute(new Reader());
        }
        try {
            barrier.await();  // 保障当前cycles测试完毕，才会进入下一个新cycles测试
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        duration = System.nanoTime() - start;
        System.out.print(String.format("%-13s: %13d\n", id, duration));
    }
    public static void report(Accumulator acc1, Accumulator acc2) {
        System.out.print(String.format("%-22s: %.2f\n", acc1.id + "/" + acc2.id,
                (double) acc1.duration / (double) acc2.duration));
    }
}

class BaseLine extends Accumulator {
    { id = "BaseLine"; }
    public void accumulate() {
        int ind = index++;
        if (ind >= SIZE) {
            index = 0;
            value += preLoaded[0];
        } else {
            value += preLoaded[ind];

        }
    }

    public long read() { return value; }
}


class SynchronizedTest extends Accumulator {
    { id = "Synchronized"; }
    public synchronized void accumulate() {
        value += preLoaded[index++];
        if (index >= SIZE) index = 0;
    }

    public synchronized long read() { return value; }
}

class LockTest extends Accumulator {
    { id = "Lock"; }
    private Lock lock = new ReentrantLock();
    @Override
    public void accumulate() {
        lock.lock();
        try {
            value += preLoaded[index++];
            if (index >= SIZE) index = 0;
        } finally {
            lock.unlock();
        }
    }
    public long read() {
        lock.lock();
        try {
            return value;
        } finally {
            lock.unlock();
        }
    }
}

class AtomicTest extends Accumulator {
    { id = "Atomic"; }
    private AtomicInteger index = new AtomicInteger(0);
    private AtomicLong value = new AtomicLong(0);
    public void accumulate() {
        int i = index.getAndIncrement();
        if (i >= SIZE) {
            index.set(0);
            value.getAndAdd(0);
        } else {
            value.getAndAdd(preLoaded[i]);
        }

    }

    public long read() {
        return value.get();
    }
}
public class SynchronizationComarisons {
    static BaseLine baseLine = new BaseLine();
    static SynchronizedTest synch = new SynchronizedTest();
    static LockTest lock = new LockTest();
    static AtomicTest atomic = new AtomicTest();
    static void test() {
        System.out.println("==================================");
        System.out.print(String.format("%-12s : %13d\n", "Cycles", Accumulator.cycles));
        baseLine.timedTest();
        synch.timedTest();
        lock.timedTest();
        atomic.timedTest();
        Accumulator.report(synch, baseLine);
        Accumulator.report(lock, baseLine);
        Accumulator.report(atomic, baseLine);
        Accumulator.report(synch, lock);
        Accumulator.report(synch, atomic);
        Accumulator.report(lock, atomic);
    }

    public static void main(String[] args) {
        int iterations = 5;
        System.out.println("Warmup");
        baseLine.timedTest();
        for (int i = 0; i < iterations; i++) {
            test();
            Accumulator.cycles *= 2;
        }
        Accumulator.exec.shutdownNow();
    }
}
/*output
Warmup
BaseLine     :      18430064
==================================
Cycles       :         50000
BaseLine     :      13395434
Synchronized :      43795774
Lock         :      31507496
Atomic       :      11334757
Synchronized/BaseLine : 3.27
Lock/BaseLine         : 2.35
Atomic/BaseLine       : 0.85
Synchronized/Lock     : 1.39
Synchronized/Atomic   : 3.86
Lock/Atomic           : 2.78
==================================
Cycles       :        100000
BaseLine     :      26103847
Synchronized :     123044307
Lock         :      42068748
Atomic       :      18362378
Synchronized/BaseLine : 4.71
Lock/BaseLine         : 1.61
Atomic/BaseLine       : 0.70
Synchronized/Lock     : 2.92
Synchronized/Atomic   : 6.70
Lock/Atomic           : 2.29
==================================
Cycles       :        200000
BaseLine     :      52618563
Synchronized :     233851583
Lock         :      81057204
Atomic       :      41032168
Synchronized/BaseLine : 4.44
Lock/BaseLine         : 1.54
Atomic/BaseLine       : 0.78
Synchronized/Lock     : 2.89
Synchronized/Atomic   : 5.70
Lock/Atomic           : 1.98
==================================
Cycles       :        400000
BaseLine     :     112605702
Synchronized :     447144399
Lock         :     176562031
Atomic       :      66380390
Synchronized/BaseLine : 3.97
Lock/BaseLine         : 1.57
Atomic/BaseLine       : 0.59
Synchronized/Lock     : 2.53
Synchronized/Atomic   : 6.74
Lock/Atomic           : 2.66
==================================
Cycles       :        800000
BaseLine     :     207776803
Synchronized :    1004449102
Lock         :     351125515
Atomic       :     133602972
Synchronized/BaseLine : 4.83
Lock/BaseLine         : 1.69
Atomic/BaseLine       : 0.64
Synchronized/Lock     : 2.86
Synchronized/Atomic   : 7.52
Lock/Atomic           : 2.63
* */
