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
 * FIZZBUZZ. Problem: for 1..n give "Fizz" for multiples of 3, "Buzz" for 5, "FizzBuzz" for both, else the number.
 * Idea: test the combined case (15) FIRST in a when, or the earlier branches swallow it.
 * Time O(n), space O(n) for the list.
 */
fun fizzBuzz(n: Int): List<String> = (1..n).map { i ->
    when {
        i % 15 == 0 -> "FizzBuzz"
        i % 3 == 0 -> "Fizz"
        i % 5 == 0 -> "Buzz"
        else -> i.toString()
    }
}

/**
 * PALINDROME NUMBER. Problem: does an Int read the same backwards? No String allowed.
 * Idea: reverse only the second half of the digits and compare it with the first half (no overflow possible).
 * Negatives are never palindromes; a positive number ending in 0 cannot be one either. Time O(log10 x), space O(1).
 */
fun isPalindromeNumber(x: Int): Boolean {
    if (x < 0 || (x % 10 == 0 && x != 0)) return false
    var rest = x
    var reversedHalf = 0
    while (rest > reversedHalf) {
        reversedHalf = reversedHalf * 10 + rest % 10
        rest /= 10
    }
    return rest == reversedHalf || rest == reversedHalf / 10     // the second test drops the middle digit of an odd length
}

/**
 * REVERSE INTEGER. Problem: reverse the digits of an Int; return 0 when the result does not fit in an Int.
 * Idea: pop the last digit with % 10 and push it with * 10; check BEFORE the multiplication whether it would overflow.
 * Time O(log10 x), space O(1).
 */
fun reverseInteger(x: Int): Int {
    var rest = x
    var reversed = 0
    while (rest != 0) {
        val digit = rest % 10
        rest /= 10
        if (reversed > Int.MAX_VALUE / 10 || (reversed == Int.MAX_VALUE / 10 && digit > 7)) return 0
        if (reversed < Int.MIN_VALUE / 10 || (reversed == Int.MIN_VALUE / 10 && digit < -8)) return 0
        reversed = reversed * 10 + digit
    }
    return reversed
}

/**
 * GREATEST COMMON DIVISOR. Problem: the largest number that divides both a and b (a, b >= 0).
 * Idea: Euclid. gcd(a, b) = gcd(b, a % b), and gcd(a, 0) = a. Time O(log min(a, b)), space O(1) (tailrec is a loop).
 */
tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

/**
 * IS PRIME. Problem: is n a prime (greater than 1, divisible only by 1 and itself)?
 * Idea: trial division up to the square root, odd divisors only. Written as i <= n / i to avoid the Int overflow of i * i.
 * Time O(sqrt n), space O(1).
 */
fun isPrime(n: Int): Boolean {
    if (n < 2) return false
    if (n % 2 == 0) return n == 2
    var i = 3
    while (i <= n / i) {
        if (n % i == 0) return false
        i += 2
    }
    return true
}

/** FIBONACCI, iterative: two variables. O(n) time, O(1) space. Long holds up to fib(92). */
fun fib(n: Int): Long {
    var a = 0L
    var b = 1L
    repeat(n) {
        val next = a + b
        a = b
        b = next
    }
    return a
}

/** FIBONACCI, memoized recursion: each n is computed once and cached. O(n) time, O(n) space. */
fun fibMemo(n: Int, memo: MutableMap<Int, Long> = HashMap()): Long {
    if (n <= 1) return n.toLong()
    return memo.getOrPut(n) { fibMemo(n - 1, memo) + fibMemo(n - 2, memo) }
}

/** How many calls the plain recursion fib(n) = fib(n - 1) + fib(n - 2) makes: the cost of NOT caching. O(2^n). */
fun fibNaiveCalls(n: Int): Long = if (n <= 1) 1 else 1 + fibNaiveCalls(n - 1) + fibNaiveCalls(n - 2)

