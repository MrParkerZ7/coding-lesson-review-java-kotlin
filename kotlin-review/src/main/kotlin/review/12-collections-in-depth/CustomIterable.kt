package review.collectionsdepth

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** A class you can loop over, and that gets every Iterable operator for free. */
class Countdown(private val from: Int) : Iterable<Int> {
    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        private var current = from
        override fun hasNext(): Boolean = current > 0
        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            return current--
        }
    }
}

/** The same idea, with the `iterator { yield(...) }` builder instead of a hand-written Iterator. */
class Squares(private val n: Int) : Iterable<Int> {
    override fun iterator(): Iterator<Int> = kotlin.sequences.iterator { for (i in 1..n) yield(i * i) }
}

/** NOT an Iterable: only `operator fun iterator()`, which is enough for for-in. */
class Letters {
    operator fun iterator(): Iterator<Char> = "abc".iterator()
}

data class Version(val major: Int, val minor: Int) : Comparable<Version> {
    override fun compareTo(other: Version): Int = compareValuesBy(this, other, Version::major, Version::minor)
}

/** Looks right, but a - b overflows for a large a and a negative b. */
fun badCompare(a: Int, b: Int): Int = a - b

/**
 * 12c. CUSTOM ITERABLE: for-in on your own class, Comparable and Comparator
 *      Run: mvn -q compile exec:java -Dmain.class=review.collectionsdepth.CustomIterableKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 *      (errorOf, used below, is declared in ReadOnlyVsMutable.kt: same package.)
 */
fun main() {
    title("CUSTOM ITERABLE  -  for-in on your own class, Comparable, Comparator")

    section("A class you can loop over")
    syntax(
        "operator fun iterator(): Iterator<T>" to "enough for for (x in obj)",
        "class C : Iterable<T>" to "for-in AND the whole stdlib: map, filter, sum, toList ...",
        "Iterator<T>: hasNext() / next()" to "hasNext asks; next hands the element out and moves on",
    )
    show { buildList { for (x in Countdown(3)) add(x) } }
    show { Countdown(5).toList() }
    show { Countdown(5).filter { it % 2 == 1 } }                               // the Iterable extension functions come for free
    show { Countdown(4).sum() }
    show { Squares(4).toList() }                                               // iterator { yield(...) } writes the Iterator for you
    show { buildList { for (c in Letters()) add(c) } }                         // an operator iterator() alone is enough for for-in
    wontCompile("Letters().map { it }", "Letters is not an Iterable: only for-in works on it")
    val iter = Countdown(2).iterator()
    show { iter.next() }
    show { iter.next() }
    show { iter.hasNext() }                                                    // false: the countdown is over
    show { errorOf { iter.next() } }                                           // next() past the end throws

    section("Iterable, Collection, Sequence")
    syntax(
        "Iterable<T>" to "iterator() only: for-in and the operators; may even be endless",
        "Collection<T> : Iterable<T>" to "+ size, isEmpty(), contains(): finite, in memory (List, Set)",
        "Sequence<T>" to "iterator() only, but its operators are LAZY. NOT an Iterable",
        "IntRange | IntProgression" to "Iterable<Int>, but not a Collection: 1..1_000_000 stores no elements",
    )
    val aList: Iterable<Int> = listOf(1, 2)
    val aRange: Iterable<Int> = 1..3
    val aSequence: Any = sequenceOf(1)
    show { aList is Collection<*> }
    show { aRange is Collection<*> }                                           // a range only remembers start, end and step
    show { aSequence is Iterable<*> }                                          // a Sequence is not an Iterable (Kotlin: separate interface)
    show { (1..9 step 4).toList() }                                            // a progression is an Iterable
    show { (3 downTo 1).map { it * it } }

    section("Comparable: the natural order of a class")
    syntax("class A : Comparable<A>" to "compareTo(other): negative, 0 or positive; gives <, >, sorted(), max()")
    show { Version(1, 2) < Version(1, 10) }                                    // true: numbers, where the text "1.2" would sort AFTER "1.10"
    show { listOf(Version(2, 0), Version(1, 10), Version(1, 2)).sorted() }
    show { listOf(Version(2, 0), Version(1, 10)).max() }
    show { badCompare(Int.MAX_VALUE, -1) }                                     // TRAP: negative, as if MAX_VALUE were smaller than -1
    note("Only the SIGN of compareTo counts. Never return a - b: use a.compareTo(b) or compareValuesBy")

    section("Comparator: an order chosen from outside")
    syntax(
        "compareBy<T> { it.a }.thenBy { it.b }" to "a Comparator from keys; the first key decides, the next breaks ties",
        "compareByDescending<T> { }  |  .reversed()" to "biggest first",
        "xs.sortedWith(comparator)" to "sort with it; sortedBy { } is the one-key shortcut",
    )
    val words = listOf("bb", "a", "ab", "c")
    val byLengthThenAlpha = compareBy<String> { it.length }.thenBy { it }
    show { words.sortedWith(byLengthThenAlpha) }
    show { words.sortedWith(byLengthThenAlpha.reversed()) }
    show { words.sortedWith(compareByDescending<String> { it.length }.thenBy { it }) }
    show { words.sortedBy { it.length } }                                      // stable: "bb" stays before "ab", they have equal keys
    show { words.sortedWith { x, y -> y.compareTo(x) } }                       // a lambda converts to a Comparator

    section("Cost")
    bigO(
        "for (x in iterable)" to "O(1) per element, O(n) per pass",
        "each Iterable operator (map, filter ...)" to "O(n) and builds a NEW list: k operators allocate k lists",
        "the same chain on a Sequence" to "O(n) in total, no intermediate lists, may stop early",
        "sorted() | sortedWith(c)" to "O(n log n) comparisons, each compareTo is O(1)",
        "Countdown(n).toList()" to "O(n): the custom iterator is walked once",
    )
}
