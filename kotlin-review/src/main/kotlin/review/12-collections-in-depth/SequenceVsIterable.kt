package review.collectionsdepth

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** The chain on a List (eager): records the order in which the two lambdas run. */
fun eagerOrder(): List<String> {
    val steps = mutableListOf<String>()
    listOf(1, 2, 3, 4)
        .map { steps += "map$it"; it * 2 }
        .filter { steps += "filter$it"; it > 2 }
        .first()
    return steps
}

/** The same chain on a Sequence (lazy). */
fun lazyOrder(): List<String> {
    val steps = mutableListOf<String>()
    listOf(1, 2, 3, 4).asSequence()
        .map { steps += "map$it"; it * 2 }
        .filter { steps += "filter$it"; it > 2 }
        .first()
    return steps
}

/**
 * 12b. SEQUENCE vs ITERABLE: eager vs lazy
 *      Run: mvn -q compile exec:java -Dmain.class=review.collectionsdepth.SequenceVsIterableKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 *      (errorOf, used below, is declared in ReadOnlyVsMutable.kt: same package.)
 */
fun main() {
    title("SEQUENCE vs ITERABLE  -  eager operator by operator, lazy element by element")

    section("Eager (List) vs lazy (Sequence)")
    syntax(
        "list.map { }.filter { }" to "EAGER: each operator finishes a whole new list before the next starts",
        "list.asSequence().map { }.filter { }" to "LAZY: each element runs through the whole chain, one at a time",
        "seq.toList() | first() | count() | sum()" to "terminal operation: the only thing that starts the work",
    )
    show { eagerOrder().joinToString(" ") }                                    // all 4 maps, then all 4 filters: 8 calls
    show { lazyOrder().joinToString(" ") }                                     // element by element, stops at the first match: 4 calls
    val log = mutableListOf<String>()
    val pipeline = listOf(1, 2, 3).asSequence().map { log += "map$it"; it * 2 }
    show { log }                                                               // [] : building the chain ran nothing
    show { pipeline.toList() }                                                 // toList() is terminal: now it runs
    show { log }

    section("Creating sequences")
    syntax(
        "xs.asSequence()  |  sequenceOf(1, 2, 3)" to "from a collection, or from values",
        "generateSequence(seed) { next }" to "each element from the previous one; ends when the lambda returns null",
        "sequence { yield(x) }" to "a builder that hands out one value at a time, lazily",
    )
    show { generateSequence(1) { it * 2 }.take(6).toList() }
    show { generateSequence(1) { if (it < 5) it + 1 else null }.toList() }     // null ends it
    show { generateSequence(1) { it * 2 }.first { it > 100 } }                 // an endless sequence is fine when something stops it
    val fib = sequence {
        var a = 0
        var b = 1
        while (true) { yield(a); val next = a + b; a = b; b = next }
    }
    show { fib.take(8).toList() }                                              // a while (true) that never runs away
    val big = (1..Int.MAX_VALUE).asSequence()
    show { big.filter { it % 3 == 0 }.take(3).toList() }                       // instant: only the first 9 numbers are looked at
    note("(1..Int.MAX_VALUE).filter { } without asSequence() would walk 2 billion numbers and build a list of about 700 million Ints first")

    section("Short-circuit: stop as soon as the answer is known")
    var seqVisited = 0
    val firstBig = (1..1_000_000).asSequence().onEach { seqVisited++ }.first { it > 3 }
    show { firstBig }
    show { seqVisited }                                                        // 4 of 1,000,000 elements were touched
    var listVisited = 0
    val firstBigList = (1..1_000_000).toList().onEach { listVisited++ }.first { it > 3 }
    show { firstBigList }
    show { listVisited }                                                       // onEach on a List ran over ALL of them first

    section("sorted() is a barrier")
    val seen = mutableListOf<Int>()
    val smallest = sequenceOf(3, 1, 2).onEach { seen += it }.sorted().first()
    show { smallest }
    show { seen }                                                              // sorted() must read EVERY element before it hands out the first
    note("map, filter, take, takeWhile, distinct stay lazy; sorted and sortedBy buffer everything")

    section("Iterating twice, and one-shot sequences")
    syntax(
        "xs.asSequence()" to "may be iterated again and again: each pass re-runs the chain",
        "seq.constrainOnce()  |  iterator.asSequence()" to "one-shot: a second pass throws IllegalStateException",
    )
    val reusable = listOf(1, 2, 3).asSequence().map { it * 2 }
    show { reusable.toList() }
    show { reusable.toList() }                                                 // the same result again
    val once = listOf(1, 2, 3).asSequence().constrainOnce()
    show { once.toList() }
    show { errorOf { once.toList() } }                                         // consumed only once
    val fromIterator = listOf(1, 2, 3).iterator().asSequence()
    show { fromIterator.toList() }
    show { errorOf { fromIterator.toList() } }                                 // an Iterator can be walked once, so its Sequence too

    section("When a Sequence wins, and when it does not")
    note("WINS: big or endless data, an early exit (first, take, any), long chains: no intermediate list is built")
    note("LOSES: small lists, a single operator (asSequence only adds wrapper objects), anything that needs size or an index")
    note("Rule of thumb: 3 or more operators on a big list, or a first / take at the end: asSequence()")

    section("Cost")
    bigO(
        "list.map { }.filter { }.first()" to "O(n) per operator, and 2 intermediate lists of up to n elements",
        "list.asSequence().map { }.filter { }.first()" to "O(k): only the k elements up to the first match, no lists",
        "seq.sorted()" to "O(n log n): buffers all n elements first",
        "list.asSequence()" to "O(1): a wrapper, nothing is copied",
    )
}
