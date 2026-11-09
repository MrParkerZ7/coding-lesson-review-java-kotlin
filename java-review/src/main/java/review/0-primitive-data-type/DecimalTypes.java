package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 0b. DECIMAL TYPES: double, float
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.DecimalTypes
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class DecimalTypes {

    public static void main(String[] args) {
        title("DECIMAL TYPES  -  double, float");

        section("Declaring and literals");
        syntax(
                "double name = 3.14;", "double: 64 bit, the default for decimals",
                "float name = 3.14f;", "float: 32 bit, needs the f suffix",
                "1.5e3 | 2d | 2f", "scientific notation (1.5 x 10^3) | double suffix | float suffix"
        );
        double d = 3.14;     show(() -> d);                            // double: 64 bit, the DEFAULT for decimals
        float f = 3.14f;     show(() -> f);                            // float: 32 bit, needs the f suffix
        show(() -> 1.5e3);                                             // scientific notation
        wontCompile("float f = 3.14;", "possible lossy conversion from double to float: write 3.14f");
        note("Java 17+: every floating-point expression is strict IEEE 754, so the old strictfp keyword does nothing (javac warns)");

        section("Precision: never compare decimals with ==");
        syntax("Math.abs(a - b) < 1e-9", "compare decimals with a tolerance");
        show(() -> 0.1 + 0.2);                                         // binary fractions cannot be exact
        show(() -> 0.1 + 0.2 == 0.3);                                  // so == fails
        show(() -> Math.abs((0.1 + 0.2) - 0.3) < 1e-9);                // compare with a tolerance instead
        show(() -> 0.1f == 0.1);                                       // float 0.1f is widened to double: a different number
        show(() -> (double) 0.1f);                                     // the float error becomes visible after widening
        show(() -> 3 * 0.1);                                           // the same error again: 0.30000000000000004
        show(() -> (int) ((0.1 + 0.7) * 10));                          // TRAP: 7.999999999999999 cut to 7, not 8

        section("float keeps about 7 digits, double about 15-16");
        show(() -> 3.14159265358979f);                                 // float: the rest of the digits are lost
        show(() -> 3.14159265358979);                                  // double: all of these digits survive
        show(() -> 1.0f / 3);                                          // float: 8 digits
        show(() -> 1.0 / 3);                                           // double: 16 digits
        show(() -> 16_777_216f + 1f);                                  // float cannot hold 16,777,217: the +1 is lost
        note("Use double by default; float only for big arrays or graphics where memory matters");

        section("Limits");
        show(() -> Float.MAX_VALUE);
        show(() -> Double.MAX_VALUE);
        show(() -> -Double.MAX_VALUE);                                 // the most negative double
        show(() -> Double.MIN_VALUE);                                  // TRAP: the smallest POSITIVE double, not the most negative
        show(() -> Double.MAX_VALUE * 2);                              // overflow gives Infinity, no exception

        section("Special values");
        syntax("Double.isNaN(x) | isInfinite(x) | isFinite(x)", "the reliable tests for these values");
        show(() -> 1 / 0.0);                                           // positive infinity (no exception for decimals)
        show(() -> -1 / 0.0);                                          // negative infinity
        show(() -> 0.0 / 0.0);                                         // NaN = not a number
        show(() -> Double.NaN == Double.NaN);                          // NaN is not equal to itself
        show(() -> Double.isNaN(0.0 / 0.0));                           // use isNaN() to test
        show(() -> Math.sqrt(-1));                                     // invalid math gives NaN, not an exception
        show(() -> Double.compare(0.0, -0.0));                         // 1: compare() puts 0.0 above -0.0 though 0.0 == -0.0
        show(() -> Double.compare(Double.NaN, 1.0));                   // 1: compare() puts NaN above every number
        note("== versus equals() on NaN and -0.0: lesson 1, Equality");

        section("Converting and rounding");
        syntax(
                "(int) x", "cut off the decimals (toward zero)",
                "Math.round(x)", "nearest whole number, .5 goes UP; returns long for double, int for float",
                "Math.floor(x) | Math.ceil(x) | Math.rint(x)", "down | up | nearest with .5 to the even number (all return double)",
                "String.format(\"%.2f\", x)", "text with 2 decimals (lesson 1, TextAndFormatting)"
        );
        show(() -> (int) 3.99);                                        // truncates toward zero
        show(() -> (int) -3.99);                                       // toward zero, not floor
        show(() -> Math.round(2.5));                                   // half up: 3 (a long)
        show(() -> Math.round(-2.5));                                  // TRAP: half up means toward +infinity: -2, not -3
        show(() -> Math.round(2.5f));                                  // a float argument returns an int
        show(() -> Math.rint(2.5));                                    // half to EVEN: 2.0
        show(() -> Math.rint(3.5));                                    // half to even again: 4.0
        show(() -> Math.floor(-2.5));                                  // toward minus infinity
        show(() -> Math.ceil(-2.5));                                   // toward plus infinity: -2.0
        show(() -> (int) Double.NaN);                                  // a cast of NaN gives 0
        show(() -> (int) 1e20);                                        // too big: saturates at Integer.MAX_VALUE (no wrap-around)
    }
}
