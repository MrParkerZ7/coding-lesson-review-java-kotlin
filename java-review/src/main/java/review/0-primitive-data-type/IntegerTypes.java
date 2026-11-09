package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 0a. INTEGER TYPES: byte, short, int, long
 *     Run: mvn -q compile exec:java        (this is the default class)
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class IntegerTypes {

    public static void main(String[] args) {
        title("INTEGER TYPES  -  byte, short, int, long");

        section("Declaring and literals");
        syntax(
                "Type name = value;", "explicit type (Java has no val: use final for read-only)",
                "var name = value;", "type inferred from the value (local variables only, Java 10+)",
                "42L | 0xFF | 0b1010 | 1_000_000", "long suffix | hex | binary | digit separator"
        );
        byte b = 127;        show(() -> b);                            // 8 bit
        short s = 32_767;    show(() -> s);                            // 16 bit
        int i = 42;          show(() -> i);                            // 32 bit: the DEFAULT type for whole numbers
        long l = 42L;        show(() -> l);                            // 64 bit: needs the L suffix
        show(() -> 0xFF);                                              // hexadecimal literal
        show(() -> 0b1010);                                            // binary literal
        show(() -> 1_000_000);                                         // underscores are only for readability
        note("The type column shows the wrapper class of the value: an int prints as Integer, a long as Long");
        wontCompile("int big = 3_000_000_000;", "the literal does not fit in an int: write 3_000_000_000L");

        section("Ranges");
        syntax("Type.MIN_VALUE | Type.MAX_VALUE", "smallest and largest value of a wrapper class (Integer, Long, ...)");
        show(() -> Byte.MIN_VALUE);
        show(() -> Byte.MAX_VALUE);
        show(() -> Short.MIN_VALUE);
        show(() -> Short.MAX_VALUE);
        show(() -> Integer.MIN_VALUE);                                 // about -2.1 billion
        show(() -> Integer.MAX_VALUE);                                 // about +2.1 billion
        show(() -> Long.MIN_VALUE);                                    // about -9.2 quintillion
        show(() -> Long.MAX_VALUE);                                    // about +9.2 quintillion

        section("Overflow wraps around silently");
        show(() -> Integer.MAX_VALUE + 1);                             // wraps to Integer.MIN_VALUE, no exception
        show(() -> Integer.MAX_VALUE + 1L);                            // int + long = long, so no overflow
        show(() -> thrown(() -> Math.addExact(Integer.MAX_VALUE, 1))); // addExact throws instead of wrapping
        note("Interview trap: (low + high) / 2 can overflow; write low + (high - low) / 2");

        section("Division and remainder");
        syntax(
                "a / b", "int / int stays an int (the decimals are cut off)",
                "a % b", "remainder, keeps the sign of a",
                "Math.floorMod(a, b)", "modulo, never negative when b is positive"
        );
        show(() -> 7 / 2);                                             // int / int truncates toward zero
        show(() -> -7 / 2);                                            // toward zero, not floor
        show(() -> 7 % 3);
        show(() -> -7 % 3);                                            // % keeps the sign of the left side
        show(() -> Math.floorMod(-7, 3));                              // floorMod is never negative
        show(() -> 7 / 2.0);                                           // one double makes the result a double
        show(() -> thrown(() -> 7 / (i - 42)));                        // integer division by zero throws
        show(() -> 7 / (i - 42.0));                                    // double division by zero does NOT throw

        section("Bit operations");
        syntax(
                "a & b | a | b | a ^ b", "and | or | xor",
                "a << n | a >> n | a >>> n | ~a", "shift left | shift right (keeps sign) | shift right (zero fill) | flip every bit"
        );
        show(() -> 5 & 3);                                             // 0101 and 0011
        show(() -> 5 | 3);                                             // 0101 or 0011
        show(() -> 5 ^ 3);                                             // 0101 xor 0011
        show(() -> 5 << 1);                                            // shift left = multiply by 2
        show(() -> 5 >> 1);                                            // shift right = divide by 2
        show(() -> -8 >> 1);                                           // >> keeps the sign
        show(() -> -8 >>> 28);                                         // >>> fills with zeros: a big positive number
        show(() -> ~5);                                                // flip every bit
    }
}
