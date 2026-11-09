package review.concurrency;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 16f. BLOCKING QUEUES (producer / consumer) AND DEADLOCK
 *      Run: mvn -q compile exec:java -Dmain.class=review.concurrency.QueuesAndDeadlock
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class QueuesAndDeadlock {

    /** Takes `first`, waits until the other thread holds ITS first lock, then tries for `second` (with a time limit). */
    static Thread grab(ReentrantLock first, ReentrantLock second, boolean[] gotSecond, int slot, CountDownLatch holding, CountDownLatch tried) {
        return new Thread(Workers.quietly(() -> {
            first.lock();
            try {
                holding.countDown();
                holding.await();                                           // both threads now hold one lock each
                gotSecond[slot] = second.tryLock(100, TimeUnit.MILLISECONDS);
                if (gotSecond[slot]) second.unlock();
                tried.countDown();
                tried.await();                                             // keep the first lock until both have tried
            } finally {
                first.unlock();
            }
        }));
    }

    public static void main(String[] args) throws InterruptedException {
        title("QUEUES AND DEADLOCK  -  hand work over safely, and how it gets stuck");

        section("BlockingQueue: producer and consumer");
        syntax(
                "queue.put(x) | queue.take()", "block while the queue is full | empty (back pressure)",
                "queue.offer(x) | queue.poll(t, unit)", "never block (or wait up to t): false | null when not possible"
        );
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);
        List<Integer> consumed = new ArrayList<>();
        Thread producer = new Thread(Workers.quietly(() -> {
            for (int i = 1; i <= 5; i++) queue.put(i);                     // blocks whenever 2 items are waiting
            queue.put(-1);                                                 // a 'poison pill' tells the consumer to stop
        }));
        Thread consumer = new Thread(Workers.quietly(() -> {
            for (int v = queue.take(); v != -1; v = queue.take()) consumed.add(v);
        }));
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        show(() -> consumed);                                              // one producer, one consumer: FIFO order is guaranteed
        BlockingQueue<Integer> full = new ArrayBlockingQueue<>(2);
        show(() -> full.offer(1));                                         // fits
        show(() -> full.offer(2));                                         // fits (capacity 2)
        show(() -> full.offer(3));                                         // full: offer returns false instead of waiting
        BlockingQueue<Integer> empty = new ArrayBlockingQueue<>(2);
        Integer timedOut = empty.poll(50, TimeUnit.MILLISECONDS);   show(() -> timedOut);   // an empty queue: null after the wait
        note("take() and put() throw InterruptedException: that is how a consumer is asked to stop");

        section("Deadlock: two locks, opposite order");
        syntax("T1: lock A then B   |   T2: lock B then A", "T1 holds A and waits for B, T2 holds B and waits for A: both wait forever");
        ReentrantLock lockA = new ReentrantLock();
        ReentrantLock lockB = new ReentrantLock();
        boolean[] gotSecond = new boolean[2];
        CountDownLatch holding = new CountDownLatch(2);
        CountDownLatch tried = new CountDownLatch(2);
        Thread t1 = grab(lockA, lockB, gotSecond, 0, holding, tried);
        Thread t2 = grab(lockB, lockA, gotSecond, 1, holding, tried);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        show(() -> Arrays.toString(gotSecond));                            // [false, false]: neither got its second lock. With lock() they would hang
        note("The four ingredients: mutual exclusion, hold and wait, no preemption, circular wait. Break one to be safe");
        note("Fixes: always lock in the SAME global order (A then B everywhere); or tryLock with a timeout; or use ONE lock");
        note("HashMap, ArrayList, HashSet are never safe for concurrent writes: use the concurrent versions or a lock");
    }
}