/**
 * 9e. CLASSICS AND THE INTERVIEW ROUTINE
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.ClassicsAndApproachKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CLASSICS  -  the small problems every interview starts with")

    section("FizzBuzz")
    show { fizzBuzz(15) }                                                      // 15 is FizzBuzz, not Fizz
    show { fizzBuzz(0) }                                                       // 1..0 is empty, so an empty list
    show { fizzBuzz(1) }

    section("Palindrome number (no String)")
    show { isPalindromeNumber(121) }
    show { isPalindromeNumber(-121) }                                          // the minus sign makes it not a palindrome
    show { isPalindromeNumber(10) }                                            // TRAP: reversed it would be 01
    show { isPalindromeNumber(0) }
    show { isPalindromeNumber(12321) }                                         // odd length: the middle digit is dropped by / 10
    show { isPalindromeNumber(1221) }

    section("Reverse integer (overflow trap)")
    syntax("if (reversed > Int.MAX_VALUE / 10 || ...) return 0" to "test BEFORE reversed * 10 + digit, afterwards it is too late")
    show { reverseInteger(123) }
    show { reverseInteger(-123) }
    show { reverseInteger(120) }                                               // trailing zero disappears: 21
    show { reverseInteger(1534236469) }                                        // 9646324351 does not fit in an Int: 0
    show { reverseInteger(Int.MIN_VALUE) }                                     // -2147483648 reversed is -8463847412: out of range, so 0
    show { errorName { 1534236469.toString().reversed().toInt() } }           // the String shortcut crashes on the same input
    show { reverseInteger(0) }

    section("GCD, LCM and primes")
    syntax("gcd(a, b) = if (b == 0) a else gcd(b, a % b)" to "Euclid; written as a tailrec function, which compiles to a loop")
    show { gcd(12, 18) }
    show { gcd(7, 13) }                                                        // coprime: 1
    show { gcd(0, 5) }
    show { gcd(5, 0) }
    show { 12 / gcd(12, 18) * 18 }                                             // lcm = a / gcd * b (divide first: a * b may overflow)
    show { isPrime(97) }
    show { isPrime(91) }                                                       // 7 * 13
    show { isPrime(2) }                                                        // the only even prime
    show { isPrime(1) }                                                        // 1 is not prime
    show { isPrime(-7) }
    show { isPrime(Int.MAX_VALUE) }                                            // 2^31 - 1 is prime; i <= n / i keeps i * i from overflowing

    section("Fibonacci: iterative, memoized, and why not plain recursion")
    show { fib(10) }
    show { fib(0) }
    show { fibMemo(50) }                                                       // instant, thanks to the cache
    show { fib(92) }                                                           // the largest Fibonacci number that fits in a Long
    show { fib(93) }                                                           // TRAP: overflow wraps to a negative number
    show { fibNaiveCalls(20) }                                                 // calls for fib(20) without a cache; the loop does 20 additions
    show { fibNaiveCalls(35) }                                                 // almost 30 million calls for fib(35): exponential growth

    section("The interview routine")
    syntax(
        "sorted input + a pair or target" to "two pointers or binary search",
        "subarray or substring with a condition" to "sliding window",
        "seen before? count? duplicates?" to "HashMap or HashSet",
        "matching brackets, undo, nesting" to "stack",
        "shortest path with equal steps" to "BFS with a queue",
        "count the ways | best value from smaller cases" to "dynamic programming",
        "top K | K-th" to "heap, or bucket by count",
        "intervals" to "sort by start, then sweep",
    )
    note("1 CLARIFY: input size and range, empty input, duplicates, negatives, sorted or not, one answer or all")
    note("2 EXAMPLES: walk one normal example and one edge case by hand before coding")
    note("3 BRUTE FORCE: say the obvious solution out loud with its cost (often O(n^2))")
    note("4 OPTIMIZE: name the pattern from the table above and the trade: extra space for less time")
    note("5 CODE: guard clauses first, clear names, no clever one-liners; talk while you type")
    note("6 TEST: run your example through the code; then empty, single element, duplicates, negatives, overflow")
    note("7 COMPLEXITY: always say time AND space, and what the worst case looks like")

    section("Cost")
    bigO(
        "fizzBuzz | isPalindromeNumber | reverseInteger" to "O(n) | O(log10 x) | O(log10 x) time",
        "gcd (Euclid)" to "O(log min(a, b)) time",
        "isPrime (trial division)" to "O(sqrt n) time, O(1) space",
        "fib iterative | memoized" to "O(n) time; O(1) space | O(n) space",
        "fib plain recursion" to "O(2^n) time: do not use it",
    )
}
