package review.control

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** The exception class a block throws (its simple name), or null when it throws nothing. */
private fun errorName(block: () -> Any?): String? = runCatching { block() }.exceptionOrNull()?.javaClass?.simpleName

/** Guard clauses: each bad case leaves at once, the main path stays flat. */
fun shippingCost(weightKg: Int, express: Boolean): Int {
    if (weightKg <= 0) return 0
    if (weightKg > 30) return -1                         // too heavy: refused
    val base = if (weightKg <= 5) 4 else 9
    return if (express) base * 2 else base
}

/** The same rules with nested ifs: the real work sits three levels deep. */
fun shippingNested(weightKg: Int, express: Boolean): Int {
    if (weightKg > 0) {
        if (weightKg <= 30) {
            val base = if (weightKg <= 5) 4 else 9
            return if (express) base * 2 else base
        } else {
            return -1
        }
    } else {
        return 0
    }
}

/** return leaves BOTH loops at once: no flag, no label. */
fun findPair(nums: IntArray, target: Int): Pair<Int, Int>? {
    for (i in nums.indices) {
        for (j in i + 1 until nums.size) {
            if (nums[i] + nums[j] == target) return nums[i] to nums[j]
        }
    }
    return null
}

/** A bare return inside forEachIndexed (an inline lambda) leaves the whole FUNCTION. */
fun firstNegativeIndex(xs: List<Int>): Int {
    xs.forEachIndexed { i, x -> if (x < 0) return i }
    return -1
}

/** return@forEach only leaves the lambda: it acts like continue. */
fun sumPositives(xs: List<Int>): Int {
    var total = 0
    xs.forEach { if (it <= 0) return@forEach; total += it }
    return total
}

fun twice(x: Int) = x * 2
fun absOf(n: Int) = if (n < 0) -n else n
fun signOf(n: Int) = when { n > 0 -> 1; n < 0 -> -1; else -> 0 }

/** A recursive function must name its return type. */
fun factorial(n: Int): Long = if (n <= 1) 1 else n * factorial(n - 1)

/** Nothing: this function never returns normally. */
fun fail(message: String): Nothing = throw IllegalStateException(message)

/** Nothing fits every type, so the else branch still gives an Int. */
fun halve(n: Int): Int = if (n % 2 == 0) n / 2 else fail("odd: $n")

/** error() is the one-liner for IllegalStateException. */
fun parseOrFail(s: String): Int = s.toIntOrNull() ?: error("not a number: $s")

/** FizzBuzz as a when: the 15 case MUST come first. */
fun fizzBuzzWord(n: Int): String = when {
    n % 15 == 0 -> "FizzBuzz"
    n % 3 == 0 -> "Fizz"
    n % 5 == 0 -> "Buzz"
    else -> n.toString()
}

/** FizzBuzz with the branches in the wrong order. */
fun fizzBuzzWrong(n: Int): String = when {
    n % 3 == 0 -> "Fizz"
    n % 5 == 0 -> "Buzz"
    n % 15 == 0 -> "FizzBuzz"
    else -> n.toString()
}

/** Leap year: divisible by 400, or by 4 but not by 100. */
fun isLeapYear(y: Int): Boolean = when {
    y % 400 == 0 -> true
    y % 100 == 0 -> false
    else -> y % 4 == 0
}

fun gradeOf(score: Int): String = when (score) {
    in 90..100 -> "A"
    in 80..89 -> "B"
    in 70..79 -> "C"
    in 0..69 -> "F"
    else -> "invalid"
}

fun dayName(n: Int): String = when (n) {
    1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; 7 -> "Sun"
    else -> "?"
}

/**
 * 8c. EARLY RETURN, EXPRESSION BODIES, NOTHING, AND CLASSICS WITH WHEN
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.EarlyReturnKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("EARLY RETURN  -  leave as soon as the answer is known")

    section("Guard clauses")
    syntax("if (bad case) return value" to "handle the odd cases first and leave; the main path stays flat")
    show { shippingCost(3, false) }
    show { shippingCost(8, true) }                                             // 9 * 2 for express
    show { shippingCost(0, true) }                                             // guard: nothing to ship
    show { shippingCost(31, false) }                                           // guard: too heavy
    show { shippingNested(8, true) == shippingCost(8, true) }                  // same result, but the nested version is harder to read

    section("return from loops and lambdas")
    syntax(
        "return value" to "leaves the function, from any depth of loops",
        "return@forEach | return@map value" to "leaves only the lambda (like continue)",
        "label@ { ... return@label }" to "name a lambda yourself",
    )
    show { findPair(intArrayOf(2, 7, 11, 15), 18) }                            // found in the inner loop: both loops end at once
    show { findPair(intArrayOf(1, 2), 100) }
    show { firstNegativeIndex(listOf(3, 5, -1, 4)) }                           // the return inside forEachIndexed left the whole function
    show { firstNegativeIndex(listOf(3, 5)) }
    show { sumPositives(listOf(3, -1, 4, 0, 2)) }                              // return@forEach skipped -1 and 0 only
    show { listOf(1, 2, 3).map { if (it == 2) return@map 0; it } }              // return@map gives this element's result and goes on
    show { (1..3).map lit@{ if (it == 2) return@lit -1; it } }                 // a label you name yourself
    note("A bare return in a lambda only works when the lambda is inline (forEach, map ...): it leaves the enclosing function")

    section("Single-expression functions")
    syntax(
        "fun name(params) = expression" to "no braces, no return; the return type is inferred",
        "fun name(params): Type = expression" to "write the type for public or recursive functions",
    )
    show { twice(21) }
    show { absOf(-8) }
    show { signOf(-9) }
    show { factorial(5) }
    show { factorial(20) }                                                     // the largest factorial that fits in a Long
    show { factorial(21) }                                                     // TRAP: overflow wraps around to a wrong negative number
    wontCompile("fun fact(n: Int) = if (n <= 1) 1 else n * fact(n - 1)", "a recursive function needs an explicit return type")

    section("Nothing: a function that never returns")
    syntax(
        "fun fail(msg: String): Nothing = throw ..." to "Nothing is a subtype of every type",
        "TODO() | error(msg) | throw ..." to "all of type Nothing: they end the flow",
    )
    show { halve(8) }
    show { errorName { halve(7) } }                                            // the else branch is fail(), so it throws
    show { parseOrFail("12") }
    show { errorName { parseOrFail("x") } }                                    // ?: error(...) is IllegalStateException
    show { errorName { TODO() } }                                              // TODO() throws NotImplementedError (an Error, not an Exception)
    note("Code after a call that returns Nothing is unreachable, and the compiler warns about it")

    section("Classics solved with when")
    show { (1..15).map { fizzBuzzWord(it) } }                                  // 15 first, then 3, then 5
    show { fizzBuzzWrong(15) }                                                 // TRAP: n % 3 matches first, so FizzBuzz is never produced
    show { isLeapYear(1900) }                                                  // divisible by 100 but not by 400
    show { isLeapYear(2000) }
    show { isLeapYear(2024) }
    show { isLeapYear(2023) }
    show { gradeOf(90) }                                                       // boundary: 90 is an A
    show { gradeOf(89) }
    show { gradeOf(101) }
    show { gradeOf(-1) }
    show { dayName(3) }
    show { dayName(9) }
}
