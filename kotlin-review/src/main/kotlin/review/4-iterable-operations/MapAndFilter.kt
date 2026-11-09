package review.iterable

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** Runs the block and returns the simple name of the exception it threw, or null when it worked. */
private fun failure(block: () -> Any?): String? = runCatching(block).exceptionOrNull()?.let { it::class.simpleName }

/**
 * 4a. MAP, FILTER, FLATMAP, ANY / ALL, FIND, TAKE: the everyday Iterable operations
 *     Run: mvn -q compile exec:java -Dmain.class=review.iterable.MapAndFilterKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("MAP AND FILTER  -  transform, select, test, find, slice")

    val nums = listOf(1, 2, 3, 4, 5, 6)
    val words = listOf("kiwi", "fig", "apple", "fig")

    section("Transform: map, mapIndexed, mapNotNull")
    syntax(
        "xs.map { f(it) }" to "a NEW list of the same size, every element transformed",
        "xs.mapIndexed { i, x -> }" to "the lambda also gets the index",
        "xs.mapNotNull { f(it) }" to "map, then drop the nulls: parse and filter in one step",
    )
    show { nums.map { it * it } }
    show { words.map { it.length } }
    show { nums.mapIndexed { i, x -> i * x } }                // index times value
    show { listOf("1", "x", "3").mapNotNull { it.toIntOrNull() } }   // "x" parses to null and disappears

    section("Select: filter, filterNot, filterIsInstance")
    show { nums.filter { it % 2 == 0 } }
    show { nums.filterNot { it % 2 == 0 } }
    show { listOf(1, "a", 2.5, "b").filterIsInstance<String>() }   // keeps one type; the result is List<String>
    show { listOf(1, null, 3).filterNotNull() }               // drops the nulls (lesson 7)

    section("Flatten: flatMap, flatten")
    syntax("xs.flatMap { listOf... }" to "map each element to a list, then join all the lists: map + flatten")
    show { listOf(listOf(1, 2), listOf(3), listOf<Int>()).flatten() }
    show { listOf("ab", "cd").flatMap { it.toList() } }       // a String is a sequence of Char
    show { listOf(1, 2, 3).flatMap { a -> listOf(10, 20).map { b -> a + b } } }   // two nested loops in one line

    section("Side effects: forEach, onEach")
    val log = mutableListOf<Int>()
    val total = nums.onEach { log.add(it * 10) }.sum()        // onEach returns the SAME list, so the chain goes on
    show { log }
    show { total }
    show { buildList { nums.forEach { if (it > 4) add(it) } } }   // forEach returns Unit: collect results yourself
    note("A bare return inside forEach leaves the WHOLE function; break and continue are in lesson 2")

    section("Test: any, all, none, count")
    show { nums.any { it > 5 } }
    show { nums.all { it > 0 } }
    show { nums.none { it > 6 } }
    show { nums.count { it % 2 == 0 } }
    show { emptyList<Int>().all { it > 0 } }                  // TRAP: true, nothing breaks the rule (vacuous truth)
    show { emptyList<Int>().any { it > 0 } }                  // nothing can match, so false
    show { emptyList<Int>().none { it > 0 } }                 // and none is true
    show { nums.any() }                                       // without a lambda: is the list not empty?

    section("Find: find, firstOrNull, first, last")
    syntax(
        "xs.find { } | xs.firstOrNull { }" to "the first match, or null",
        "xs.first { } | xs.first() | xs.last()" to "the match, but they THROW when there is none",
    )
    show { nums.find { it > 3 } }
    show { nums.find { it > 99 } }                            // null, no exception
    show { nums.first { it > 3 } }
    show { nums.last() }
    show { nums.lastOrNull { it < 3 } }                       // search from the end
    show { emptyList<Int>().firstOrNull() }
    show { failure { emptyList<Int>().first() } }             // TRAP: first() on an empty list throws
    show { failure { nums.first { it > 99 } } }               // and so does first { } without a match

    section("Slice: take, drop, takeWhile, dropWhile, distinct")
    show { nums.take(2) }
    show { nums.drop(4) }
    show { nums.take(99) }                                    // no exception: takes what exists
    show { nums.takeWhile { it < 4 } }
    show { nums.dropWhile { it < 4 } }
    show { listOf(1, 2, 5, 1, 6).takeWhile { it < 4 } }       // stops at the FIRST failure (5): not the same as filter
    show { words.distinct() }                                 // the first occurrence stays, order is kept

    section("Cost")
    bigO(
        "map | filter | distinct | onEach" to "O(n) time, a new list of up to n items",
        "flatMap | flatten" to "O(n + items produced)",
        "any | all | none | find | first { }" to "O(n) worst case, stop at the first decision",
        "take(k) | first() | last()" to "O(k), O(1), O(1) on a List",
        "chain of map / filter / ..." to "one pass and one new list per step (Sequence avoids that: lesson 12)",
    )
}
