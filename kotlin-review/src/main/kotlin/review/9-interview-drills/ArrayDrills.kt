package review.drills

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** The exception class a block throws (its simple name), or null when it throws nothing. */
private fun errorName(block: () -> Any?): String? = runCatching { block() }.exceptionOrNull()?.javaClass?.simpleName

/**
 * TWO SUM. Problem: which two numbers add up to target? Return their indexes.
 * Idea: one pass; remember value -> index of everything seen; for each x ask "is target - x already there?".
 * Time O(n), space O(n). Brute force checks every pair: O(n^2).
 */
fun twoSum(nums: IntArray, target: Int): Pair<Int, Int>? {
    val seen = HashMap<Int, Int>()                       // value -> index
    for ((i, x) in nums.withIndex()) {
        val j = seen[target - x]
        if (j != null) return j to i
        seen[x] = i                                      // store AFTER the check: an element never pairs with itself
    }
    return null
}

/**
 * CONTAINS DUPLICATE. Problem: does any value appear twice?
 * Idea: Set.add returns false when the value is already in the set, so stop at the first repeat.
 * Time O(n), space O(n).
 */
fun containsDuplicate(nums: IntArray): Boolean {
    val seen = HashSet<Int>()
    for (x in nums) if (!seen.add(x)) return true
    return false
}

/**
 * MAXIMUM SUBARRAY (Kadane). Problem: the largest sum of a contiguous, non-empty run.
 * Idea: current = best sum of a run ENDING here: extend the run, or start fresh at this element.
 * Time O(n), space O(1).
 */
fun maxSubarray(nums: IntArray): Int {
    require(nums.isNotEmpty()) { "need at least one element" }
    var best = nums[0]
    var current = nums[0]
    for (i in 1 until nums.size) {
        current = maxOf(nums[i], current + nums[i])
        best = maxOf(best, current)
    }
    return best
}

/**
 * BEST TIME TO BUY AND SELL STOCK. Problem: one buy, then one sell later: the biggest profit (0 if none).
 * Idea: walk the prices, remember the cheapest so far, and see what selling today would earn.
 * Time O(n), space O(1).
 */
fun maxProfit(prices: IntArray): Int {
    var cheapest = Int.MAX_VALUE
    var best = 0
    for (p in prices) {
        cheapest = minOf(cheapest, p)
        best = maxOf(best, p - cheapest)
    }
    return best
}

/**
 * PRODUCT OF ARRAY EXCEPT SELF. Problem: result[i] = product of all the other elements, without division.
 * Idea: a pass left to right keeps the product of everything BEFORE i, a pass right to left the product AFTER i.
 * Time O(n), extra space O(1) besides the result.
 */
fun productExceptSelf(nums: IntArray): IntArray {
    val result = IntArray(nums.size) { 1 }
    var left = 1
    for (i in nums.indices) {
        result[i] = left
        left *= nums[i]
    }
    var right = 1
    for (i in nums.indices.reversed()) {
        result[i] *= right
        right *= nums[i]
    }
    return result
}

/**
 * 9a. ARRAY DRILLS: hash map, set, running best, prefix and suffix
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.ArrayDrillsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("ARRAY DRILLS  -  one pass beats two nested loops")

    section("Two Sum (hash map)")
    syntax("seen[x] = i; look up seen[target - x]" to "value -> index map: O(n) instead of O(n^2)")
    show { twoSum(intArrayOf(2, 7, 11, 15), 9) }
    show { twoSum(intArrayOf(3, 3), 6) }                                       // duplicates: both 3s are used (indexes 0 and 1)
    show { twoSum(intArrayOf(-3, 4, 3, 90), 0) }                               // negatives work the same way
    show { twoSum(intArrayOf(3), 6) }                                          // the single 3 must not pair with itself
    show { twoSum(intArrayOf(), 5) }                                           // empty input: no pair

    section("Contains Duplicate (set)")
    show { containsDuplicate(intArrayOf(1, 2, 3, 1)) }
    show { containsDuplicate(intArrayOf(1, 2, 3)) }
    show { containsDuplicate(intArrayOf()) }                                   // empty: nothing repeats
    show { containsDuplicate(intArrayOf(7)) }

    section("Maximum Subarray (Kadane)")
    syntax("current = max(x, current + x)" to "best run ENDING here: extend it, or start fresh; best = max(best, current)")
    show { maxSubarray(intArrayOf(-2, 1, -3, 4, -1, 2, 1, -5, 4)) }           // the run 4, -1, 2, 1
    show { maxSubarray(intArrayOf(-3, -1, -2)) }                               // TRAP: all negative, the answer is -1, not 0
    show { maxSubarray(intArrayOf(5)) }
    show { errorName { maxSubarray(intArrayOf()) } }                           // empty input is rejected by require()

    section("Best time to buy and sell stock")
    show { maxProfit(intArrayOf(7, 1, 5, 3, 6, 4)) }                           // buy at 1, sell at 6
    show { maxProfit(intArrayOf(7, 6, 4, 3, 1)) }                              // always falling: do not trade, profit 0
    show { maxProfit(intArrayOf(5)) }
    show { maxProfit(intArrayOf()) }

    section("Product of array except self (no division)")
    show { productExceptSelf(intArrayOf(1, 2, 3, 4)) }
    show { productExceptSelf(intArrayOf(1, 0, 3, 4)) }                         // one zero: only the zero's own slot is non-zero
    show { productExceptSelf(intArrayOf(-1, 2, -3)) }                          // negatives
    show { productExceptSelf(intArrayOf(5)) }                                  // nothing else to multiply: the empty product is 1
    note("Dividing the total product by nums[i] fails on zeros, which is why the prefix/suffix version is the expected answer")

    section("Cost")
    bigO(
        "twoSum | containsDuplicate" to "O(n) time, O(n) space (the map or set)",
        "maxSubarray | maxProfit" to "O(n) time, O(1) space",
        "productExceptSelf" to "O(n) time, O(1) extra space besides the result",
        "brute force: compare every pair" to "O(n^2) time",
    )
}
