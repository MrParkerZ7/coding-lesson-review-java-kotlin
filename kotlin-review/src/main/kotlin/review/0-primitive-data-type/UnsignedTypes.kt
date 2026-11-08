package review.primitive

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 0f. UNSIGNED TYPES: UByte, UShort, UInt, ULong
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.UnsignedTypesKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("UNSIGNED TYPES  -  UByte, UShort, UInt, ULong (never negative)")

    section("Literals need the u suffix")
    syntax(
        "val name: UByte = 255u" to "u suffix = unsigned literal",
        "val name = 42u" to "UInt (when it fits in 32 bit)",
        "val name = 42uL" to "ULong",
    )
    val b: UByte = 255u;      show { b }                      // 8 bit, 0 .. 255
    val s: UShort = 65_535u;  show { s }                      // 16 bit, 0 .. 65535
    val i = 42u;              show { i }                      // UInt: 32 bit, the default for a u literal that fits
    val l = 42uL;             show { l }                      // ULong: 64 bit, needs uL

    section("Ranges")
    show { UByte.MAX_VALUE }
    show { UInt.MIN_VALUE }                                   // the minimum is 0, not a negative number
    show { UInt.MAX_VALUE }                                   // about 4.3 billion: twice Int.MAX_VALUE
    show { ULong.MAX_VALUE }                                  // about 18.4 quintillion

    section("Wrap-around, just like Int")
    val zero = 0u
    show { UInt.MAX_VALUE + 1u }                              // wraps to 0, no exception
    show { zero - 1u }                                        // TRAP: below zero wraps to the top, never -1

    section("Converting between signed and unsigned")
    syntax("x.toUInt() | x.toInt() | x.toUByte() | ..." to "same bits, read the other way")
    show { (-1).toUInt() }                                    // same 32 bits, read as unsigned
    show { UInt.MAX_VALUE.toInt() }                           // same 32 bits, read as signed
    show { 3_000_000_000u.toInt() }                           // does not fit in Int: reads as negative
    show { 300.toUByte() }                                    // narrowing wraps: 300 - 256
    show { UInt.MAX_VALUE > 1u }                              // compares as unsigned
    show { (-1) > 1 }                                         // the signed Int -1 is smaller

    section("Rules")
    wontCompile("42u + 1", "no mixing of signed and unsigned: write 42u + 1u")
    wontCompile("val c: UInt = -1", "an unsigned type has no negative literal")
    wontCompile("val d: UByte = 256u", "the literal is out of range")
    note("Java has no unsigned types; on the JVM a UInt is stored as an Int underneath")
}
