package review.threads

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import review.note
import review.section
import review.show
import review.syntax
import review.title

/**
 * 22a. THREADS: start, join, interrupt, daemon, virtual
 *      Run: mvn -q compile exec:java -Dmain.class=review.threads.ThreadsBasicsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("THREADS  -  start, join, interrupt, virtual")

    section("Start a thread and wait for it")
    syntax(
        "thread { ... }" to "kotlin.concurrent.thread: creates AND starts a thread (start = false only creates it)",
        "Thread { ... }  then  t.start()" to "the Java way: the lambda becomes the Runnable; nothing runs until start()",
        "t.join()" to "block until t has finished; also makes everything t wrote visible to the caller",
        "t.isAlive  |  t.state" to "started and not finished  |  NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED",
    )
    val log = mutableListOf<String>()
    val worker = thread { log.add("worker ran") }
    worker.join()
    show { log }                                           // after join the caller sees what the worker wrote
    show { worker.isAlive }                                // false: it has finished
    show { worker.state }                                  // a finished thread stays TERMINATED; the object is not reusable
    val idle = Thread { log.add("never") }
    show { idle.state }                                    // NEW: created, never started (so "never" is not in the log)

    section("start() vs run(): the classic trap")
    syntax(
        "t.start()" to "runs the body on a NEW thread; allowed once per Thread object",
        "t.run()" to "a plain method call: the body runs on the CALLER's thread, no new thread exists",
    )
    val mainThread = Thread.currentThread()
    var ranOn: Thread? = null
    val direct = Thread { ranOn = Thread.currentThread() }
    direct.run()
    show { ranOn === mainThread }                          // true: run() executed right here, in main
    val real = Thread { ranOn = Thread.currentThread() }
    real.start()
    real.join()
    show { ranOn === mainThread }                          // false: start() moved the body to another thread
    show { thrown { real.start() } }                       // a thread can be started only once, even after it has finished
    note("Kotlin has no checked exceptions, so nothing forces you to handle InterruptedException or call join(): both are easy to forget")

    section("Interrupt: a polite request to stop")
    syntax(
        "t.interrupt()" to "sets the interrupt flag; sleep / wait / join / take then throw InterruptedException",
        "Thread.currentThread().isInterrupted" to "read the flag without clearing it",
        "catch (e: InterruptedException)" to "the flag is CLEARED when it is thrown: call interrupt() again if you cannot stop",
    )
    val seen = mutableListOf<String>()
    val sleeper = thread {
        try {
            Thread.sleep(60_000)
        } catch (e: InterruptedException) {
            seen.add("woken, flag=" + Thread.currentThread().isInterrupted)
        }
    }
    sleeper.interrupt()
    sleeper.join()
    show { seen }                                          // woken at once (not after 60 s); the flag was reset by the exception
    note("interrupt() never kills a thread: the code must notice (an exception, or a check of isInterrupted) and return")

    section("Daemon threads")
    syntax("thread(isDaemon = true) { ... }" to "a background helper: the JVM exits when only daemon threads are left")
    val background = thread(start = false, isDaemon = true) { }
    show { background.isDaemon }                           // set by isDaemon = true, before the thread starts
    show { mainThread.isDaemon }                           // false: main is a normal (user) thread
    note("A fixed-pool thread is NOT a daemon: a forgotten executor keeps the program alive (shown in ExecutorsAndFutures)")

    section("Virtual threads (JDK 21)")
    syntax(
        "Thread.ofVirtual().start { ... }" to "create and start a virtual thread: cheap, made for code that mostly waits",
        "Executors.newVirtualThreadPerTaskExecutor()" to "an executor that makes a NEW virtual thread for each task",
    )
    val ran = AtomicInteger()
    val virtual = Thread.ofVirtual().start { ran.incrementAndGet() }
    virtual.join()
    show { ran.get() }                                     // 1: the body ran
    show { virtual.isVirtual }                             // a normal java.lang.Thread: same API (start, join, interrupt)
    show { mainThread.isVirtual }                          // false: main is a platform (OS) thread
    show { virtual.isDaemon }                              // virtual threads are always daemons
    val done = AtomicInteger()
    Executors.newVirtualThreadPerTaskExecutor().use { pool ->
        repeat(10_000) { pool.submit(Runnable { Thread.sleep(50); done.incrementAndGet() }) }
    }                                                      // close() waits until every task has finished
    show { done.get() }                                    // 10000 threads all sleeping at the same time: fine as virtual threads
    note("One virtual thread per task, never pooled. They help code that WAITS (HTTP, database); CPU-bound work gets no speed-up")
    note("Coroutines (lesson 17) solve the same problem in a library; virtual threads solve it in the JVM, with plain blocking code")
}
