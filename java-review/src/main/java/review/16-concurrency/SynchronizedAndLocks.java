package review.concurrency;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 16b. SHARED STATE: the race, synchronized, ReentrantLock, tryLock
 *      Run: mvn -q compile exec:java -Dmain.class=review.concurrency.SynchronizedAndLocks
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class SynchronizedAndLocks {

    static final int THREADS = 4;
    static final int PER_THREAD = 1000;

    /** value++ is THREE steps (read, add, write): two threads can read the same value and one update is lost. */
    static class UnsafeCounter {
        int value;

        void increment() {
            value++;
        }
    }

    /** synchronized method: one thread at a time per object (the lock is `this`). */
    static class SyncMethodCounter {
        private int value;

        synchronized void increment() {
            value++;
        }

        synchronized int get() {
            return value;
        }

        synchronized void incrementTwice() {
            increment();                                                   // enters another synchronized method while holding the lock: allowed
            increment();
        }
    }

    /** synchronized block on a private lock object: nobody outside can lock it by accident. */
    static class SyncBlockCounter {
        private final Object lock = new Object();
        private int value;

        void increment() {
            synchronized (lock) {
                value++;
            }
        }

        int get() {
            synchronized (lock) {
                return value;
            }
        }
    }

    /** ReentrantLock: the same idea, explicit; ALWAYS unlock in finally. */
    static class LockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int value;

        void increment() {
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock();
            }
        }

        int get() {
            lock.lock();
            try {
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    static void awaitBarrier(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    static void hammer(Runnable increment) {
        Workers.hammer(THREADS, PER_THREAD, increment);
    }

    public static void main(String[] args) throws InterruptedException {
        title("SHARED STATE  -  4 threads x 1000 increments, locks");

        int expected = THREADS * PER_THREAD;

        section("The race: value++ is not atomic");
        syntax(
                "read value, add 1, write value", "what value++ really does; two threads can interleave between the steps",
                "lost update", "both read 5, both write 6: one increment disappears"
        );
        int[] shared = {5};
        CyclicBarrier bothRead = new CyclicBarrier(2);
        Runnable lostUpdate = () -> {
            int read = shared[0];                                          // both threads read 5 ...
            awaitBarrier(bothRead);                                        // ... wait until the other one has read too ...
            shared[0] = read + 1;                                          // ... and both write 6
        };
        Thread first = new Thread(lostUpdate);
        Thread second = new Thread(lostUpdate);
        first.start();
        second.start();
        first.join();
        second.join();
        show(() -> shared[0]);                                             // 6, not 7: a forced interleaving, so it is the same every run
        UnsafeCounter unsafe = new UnsafeCounter();
        hammer(unsafe::increment);
        show(() -> unsafe.value <= expected);                              // always true: updates can only be LOST, never invented
        show(() -> expected);                                              // what a correct counter must reach
        note("The unsafe total is not printed: it is not guaranteed. On a fast machine it may well equal 4000 and hide the bug");
        note("That is the danger: a race can pass every test on your machine and fail in production");

        section("Fix 1: synchronized");
        syntax(
                "synchronized void m() { ... }", "the whole method runs holding the lock of `this`",
                "synchronized (lockObject) { ... }", "a block: smaller scope, any object as the lock",
                "static synchronized void m()", "the lock is the Class object, shared by all instances"
        );
        SyncMethodCounter method = new SyncMethodCounter();
        hammer(method::increment);
        show(() -> method.get());                                          // exactly 4000
        SyncBlockCounter block = new SyncBlockCounter();
        hammer(block::increment);
        show(() -> block.get());                                           // exactly 4000
        note("A lock must guard READS too (get is synchronized): otherwise a reader may see a stale value");
        SyncMethodCounter reentrant = new SyncMethodCounter();
        reentrant.incrementTwice();
        show(() -> reentrant.get());                                       // 2: synchronized is REENTRANT, a thread never blocks on a lock it already holds

        section("Fix 2: ReentrantLock and tryLock");
        syntax(
                "lock.lock(); try { ... } finally { lock.unlock(); }", "never forget the finally: an exception would keep the lock forever",
                "lock.tryLock()", "take the lock only if it is free right now (also with a timeout)"
        );
        LockCounter locked = new LockCounter();
        hammer(locked::increment);
        show(() -> locked.get());
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        lock.lock();
        show(() -> lock.getHoldCount());                                   // the same thread may lock again: 2 holds, needs 2 unlocks
        lock.unlock();
        lock.unlock();
        lock.lock();
        boolean[] otherThreadGotIt = new boolean[1];
        Thread other = new Thread(() -> otherThreadGotIt[0] = lock.tryLock());
        other.start();
        other.join();
        show(() -> otherThreadGotIt[0]);                                   // false: main holds the lock, tryLock does not wait
        lock.unlock();
        note("Atomic variables and volatile are the lock-free alternatives: AtomicsAndVolatile");
    }
}
