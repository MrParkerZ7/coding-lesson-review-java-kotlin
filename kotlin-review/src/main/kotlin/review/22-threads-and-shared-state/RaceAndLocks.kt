package review.threads

import java.lang.reflect.Modifier
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.concurrent.write
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

private const val THREADS = 4
private const val PER_THREAD = 1000

/** value++ is THREE steps (read, add, write): two threads can read the same value and one update is lost. */
private class UnsafeCounter {
    var value = 0

    fun increment() {
        value++
    }
}

/** @Synchronized: the whole method runs holding the lock of `this`. */
private class SyncMethodCounter {
    private var value = 0

    @Synchronized
    fun increment() {
        value++
    }

    @Synchronized
    fun get(): Int = value

    @Synchronized
    fun incrementTwice() {
        increment()                                        // enters another @Synchronized method while holding the lock: allowed
        increment()
    }
}

/** synchronized(lock) { }: a block on a private lock object, so nobody outside can lock it by accident. */
private class SyncBlockCounter {
    private val lock = Any()
    private var value = 0

    fun increment() {
        synchronized(lock) { value++ }
    }

    fun get(): Int = synchronized(lock) { value }
}

/** ReentrantLock + withLock: the same idea, explicit. */
private class LockCounter {
    private val lock = ReentrantLock()
    private var value = 0

    fun increment() {
        lock.withLock { value++ }
    }

    fun get(): Int = lock.withLock { value }
}

/**
 * 22b. SHARED STATE: the race, @Synchronized, synchronized(lock), ReentrantLock, read-write lock
 *      Run: mvn -q compile exec:java -Dmain.class=review.threads.RaceAndLocksKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("SHARED STATE  -  4 threads x 1000 increments, locks")

    val expected = THREADS * PER_THREAD

    section("The race: value++ is not atomic")
    syntax(
        "read value, add 1, write value" to "what value++ really does; two threads can interleave between the steps",
        "lost update" to "both read 5, both write 6: one increment disappears",
    )
    val shared = intArrayOf(5)
    val bothRead = CyclicBarrier(2)
    runAll(2) {
        val read = shared[0]                               // both threads read 5 ...
        bothRead.await()                                   // ... wait until the other one has read too ...
        shared[0] = read + 1                               // ... and both write 6
    }
    show { shared[0] }                                     // 6, not 7: a forced interleaving, so it is the same on every run
    val unsafe = UnsafeCounter()
    hammer(THREADS, PER_THREAD) { unsafe.increment() }
    show { unsafe.value <= expected }                      // always true: updates can only be LOST, never invented
    show { expected }                                      // what a correct counter must reach
    note("The unsafe total is not printed: it is not guaranteed. On a fast machine it may well equal 4000 and hide the bug")
    note("That is the danger: a race can pass every test on your machine and fail in production. val does not help: it only fixes the reference")

    section("Fix 1: @Synchronized and synchronized(lock) { }")
    syntax(
        "@Synchronized fun m() { ... }" to "the whole method runs holding the lock of `this` (Java: synchronized void m())",
        "synchronized(lockObject) { ... }" to "a block: smaller scope, any object as the lock; a stdlib function, not a keyword",
    )
    val method = SyncMethodCounter()
    hammer(THREADS, PER_THREAD) { method.increment() }
    show { method.get() }                                  // exactly 4000
    val block = SyncBlockCounter()
    hammer(THREADS, PER_THREAD) { block.increment() }
    show { block.get() }                                   // exactly 4000
    val incrementMethod = SyncMethodCounter::class.java.getMethod("increment")
    show { Modifier.isSynchronized(incrementMethod.modifiers) }   // true: the annotation sets the JVM synchronized flag on the method
    wontCompile("synchronized fun inc() { }", "syntax error: Kotlin has no synchronized keyword, write @Synchronized fun inc()")
    note("A lock must guard READS too (get is synchronized): otherwise a reader may see a stale value")
    val reentrant = SyncMethodCounter()
    reentrant.incrementTwice()
    show { reentrant.get() }                               // 2: the lock is REENTRANT, a thread never blocks on a lock it already holds

    section("Fix 2: ReentrantLock, withLock and tryLock")
    syntax(
        "lock.withLock { ... }" to "kotlin.concurrent: lock(), then the block in try, unlock() in finally; returns the block's value",
        "lock.lock(); try { ... } finally { lock.unlock() }" to "the manual form: never forget the finally",
        "lock.tryLock()" to "take the lock only if it is free right now (also with a timeout)",
    )
    val locked = LockCounter()
    hammer(THREADS, PER_THREAD) { locked.increment() }
    show { locked.get() }                                  // exactly 4000
    val lock = ReentrantLock()
    show { lock.withLock { 40 + 2 } }                      // 42: withLock returns what the block returns
    show { lock.isLocked }                                 // false: withLock released the lock on the way out
    lock.lock(); lock.lock()
    show { lock.holdCount }                                // 2: the same thread may lock again, it then needs 2 unlocks
    lock.unlock(); lock.unlock()
    lock.lock()
    var otherGotIt = true
    val other = thread { otherGotIt = lock.tryLock() }
    other.join()
    show { otherGotIt }                                    // false: main holds the lock, tryLock does not wait
    lock.unlock()

    section("ReentrantReadWriteLock: many readers or one writer")
    syntax(
        "rw.read { ... }" to "shared: any number of threads at once, but no writer",
        "rw.write { ... }" to "exclusive: one thread, no readers at the same time",
    )
    val rw = ReentrantReadWriteLock()
    show { rw.read { rw.readLockCount } }                  // 1: inside read { } this thread holds one read lock
    show { rw.readLockCount }                              // 0: released again
    val writeLock = rw.writeLock()
    show { rw.read { writeLock.tryLock(100, MILLISECONDS) } }   // false: the raw API cannot upgrade a read lock to a write lock
    show { rw.read { rw.write { "upgraded" } } }           // the kotlin.concurrent write { } drops your read lock first, so this works (not atomic)
    note("Use a read-write lock when reads far outnumber writes. Atomic variables and @Volatile are the lock-free tools: AtomicsAndVolatile")
}
