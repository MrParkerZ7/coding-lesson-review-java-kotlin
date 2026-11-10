package review.threads

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** Takes `first`, waits until the other thread holds ITS first lock, then tries for `second` (with a time limit). */
private fun grab(
    first: ReentrantLock,
    second: ReentrantLock,
    gotSecond: BooleanArray,
    slot: Int,
    holding: CountDownLatch,
    tried: CountDownLatch,
): Thread = thread {
    first.withLock {
        holding.countDown()
        holding.await()                                    // both threads now hold one lock each
        gotSecond[slot] = second.tryLock(100, MILLISECONDS)
        if (gotSecond[slot]) second.unlock()
        tried.countDown()
        tried.await()                                      // keep the first lock until both have tried
    }
}

/**
 * 22f. BLOCKING QUEUES (producer / consumer) AND DEADLOCK
 *      Run: mvn -q compile exec:java -Dmain.class=review.threads.QueuesAndDeadlockKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("QUEUES AND DEADLOCK  -  hand work over safely, and how it gets stuck")

    section("BlockingQueue: producer and consumer")
    syntax(
        "queue.put(x)  |  queue.take()" to "block while the queue is full  |  empty (back pressure)",
        "queue.offer(x)  |  queue.poll(t, unit)" to "never block (or wait up to t): false  |  null when not possible",
    )
    val queue = ArrayBlockingQueue<Int>(2)
    val consumed = mutableListOf<Int>()
    val producer = thread {
        for (i in 1..5) queue.put(i)                       // blocks whenever 2 items are waiting
        queue.put(-1)                                      // a 'poison pill' tells the consumer to stop
    }
    val consumer = thread {
        var v = queue.take()
        while (v != -1) {
            consumed.add(v)
            v = queue.take()
        }
    }
    producer.join()
    consumer.join()
    show { consumed }                                      // one producer, one consumer: FIFO order is guaranteed
    val full = ArrayBlockingQueue<Int>(2)
    show { full.offer(1) }                                 // fits
    show { full.offer(2) }                                 // fits (capacity 2)
    show { full.offer(3) }                                 // full: offer returns false instead of waiting
    val empty = ArrayBlockingQueue<Int>(2)
    val timedOut = empty.poll(50, MILLISECONDS);   show { timedOut }   // an empty queue: null after the wait
    note("take() and put() throw InterruptedException: that is how a consumer is asked to stop")

    section("Deadlock: two locks, opposite order")
    syntax("T1: lock A then B   |   T2: lock B then A" to "T1 holds A and waits for B, T2 holds B and waits for A: both wait forever")
    val lockA = ReentrantLock()
    val lockB = ReentrantLock()
    val gotSecond = BooleanArray(2)
    val holding = CountDownLatch(2)
    val tried = CountDownLatch(2)
    val t1 = grab(lockA, lockB, gotSecond, 0, holding, tried)
    val t2 = grab(lockB, lockA, gotSecond, 1, holding, tried)
    t1.join()
    t2.join()
    show { gotSecond }                                     // [false, false]: neither got its second lock. With lock() instead of tryLock they would hang
    note("The four ingredients: mutual exclusion, hold and wait, no preemption, circular wait. Break one to be safe")
    note("Fixes: always lock in the SAME global order (A then B everywhere); or tryLock with a timeout; or use ONE lock")
    note("HashMap and ArrayList are never safe for concurrent writes: use the concurrent versions or a lock. Coroutine Mutex: CoroutineSharedState")
}
