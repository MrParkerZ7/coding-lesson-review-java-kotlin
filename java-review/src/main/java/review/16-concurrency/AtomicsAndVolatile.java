package review.concurrency;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 16c. LOCK-FREE SHARED STATE: AtomicInteger, compareAndSet, AtomicReference, volatile
 *      Run: mvn -q compile exec:java -Dmain.class=review.concurrency.AtomicsAndVolatile
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class AtomicsAndVolatile {

    static final int THREADS = 4;
    static final int PER_THREAD = 1000;

    /** volatile: every thread sees the latest write. It says nothing about ++ or check-then-act. */
    static volatile boolean ready;

    public static void main(String[] args) throws InterruptedException {
        title("ATOMICS AND VOLATILE  -  4 threads x 1000 increments, no lock");

        section("AtomicInteger: atomic read-modify-write");
        syntax(
                "AtomicInteger n = new AtomicInteger(0)", "an int whose operations are atomic (hardware compare-and-set)",
                "n.incrementAndGet() | getAndIncrement()", "add 1 and return the NEW | the OLD value",
                "n.compareAndSet(expected, update)", "set only if the value is still `expected`; true when it did",
                "n.updateAndGet(x -> ...)", "a retry loop built in: apply f, and try again if another thread got in between"
        );
        AtomicInteger atomic = new AtomicInteger();
        Workers.hammer(THREADS, PER_THREAD, atomic::incrementAndGet);
        show(() -> atomic.get());                                          // exactly 4000, no lock
        AtomicInteger cas = new AtomicInteger(5);
        show(() -> cas.compareAndSet(5, 6));                               // value was 5: set to 6
        show(() -> cas.compareAndSet(5, 7));                               // value is 6 now: refused
        show(() -> cas.updateAndGet(x -> x * 10));
        show(() -> cas.getAndIncrement());                                 // returns the OLD value
        show(() -> cas.get());
        note("Atomics fix ONE variable. Two variables that must change together still need a lock");

        section("compareAndSet as a gate: exactly one winner");
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicInteger winners = new AtomicInteger();
        Workers.runAll(THREADS, () -> {
            if (started.compareAndSet(false, true)) winners.incrementAndGet();   // only the first thread flips false to true
        });
        show(() -> winners.get());                                         // 1, however the 4 threads are scheduled: a safe 'run once'
        AtomicReference<String> owner = new AtomicReference<>("nobody");
        show(() -> owner.compareAndSet("nobody", "Ann"));                  // AtomicReference: the same idea for an object reference
        show(() -> owner.compareAndSet("nobody", "Bo"));                   // refused: the owner is Ann now
        show(() -> owner.get());
        AtomicReference<String> other = new AtomicReference<>("nobody");
        show(() -> other.compareAndSet(new String("nobody"), "Cy"));       // false: equal text, but a different object (it compares with ==)
        note("compareAndSet compares with ==, not equals: for Strings, Integers and other objects use the very instance you read");

        section("volatile: visibility, not atomicity");
        syntax("volatile boolean ready;", "every read sees the latest write by any thread; it does NOT make ready++ or check-then-act atomic");
        Thread waiter = new Thread(() -> {
            while (!ready) Thread.onSpinWait();
        });
        waiter.start();
        ready = true;
        waiter.join();
        show(() -> waiter.isAlive());                                      // false: the waiter saw the volatile write and stopped
        note("Without volatile the Java memory model does not promise the waiter ever sees the change: it may spin forever");
        note("Rule: volatile for a simple flag written by one thread; atomic or lock for counters and compound updates");
    }
}
