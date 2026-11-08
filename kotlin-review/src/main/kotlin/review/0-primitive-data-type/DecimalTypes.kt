package review.primitive

import review.section
import review.show
import review.syntax
import review.title
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * 0b. DECIMAL TYPES: Double, Float
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.DecimalTypesKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("DECIMAL TYPES  -  Double, Float")

    section("Declaring and literals")
    syntax(
        "val name = 3.14" to "Double: the default for decimals",
        "val name = 3.14f" to "Float: needs the f suffix",
        "1.5e3" to "scientific notation (1.5 x 10^3)",
    )
    val d = 3.14;    show { d }                               // Double: 64 bit, the DEFAULT for decimals
    val f = 3.14f;   show { f }                               // Float: 32 bit, needs the f suffix
    show { 1.5e3 }                                            // scientific notation

    section("Precision: never compare decimals with ==")
    syntax("abs(a - b) < 1e-9" to "compare decimals with a tolerance")
    show { 0.1 + 0.2 }                                        // binary fractions cannot be exact
    show { 0.1 + 0.2 == 0.3 }                                 // so == fails
    show { abs((0.1 + 0.2) - 0.3) < 1e-9 }                    // compare with a tolerance instead

    section("Float keeps about 7 digits, Double about 15-16")
    show { 3.14159265358979f }                                // Float: the rest of the digits are lost
    show { 3.14159265358979 }                                 // Double: all of these digits survive
    show { 1.0f / 3 }                                         // Float: 8 digits
    show { 1.0 / 3 }                                          // Double: 16 digits
    show { 16_777_216f + 1f }                                 // Float cannot hold 16,777,217: the +1 is lost

    section("Limits")
    show { Float.MAX_VALUE }
    show { Double.MAX_VALUE }
    show { -Double.MAX_VALUE }                                // the most negative Double
    show { Double.MIN_VALUE }                                 // TRAP: the smallest POSITIVE Double, not the most negative
    show { Double.MAX_VALUE * 2 }                             // overflow gives Infinity, no exception

    section("Special values")
    show { 1 / 0.0 }                                          // positive infinity (no exception for decimals)
    show { -1 / 0.0 }                                         // negative infinity
    show { 0.0 / 0.0 }                                        // NaN = not a number
    show { Double.NaN == Double.NaN }                         // NaN is not equal to itself
    show { Double.NaN.isNaN() }                               // use isNaN() to test

    section("Converting and rounding")
    syntax(
        "x.toInt()" to "cut off the decimals (toward zero)",
        "round(x) | x.roundToInt()" to "round to the nearest whole number",
        "\"%.2f\".format(x)" to "format with 2 decimals (returns a String)",
    )
    show { 3.99.toInt() }                                     // truncates toward zero
    show { (-3.99).toInt() }                                  // toward zero, not floor
    show { round(2.5) }                                       // half to EVEN: 2.0, not 3.0
    show { round(3.5) }                                       // half to even again: 4.0
    show { 2.5.roundToInt() }                                 // half UP
    show { 3.5.roundToInt() }                                 // half up
    show { "%.2f".format(3.14159) }                           // format to 2 decimals (returns a String)
}
