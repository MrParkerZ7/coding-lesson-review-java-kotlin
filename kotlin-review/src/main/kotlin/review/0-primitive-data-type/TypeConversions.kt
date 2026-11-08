package review.primitive

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile
import kotlin.math.abs

/**
 * 0e. CONVERSIONS BETWEEN PRIMITIVE TYPES
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.TypeConversionsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("TYPE CONVERSIONS")

    val i = 42

    section("No implicit widening: always convert explicitly")
    syntax(
        "x.toByte() | toShort() | toInt() | toLong()" to "whole-number types",
        "x.toFloat() | toDouble() | toChar() | toString()" to "decimals, Char, text",
    )
    wontCompile("val l: Long = i", "Kotlin never widens an Int to a Long automatically")
    val l: Long = i.toLong();   show { l }                    // always explicit
    show { i.toDouble() }
    show { i.toFloat() }
    show { i.toByte() }
    show { i.toChar() }                                       // 42 is the code of '*'
    show { i.toString() }

    section("Narrowing wraps around")
    show { 300.toByte() }                                     // 300 - 256
    show { 3_000_000_000L.toInt() }                           // keeps only the low 32 bits

    section("Result type of mixed arithmetic")
    syntax("Int + Long -> Long | Int + Double -> Double | Int / Int -> Int" to "the wider type wins")
    show { i + l }                                            // Int + Long = Long
    show { i + 1.5 }                                          // Int + Double = Double
    show { i / 5 }                                            // Int / Int = Int: the decimals are lost
    show { i / 5.0 }                                          // one Double side is enough
    show { i.toDouble() / 5 }                                 // same result: convert first

    section("From String")
    syntax("\"42\".toInt() | toDouble() | toBoolean() | toIntOrNull()" to "text -> number")
    show { "42".toInt() }
    show { "3.5".toDouble() }
    show { "true".toBoolean() }
    show { "4x".toIntOrNull() }                               // null instead of an exception
    note("\"4x\".toInt() would throw NumberFormatException")

    section("Handy helpers")
    show { maxOf(3, 9) }
    show { minOf(3, 9) }
    show { 15.coerceIn(0, 10) }                               // clamp into a range
    show { abs(-5) }
    show { 10.compareTo(5) }                                  // 1 = bigger, 0 = equal, -1 = smaller
}
