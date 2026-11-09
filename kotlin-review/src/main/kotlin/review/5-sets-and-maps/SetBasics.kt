package review.sets

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 5a. SETS: unique elements and fast contains
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.SetBasicsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("SETS  -  no duplicates, fast lookup")

    section("Creating")
    syntax(
        "setOf(1, 2, 3) | mutableSetOf(1, 2, 3)" to "read-only | mutable, keeps insertion order (LinkedHashSet)",
        "hashSetOf(...) | sortedSetOf(...)" to "HashSet: no order promised | TreeSet: sorted",
        "xs.toSet()" to "a List without its duplicates",
    )
    val ids = setOf(3, 1, 3, 2);           show { ids }                        // duplicates dropped, first-seen order kept
    val sorted = sortedSetOf(3, 1, 2);     show { sorted }                     // a TreeSet keeps the elements sorted
    show { listOf(3, 3, 1).toSet() }                                           // the quick way to de-duplicate
    show { setOf(1, 2) == setOf(2, 1) }                                        // equality ignores the order
    wontCompile("val s = setOf(1); s.add(2)", "a read-only Set has no add: use mutableSetOf")

    section("Changing and asking")
    syntax(
        "s.add(x) | s.remove(x)" to "returns true when the set changed",
        "x in s | s.contains(x)" to "fast membership test",
    )
    val s = mutableSetOf(1, 2)
    show { s.add(3) }                                                          // true: it was new
    show { s.add(3) }                                                          // false: already there, nothing changed
    show { s.remove(1) }
    show { 2 in s }
    show { s }

    section("Set algebra")
    syntax("a union b | a intersect b | a subtract b" to "everything | in both | only in a")
    val a = setOf(1, 2, 3)
    val b = setOf(3, 4)
    show { a union b }
    show { a intersect b }
    show { a subtract b }
    show { a + 4 }                                                             // plus: a NEW set
    show { a - 1 }
    show { a.containsAll(listOf(1, 2)) }

    section("Two classic uses")
    show { listOf(1, 2, 2).size != listOf(1, 2, 2).toSet().size }              // contains a duplicate? compare the sizes
    val seen = mutableSetOf<Int>()
    show { listOf(4, 2, 7, 2, 4).firstOrNull { !seen.add(it) } }               // first duplicate: add() is false when it was already there
    note("set.add(x) doing the check and the insert in one call is the interview idiom for duplicates")

    section("Cost")
    bigO(
        "HashSet add | remove | contains" to "O(1) on average",
        "TreeSet (sortedSetOf) add | contains" to "O(log n), stays sorted",
        "list.contains(x)" to "O(n): a set is the fix for slow lookups",
    )
}
