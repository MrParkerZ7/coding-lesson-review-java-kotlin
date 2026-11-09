package review.concurrency;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 16g. VIRTUAL THREADS (Java 21): cheap threads for waiting work
 *      Run: mvn -q compile exec:java -Dmain.class=review.concurrency.VirtualThreads
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class VirtualThreads {

    /** Runs `tasks` tasks that each sleep `millis`, one executor per call; returns how long it all took. */
    static long elapsedMs(ExecutorService executor, int tasks, long millis) {
        long start = System.nanoTime();
        try (executor) {
            for (int i = 0; i < tasks; i++) {
                executor.submit(Workers.quietly(() -> Thread.sleep(Duration.ofMillis(millis))));
            }
        }                                                                  // close() waits for every task
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        title("VIRTUAL THREADS  -  one thread per task, millions are fine");

        section("Start one");
        syntax(
                "Thread.ofVirtual().start(runnable)", "create and start a virtual thread (Java 21, final feature)",
                "Thread.startVirtualThread(runnable)", "the short form",
                "Thread.ofPlatform().start(runnable)", "the classic OS-backed thread, for comparison",
                "Executors.newVirtualThreadPerTaskExecutor()", "an executor that makes a NEW virtual thread for each task"
        );
        AtomicInteger ran = new AtomicInteger();
        Thread virtual = Thread.ofVirtual().start(ran::incrementAndGet);
        virtual.join();
        show(() -> ran.get());                                             // 1: the body ran
        show(() -> virtual.isVirtual());
        show(() -> Thread.currentThread().isVirtual());                    // main is a platform thread
        show(() -> virtual.isDaemon());                                    // virtual threads are always daemons
        show(() -> thrown(() -> { virtual.setDaemon(false); return 0; })); // and that cannot be changed
        note("A virtual thread is a normal java.lang.Thread: same API (start, join, interrupt), so existing blocking code works");

        section("Many tasks, many threads");
        List<Future<Integer>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1000; i++) {
                int n = i;
                futures.add(executor.submit(() -> n * 2));
            }
        }                                                                  // close() waits until all 1000 tasks are done
        int sum = 0;
        for (Future<Integer> f : futures) sum += f.get();
        int total = sum;
        show(() -> futures.size());                                        // 1000 virtual threads were created and finished
        show(() -> total);                                                 // 2 * (0 + 1 + ... + 999)
        show(() -> futures.get(0).isDone());

        section("Why: blocking is cheap");
        syntax("Thread.sleep(..) | socket read | JDBC call", "a virtual thread PARKS: its carrier (OS) thread is freed for other virtual threads");
        long fixedPool = elapsedMs(Executors.newFixedThreadPool(10), 100, 100);
        long virtualPerTask = elapsedMs(Executors.newVirtualThreadPerTaskExecutor(), 100, 100);
        show(() -> fixedPool > 500);                                       // 100 tasks x 100 ms on 10 threads = 10 rounds, about 1000 ms
        show(() -> virtualPerTask < 500);                                  // 100 virtual threads sleep at the same time: about 100 ms
        long tenThousand = elapsedMs(Executors.newVirtualThreadPerTaskExecutor(), 10_000, 100);
        show(() -> tenThousand < 3000);                                    // 10 000 threads waiting at once: no problem (as platform threads each would need an OS thread and its own stack)
        note("The numbers are never printed (they vary by machine); only comparisons with a wide margin are");

        section("Limit concurrency with a Semaphore, not with a pool size");
        syntax("new Semaphore(n)  |  acquire()  |  release()", "at most n holders at once: protects a database or an API from 10 000 callers");
        Semaphore permits = new Semaphore(3);
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger mostInside = new AtomicInteger();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) {
                executor.submit(Workers.quietly(() -> {
                    permits.acquire();
                    try {
                        mostInside.accumulateAndGet(inside.incrementAndGet(), Math::max);
                        Thread.sleep(Duration.ofMillis(10));
                        inside.decrementAndGet();
                    } finally {
                        permits.release();
                    }
                }));
            }
        }
        show(() -> mostInside.get() <= 3);                                 // 20 tasks, never more than 3 inside at once
        show(() -> mostInside.get() >= 1);

        section("When to use it, and the traps");
        note("Use for many tasks that mostly WAIT (HTTP calls, database, sleep). CPU-bound work gets no speedup: use a pool sized to the cores");
        note("Never pool virtual threads: they are cheap, make one per task (newVirtualThreadPerTaskExecutor)");
        note("Pinning (Java 21): blocking inside a synchronized block or a native call keeps the carrier thread busy; prefer ReentrantLock");
        note("JDK 24 (JEP 491) removed the synchronized pinning. Structured concurrency (StructuredTaskScope) is still a preview feature in 21");
        note("ThreadLocal works, but 1 million virtual threads x a big ThreadLocal value is a million copies");
    }
}
