package review.primitive

import review.note
import review.section
import review.show
import review.syntax
import review.title

/**
 * 0a. INTEGER TYPES: Byte, Short, Int, Long
 *     Run: mvn -q compile exec:java        (this is the default class)
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("INTEGER TYPES  -  Byte, Short, Int, Long")

    section("Declaring and literals")
    syntax(
        "val name: Type = value" to "explicit type",
        "val name = value" to "type inferred from the value",
        "42L | 0xFF | 0b1010 | 1_000_000" to "Long suffix | hex | binary | digit separator",
    )
    val b: Byte = 127;       show { b }                       // 8 bit
    val s: Short = 32_767;   show { s }                       // 16 bit
    val i = 42;              show { i }                       // 32 bit: the DEFAULT type for whole numbers
    val l = 42L;             show { l }                       // 64 bit: needs the L suffix
    show { 0xFF }                                             // hexadecimal literal
    show { 0b1010 }                                           // binary literal
    show { 1_000_000 }                                        // underscores are only for readability

    section("Ranges")
    syntax("Type.MIN_VALUE | Type.MAX_VALUE" to "smallest and largest value of a type")
    show { Byte.MIN_VALUE }
    show { Byte.MAX_VALUE }
    show { Short.MIN_VALUE }
    show { Short.MAX_VALUE }
    show { Int.MIN_VALUE }                                    // about -2.1 billion
    show { Int.MAX_VALUE }                                    // about +2.1 billion
    show { Long.MIN_VALUE }                                   // about -9.2 quintillion
    show { Long.MAX_VALUE }                                   // about +9.2 quintillion

    section("Overflow wraps around silently")
    show { Int.MAX_VALUE + 1 }                                // wraps to Int.MIN_VALUE, no exception
    show { Int.MAX_VALUE + 1L }                               // Int + Long = Long, so no overflow
    note("Math.addExact(Int.MAX_VALUE, 1) would throw ArithmeticException instead")

    section("Division and remainder")
    syntax(
        "a / b" to "Int / Int stays an Int (the decimals are cut off)",
        "a % b" to "remainder, keeps the sign of a",
        "a.mod(b)" to "modulo, never negative when b is positive",
    )
    show { 7 / 2 }                                            // Int / Int truncates toward zero
    show { -7 / 2 }                                           // toward zero, not floor
    show { 7 % 3 }
    show { -7 % 3 }                                           // % keeps the sign of the left side
    show { (-7).mod(3) }                                      // mod() is never negative
    show { 7 / 2.0 }                                          // one Double makes the result a Double
    note("7 / 0 throws ArithmeticException (integers only)")

    section("Bit operations")
    syntax(
        "a and b | a or b | a xor b" to "infix functions, not symbols (& | ^)",
        "a shl n | a shr n | a.inv()" to "shift left | shift right | flip every bit (~)",
    )
    show { 5 and 3 }                                          // 0101 and 0011
    show { 5 or 3 }                                           // 0101 or 0011
    show { 5 xor 3 }                                          // 0101 xor 0011
    show { 5 shl 1 }                                          // shift left = multiply by 2
    show { 5 shr 1 }                                          // shift right = divide by 2
    show { 5.inv() }                                          // flip every bit (~5)
}
