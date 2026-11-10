package review.threads

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

private const val COROUTINES = 100
private const val PER_COROUTINE = 1000

/** Runs `body` PER_COROUTINE times in each of COROUTINES coroutines spread over the Default dispatcher's threads. */
private fun spread(body: suspend () -> Unit) = runBlocking {
    withContext(Dispatchers.Default) {
        repeat(COROUTINES) { launch { repeat(PER_COROUTINE) { body() } } }
    }
}

private suspend fun tryTwice(m: Mutex): Boolean = m.withLock { m.tryLock() }

private suspend fun lockTwice(m: Mutex): Int? = m.withLock { withTimeoutOrNull(100) { m.withLock { 1 } } }

/**
 * 22g. THE SAME RACE WITH COROUTINES: Mutex, Semaphore, atomics, one thread
 *      Run: mvn -q compile exec:java -Dmain.class=review.threads.CoroutineSharedStateKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    title("COROUTINES AND SHARED STATE  -  100 coroutines x 1000 increments")

    val expected = COROUTINES * PER_COROUTINE

    section("Coroutines race too")
    syntax(
        "launch { counter++ }" to "on Dispatchers.Default the coroutines run on SEVERAL threads: the same lost update as with threads",
        "Mutex  |  Semaphore" to "locks that SUSPEND the coroutine instead of blocking the thread",
    )
    var racy = 0
    spread { racy++ }
    show { racy <= expected }                              // always true: updates can only be LOST. The total is not printed
    show { expected }                                      // what a correct counter must reach

    section("Fix 1: Mutex.withLock")
    syntax(
        "val mutex = Mutex()" to "a lock for coroutines; NOT reentrant",
        "mutex.withLock { ... }" to "suspend until free, run the block, unlock in finally; the block may suspend",
    )
    val mutex = Mutex()
    var guarded = 0
    spread { mutex.withLock { guarded++ } }
    show { guarded }                                       // exactly 100000
    show { mutex.isLocked }                                // false: released
    show { runBlocking { tryTwice(mutex) } }               // false: the SAME coroutine cannot take it again (a ReentrantLock would let it)
    show { runBlocking { lockTwice(mutex) } }              // null: it waited for itself and gave up; without the timeout it would hang forever
    wontCompile("synchronized(lock) { delay(10) }", "The 'delay' suspension point is inside a critical section: a thread lock cannot span a suspension")
    note("synchronized and ReentrantLock block the THREAD; Mutex suspends the COROUTINE and frees the thread. Never suspend while holding a thread lock")

    section("Fix 2: atomics, StateFlow.update, one thread")
    syntax(
        "AtomicInteger.incrementAndGet()" to "lock-free, works the same in coroutines",
        "state.update { it + 1 }" to "MutableStateFlow: a compare-and-set retry loop, atomic",
        "Dispatchers.Default.limitedParallelism(1)" to "a view that runs on at most one thread at a time: no race, no lock",
    )
    val atomic = AtomicInteger()
    spread { atomic.incrementAndGet() }
    show { atomic.get() }                                  // exactly 100000
    val state = MutableStateFlow(0)
    spread { state.update { it + 1 } }
    show { state.value }                                   // exactly 100000
    var confined = 0
    val single = Dispatchers.Default.limitedParallelism(1)
    runBlocking { withContext(single) { repeat(COROUTINES) { launch { repeat(PER_COROUTINE) { confined++ } } } } }
    show { confined }                                      // exactly 100000 with a plain Int: only one thread ever touches it (confinement)
    note("state.value = state.value + 1 is a race again: read-modify-write needs update { }")

    section("Semaphore: at most n at once")
    syntax(
        "val permits = Semaphore(n)" to "n permits",
        "permits.withPermit { ... }" to "suspend until a permit is free; release it afterwards",
    )
    val permits = Semaphore(3)
    val inside = AtomicInteger()
    val mostInside = AtomicInteger()
    runBlocking {
        repeat(20) {
            launch(Dispatchers.Default) {
                permits.withPermit {
                    mostInside.accumulateAndGet(inside.incrementAndGet()) { a, b -> maxOf(a, b) }
                    delay(10)
                    inside.decrementAndGet()
                }
            }
        }
    }
    show { mostInside.get() <= 3 }                         // 20 coroutines, never more than 3 inside at once
    show { mostInside.get() >= 1 }
    show { permits.availablePermits }                      // 3: every permit came back
    note("Pick the tool: independent counter = atomic; several fields change together = Mutex; many callers, few resources = Semaphore")
    note("Threads, executors and locks (a Java-style API) are in 22a to 22f; coroutines, Flow and cancellation are in lesson 17")
}
