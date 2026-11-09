package review.iterable

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import kotlin.math.abs

/**
 * 4d. ZIP, WINDOWED, CHUNKED, WITHINDEX, JOINTOSTRING: pair things up, slide over neighbours, print
 *     (lesson 3 ListSliceSortSearch has the first look at chunked, windowed and zipWithNext)
 *     Run: mvn -q compile exec:java -Dmain.class=review.iterable.ZipAndWindowKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("ZIP AND WINDOW  -  pairs, neighbours, sliding windows, join")

    val nums = listOf(1, 2, 3, 4, 5)
    val words = listOf("kiwi", "fig", "apple")

    section("zip and unzip")
    syntax(
        "a.zip(b)" to "List<Pair>; it stops at the SHORTER list",
        "a.zip(b) { x, y -> }" to "combine each pair straight away",
        "pairs.unzip()" to "Pair of two lists: the reverse of zip",
    )
    show { listOf(1, 2, 3).zip(listOf("a", "b", "c")) }
    show { listOf(1, 2, 3).zip(listOf("a", "b")) }            // TRAP: the unmatched 3 is silently dropped
    show { listOf(1, 2, 3).zip(listOf(10, 20, 30)) { a, b -> a * b } }
    show { listOf(1 to "a", 2 to "b").unzip() }
    show { words.zip(words.map { it.length }).toMap() }       // two lists to a Map

    section("Neighbours: zipWithNext")
    syntax("xs.zipWithNext() | xs.zipWithNext { a, b -> }" to "each element with the next one: n - 1 pairs")
    show { nums.zipWithNext() }
    show { listOf(1, 3, 2, 5).zipWithNext { a, b -> b - a } } // differences between neighbours
    show { nums.zipWithNext().all { (a, b) -> a <= b } }      // is the list sorted?
    show { listOf(1, 9, 4).zipWithNext { a, b -> abs(b - a) }.max() }   // the biggest jump between neighbours

    section("windowed and chunked")
    syntax(
        "xs.windowed(size, step = 1, partialWindows = false)" to "sliding windows; each one is a List",
        "xs.chunked(size)" to "blocks that do not overlap; the last may be shorter",
        "xs.windowed(size) { window -> }" to "transform each window directly",
    )
    show { nums.windowed(3) }
    show { nums.windowed(3, step = 2) }                       // jump 2 elements between windows
    show { nums.windowed(3, partialWindows = true) }          // also keep the short windows at the end
    show { nums.windowed(3) { it.sum() } }                    // sliding-window sums (lesson 2 does it in O(n) by updating a total)
    show { nums.windowed(3) { w -> w.sumOf { it * it } } }    // sumOf inside each window: sums of squares
    show { nums.windowed(2).all { (a, b) -> a < b } }         // strictly increasing?
    show { nums.windowed(7) }                                 // TRAP: a window bigger than the list gives an EMPTY list
    show { nums.chunked(2) }
    show { nums.chunked(2) { it.sum() } }                     // sum per block
    show { nums.windowed(3) { it } }                          // TRAP: the lambda gets ONE reused window, so all entries show the last position
    show { nums.windowed(3) { it.toList() } }                 // copy the window inside the lambda when you keep it
    note("Inside windowed { } use the window right away (sum, max, joinToString); keep a copy only with toList()")

    section("withIndex, indices, reversed, slice")
    show { words.withIndex().map { (i, w) -> "$i:$w" } }
    show { words.withIndex().filter { it.index % 2 == 0 }.map { it.value } }   // every second element
    show { words.indices }                                    // the range 0..lastIndex
    show { words.reversed() }
    show { nums.slice(listOf(0, 2, 4)) }                      // pick positions in any order
    show { nums.slice(1..3) }

    section("joinToString")
    syntax("xs.joinToString(separator, prefix, postfix, limit, truncated) { transform }" to "one String; every argument is optional")
    show { nums.joinToString() }                              // the default separator is ", "
    show { nums.joinToString("-") }
    show { nums.joinToString(separator = ", ", prefix = "[", postfix = "]") }
    show { nums.joinToString(limit = 3) }                     // only the first 3, then ...
    show { nums.joinToString("") { (it * 2).toString() } }    // transform each element while joining
    show { emptyList<Int>().joinToString(prefix = "<", postfix = ">") }   // prefix and postfix appear even when empty

    section("Cost")
    bigO(
        "zip | zipWithNext | withIndex | reversed | slice" to "O(n): one pass, one new list",
        "chunked(k)" to "O(n): each element is copied once",
        "windowed(k)" to "O(n * k): k elements per window, so the work grows with k",
        "joinToString" to "O(total characters)",
        "a chain of these" to "every step builds a new list (lesson 12: Sequence avoids that)",
    )
}
