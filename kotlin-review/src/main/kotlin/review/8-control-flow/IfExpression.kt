package review.control

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** The larger of two numbers: an if EXPRESSION as the whole function body. */
fun bigger(a: Int, b: Int) = if (a > b) a else b

/** A chain of else if: the first true condition decides. */
fun sizeWord(n: Int): String =
    if (n < 0) "negative" else if (n == 0) "zero" else if (n < 10) "small" else "large"

/** if with is: inside each branch x already has the checked type. */
fun describeAny(x: Any): String =
    if (x is String) "text of ${x.length} chars" else if (x is Int) "number ${x + 1}" else "other"

/** A Boolean? that is true, false or null. */
private fun maybeFlag(kind: Int): Boolean? = when (kind) {
    0 -> true
    1 -> false
    else -> null
}

/**
 * 8a. IF AS AN EXPRESSION (there is no ternary operator)
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.IfExpressionKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("IF EXPRESSION  -  if returns a value")

    section("if gives a value")
    syntax(
        "val v = if (cond) a else b" to "if IS the ternary operator: it returns a or b",
        "if (cond) { ...; last } else { ...; last }" to "a block's value is its LAST expression",
        "if (cond) statement" to "as a plain statement the else is optional",
    )
    val n = 7
    show { if (n > 5) "big" else "small" }
    val par = if (n % 2 == 0) "even" else "odd";  show { par }
    val r = if (n > 5) { val h = n / 2; h * 10 } else 0;  show { r }          // the block's last expression, h * 10, is the value
    show { bigger(3, 9) }                                                      // an expression body: no braces, no return
    show { maxOf(3, 9) }                                                       // the standard library already has it
    show { 15.coerceIn(0, 10) }                                                // clamp into a range: 10
    wontCompile("val s = if (n > 5) 1", "an if used as a value needs an else branch")
    wontCompile("val r = n > 5 ? 1 : 2", "Kotlin has no ?: ternary (?: is the elvis operator); write if (n > 5) 1 else 2")

    section("Conditions: in, !in, && and else if")
    syntax(
        "x in a..b" to "a <= x <= b, both ends included",
        "x !in a..b" to "outside the range",
        "x in a..<b  |  x in a until b" to "the end b is excluded",
        "if (a) ... else if (b) ... else ..." to "the first true condition wins",
    )
    show { n in 1..10 }
    show { n in 1..<7 }                                                        // false: 1..<7 stops before 7
    show { n !in 1..5 }
    show { 0 in 1..0 }                                                         // TRAP: 1..0 is EMPTY, nothing is in it (lesson 2)
    show { sizeWord(-4) }
    show { sizeWord(0) }
    show { sizeWord(7) }
    show { sizeWord(10) }                                                      // boundary: n < 10 is false, so the last branch
    show { n > 100 && 10 / (n - 7) > 0 }                                       // false, no ArithmeticException: && skips the right side
    wontCompile("0 < n < 10", "(0 < n) is a Boolean, which cannot be compared with 10; write n in 1..9")

    section("if with is (smart cast)")
    syntax("if (x is Type) x.member" to "inside the branch x already has the Type")
    show { describeAny("kotlin") }
    show { describeAny(41) }                                                   // x + 1 compiled because x is an Int in that branch
    show { describeAny(2.5) }

    section("Traps")
    show { maybeFlag(0) == true }
    show { maybeFlag(2) == true }                                              // null == true is false: compare a Boolean? like this
    show { maybeFlag(2) ?: false }                                             // or give it a default with ?:
    wontCompile("if (flag) ... (flag: Boolean?)", "a condition must be a Boolean, not Boolean?")
    wontCompile("if ((a = 5) > 0) ...", "an assignment is a statement, not a value: no while ((x = read()) != null) idiom")
    note("A plain if statement may omit else; an if used as a VALUE may not. The compiler tells you at once")
}
