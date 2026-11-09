package review.extensions

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** Swap two elements in place. O(1). */
fun IntArray.swap(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

/** Palindrome check with two pointers, no copy of the string. O(n) time, O(1) space. */
fun String.isPalindrome(): Boolean {
    var left = 0
    var right = lastIndex
    while (left < right) {
        if (this[left] != this[right]) return false
        left++
        right--
    }
    return true
}

val Int.isEven: Boolean get() = this % 2 == 0

/** Looks right, is wrong for negatives: -3 % 2 is -1, not 1. */
val Int.isOddBuggy: Boolean get() = this % 2 == 1

val Int.isOdd: Boolean get() = this % 2 != 0

/** Product of all values; the product of nothing is 1. Overflows silently as an Int. O(n). */
fun Iterable<Int>.product(): Int = fold(1) { acc, x -> acc * x }

/** The same with a Long accumulator: no overflow for these sizes. */
fun Iterable<Int>.productLong(): Long = fold(1L) { acc, x -> acc * x }

/** Second largest DISTINCT value, or null. Sorting makes it O(n log n). */
fun List<Int>.secondLargest(): Int? = distinct().sortedDescending().getOrNull(1)

/**
 * 14c. EXTENSIONS IN PRACTICE: small helpers for problems
 *      Run: mvn -q compile exec:java -Dmain.class=review.extensions.ExtensionsInPracticeKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("EXTENSIONS IN PRACTICE  -  helpers that make solutions read well")

    section("IntArray.swap(i, j)")
    syntax("fun IntArray.swap(i: Int, j: Int)" to "an in-place helper: sorting and permutation problems use it all the time")
    val arr = intArrayOf(1, 2, 3, 4)
    arr.swap(0, 3);  show { arr }                                   // first and last exchanged
    arr.swap(1, 1);  show { arr }                                   // same index: nothing changes

    section("String.isPalindrome()")
    show { "racecar".isPalindrome() }
    show { "abca".isPalindrome() }
    show { "".isPalindrome() }                                      // empty: true (the loop never runs)
    show { "a".isPalindrome() }                                     // one char: true
    show { "Aba".isPalindrome() }                                   // case matters here: 'A' != 'a'

    section("Int.isEven and the negative-number trap")
    show { 4.isEven }
    show { (-3).isOdd }                                             // != 0 works for negatives
    show { (-3).isOddBuggy }                                        // trap: -3 % 2 is -1, so "== 1" says false
    show { -3 % 2 }                                                 // the sign follows the left operand

    section("Iterable<Int>.product() and List<Int>.secondLargest()")
    show { listOf(2, 3, 4).product() }
    show { emptyList<Int>().product() }                             // the empty product is 1, the fold start value
    show { listOf(100_000, 100_000).product() }                     // trap: overflow, no exception
    show { listOf(100_000, 100_000).productLong() }                 // a Long accumulator fixes it
    show { setOf(1, 2, 3).product() }                               // any Iterable<Int> works, here a Set
    show { (1..5).product() }                                       // a range is an Iterable<Int>
    show { listOf(5, 9, 9, 3).secondLargest() }                     // duplicates of the max do not count twice
    show { listOf(7).secondLargest() }                              // only one distinct value: null
    show { emptyList<Int>().secondLargest() }

    section("How much of the standard library is extensions?")
    show { Iterable::class.java.declaredMethods.map { it.name }.sorted() }   // Iterable itself has almost no methods (forEach, spliterator are Java)
    note("map, filter, fold, joinToString, sorted, first ... are extensions on Iterable / Collection / List, not members")
    note("let, run, apply, also, takeIf are extensions on T (any type); trim, split, lowercase are extensions on String")
    note("So you can read the source of any of them and write your own the same way")

    section("Cost")
    bigO(
        "IntArray.swap(i, j)" to "O(1)",
        "String.isPalindrome()" to "O(n) time, O(1) space",
        "Iterable<Int>.product()" to "O(n) time, O(1) space",
        "List<Int>.secondLargest()" to "O(n log n): distinct + sort (one pass can do O(n))",
        "calling an extension" to "same as a static function: no object, no extra cost",
    )
}
