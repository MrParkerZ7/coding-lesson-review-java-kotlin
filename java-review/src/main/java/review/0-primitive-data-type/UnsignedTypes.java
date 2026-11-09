package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 0f. UNSIGNED NUMBERS: Java has no unsigned types (except char)
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.UnsignedTypes
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class UnsignedTypes {

    public static void main(String[] args) {
        title("UNSIGNED NUMBERS  -  byte, int, long are always signed in Java");

        section("No unsigned types: the bits are the same, you read them differently");
        syntax(
                "Integer.toUnsignedString(i) | Long.toUnsignedString(l)", "print the bits as a non-negative number",
                "Integer.toUnsignedLong(i)", "int bits -> long with the same unsigned value",
                "Integer.compareUnsigned(a, b)", "compare as unsigned",
                "Integer.divideUnsigned(a, b) | remainderUnsigned", "divide / remainder as unsigned (also on Long)"
        );
        show(() -> Integer.toUnsignedString(-1));                      // -1 is 32 one-bits: 4294967295 when read as unsigned
        show(() -> Integer.toUnsignedLong(-1));                        // the same value in a long
        show(() -> Long.toUnsignedString(-1L));                        // about 18.4 quintillion
        show(() -> Integer.parseUnsignedInt("4294967295"));            // text -> the same bits: prints as -1
        show(() -> Integer.toBinaryString(-1).length());               // 32 one-bits
        show(() -> Integer.toHexString(-1));                           // ffffffff

        section("Comparing and dividing");
        show(() -> -1 > 1);                                            // signed: -1 is smaller
        show(() -> Integer.compareUnsigned(-1, 1) > 0);                // unsigned: 4294967295 is bigger
        show(() -> -1 / 2);                                            // signed division
        show(() -> Integer.divideUnsigned(-1, 2));                     // 4294967295 / 2
        show(() -> Long.divideUnsigned(-1L, 2));                       // 18446744073709551615 / 2
        show(() -> Integer.remainderUnsigned(-1, 10));                 // 4294967295 % 10
        int doubled = Integer.MAX_VALUE * 2;   show(() -> doubled);   // int overflow: wraps to -2
        show(() -> Integer.toUnsignedString(doubled));                 // the same bits read as unsigned: the correct 4294967294
        note("Only + - * give the same bits signed or unsigned; / % >> and comparisons need the Unsigned methods (>>> is the unsigned shift)");

        section("Bytes: b & 0xFF");
        syntax(
                "b & 0xFF | Byte.toUnsignedInt(b)", "byte (-128..127) -> int 0..255",
                "(byte) n", "int -> byte (wraps)"
        );
        byte b = (byte) 200;     show(() -> b);                        // 200 does not fit: the byte holds -56
        show(() -> b & 0xFF);                                          // 0xFF keeps only the low 8 bits: 200
        show(() -> Byte.toUnsignedInt(b));                             // the same, with a name
        show(() -> Integer.toHexString(b));                            // TRAP: b is sign-extended to int first: ffffffc8
        show(() -> Integer.toHexString(b & 0xFF));                     // c8
        show(() -> String.format("%02x", b));                          // %x on a Byte already prints it as unsigned
        show(() -> Short.toUnsignedInt((short) 40_000));               // same idea for short (40000 does not fit in a short)
        wontCompile("byte x = 0xFF;", "possible lossy conversion from int to byte: write (byte) 0xFF");
        byte hi = (byte) 0xFF;   show(() -> hi);                       // all 8 bits set = -1
        show(() -> hi >> 4);                                           // >> keeps the sign: -1
        show(() -> (hi & 0xFF) >> 4);                                  // mask first to get the top nibble: 15

        section("Why it matters: hash codes and bucket numbers");
        final int hash = "polygenelubricants".hashCode();   // a String whose hashCode is Integer.MIN_VALUE
        show(() -> hash);
        show(() -> Math.abs(hash) % 10);                               // TRAP: abs(MIN_VALUE) is still negative: a NEGATIVE bucket
        show(() -> Math.floorMod(hash, 10));                           // floorMod is never negative
        show(() -> Integer.remainderUnsigned(hash, 10));               // unsigned remainder: also fine, but a different number
        show(() -> (hash & 0x7FFFFFFF) % 10);                          // clear the sign bit, then %: never negative
        note("(low + high) >>> 1 is the other way to average two non-negative ints without overflow (lesson 2)");
        note("char is the only unsigned primitive: 0..65535 (see CharType)");
    }
}
