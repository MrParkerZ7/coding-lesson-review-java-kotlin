package review.threads

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 22e. CONCURRENT COLLECTIONS: ConcurrentHashMap, CopyOnWriteArrayList, synchronizedList, CountDownLatch
 *      Run: mvn -q compile exec:java -Dmain.class=review.threads.ConcurrentCollectionsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CONCURRENT COLLECTIONS  -  safe sharing without writing locks yourself")

    section("ConcurrentHashMap")
    syntax(
        "val map = ConcurrentHashMap<K, V>()" to "a thread-safe map; HashMap is NOT (concurrent updates can be lost)",
        "map.merge(key, 1) { old, inc -> old + inc }" to "ATOMIC read-modify-write: the safe counter idiom",
        "map.compute(k) { k, v -> ... } | computeIfAbsent | putIfAbsent" to "atomic per key; never do get-then-put yourself",
    )
    val counts = ConcurrentHashMap<String, Int>()
    runAll(4) { repeat(1000) { counts.merge("hits", 1) { old, inc -> old + inc } } }
    show { counts }                                        // {hits=4000}: 4 threads x 1000, nothing lost
    show { counts.compute("hits") { _, v -> if (v == null) 1 else v + 1 } }
    show { counts.putIfAbsent("hits", 0) }                 // returns the existing value and changes nothing
    wontCompile("counts.put(\"k\", null)", "Null cannot be a value of a non-null type Int: Kotlin stops it before the run")
    val loose = ConcurrentHashMap<String, Int?>()
    show { thrown { loose.put("k", null) } }               // declare V as Int? and the MAP refuses null at run time
    val weak = ConcurrentHashMap(mapOf("a" to 1, "b" to 2))
    show { thrown { for (k in weak.keys) weak.remove(k) } }   // null = nothing thrown, although the map changes during the loop
    show { weak.size }                                     // 0: both entries went; iteration is 'weakly consistent', it does not fail
    note("Each ConcurrentHashMap call is atomic, a SEQUENCE of calls is not: if (k !in map) map[k] = v is still a race")

    section("CopyOnWriteArrayList: iterate a snapshot")
    syntax("CopyOnWriteArrayList(list)" to "every write copies the whole array: great for read-mostly lists (listeners), bad for many writes")
    val plain = mutableListOf(1, 2, 3)
    show { thrown { for (n in plain) plain.add(n + 10) } }   // ArrayList: adding while iterating fails (one thread is enough)
    val cow = CopyOnWriteArrayList(listOf(1, 2, 3))
    for (n in cow) cow.add(n + 10)                         // the loop walks the OLD snapshot, so it ends after 3 rounds
    show { cow }

    section("Collections.synchronizedList: a lock around every call")
    syntax("Collections.synchronizedList(mutableListOf<T>())" to "every method locks the wrapper; iterating still needs synchronized(list) { ... }")
    val synced = Collections.synchronizedList(mutableListOf<Int>())
    runAll(4) { repeat(1000) { synced.add(it) } }
    show { synced.size }                                   // 4000: no add was lost (a plain ArrayList is not synchronized: adds may be lost)
    note("Prefer ConcurrentHashMap, CopyOnWriteArrayList or a queue to a synchronized wrapper: they scale better")

    section("CountDownLatch: wait for N things")
    syntax(
        "val latch = CountDownLatch(n)" to "starts at n",
        "latch.countDown()  |  latch.await()" to "each worker counts down once  |  the waiter blocks until the count is 0",
    )
    val latch = CountDownLatch(3)
    val finished = ConcurrentLinkedQueue<Int>()
    for (id in 1..3) thread { finished.add(id); latch.countDown() }
    latch.await()
    show { finished.size }                                 // 3: await returned only after all three counted down
    show { latch.count }
    note("A latch is single-use: the count never goes back up. Use CyclicBarrier to wait again and again")
    note("Queues, producer / consumer and deadlock: QueuesAndDeadlock")
}
