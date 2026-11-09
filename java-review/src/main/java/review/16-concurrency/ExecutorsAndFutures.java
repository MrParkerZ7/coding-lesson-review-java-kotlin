package review.concurrency;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 16d. EXECUTORS, Future AND CompletableFuture
 *      Run: mvn -q compile exec:java -Dmain.class=review.concurrency.ExecutorsAndFutures
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ExecutorsAndFutures {

    /** A task that returns a value, so it is a Callable, not a Runnable. */
    static Callable<Integer> square(int n) {
        return () -> n * n;
    }

    /** Value of a call that may throw, or the class name of what it threw plus its cause (so show can print either). */
    static Object outcome(Callable<?> call) {
        try {
            return call.call();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            String name = e.getClass().getSimpleName();
            return cause == null ? name : name + "(cause " + cause.getClass().getSimpleName() + ")";
        }
    }

    static int slowFail() {
        throw new IllegalStateException("boom");
    }

    public static void main(String[] args) throws Exception {
        title("EXECUTORS AND FUTURES  -  a pool of threads, results you wait for");

        section("ExecutorService: submit a task, get a Future");
        syntax(
                "ExecutorService pool = Executors.newFixedThreadPool(n)", "n worker threads reuse a queue of tasks",
                "Future<T> f = pool.submit(callable)", "starts the task; returns at once with a ticket",
                "f.get()", "BLOCKS until the result is ready; rethrows a task failure as ExecutionException",
                "pool.shutdown(); pool.awaitTermination(t, unit)", "stop accepting tasks, then wait for the running ones",
                "try (ExecutorService pool = ...) { ... }", "Java 19+: close() = shutdown + wait; the modern form"
        );
        ExecutorService pool = Executors.newFixedThreadPool(3);
        Future<Integer> nine = pool.submit(square(3));
        int nineValue = nine.get();   show(() -> nineValue);               // 9 (get blocks until the result is ready)
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) futures.add(pool.submit(square(i)));
        int sum = 0;
        for (Future<Integer> f : futures) sum += f.get();                  // collected in submit order, however they finished
        int total = sum;
        show(() -> total);                                                 // 1 + 4 + 9 + 16 + 25
        List<Future<Integer>> all = pool.invokeAll(List.of(square(2), square(3), square(4)));
        List<Integer> results = new ArrayList<>();
        for (Future<Integer> f : all) results.add(f.get());
        show(() -> results);                                               // invokeAll waits for all; the futures keep the task order
        pool.shutdown();
        boolean finished = pool.awaitTermination(5, TimeUnit.SECONDS);   show(() -> finished);   // true: every task finished within the wait
        show(() -> pool.isShutdown());
        show(() -> outcome(() -> pool.submit(square(1))));                 // a closed pool refuses new tasks
        note("Forgetting shutdown() keeps the JVM alive: the pool threads are not daemons");

        section("Failures, timeouts and cancel");
        try (ExecutorService scoped = Executors.newSingleThreadExecutor()) {   // single thread: tasks run one after another, in order
            Future<Integer> failing = scoped.submit(ExecutorsAndFutures::slowFail);
            show(() -> outcome(() -> failing.get()));                      // the task's exception arrives wrapped; getCause() has the real one
            CountDownLatch release = new CountDownLatch(1);
            Future<Integer> blocked = scoped.submit(() -> {
                release.await();                                           // blocks until main lets it go
                return 7;
            });
            show(() -> outcome(() -> blocked.get(50, TimeUnit.MILLISECONDS)));   // get with a timeout gives up; the task keeps running
            show(() -> blocked.cancel(true));                              // true: interrupts the waiting task
            show(() -> blocked.isCancelled());
            show(() -> outcome(() -> blocked.get()));                      // a cancelled future has no result
            release.countDown();
        }                                                                  // close(): waits for the pool to finish
        note("Never call f.get() from a task that runs in the SAME small pool: all workers wait on each other (thread starvation)");
        wontCompile("String s = future.get();  // no try / throws", "unreported exception InterruptedException: get() declares InterruptedException and ExecutionException");

        section("CompletableFuture: a pipeline of steps");
        syntax(
                "supplyAsync(() -> value)", "run on the common pool, return a CompletableFuture<T>",
                "thenApply(f) | thenCompose(f) | thenCombine(other, f)", "map | flatMap (f returns a future) | join two futures",
                "allOf(a, b, c)", "completes when every future has completed",
                "exceptionally(f) | handle((v, ex) -> ...)", "recover from a failure | see value and failure together",
                "orTimeout(t, unit) | completeOnTimeout(v, t, unit)", "fail | complete with a default when too slow",
                "join() | get()", "wait for the result: join throws CompletionException (unchecked), get throws checked ones"
        );
        CompletableFuture<Integer> ten = CompletableFuture.supplyAsync(() -> 10);
        show(() -> ten.thenApply(x -> x * 2).join());                      // 20
        show(() -> ten.thenCombine(CompletableFuture.supplyAsync(() -> 5), Integer::sum).join());   // two independent futures combined: 10 + 5
        show(() -> ten.thenCompose(x -> CompletableFuture.supplyAsync(() -> x + 1)).join());        // the next step is itself async
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 1);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 2);
        CompletableFuture.allOf(a, b).join();
        show(() -> a.join() + b.join());                                   // after allOf both are done, join returns at once
        CompletableFuture<Integer> broken = CompletableFuture.supplyAsync(ExecutorsAndFutures::slowFail);
        show(() -> broken.exceptionally(ex -> -1).join());                 // recovered with a default
        CompletableFuture<String> described = broken.handle((value, ex) -> ex == null ? "ok" : "failed: " + ex.getCause().getMessage());
        show(() -> described.join());                                      // handle sees value and failure together; ex is the CompletionException
        show(() -> outcome(() -> broken.join()));                          // join: CompletionException wrapping the real one
        show(() -> outcome(() -> broken.get()));                           // get: ExecutionException wrapping the real one
        show(() -> outcome(() -> new CompletableFuture<Integer>().orTimeout(50, TimeUnit.MILLISECONDS).join()));   // never completed: times out
        show(() -> new CompletableFuture<Integer>().completeOnTimeout(99, 50, TimeUnit.MILLISECONDS).join());      // or answer with a default
        note("supplyAsync without an executor uses the common pool (daemon threads): pass your own pool for blocking work");
        note("Do not use .join() in the middle of a chain: chain with thenApply / thenCompose so nothing blocks");
    }
}
