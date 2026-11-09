package review.lists

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 3a. ARRAYS: fixed size, fast, the input type of many interview problems
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ArraysBasicsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("ARRAYS  -  IntArray, Array<T>")

    section("Creating and reading")
    syntax(
        "intArrayOf(1, 2, 3)" to "IntArray from values (int[] underneath, no boxing)",
        "IntArray(size)" to "IntArray of zeros",
        "Array(size) { index -> value }" to "Array<T> built by a lambda",
        "arrayOf(\"a\", \"b\")" to "Array<T> from values (boxed objects)",
    )
    val nums = intArrayOf(3, 1, 2);        show { nums }     // IntArray = int[]
    val zeros = IntArray(4);               show { zeros }    // size 4, all zeros
    val squares = Array(4) { it * it };    show { squares }  // Array<Int> built by a lambda: index -> value
    val names = arrayOf("b", "a");         show { names }
    show { nums.contentToString() }                                            // contentToString() is how an array becomes text
    show { nums.size }                                                         // size is a property and never changes
    show { nums[0] }                                                           // index from 0
    show { nums.last() }
    nums[0] = 10;                          show { nums }     // elements can change, even in a val
    show { runCatching { nums[3] }.exceptionOrNull()?.javaClass?.simpleName }  // out of range throws

    section("Traps")
    show { nums.toString().startsWith("[I@") }                                 // println(array) prints a hash like [I@1b6d3586
    show { intArrayOf(1, 2) == intArrayOf(1, 2) }                              // false: == compares REFERENCES for arrays
    show { intArrayOf(1, 2).contentEquals(intArrayOf(1, 2)) }                  // true: compare the contents
    wontCompile("val a: IntArray = arrayOf(1, 2)", "Array<Int> is not an IntArray: use intArrayOf")

    section("Everyday operations")
    syntax(
        "arr.sorted() | arr.sortedArray()" to "sorted copy as List | as array",
        "arr.sort() | arr.fill(x)" to "change the array IN PLACE",
        "arr.copyOf() | arr.copyOfRange(from, to)" to "copy | copy a slice (end exclusive)",
    )
    val data = intArrayOf(5, 3, 8, 1)
    show { data.sorted() }                                                     // a NEW List, the array is untouched
    show { data.sortedArray() }                              // a NEW sorted array
    show { data.reversedArray() }
    show { data.copyOf() }
    show { data.copyOfRange(1, 3) }                          // end index is exclusive
    show { data.sum() }
    show { data.max() }
    show { data.toList() }
    show { data.indices }
    data.sort();                           show { data }     // sorts IN PLACE

    section("2D arrays")
    syntax("Array(rows) { IntArray(cols) }" to "a matrix of zeros: every row is its own array")
    val grid = Array(2) { IntArray(3) }
    grid[1][2] = 7;                        show { grid }                       // a 2D array prints as nested lists
    show { grid.joinToString { it.contentToString() } }                        // or one row at a time
    show { grid.size }                                                         // number of rows
    show { grid[0].size }                                                      // number of columns
    show { grid.sumOf { it.sum() } }
    note("Build each row INSIDE the lambda: one shared row object would be 3 names for the same row")

    section("Cost")
    bigO(
        "arr[i] | arr[i] = x" to "O(1)",
        "contains | indexOf" to "O(n)",
        "sort()" to "O(n log n)",
        "copyOf()" to "O(n)",
    )
}
