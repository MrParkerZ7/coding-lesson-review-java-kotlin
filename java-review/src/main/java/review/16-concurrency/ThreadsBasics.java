package review.concurrency;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 16a. THREADS: create, start vs run, join, states, interrupt, daemon.  (Every thread is joined, so the output never changes.)
 *      Run: mvn -q compile exec:java -Dmain.class=review.concurrency.ThreadsBasics
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ThreadsBasics {

    public static void main(String[] args) throws InterruptedException {
        title("THREADS  -  start, join, interrupt, daemon");

        section("Create and start");
        syntax(
                "new Thread(runnable)", "a thread object; nothing runs yet (state NEW)",
                "t.start()", "starts a NEW thread that runs the body; start() returns at once",
                "t.run()", "TRAP: just calls the body on the CURRENT thread, no new thread at all",
                "t.join()", "wait until t has finished (also makes its writes visible to you)"
        );
        Thread caller = Thread.currentThread();
        AtomicReference<Thread> ranOn = new AtomicReference<>();
        Thread viaRun = new Thread(() -> ranOn.set(Thread.currentThread()));
        viaRun.run();
        show(() -> ranOn.get() == caller);                                 // true: run() did not create a thread, main ran the body
        Thread viaStart = new Thread(() -> ranOn.set(Thread.currentThread()));
        viaStart.start();
        viaStart.join();
        show(() -> ranOn.get() == caller);                                 // false: start() ran the body on another thread
        wontCompile("new Thread(() -> Thread.sleep(10));", "unreported exception InterruptedException: Runnable.run() declares no checked exceptions");
        wontCompile("int n = 0; new Thread(() -> n++);", "local variables referenced from a lambda must be final or effectively final");
        note("A thread cannot change a local variable: share state through an object (AtomicInteger, a synchronized counter, a list)");

        section("Thread states");
        syntax("NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED", "Thread.State: where a thread is in its life");
        Thread worker = new Thread(() -> { });
        show(() -> worker.getState());                                     // created, not started
        worker.start();
        worker.join();
        show(() -> worker.getState());                                     // finished
        show(() -> thrown(() -> { worker.start(); return 0; }));           // a thread can be started only once
        note("A finished Thread object cannot be restarted: create a new one, or better use an executor (ExecutorsAndFutures)");

        section("Interrupt: a request to stop, not a kill");
        syntax(
                "t.interrupt()", "sets the interrupt flag; sleep / wait / join then throw InterruptedException",
                "Thread.currentThread().interrupt()", "restore the flag after catching InterruptedException"
        );
        AtomicBoolean interrupted = new AtomicBoolean();
        AtomicBoolean flagInsideCatch = new AtomicBoolean(true);
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                interrupted.set(true);
                flagInsideCatch.set(Thread.currentThread().isInterrupted());
            }
        });
        sleeper.start();
        sleeper.interrupt();
        sleeper.join();
        show(() -> interrupted.get());                                     // the 60 s sleep ended at once
        show(() -> flagInsideCatch.get());                                 // TRAP: throwing InterruptedException CLEARS the flag: restore it yourself
        note("Never swallow InterruptedException with an empty catch: call Thread.currentThread().interrupt() or rethrow");

        section("An exception stays inside its thread");
        AtomicReference<String> seenByHandler = new AtomicReference<>("nothing");
        Thread failing = new Thread(() -> { throw new IllegalStateException("boom"); });
        failing.setUncaughtExceptionHandler((t, e) -> seenByHandler.set(e.getClass().getSimpleName()));
        failing.start();
        failing.join();
        show(() -> seenByHandler.get());                                   // main never saw it: only the thread's handler did
        note("Without a handler the JVM prints the stack trace to stderr and the thread dies; main carries on as if nothing happened");

        section("Daemon threads");
        syntax("t.setDaemon(true)", "a daemon does not keep the JVM alive: when the last normal thread ends, daemons are cut off");
        Thread daemon = new Thread(() -> { });
        daemon.setDaemon(true);
        show(() -> daemon.isDaemon());
        show(() -> caller.isDaemon());                                     // main is a normal (non-daemon) thread
        CountDownLatch release = new CountDownLatch(1);
        Thread alive = new Thread(() -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        alive.start();
        show(() -> thrown(() -> { alive.setDaemon(true); return 0; }));   // too late: setDaemon throws once the thread is alive
        release.countDown();
        alive.join();
        note("Use a daemon for background housekeeping only: it may be stopped in the middle of its work, finally blocks included");
        note("Next: SynchronizedAndLocks and AtomicsAndVolatile (shared state), ExecutorsAndFutures, ConcurrentCollections, QueuesAndDeadlock, VirtualThreads");
    }
}
