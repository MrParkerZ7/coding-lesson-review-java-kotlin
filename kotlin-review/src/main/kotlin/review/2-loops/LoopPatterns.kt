package review.loops

import review.bigO
import review.section
import review.show
import review.syntax
import review.title

/** Total of an array with an accumulator. O(n). */
fun sumAll(xs: IntArray): Int {
    var total = 0
    for (x in xs) total += x
    return total
}

/** Largest value, or null for an empty list. One pass, O(n). */
fun largest(xs: List<Int>): Int? {
    if (xs.isEmpty()) return null
    var best = xs[0]
    for (i in 1 until xs.size) if (xs[i] > best) best = xs[i]
    return best
}

/** Reverse in place with two pointers moving toward each other. O(n) time, O(1) extra space. */
fun reverseInPlace(arr: IntArray) {
    var left = 0
    var right = arr.lastIndex
    while (left < right) {
        val tmp = arr[left]
        arr[left] = arr[right]
        arr[right] = tmp
        left++
        right--
    }
}

/** Brute force Two Sum: every pair i < j. O(n^2) time, O(1) space. */
fun twoSumBrute(nums: IntArray, target: Int): Pair<Int, Int>? {
    for (i in nums.indices) {
        for (j in i + 1 until nums.size) {
            if (nums[i] + nums[j] == target) return i to j
        }
    }
    return null
}

/** Two pointers on a SORTED array: is there a pair adding up to target? O(n). */
fun hasPairWithSum(sorted: IntArray, target: Int): Boolean {
    var left = 0
    var right = sorted.lastIndex
    while (left < right) {
        val sum = sorted[left] + sorted[right]
        when {
            sum == target -> return true
            sum < target -> left++
            else -> right--
        }
    }
    return false
}

/** Sliding window: best sum of k neighbours, updating the sum instead of recomputing it. O(n). */
fun maxWindowSum(nums: IntArray, k: Int): Int {
    var window = 0
    for (i in 0 until k) window += nums[i]
    var best = window
    for (i in k until nums.size) {
        window += nums[i] - nums[i - k]          // add the new element, drop the old one
        if (window > best) best = window
    }
    return best
}

/** Prefix sums: prefix[i] is the sum of the first i elements; any range sum is one subtraction. O(n). */
fun prefixSums(nums: IntArray): IntArray {
    val prefix = IntArray(nums.size + 1)
    for (i in nums.indices) prefix[i + 1] = prefix[i] + nums[i]
    return prefix
}

/** Binary search with a loop: halve the range each round. O(log n). */
fun binarySearch(sorted: IntArray, target: Int): Int {
    var low = 0
    var high = sorted.lastIndex
    while (low <= high) {
        val mid = low + (high - low) / 2         // avoids the Int overflow of (low + high) / 2
        when {
            sorted[mid] == target -> return mid
            sorted[mid] < target -> low = mid + 1
            else -> high = mid - 1
        }
    }
    return -1
}

/** Palindrome check with two pointers, ignoring case. O(n). */
fun isPalindrome(s: String): Boolean {
    var i = 0
    var j = s.lastIndex
    while (i < j) {
        if (s[i].lowercaseChar() != s[j].lowercaseChar()) return false
        i++
        j--
    }
    return true
}

/** FizzBuzz: the classic loop with when. */
fun fizzBuzz(n: Int): List<String> {
    val out = mutableListOf<String>()
    for (i in 1..n) {
        out += when {
            i % 15 == 0 -> "FizzBuzz"
            i % 3 == 0 -> "Fizz"
            i % 5 == 0 -> "Buzz"
            else -> i.toString()
        }
    }
    return out
}

/**
 * 2c. LOOP PATTERNS: the loops behind most coding-interview problems
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.LoopPatternsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("LOOP PATTERNS  -  accumulator, two pointers, sliding window, binary search")

    section("Accumulator")
    syntax("var result = start; for (x in xs) result = combine(result, x)" to "one pass, keep a running answer")
    show { sumAll(intArrayOf(4, 8, 1, 9)) }
    show { largest(listOf(4, 8, 1, 9)) }
    show { largest(emptyList()) }                                              // handle the empty input: edge cases are graded
    show { fizzBuzz(15).takeLast(6) }
    show { var t = 0; for (x in listOf(1, 2, 3, 4, 5, 6)) if (x % 2 == 0) t += x * x; t }   // sum of squares of the evens
    show { listOf(1, 2, 3, 4, 5, 6).filter { it % 2 == 0 }.sumOf { it * it } } // the same result without a loop

    section("Two pointers")
    syntax("var left = 0; var right = last; while (left < right) { ...; left++ or right-- }" to "walk toward each other")
    val arr = intArrayOf(1, 2, 3, 4, 5);   reverseInPlace(arr);   show { arr }
    show { hasPairWithSum(intArrayOf(1, 3, 4, 6, 9), 10) }                     // 1 + 9 = 10 on a sorted array
    show { hasPairWithSum(intArrayOf(1, 3, 4, 6, 9), 100) }
    show { isPalindrome("Level") }
    show { isPalindrome("Kotlin") }

    section("Nested loops (brute force) vs one pass")
    show { twoSumBrute(intArrayOf(2, 7, 11, 15), 9) }                          // indexes (0, 1); correct but O(n^2)
    show { twoSumBrute(intArrayOf(1, 2, 3), 100) }                             // null when no pair exists

    section("Sliding window and prefix sums")
    syntax(
        "window += nums[i] - nums[i - k]" to "slide: add the new element, drop the old one",
        "prefix[i + 1] = prefix[i] + nums[i]" to "range sum (a..b) = prefix[b + 1] - prefix[a]",
    )
    show { maxWindowSum(intArrayOf(2, 1, 5, 1, 3, 2), 3) }                     // best 3 in a row: 5 + 1 + 3
    val prefix = prefixSums(intArrayOf(3, 1, 4, 1, 5));   show { prefix }
    show { prefix[4] - prefix[1] }                                             // sum of elements 1..3 = 1 + 4 + 1

    section("Binary search")
    syntax("while (low <= high) { mid = low + (high - low) / 2; ... }" to "halve the range each round: needs SORTED input")
    val sorted = intArrayOf(1, 3, 5, 7, 9, 11)
    show { binarySearch(sorted, 7) }
    show { binarySearch(sorted, 1) }
    show { binarySearch(sorted, 11) }
    show { binarySearch(sorted, 4) }                                           // -1 when missing

    section("Cost")
    bigO(
        "accumulator | two pointers | sliding window" to "O(n) time, O(1) space",
        "nested loops over all pairs" to "O(n^2) time",
        "binary search" to "O(log n) time",
        "prefix sums" to "O(n) to build, then O(1) per range sum",
    )
}
