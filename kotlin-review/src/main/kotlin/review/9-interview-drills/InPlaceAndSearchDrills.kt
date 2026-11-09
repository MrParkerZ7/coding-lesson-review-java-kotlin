package review.drills

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/**
 * MERGE TWO SORTED ARRAYS. Problem: one sorted array out of two sorted arrays.
 * Idea: two pointers, always take the smaller front element; then copy what is left of the longer one.
 * Time O(n + m), space O(n + m) for the result.
 */
fun mergeSorted(a: IntArray, b: IntArray): IntArray {
    val result = IntArray(a.size + b.size)
    var i = 0
    var j = 0
    var k = 0
    while (i < a.size && j < b.size) {
        result[k++] = if (a[i] <= b[j]) a[i++] else b[j++]     // <= keeps equal values in their original order
    }
    while (i < a.size) result[k++] = a[i++]
    while (j < b.size) result[k++] = b[j++]
    return result
}

/**
 * MOVE ZEROES. Problem: push every 0 to the end, keep the order of the other values, in place.
 * Idea: a write index; copy each non-zero to it, then fill the rest with zeros.
 * Time O(n), space O(1).
 */
fun moveZeroes(nums: IntArray) {
    var write = 0
    for (x in nums) if (x != 0) nums[write++] = x
    while (write < nums.size) nums[write++] = 0
}

/**
 * ROTATE RIGHT BY k. Problem: shift an array right by k places, in place (k >= 0).
 * Idea: reverse everything, then reverse the first k and the rest; k % size handles k larger than the array.
 * Time O(n), space O(1).
 */
fun rotateRight(nums: IntArray, k: Int) {
    if (nums.isEmpty()) return
    val shift = k % nums.size
    if (shift == 0) return
    reverseRange(nums, 0, nums.lastIndex)
    reverseRange(nums, 0, shift - 1)
    reverseRange(nums, shift, nums.lastIndex)
}

private fun reverseRange(a: IntArray, from: Int, to: Int) {
    var i = from
    var j = to
    while (i < j) {
        val tmp = a[i]
        a[i] = a[j]
        a[j] = tmp
        i++
        j--
    }
}

/**
 * BINARY SEARCH, LOWER BOUND. Problem: the first index whose value is >= target in a sorted array
 * (the insertion point; the size when every value is smaller).
 * Idea: halve the half-open range [low, high); keep the left half whenever the middle is already >= target.
 * Time O(log n), space O(1).
 */
fun lowerBound(sorted: IntArray, target: Int): Int {
    var low = 0
    var high = sorted.size
    while (low < high) {
        val mid = low + (high - low) / 2                 // not (low + high) / 2: that can overflow
        if (sorted[mid] < target) low = mid + 1 else high = mid
    }
    return low
}

/**
 * 9b. IN-PLACE AND SEARCH DRILLS: two pointers, a write index, reverse tricks, binary search
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.InPlaceAndSearchDrillsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("IN-PLACE AND SEARCH DRILLS  -  O(1) extra space, O(log n) search")

    section("Merge two sorted arrays (two pointers)")
    show { mergeSorted(intArrayOf(1, 3, 5), intArrayOf(2, 4, 6)) }
    show { mergeSorted(intArrayOf(1, 1), intArrayOf(1)) }                      // duplicates are all kept
    show { mergeSorted(intArrayOf(), intArrayOf(2, 3)) }                       // one side empty: the other is copied
    show { mergeSorted(intArrayOf(-5, 0), intArrayOf(-3, 4)) }                 // negatives
    show { mergeSorted(intArrayOf(), intArrayOf()) }

    section("Move zeroes (write index)")
    syntax("for (x in nums) if (x != 0) nums[write++] = x" to "compact the non-zeros to the front, then fill zeros")
    val a = intArrayOf(0, 1, 0, 3, 12);  moveZeroes(a);  show { a }            // order of the non-zeros is kept
    val b = intArrayOf(4, 5);  moveZeroes(b);  show { b }                      // no zeros: unchanged
    val c = intArrayOf(0, 0, 0);  moveZeroes(c);  show { c }
    val d = intArrayOf();  moveZeroes(d);  show { d }

    section("Rotate right by k (three reversals)")
    syntax("reverse all, reverse [0, k), reverse [k, end)" to "k % size first, so k may exceed the length")
    val r1 = intArrayOf(1, 2, 3, 4, 5, 6, 7);  rotateRight(r1, 3);  show { r1 }   // the last 3 move to the front
    val r2 = intArrayOf(1, 2, 3);  rotateRight(r2, 10);  show { r2 }          // 10 % 3 = 1 step
    val r3 = intArrayOf(1, 2, 3);  rotateRight(r3, 3);  show { r3 }           // k equal to the size: a full turn, unchanged
    val r4 = intArrayOf();  rotateRight(r4, 5);  show { r4 }                  // empty: no division by zero thanks to the early return

    section("Binary search: lower bound")
    syntax("if (a[mid] < target) low = mid + 1 else high = mid" to "loop while low < high: ends at the first index with value >= target")
    val sorted = intArrayOf(1, 3, 3, 3, 5)
    show { lowerBound(sorted, 3) }                                             // the FIRST of the equal values
    show { lowerBound(sorted, 4) }                                             // not present: where it would be inserted
    show { lowerBound(sorted, 0) }                                             // smaller than everything
    show { lowerBound(sorted, 9) }                                             // larger than everything: the size
    show { lowerBound(intArrayOf(), 7) }
    show { lowerBound(sorted, 4) - lowerBound(sorted, 3) }                     // how many 3s there are: two lower bounds
    note("Plain binarySearch from lesson 2 returns -1 for a missing value; the lower bound also answers 'where would it go'")

    section("Cost")
    bigO(
        "mergeSorted" to "O(n + m) time, O(n + m) space",
        "moveZeroes | rotateRight" to "O(n) time, O(1) extra space",
        "lowerBound" to "O(log n) time, O(1) space (needs SORTED input)",
        "sorting first to make binary search possible" to "O(n log n)",
    )
}
