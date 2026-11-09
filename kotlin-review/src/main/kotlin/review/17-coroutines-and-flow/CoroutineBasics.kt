package review.coroutines

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

suspend fun fetchTen(): Int { delay(300); return 10 }      // a suspend function: pauses without blocking its thread
suspend fun fetchTwentyTwo(): Int { delay(300); return 22 }

private fun elapsedMs(block: () -> Unit): Long {
    val start = System.nanoTime()
    block()
    return (System.nanoTime() - start) / 1_000_000
}

/**
 * 17a. COROUTINE BASICS: runBlocking, launch, async, suspend, delay
 *      Run: mvn -q compile exec:java -Dmain.class=review.coroutines.CoroutineBasicsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("COROUTINE BASICS  -  launch, async, await, suspend")

    section("suspend and runBlocking")
    syntax(
        "suspend fun name(): T" to "can pause (delay, await ...) without blocking its thread",
        "runBlocking { ... }" to "starts a coroutine and BLOCKS the thread until it ends: main and tests only",
        "delay(ms)" to "pauses the coroutine and frees the thread; Thread.sleep blocks it",
    )
    show { runBlocking { fetchTen() } }                    // the value of the last expression comes back out
    wontCompile("fun plain() = fetchTen()", "a suspend function can only be called from a coroutine or another suspend function")
    note("A suspend function is not a thread and not async by itself: it is an ordinary function that is ALLOWED to pause")

    section("launch: start a child and keep going")
    syntax(
        "launch { ... }" to "fire and forget: returns a Job at once, the body runs concurrently",
        "job.join()" to "wait until that job is finished",
    )
    val order = mutableListOf<String>()
    runBlocking {
        launch { delay(300); order.add("C") }
        launch { delay(100); order.add("A") }
        launch { delay(200); order.add("B") }
        order.add("all launched")                          // launch did not wait: this runs first
    }
    show { order }                                         // the delays decide the order, not the order of the launch calls
    val joined = mutableListOf<String>()
    runBlocking {
        val job = launch { delay(100); joined.add("job body") }
        joined.add("before join")
        job.join()                                         // suspends here until the job is done
        joined.add("after join")
    }
    show { joined }
    val states = runBlocking {
        val job = launch { delay(50) }
        val before = job.isActive
        job.join()
        listOf(before, job.isActive, job.isCompleted)
    }
    show { states }                                        // [active before, active after join, completed]

    section("async / await: start a child that returns a value")
    syntax(
        "async { ... }" to "returns Deferred<T>: a Job that carries a result",
        "deferred.await()" to "suspends until the value is ready (and rethrows its exception)",
        "async(start = CoroutineStart.LAZY)" to "does not start until await() or start() is called",
    )
    val total = runBlocking {
        val a = async { fetchTen() }
        val b = async { fetchTwentyTwo() }
        a.await() + b.await()
    }
    show { total }                                         // 10 + 22, both fetches ran at the same time
    val lazyLog = mutableListOf<String>()
    runBlocking {
        val lazyValue = async(start = CoroutineStart.LAZY) { lazyLog.add("computing"); 5 }
        lazyLog.add("created")
        delay(50)
        lazyLog.add("awaiting")
        lazyLog.add("got ${lazyValue.await()}")
    }
    show { lazyLog }                                       // "computing" appears only after await() asked for it

    section("Prove it with the clock: sequential vs concurrent")
    var sequentialSum = 0
    val sequentialMs = elapsedMs { runBlocking { sequentialSum = fetchTen() + fetchTwentyTwo() } }
    var concurrentSum = 0
    val concurrentMs = elapsedMs {
        runBlocking {
            val a = async { fetchTen() }
            val b = async { fetchTwentyTwo() }
            concurrentSum = a.await() + b.await()
        }
    }
    show { sequentialSum == concurrentSum }                // the same answer, 32
    show { sequentialMs > 550 }                            // two 300 ms waits one after the other: about 600
    show { concurrentMs < 550 }                            // the two waits overlap: about 300

    section("delay vs Thread.sleep")
    val delayMs = elapsedMs { runBlocking { repeat(3) { launch { delay(300) } } } }
    val sleepMs = elapsedMs { runBlocking { repeat(2) { launch { Thread.sleep(300) } } } }
    show { delayMs < 550 }                                 // three delay(300) overlap on ONE thread: about 300
    show { sleepMs > 550 }                                 // TRAP: two sleep(300) block that one thread in turn: about 600
    val manyMs = elapsedMs { runBlocking { repeat(10_000) { launch { delay(100) } } } }
    show { manyMs < 3000 }                                 // 10 000 coroutines at once: about 100 ms of waiting; 10 000 threads would be very heavy
    note("Never call Thread.sleep or other blocking calls inside a coroutine on a shared dispatcher: use delay or withContext(Dispatchers.IO)")
    note("GlobalScope.launch is fire-and-forget outside any parent: it can leak and outlive its caller. Prefer a scope you own")
}
