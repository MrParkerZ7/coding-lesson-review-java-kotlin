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
 * 4b. SUM, FOLD, REDUCE, RUNNING TOTALS, MAX / MIN, AVERAGE
 *     Run: mvn -q compile exec:java -Dmain.class=review.iterable.FoldAndReduceKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("FOLD AND REDUCE  -  collapse many elements into one value")

    val nums = listOf(1, 2, 3, 4)
    val words = listOf("kiwi", "fig", "apple", "fig")

    section("sum, sumOf, count")
    syntax(
        "xs.sum()" to "total of numbers (0 for an empty list)",
        "xs.sumOf { selector }" to "total of a value computed from each element",
    )
    show { nums.sum() }
    show { emptyList<Int>().sum() }                           // 0, no exception
    show { words.sumOf { it.length } }                        // total letters
    show { nums.count { it > 1 } }
    show { listOf(Int.MAX_VALUE, 1).sum() }                   // TRAP: Int overflow wraps around to a negative number
    show { listOf(Int.MAX_VALUE, 1).sumOf { it.toLong() } }   // sum as Long when the total can be big

    section("fold: a start value, and the result can be any type")
    syntax(
        "xs.fold(initial) { acc, x -> newAcc }" to "start from initial; the result has the type of initial",
        "xs.reduce { acc, x -> newAcc }" to "start from the first element; the result type is the element type",
    )
    show { nums.fold(0) { acc, x -> acc + x } }
    show { nums.fold(1) { acc, x -> acc * x } }               // product: the start value is 1, not 0
    show { nums.fold("") { acc, x -> acc + x } }              // Int elements, String result: fold can change the type
    val counts = words.fold(mutableMapOf<String, Int>()) { acc, w -> acc[w] = (acc[w] ?: 0) + 1; acc }
    show { counts }                                           // fold building a Map: word frequencies
    show { listOf(1, 2, 3).fold("") { acc, x -> "($acc$x)" } }   // the parentheses show the order: the oldest result is the innermost

    section("reduce: no start value, so it fails on an empty list")
    show { nums.reduce { acc, x -> acc + x } }
    show { nums.reduce { acc, x -> maxOf(acc, x) } }          // a hand-made max
    show { emptyList<Int>().fold(0) { acc, x -> acc + x } }   // fold is safe: it just returns the start value
    show { failure { emptyList<Int>().reduce { acc, x -> acc + x } } }   // TRAP: reduce on an empty list throws
    show { emptyList<Int>().reduceOrNull { acc, x -> acc + x } }   // the safe version returns null
    note("fold vs reduce: fold has a start value (safe on empty, any result type); reduce starts at the first element")

    section("Running totals: runningFold, runningReduce")
    syntax(
        "xs.runningFold(initial) { acc, x -> }" to "every intermediate result, the initial value first (n + 1 items)",
        "xs.runningReduce { acc, x -> }" to "every intermediate result, no initial value (n items)",
    )
    show { nums.runningFold(0) { acc, x -> acc + x } }        // prefix sums with a leading 0 (lesson 2)
    show { nums.runningReduce { acc, x -> acc + x } }
    show { listOf(3, 1, 4, 1, 5).runningReduce { acc, x -> maxOf(acc, x) } }   // the best value seen so far

    section("max, min and their relatives")
    syntax(
        "xs.max() | xs.min()" to "THROWS NoSuchElementException on an empty collection",
        "xs.maxOrNull() | xs.minOrNull()" to "null on an empty collection",
        "xs.maxBy { } | xs.maxOf { }" to "the ELEMENT with the biggest key | the biggest key itself",
    )
    show { nums.max() }
    show { nums.min() }
    show { failure { emptyList<Int>().max() } }               // TRAP: max() throws on empty
    show { emptyList<Int>().maxOrNull() }
    show { words.maxByOrNull { it.length } }                  // the ELEMENT that has the longest length
    show { words.maxOf { it.length } }                        // the longest length itself
    show { words.minOf { it.length } }                        // and the shortest length
    show { listOf("bb", "aa").maxByOrNull { it.length } }     // a tie: the FIRST element with the biggest key wins
    show { listOf(5, 9, 2).maxOf { it * 2 } }

    section("average")
    show { nums.average() }                                   // always a Double
    show { listOf(1, 2).average() }                           // 1.5, not 1
    show { emptyList<Int>().average() }                       // TRAP: NaN, no exception and no null

    section("Cost")
    bigO(
        "sum | sumOf | fold | reduce | max | min | average" to "O(n) time, O(1) extra space: one pass",
        "runningFold | runningReduce" to "O(n) time, O(n) space for the list of results",
        "fold into a Map" to "O(n) time, O(distinct keys) space",
    )
}
