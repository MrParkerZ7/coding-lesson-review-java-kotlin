package review.lists

import review.bigO
import review.section
import review.show
import review.syntax
import review.title

data class Person(val name: String, val age: Int)

/**
 * 3e. LISTS: slicing, sorting, searching
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ListSliceSortSearchKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("LISTS  -  slice, sort, search")

    section("Slicing")
    syntax(
        "xs.subList(from, to) | xs.slice(a..b)" to "a part of the list (to is exclusive in subList)",
        "xs.take(n) | takeLast(n) | drop(n) | dropLast(n)" to "first or last n | all but the first or last n",
        "xs.chunked(n) | xs.windowed(n)" to "blocks of n | sliding windows of n",
    )
    val xs = listOf(10, 20, 30, 40, 50)
    show { xs.subList(1, 3) }                                                  // end index is exclusive; a VIEW of xs
    show { xs.slice(1..3) }                                                    // a copy, the range is inclusive
    show { xs.take(2) }
    show { xs.takeLast(2) }
    show { xs.drop(2) }
    show { xs.dropLast(2) }
    show { xs.takeWhile { it < 30 } }
    show { xs.dropWhile { it < 30 } }
    show { xs.chunked(2) }                                                     // the last block may be shorter
    show { xs.windowed(3) }                                                    // sliding window: sums of k in a row, max subarray
    show { xs.zipWithNext() }                                                  // neighbouring pairs
    show { xs.zipWithNext { a, b -> b - a } }                                  // differences between neighbours
    show { xs.reversed() }

    section("Sorting")
    syntax(
        "xs.sorted() | sortedBy { } | sortedDescending()" to "return a NEW list",
        "sortedWith(compareBy({ -it.a }, { it.b }))" to "several keys: a descending, then b ascending",
        "m.sort() | m.sortBy { } | m.reverse() | m.shuffle()" to "change a MutableList IN PLACE",
    )
    val people = listOf(Person("Ann", 30), Person("Bob", 25), Person("Cid", 30))
    show { people.sortedBy { it.age }.map { it.name } }                        // stable: Ann stays before Cid
    show { people.sortedByDescending { it.age }.map { it.name } }
    show { people.sortedWith(compareBy({ -it.age }, { it.name })).map { it.name } }   // age descending, then name ascending
    show { listOf("pear", "fig", "apple").sortedBy { it.length } }
    show { listOf("b", "A", "c").sortedBy { it.lowercase() } }                 // case-insensitive
    val m = mutableListOf(3, 1, 2)
    m.sort();                              show { m }                          // in place, ascending
    m.sortDescending();                    show { m }                          // in place, descending

    section("Searching")
    syntax(
        "xs.indexOf(x) | lastIndexOf(x) | indexOfFirst { }" to "position, or -1",
        "xs.find { } | xs.count { } | x in xs" to "first match | how many | contains",
        "sorted.binarySearch(x)" to "only on a SORTED list: index, or a negative number",
    )
    val data = listOf(5, 3, 8, 3, 9)
    show { data.indexOf(3) }
    show { data.lastIndexOf(3) }
    show { data.indexOf(42) }                                                  // -1 when not found
    show { data.indexOfFirst { it > 5 } }
    show { data.find { it > 5 } }
    show { data.count { it > 4 } }
    show { data.sorted().binarySearch(8) }                                     // [3, 3, 5, 8, 9] -> index 3
    show { data.min() }
    show { data.max() }
    show { data.sum() }
    show { emptyList<Int>().maxOrNull() }                                      // null on empty; max() would throw

    section("Cost")
    bigO(
        "sorted() | sort()" to "O(n log n)",
        "indexOf | contains | count" to "O(n)",
        "binarySearch (sorted list)" to "O(log n)",
        "subList | take | drop" to "O(1) view | O(k) copy",
    )
}
