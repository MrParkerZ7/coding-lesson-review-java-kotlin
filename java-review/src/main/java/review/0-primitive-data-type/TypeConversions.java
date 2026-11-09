package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 0e. CONVERSIONS BETWEEN PRIMITIVE TYPES
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.TypeConversions
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class TypeConversions {

    /** x += d means x = (int) (x + d): the cast is hidden, the decimals are cut off. */
    private static int addDecimal(int x, double d) {
        x += d;
        return x;
    }

    /** b += n compiles although b + n is an int: the compound assignment casts back to byte. */
    private static byte addToByte(byte b, int n) {
        b += n;
        return b;
    }

    public static void main(String[] args) {
        title("TYPE CONVERSIONS");

        int i = 42;

        section("Widening is automatic, narrowing needs a cast");
        syntax(
                "byte -> short -> int -> long -> float -> double", "widening: implicit (int or long to float can still lose digits, see below)",
                "char -> int", "a char widens to int, long, float, double",
                "(Type) value", "narrowing cast: you take the risk"
        );
        long l = i;          show(() -> l);                            // int -> long: implicit
        double d = l;        show(() -> d);                            // long -> double: implicit
        int code = 'A';      show(() -> code);                         // char -> int: implicit
        float lost = 16_777_217;   show(() -> lost);                   // int -> float is implicit but float has only 24 bits of precision
        wontCompile("long big = 5; int j = big;", "possible lossy conversion from long to int");
        wontCompile("int k = 3.9;", "possible lossy conversion from double to int");

        section("Casting narrows (and may change the value)");
        show(() -> (int) 3.9);                                         // decimals are cut off, toward zero
        show(() -> (int) -3.9);                                        // -3, not -4
        show(() -> (byte) 200);                                        // whole numbers WRAP: 200 - 256
        show(() -> (byte) 300);                                        // 300 - 256 = 44
        show(() -> (short) 70_000);                                    // 70000 - 65536
        show(() -> (int) 3_000_000_000L);                              // keeps only the low 32 bits
        show(() -> (int) 1e20);                                        // decimals SATURATE at Integer.MAX_VALUE instead
        show(() -> (long) 3.9e18);                                     // fits in a long
        show(() -> thrown(() -> Math.toIntExact(3_000_000_000L)));     // toIntExact throws instead of wrapping
        note("Cast = cut: whole number to smaller whole number wraps; decimal to whole number truncates and saturates");

        section("Result type of mixed arithmetic");
        syntax(
                "byte, short, char -> int", "operands smaller than int are first promoted to int",
                "int + long -> long | int + double -> double", "the wider type wins"
        );
        byte x = 10;
        byte y = 20;
        show(() -> x + y);                                             // byte + byte = int 30 (type column: Integer)
        show(() -> (byte) (x + y));                                    // cast back to get a byte
        wontCompile("byte sum = x + y;", "possible lossy conversion from int to byte");
        note("final byte fx = 10, fy = 20; byte sum = fx + fy; compiles: constants whose result fits need no cast");
        show(() -> i + l);                                             // int + long = long
        show(() -> i + 1.5);                                           // int + double = double
        show(() -> i / 5);                                             // int / int = int: the decimals are lost
        show(() -> i / 5.0);                                           // one double side is enough
        show(() -> (double) i / 5);                                    // the cast binds tighter than /: 8.4
        show(() -> (double) (i / 5));                                  // TRAP: divide first (8), convert after (8.0)
        double avg = 7 / 2;  show(() -> avg);                          // TRAP: 7 / 2 is already 3 when it is widened to 3.0
        long wrong = 1_000_000 * 1_000_000;   show(() -> wrong);       // TRAP: the multiplication overflows as int BEFORE widening
        long right = 1_000_000L * 1_000_000;  show(() -> right);       // make one operand long first
        show(() -> true ? 1 : 2.0);                                    // TRAP: the ?: operands are promoted together: 1.0, not 1

        section("Compound assignment hides a cast");
        syntax("x += y;  is  x = (Type) (x + y);", "so += compiles where x = x + y does not");
        show(() -> addToByte((byte) 120, 10));                         // 130 does not fit in a byte: wraps to -126, no error
        show(() -> addDecimal(5, 3.7));                                // 5 + 3.7 = 8.7 cut to 8
        wontCompile("byte b = 1; b = b + 5;", "possible lossy conversion from int to byte");
        note("javac -Xlint:lossy-conversions (Java 20+) warns about the hidden cast of += (off by default)");

        section("From String and to String");
        syntax(
                "Integer.parseInt(s) | Double.parseDouble(s)", "text -> primitive (Boolean.parseBoolean: BooleanType)",
                "String.valueOf(x) | Integer.toString(x) | \"\" + x", "primitive -> text"
        );
        show(() -> Integer.parseInt("42"));
        show(() -> Integer.parseInt("+5"));                            // a leading + or - is allowed
        show(() -> Double.parseDouble("3.5"));
        show(() -> thrown(() -> Integer.parseInt("4x")));              // not a number: NumberFormatException (there is no null result)
        show(() -> thrown(() -> Integer.parseInt(" 42")));             // TRAP: parseInt does NOT trim spaces
        show(() -> Double.parseDouble(" 3.5 "));                       // but parseDouble does
        show(() -> Integer.parseInt("ff", 16));                        // second argument = radix
        show(() -> Integer.toBinaryString(10));
        show(() -> Integer.toHexString(255));
        show(() -> String.valueOf(i));

        section("Handy helpers");
        show(() -> Math.max(3, 9));
        show(() -> Math.min(3, 9));
        show(() -> Math.clamp(15, 0, 10));                             // Java 21: clamp into a range
        show(() -> Math.abs(-5));
        show(() -> Math.abs(Integer.MIN_VALUE));                       // TRAP: still negative, +2147483648 does not fit in an int
        show(() -> Integer.compare(10, 5));                            // 1 = bigger, 0 = equal, -1 = smaller (never a - b: overflow)
    }
}
