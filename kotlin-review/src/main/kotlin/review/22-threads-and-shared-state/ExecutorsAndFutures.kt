package review.threads

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** A task that returns a value: written as Callable { }, because a bare lambda would become a Runnable and lose the value. */
private fun square(n: Int): Callable<Int> = Callable { n * n }

private fun slowFail(): Int = error("boom")

private fun plusOne(x: Int): CompletableFuture<Int> = CompletableFuture.supplyAsync { x + 1 }

/** The value of a call, or the class of the exception that came out (plus its cause), so show can print either. */
private fun outcome(call: () -> Any?): Any? = try {
    call()
} catch (e: Exception) {
    val cause = e.cause
    if (cause == null) e.javaClass.simpleName else e.javaClass.simpleName + "(cause " + cause.javaClass.simpleName + ")"
}

/**
 * 22d. EXECUTORS, Future AND CompletableFuture
 *      Run: mvn -q compile exec:java -Dmain.class=review.threads.ExecutorsAndFuturesKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("EXECUTORS AND FUTURES  -  a pool of threads, results you wait for")

    section("ExecutorService: submit a task, get a Future")
    syntax(
        "val pool = Executors.newFixedThreadPool(n)" to "n worker threads reuse a queue of tasks",
        "val f = pool.submit(Callable { ... })" to "starts the task; returns at once with a ticket (a Future)",
        "f.get()" to "BLOCKS until the result is ready; rethrows a task failure as ExecutionException",
        "pool.shutdown(); pool.awaitTermination(t, unit)" to "stop accepting tasks, then wait for the running ones",
        "pool.use { ... }" to "JDK 19+: an executor is AutoCloseable; close() = shutdown + wait, the modern form",
    )
    val pool = Executors.newFixedThreadPool(3)
    val nine = pool.submit(square(3)).get();   show { nine }   // 9 (get blocks until the result is ready)
    val futures = (1..5).map { pool.submit(square(it)) }
    val total = futures.sumOf { it.get() }
    show { total }                                         // 1 + 4 + 9 + 16 + 25, collected in submit order however they finished
    val results = pool.invokeAll(listOf(square(2), square(3), square(4))).map { it.get() }
    show { results }                                       // invokeAll waits for all; the futures keep the task order
    pool.shutdown()
    val finished = pool.awaitTermination(5, SECONDS);   show { finished }   // true: every task finished within the wait
    show { pool.isShutdown }
    show { outcome { pool.submit(square(1)) } }            // a closed pool refuses new tasks
    val daemonCheck = Callable { Thread.currentThread().isDaemon }
    Executors.newFixedThreadPool(1).use { fixed ->
        show { fixed.submit(daemonCheck).get() }           // false: a fixed-pool thread is not a daemon
    }
    note("Forgetting shutdown() (or use { }) keeps the JVM alive, because the pool threads are not daemons")

    section("Trap: submit { } with a bare lambda")
    Executors.newSingleThreadExecutor().use { single ->
        show { single.submit { 42 }.get() }                // null! Kotlin picks submit(Runnable) and throws the 42 away
        show { single.submit(Callable { 42 }).get() }      // 42: Callable { } is the form that returns a value
    }

    section("Failures, timeouts and cancel")
    Executors.newSingleThreadExecutor().use { scoped ->     // one thread: tasks run one after another, in order
        val failing = scoped.submit(Callable { slowFail() })
        show { outcome { failing.get() } }                 // the task's exception arrives wrapped; cause is the real one
        val release = CountDownLatch(1)
        val blocked = scoped.submit(Callable { release.await(); 7 })
        show { outcome { blocked.get(50, MILLISECONDS) } }   // get with a timeout gives up; the task keeps running
        show { blocked.cancel(true) }                      // true: interrupts the waiting task
        show { blocked.isCancelled }
        show { outcome { blocked.get() } }                 // a cancelled future has no result
        release.countDown()
    }
    Executors.newSingleThreadExecutor().use { one ->
        val outer = one.submit(Callable { one.submit(Callable { 1 }).get(200, MILLISECONDS) })
        show { outcome { outer.get() } }                   // starvation: the only worker waits for a task that can never start
    }
    note("Never call f.get() from a task running in the SAME small pool: every worker waits for a task that is stuck behind it")

    section("CompletableFuture: a pipeline of steps")
    syntax(
        "CompletableFuture.supplyAsync { value }" to "run on the common pool, return a CompletableFuture<T>",
        "thenApply { } | thenCompose { } | thenCombine(other) { a, b -> }" to "map | flatMap (the lambda returns a future) | join two futures",
        "CompletableFuture.allOf(a, b)" to "completes when every future has completed",
        "exceptionally { } | handle { value, ex -> }" to "recover from a failure | see value and failure together",
        "orTimeout(t, unit) | completeOnTimeout(v, t, unit)" to "fail | complete with a default when too slow",
        "join() | get()" to "wait for the result: join throws CompletionException, get throws ExecutionException",
    )
    val ten = CompletableFuture.supplyAsync { 10 }
    show { ten.thenApply { it * 2 }.join() }               // 20
    val five = CompletableFuture.supplyAsync { 5 }
    show { ten.thenCombine(five) { a, b -> a + b }.join() }   // two independent futures combined: 10 + 5
    show { ten.thenCompose(::plusOne).join() }             // the next step is itself async (plusOne returns a future)
    val a = CompletableFuture.supplyAsync { 1 }
    val b = CompletableFuture.supplyAsync { 2 }
    CompletableFuture.allOf(a, b).join()
    show { a.join() + b.join() }                           // after allOf both are done, join returns at once
    val broken = CompletableFuture.supplyAsync { slowFail() }
    show { broken.exceptionally { -1 }.join() }            // recovered with a default
    val described = broken.handle { _, ex -> if (ex == null) "ok" else "failed: " + ex.cause?.message }
    show { described.join() }                              // handle sees value and failure together; ex is the CompletionException
    show { outcome { broken.join() } }                     // join: CompletionException wrapping the real one
    show { outcome { broken.get() } }                      // get: ExecutionException wrapping the real one
    val neverDone = CompletableFuture<Int>()
    show { outcome { neverDone.orTimeout(50, MILLISECONDS).join() } }   // never completed: times out
    val alsoNeverDone = CompletableFuture<Int>()
    show { alsoNeverDone.completeOnTimeout(99, 50, MILLISECONDS).join() }   // or answer with a default
    show { CompletableFuture.supplyAsync { Thread.currentThread().isDaemon }.join() }   // true: the common pool uses daemon threads
    note("supplyAsync without an executor uses the common pool: pass your own pool for blocking work")
    note("Do not use .join() in the middle of a chain: chain with thenApply / thenCompose so nothing blocks. Coroutines (lesson 17) replace this style")
}
