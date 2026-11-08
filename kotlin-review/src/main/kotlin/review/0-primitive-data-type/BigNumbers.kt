package review.primitive

import review.note
import review.section
import review.show
import review.syntax
import review.title
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * 0g. BIG NUMBERS: BigInteger, BigDecimal (java.math, used from Kotlin with + - * / operators)
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.BigNumbersKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("BIG NUMBERS  -  BigInteger and BigDecimal")

    section("BigInteger: whole numbers that never overflow")
    syntax(
        "BigInteger(\"123\") | BigInteger.valueOf(123L)" to "create from a String or a Long",
        "a + b | a - b | a * b | a / b | a.pow(n)" to "the usual operators work",
    )
    show { Long.MAX_VALUE + 1 }                                                       // Long wraps around
    show { BigInteger.valueOf(Long.MAX_VALUE) + BigInteger.ONE }                      // BigInteger just grows
    show { BigInteger.TWO.pow(100) }                                                  // 2^100 does not fit in a Long
    show { (1..25).fold(BigInteger.ONE) { acc, n -> acc * n.toBigInteger() } }        // 25 factorial: a Long overflows at 21!
    show { BigInteger("12") == BigInteger("12") }                                     // use ==, which calls equals()

    section("BigDecimal: exact decimals (money!)")
    syntax(
        "BigDecimal(\"0.1\")" to "create from a String: exact",
        "BigDecimal.valueOf(0.1)" to "create from a Double through its text: fine",
        "BigDecimal(0.1)" to "from a Double directly: keeps the Double's error",
    )
    show { 0.1 + 0.2 }                                                                // Double: not exact
    show { BigDecimal("0.1") + BigDecimal("0.2") }                                    // exactly 0.3 when built from a String
    show { BigDecimal.valueOf(0.1) }                                                  // valueOf(Double) goes through the text "0.1": fine
    show { BigDecimal(0.1) }                                                          // TRAP: built from a Double it keeps the Double's error
    note("Rule: build a BigDecimal from a String (or valueOf), never from a Double literal")

    section("Scale: equals() and compareTo() disagree")
    show { BigDecimal("2.0") == BigDecimal("2.00") }                                  // TRAP: equals() compares the scale too
    show { BigDecimal("2.0").compareTo(BigDecimal("2.00")) }                          // 0 means equal in value: use compareTo
    show { BigDecimal("2.50").stripTrailingZeros() }                                  // drops the useless zero

    section("Division needs a rounding rule")
    syntax(
        "a.divide(b, scale, RoundingMode.HALF_UP)" to "divide and round in one step",
        "x.setScale(2, RoundingMode.HALF_UP)" to "round to 2 decimals",
    )
    val attempt = runCatching { BigDecimal.ONE.divide(BigDecimal(3)) }
    show { attempt.exceptionOrNull()?.javaClass?.simpleName }                         // ONE.divide(BigDecimal(3)) throws: 1/3 never ends
    show { BigDecimal.ONE.divide(BigDecimal(3), 5, RoundingMode.HALF_UP) }            // 5 decimals, round half up
    show { BigDecimal("2.345").setScale(2, RoundingMode.HALF_UP) }                    // school rounding
    show { BigDecimal("2.345").setScale(2, RoundingMode.HALF_EVEN) }                  // banker's rounding: ties go to the even digit
}
