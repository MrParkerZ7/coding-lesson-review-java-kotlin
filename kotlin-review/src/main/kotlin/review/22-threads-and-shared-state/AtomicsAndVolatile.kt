package review.threads

import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder
import kotlin.concurrent.thread
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

private const val THREADS = 4
private const val PER_THREAD = 1000

/** @Volatile: every read sees the latest write by any thread. It says nothing about ++ or check-then-act. */
private class Flag {
    @Volatile
    var on = false
}

private class VolatileCounter {
    @Volatile
    var value = 0
}

/**
 * 22c. LOCK-FREE SHARED STATE: AtomicInteger, compareAndSet, AtomicReference, LongAdder, @Volatile
 *      Run: mvn -q compile exec:java -Dmain.class=review.threads.AtomicsAndVolatileKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("ATOMICS AND VOLATILE  -  4 threads x 1000 increments, no lock")

    section("AtomicInteger: atomic read-modify-write")
    syntax(
        "val n = AtomicInteger(0)" to "an Int whose operations are atomic (hardware compare-and-set)",
        "n.incrementAndGet()  |  getAndIncrement()" to "add 1 and return the NEW  |  the OLD value",
        "n.compareAndSet(expected, update)" to "set only if the value is still `expected`; true when it did",
        "n.updateAndGet { x -> ... }" to "a retry loop built in: apply the lambda, try again if another thread got in between",
    )
    val atomic = AtomicInteger()
    hammer(THREADS, PER_THREAD) { atomic.incrementAndGet() }
    show { atomic.get() }                                  // exactly 4000, no lock
    val cas = AtomicInteger(5)
    show { cas.compareAndSet(5, 6) }                       // the value was 5: set to 6
    show { cas.compareAndSet(5, 7) }                       // the value is 6 now: refused
    show { cas.updateAndGet { it * 10 } }                  // 6 * 10; the lambda may run again after a lost race: keep it free of side effects
    show { cas.getAndIncrement() }                         // returns the OLD value
    show { cas.get() }
    wontCompile("val n = AtomicInteger(); n++", "AtomicInteger has no inc() operator (and n is a val): call n.incrementAndGet()")
    note("Atomics fix ONE variable. Two variables that must change together still need a lock")

    section("compareAndSet as a gate: exactly one winner")
    val started = AtomicBoolean(false)
    val winners = AtomicInteger()
    runAll(THREADS) {
        if (started.compareAndSet(false, true)) winners.incrementAndGet()   // only the first thread flips false to true
    }
    show { winners.get() }                                 // 1, however the 4 threads are scheduled: a safe 'run once'
    val owner = AtomicReference("nobody")
    show { owner.compareAndSet("nobody", "Ann") }         // AtomicReference: the same idea for an object reference
    show { owner.compareAndSet("nobody", "Bo") }          // refused: the owner is Ann now
    show { owner.get() }
    val other = AtomicReference("nobody")
    val copy = String("nobody".toCharArray())
    show { copy == "nobody" }                              // true: equal text ...
    show { other.compareAndSet(copy, "Cy") }               // ... but false here: compareAndSet compares with ===, not equals
    note("For Strings, boxed numbers and other objects pass the very instance you read, not an equal copy")

    section("LongAdder: a counter for many writers")
    syntax("val adder = LongAdder()" to "a counter split in cells: faster than AtomicLong when many threads add; read it with sum()")
    val adder = LongAdder()
    hammer(THREADS, PER_THREAD) { adder.increment() }
    show { adder.sum() }                                   // 4000 (a Long)
    note("LongAdder has no compareAndSet: use AtomicLong for a conditional update, LongAdder for pure statistics")

    section("@Volatile: visibility, not atomicity")
    syntax("@Volatile var on = false" to "every read sees the latest write by any thread; it does NOT make on++ or check-then-act atomic")
    val flag = Flag()
    val waiter = thread { while (!flag.on) Thread.onSpinWait() }
    flag.on = true
    waiter.join()
    show { waiter.isAlive }                                // false: the waiter saw the volatile write and stopped
    val onField = Flag::class.java.getDeclaredField("on")
    show { Modifier.isVolatile(onField.modifiers) }        // true: the annotation sets the JVM volatile flag on the field
    val volatileCounter = VolatileCounter()
    hammer(THREADS, PER_THREAD) { volatileCounter.value++ }
    val expected = THREADS * PER_THREAD
    show { volatileCounter.value <= expected }             // always true; the total is NOT printed: @Volatile does not fix the lost update
    note("Without @Volatile the JVM does not promise the waiter ever sees the change")
    note("Rule: @Volatile for a simple flag written by one thread; atomic or lock for counters and compound updates")
}
