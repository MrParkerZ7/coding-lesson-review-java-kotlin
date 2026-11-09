package review.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import review.note
import review.section
import review.show
import review.syntax
import review.title

/**
 * 17c. CANCELLATION, TIMEOUTS and the runCatching trap
 *      Run: mvn -q compile exec:java -Dmain.class=review.coroutines.CancellationAndTimeoutsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CANCELLATION AND TIMEOUTS  -  cooperative, finally, withTimeout")

    section("job.cancel(): stop a coroutine")
    syntax(
        "job.cancel()" to "ask the coroutine to stop; it stops at its next suspension point",
        "job.cancelAndJoin()" to "cancel, then wait until it has really finished",
        "job.isActive | isCancelled | isCompleted" to "the three states you can ask",
    )
    val ticks = mutableListOf<String>()
    runBlocking {
        val job = launch { repeat(10) { i -> ticks.add("tick$i"); delay(200) } }
        delay(500)                                         // ticks happen at 0, 200 and 400 ms
        job.cancelAndJoin()
    }
    show { ticks }                                         // delay() noticed the cancellation: no tick3

    section("Cancellation is cooperative")
    syntax(
        "delay, yield, await, withContext ..." to "suspension points: they check for cancellation themselves",
        "ensureActive()  |  while (isActive)" to "what a long loop must do itself: it has no suspension point",
    )
    val blind = mutableListOf<String>()
    runBlocking {
        val started = CompletableDeferred<Unit>()
        val job = launch(Dispatchers.Default) {
            started.complete(Unit)
            repeat(3) { Thread.sleep(100); blind.add("step$it") }   // blocking code never looks at the cancellation
        }
        started.await()
        job.cancelAndJoin()                                // the cancel arrives during step0's sleep
    }
    show { blind }                                         // TRAP: cancelled, yet all three steps ran
    val polite = mutableListOf<String>()
    runBlocking {
        val started = CompletableDeferred<Unit>()
        val job = launch(Dispatchers.Default) {
            started.complete(Unit)
            repeat(3) { Thread.sleep(100); polite.add("step$it"); ensureActive() }   // throws once cancelled
        }
        started.await()
        job.cancelAndJoin()
    }
    show { polite }                                        // stops right after the step that was running
    val spinning = runBlocking {
        val job = launch(Dispatchers.Default) { var n = 0L; while (isActive) { n++ } }   // a tight loop that checks isActive
        delay(50)
        job.cancelAndJoin()                                // would wait forever without the isActive check
        job.isCancelled
    }
    show { spinning }

    section("finally runs on cancel; suspending there needs NonCancellable")
    val plain = mutableListOf<String>()
    runBlocking {
        val job = launch {
            try { delay(1000) }
            finally { plain.add("finally ran"); delay(50); plain.add("after delay") }   // delay throws at once: already cancelled
        }
        delay(100)
        job.cancelAndJoin()
    }
    show { plain }                                         // TRAP: the second line of the finally block is never reached
    val safe = mutableListOf<String>()
    runBlocking {
        val job = launch {
            try { delay(1000) }
            finally { withContext(NonCancellable) { delay(50); safe.add("cleanup done") } }
        }
        delay(100)
        job.cancelAndJoin()
    }
    show { safe }                                          // NonCancellable lets the cleanup suspend

    section("withTimeout and withTimeoutOrNull")
    syntax(
        "withTimeout(ms) { ... }" to "throws TimeoutCancellationException when the block takes longer",
        "withTimeoutOrNull(ms) { ... }" to "returns null instead of throwing",
    )
    show { runBlocking { withTimeoutOrNull(100) { delay(500); "slow" } } }   // too slow: null
    show { runBlocking { withTimeoutOrNull(500) { delay(100); "fast" } } }   // in time: the block's value
    val timeout = runCatching { runBlocking { withTimeout(100) { delay(500) } } }.exceptionOrNull()
    show { timeout?.javaClass?.simpleName }
    show { timeout is CancellationException }              // TimeoutCancellationException is a kind of CancellationException

    section("TRAP: runCatching swallows the cancellation")
    val swallowed = mutableListOf<String>()
    runBlocking {
        val job = launch {
            val result = runCatching { delay(1000) }       // runCatching catches Throwable, CancellationException included
            swallowed.add("caught cancel: ${result.exceptionOrNull() is CancellationException}")
            swallowed.add("isActive: $isActive")
        }
        delay(100)
        job.cancelAndJoin()
    }
    show { swallowed }                                     // the coroutine went on running after it was cancelled
    note("Inside a coroutine, rethrow it: catch (e: CancellationException) { throw e }, or check the Result and throw it again")
    note("Cancellation propagates DOWN (parent cancels children); a child's failure propagates UP (see StructuredConcurrency)")
}
