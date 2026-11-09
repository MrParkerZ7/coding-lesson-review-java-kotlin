package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 0g. BIG NUMBERS: BigInteger, BigDecimal (java.math: no operators, every operation is a method call)
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.BigNumbers
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class BigNumbers {

    /** n! as a BigInteger: a long overflows at 21!. */
    private static BigInteger factorial(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    public static void main(String[] args) {
        title("BIG NUMBERS  -  BigInteger and BigDecimal");

        section("BigInteger: whole numbers that never overflow");
        syntax(
                "new BigInteger(\"123\") | BigInteger.valueOf(123L)", "create from a String or a long",
                "a.add(b) | subtract | multiply | divide | mod | pow(n)", "no operators in Java: every operation is a method",
                "BigInteger.ZERO | ONE | TWO | TEN", "ready-made constants"
        );
        BigInteger max = BigInteger.valueOf(Long.MAX_VALUE);
        show(() -> Long.MAX_VALUE + 1);                                // long wraps around
        show(() -> max.add(BigInteger.ONE));                           // BigInteger just grows
        BigInteger huge = BigInteger.TWO.pow(100);
        show(() -> huge);                                              // 2^100 does not fit in a long
        show(() -> thrown(huge::longValueExact));                      // longValue() would silently cut the bits, longValueExact throws
        show(() -> factorial(25));                                     // a long overflows at 21!
        BigInteger twelveA = new BigInteger("12");
        BigInteger twelveB = new BigInteger("12");
        show(() -> twelveA.equals(twelveB));                           // equals compares the value
        show(() -> twelveA == twelveB);                                // TRAP: == compares the objects
        note("BigInteger is immutable: a.add(b) alone changes nothing, the result must be used (x = x.add(b))");

        section("BigDecimal: exact decimals (money!)");
        syntax(
                "new BigDecimal(\"0.1\")", "from a String: exact, the safe way",
                "BigDecimal.valueOf(0.1)", "from a double through its text \"0.1\": fine",
                "new BigDecimal(0.1)", "from a double directly: keeps the double's error"
        );
        BigDecimal tenth = new BigDecimal("0.1");
        BigDecimal fifth = new BigDecimal("0.2");
        show(() -> 0.1 + 0.2);                                         // double: not exact
        show(() -> tenth.add(fifth));                                  // exactly 0.3 when built from Strings
        show(() -> BigDecimal.valueOf(0.1));                           // valueOf(double) goes through the text "0.1": fine
        show(() -> new BigDecimal(0.1));                               // TRAP: built from a double it shows the double's real value
        note("Rule: build a BigDecimal from a String (or valueOf), never from a double literal");

        section("Scale: equals() and compareTo() disagree");
        syntax("a.compareTo(b) == 0", "equal in VALUE; equals() also compares the scale (number of decimals)");
        BigDecimal two1 = new BigDecimal("2.0");
        BigDecimal two2 = new BigDecimal("2.00");
        show(() -> two1.equals(two2));                                 // TRAP: equals() compares the scale too
        show(() -> two1.compareTo(two2));                              // 0 means equal in value: use compareTo
        show(() -> two2.scale());                                      // 2 digits after the point
        BigDecimal half = new BigDecimal("2.50");
        show(() -> half.stripTrailingZeros());                         // drops the useless zero
        BigDecimal hundred = new BigDecimal("100");
        show(() -> hundred.stripTrailingZeros());                      // TRAP: prints in scientific notation 1E+2
        show(() -> hundred.stripTrailingZeros().toPlainString());      // toPlainString() never uses an exponent
        Set<BigDecimal> hashed = new HashSet<>(List.of(two1, two2));
        show(() -> hashed.size());                                     // HashSet uses equals/hashCode: both are kept
        Set<BigDecimal> sorted = new TreeSet<>(List.of(two1, two2));
        show(() -> sorted.size());                                     // TreeSet uses compareTo: one element

        section("Division needs a rounding rule");
        syntax(
                "a.divide(b, scale, RoundingMode.HALF_UP)", "divide and round to `scale` decimals in one step",
                "x.setScale(2, RoundingMode.HALF_UP)", "round to 2 decimals (HALF_UP = school rounding)"
        );
        BigDecimal three = new BigDecimal(3);
        show(() -> thrown(() -> BigDecimal.ONE.divide(three)));        // 1/3 never ends: ArithmeticException
        show(() -> BigDecimal.ONE.divide(three, 5, RoundingMode.HALF_UP));   // 5 decimals, round half up
        BigDecimal four = new BigDecimal(4);
        show(() -> BigDecimal.TEN.divide(four, RoundingMode.HALF_UP)); // TRAP: keeps the dividend's scale (0): 2.5 becomes 3
        BigDecimal tie = new BigDecimal("2.345");
        show(() -> tie.setScale(2, RoundingMode.HALF_UP));             // school rounding
        show(() -> tie.setScale(2, RoundingMode.HALF_EVEN));           // banker's rounding: ties go to the even digit
        show(() -> thrown(() -> tie.setScale(2)));                     // no rounding mode and digits would be lost
        note("Money: BigDecimal built from Strings, setScale(2, HALF_UP) at the end; or keep long cents");

        section("Money example");
        BigDecimal price = new BigDecimal("19.99");
        show(() -> price.multiply(BigDecimal.valueOf(3)));             // 59.97 exactly (scales add: 2 + 0)
        BigDecimal withTax = price.multiply(new BigDecimal("1.10"));
        show(() -> withTax);                                           // 4 decimals after a multiply
        show(() -> withTax.setScale(2, RoundingMode.HALF_UP));         // round once, at the end
        show(() -> price.doubleValue() * 100);                         // double: 1998.9999999999998
        show(() -> price.multiply(BigDecimal.valueOf(100)));           // BigDecimal: exactly 1999.00
    }
}
