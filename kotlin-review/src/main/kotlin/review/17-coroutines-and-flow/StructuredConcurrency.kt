package review.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import review.note
import review.section
import review.show
import review.syntax
import review.title

private fun <T> onDispatcher(d: CoroutineDispatcher, block: () -> T): T = runBlocking { withContext(d) { block() } }
private fun inPool(): Boolean = Thread.currentThread().name.startsWith("DefaultDispatcher-worker")

/** The idiom for parallel decomposition: both loads run at once, the function returns when both are done. */
suspend fun loadBoth(): Pair<Int, Int> = coroutineScope {
    val a = async { delay(100); 1 }
    val b = async { delay(100); 2 }
    a.await() to b.await()
}

/**
 * 17b. STRUCTURED CONCURRENCY: scopes, dispatchers, failure
 *      Run: mvn -q compile exec:java -Dmain.class=review.coroutines.StructuredConcurrencyKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STRUCTURED CONCURRENCY  -  scopes, dispatchers, failure")

    section("coroutineScope: the parent waits for every child")
    syntax(
        "coroutineScope { ... }" to "suspends until all its children finish; returns the block's value",
        "a child fails" to "the scope is cancelled: siblings are cancelled, the exception goes up",
        "a coroutine never outlives its scope" to "that is what 'structured' means: no leaked work",
    )
    val order = mutableListOf<String>()
    runBlocking {
        coroutineScope {
            launch { delay(200); order.add("child 1") }
            launch { delay(100); order.add("child 2") }
            order.add("scope body")
        }
        order.add("after scope")                           // reached only when BOTH children are finished
    }
    show { order }
    show { runBlocking { loadBoth() } }                    // two 100 ms loads, run in parallel

    section("Dispatchers: which thread runs the coroutine")
    syntax(
        "Dispatchers.Default" to "CPU work: a shared pool, one thread per core",
        "Dispatchers.IO" to "blocking IO (files, JDBC): shares the Default pool, may add up to 64 threads",
        "Dispatchers.Unconfined" to "runs in the caller's thread until the first suspension (rare)",
        "Dispatchers.Main" to "the UI thread: needs kotlinx-coroutines-android/swing/javafx",
        "withContext(dispatcher) { ... }" to "switch dispatcher for a block, then come back with its result",
    )
    val callerThread = Thread.currentThread()
    show { runBlocking { Thread.currentThread() === callerThread } }   // runBlocking runs on the thread that called it
    show { onDispatcher(Dispatchers.Default) { Thread.currentThread() !== callerThread } }   // moved to a pool thread
    show { onDispatcher(Dispatchers.Default) { inPool() } }   // inPool(): the thread is named DefaultDispatcher-worker-N
    show { onDispatcher(Dispatchers.IO) { inPool() } }     // IO and Default share one pool of threads
    show { onDispatcher(Dispatchers.Unconfined) { Thread.currentThread() === callerThread } }   // stayed in the caller's thread
    show { runCatching { onDispatcher(Dispatchers.Main) { 1 } }.exceptionOrNull()?.javaClass?.simpleName }   // no Main on a plain JVM

    section("Failure: coroutineScope vs supervisorScope")
    syntax(
        "coroutineScope { }" to "one child fails: ALL siblings are cancelled and the scope throws",
        "supervisorScope { }" to "children fail independently: siblings keep running",
        "CoroutineExceptionHandler { _, e -> }" to "last stop for an exception nobody caught in a launch",
    )
    val scopeLog = mutableListOf<String>()
    val scopeResult = runCatching {
        runBlocking {
            coroutineScope {
                launch {
                    try { delay(500); scopeLog.add("sibling finished") }
                    catch (e: CancellationException) { scopeLog.add("sibling cancelled"); throw e }
                }
                launch { delay(100); error("boom") }
            }
        }
    }
    show { scopeLog }                                      // the failure at 100 ms cancelled the sibling before its 500 ms
    show { scopeResult.exceptionOrNull()?.message }        // and the exception came out of coroutineScope
    val superLog = mutableListOf<String>()
    val handler = CoroutineExceptionHandler { _, e -> superLog.add("handler got ${e.message}") }
    runBlocking {
        supervisorScope {
            launch { delay(300); superLog.add("sibling finished") }
            launch(handler) { delay(100); error("boom") }
        }
    }
    show { superLog }                                      // the sibling survived; the handler saw the failure

    section("launch vs async: where the exception shows up")
    var caughtAroundLaunch = false
    val launchFailure = runCatching {
        runBlocking {
            try { launch { error("from launch") } } catch (e: IllegalStateException) { caughtAroundLaunch = true }
            delay(50)
        }
    }
    show { caughtAroundLaunch }                            // TRAP: try/catch around launch catches nothing: launch returns at once
    show { launchFailure.exceptionOrNull()?.message }      // the failure went to the parent instead
    val awaitMessage = runBlocking {
        supervisorScope {
            val failing = async { error("async boom") }
            runCatching { failing.await() }.exceptionOrNull()?.message   // async keeps its exception until await()
        }
    }
    show { awaitMessage }
    val trapped = runCatching {
        runBlocking {
            coroutineScope {
                val failing = async { error("still fatal") }
                runCatching { failing.await() }            // caught here ...
                "survived"
            }
        }
    }
    show { trapped.isSuccess }                             // TRAP: ... but inside a plain coroutineScope the failed child already cancelled the scope
    show { trapped.exceptionOrNull()?.message }
    note("Catching at await() is not enough inside coroutineScope: wrap the whole scope in try/catch, or use supervisorScope")
    note("Rule of thumb: launch = report failures to the parent; async = hand the failure to whoever calls await()")
}
