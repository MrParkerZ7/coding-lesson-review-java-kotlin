package review.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import review.note
import review.section
import review.show
import review.syntax
import review.title

private fun elapsedMs(block: () -> Unit): Long {
    val start = System.nanoTime()
    block()
    return (System.nanoTime() - start) / 1_000_000
}

/** Collect a whole flow into a list from plain code. */
private fun <T> Flow<T>.items(): List<T> = runBlocking { toList() }

private fun errorOf(block: () -> Unit): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName

/**
 * 17d. FLOW BASICS: a cold stream of values over time
 *      Run: mvn -q compile exec:java -Dmain.class=review.coroutines.FlowBasicsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("FLOW BASICS  -  flow, collect, operators, cold, flowOn, buffer")

    section("Building and collecting a Flow")
    syntax(
        "flow { emit(x) }" to "builder: the block may suspend between emissions",
        "flowOf(a, b)  |  list.asFlow()" to "ready-made flows",
        "flow.collect { v -> }  |  toList()  |  first()" to "terminal operators: they START the flow",
        "List<T> | Sequence<T> | Flow<T>" to "all values now | lazy and blocking | lazy and may suspend",
    )
    val numbers = flow { for (i in 1..3) emit(i) }
    show { numbers.items() }                               // items() = runBlocking { toList() }: toList() is a terminal operator
    val collected = mutableListOf<Int>()
    runBlocking { numbers.collect { collected.add(it * 10) } }
    show { collected }                                     // collect { } receives each value as it is emitted
    show { flowOf("a", "b").items() }
    show { listOf(1, 2, 3).asFlow().items() }

    section("Operators: map, filter, take, onEach, reduce ...")
    val source = flowOf(1, 2, 3, 4, 5, 6)
    show { source.map { it * it }.filter { it % 2 == 0 }.items() }   // squares, keep the even ones
    show { source.take(2).items() }
    show { runBlocking { source.first() } }
    show { runBlocking { source.reduce { a, b -> a + b } } }   // 1+2+3+4+5+6
    show { runBlocking { source.fold(100) { a, b -> a + b } } }   // like reduce, with a starting value
    show { runBlocking { source.count { it > 3 } } }
    val seen = mutableListOf<Int>()
    val withEach = flowOf(1, 2, 3).onEach { seen.add(it) }
    show { seen }                                          // TRAP: nothing ran yet: a flow without a terminal operator does nothing
    runBlocking { withEach.collect() }
    show { seen }                                          // now collect() started it
    val trace = mutableListOf<String>()
    val traced = flow { for (i in 1..5) { trace.add("emit $i"); emit(i) } }
    runBlocking { traced.take(2).toList() }
    show { trace }                                         // take(2) cancelled the producer: it never got to 3

    section("Cold: every collector runs the block again")
    var runs = 0
    val cold = flow { runs++; emit(1); emit(2) }
    show { runs }                                          // building the flow ran nothing
    runBlocking { cold.toList(); cold.toList() }
    show { runs }                                          // two collectors = two runs of the block (StateFlow/SharedFlow are the hot ones)

    section("catch, flowOn, buffer, zip")
    syntax(
        "flow.catch { e -> emit(x) }" to "handles an exception thrown UPSTREAM of it",
        "flow.flowOn(Dispatchers.Default)" to "changes the context of everything ABOVE it",
        "flow.buffer()" to "producer and collector run as two coroutines",
        "a.zip(b) { x, y -> }" to "pair the n-th values of two flows; stops at the shorter one",
    )
    show { flow { emit(1); error("bad") }.catch { emit(-1) }.items() }   // the failure became a value
    show { flowOf(1).map { check(it < 0); it }.catch { emit(0) }.items() }   // map fails, and map is upstream of catch
    show { errorOf { flowOf(1).catch { emit(0) }.onEach { error("in collector") }.items() } }   // TRAP: catch only guards what is ABOVE it
    val callerThread = Thread.currentThread()
    show { runBlocking { flow { emit(Thread.currentThread() === callerThread) }.first() } }   // runs in the collector's thread
    show { runBlocking { flow { emit(Thread.currentThread() === callerThread) }.flowOn(Dispatchers.Default).first() } }   // moved to a pool thread
    show { errorOf { flow { withContext(Dispatchers.Default) { emit(1) } }.items() } }   // TRAP: emit from another context: use flowOn
    val producer = flow { for (i in 1..3) { delay(150); emit(i) } }
    val plainMs = elapsedMs { runBlocking { producer.collect { delay(150) } } }
    val bufferedMs = elapsedMs { runBlocking { producer.buffer().collect { delay(150) } } }
    show { plainMs > 800 }                                 // 3 x (150 produce + 150 consume) one after the other: about 900
    show { bufferedMs < 750 }                              // buffer() lets them overlap: 150 + 3 x 150 = about 600
    show { flowOf(1, 2, 3).zip(flowOf("a", "b")) { n, s -> "$n$s" }.items() }   // the third number has no partner
    note("A Flow is cold and sequential: values arrive one at a time, in order, in the collector's coroutine")
    note("Also worth knowing: flatMapConcat/flatMapMerge, combine, debounce, distinctUntilChanged, stateIn/shareIn, callbackFlow")
}
