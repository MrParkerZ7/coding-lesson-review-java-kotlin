package review.variables;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 1d. ORDER: compareTo, the static compare methods, range checks (equality is 1c)
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.ComparingValues
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ComparingValues {

    public static void main(String[] args) {
        title("COMPARING VALUES  -  compareTo and the compare methods");

        section("compareTo: negative, zero or positive");
        syntax(
                "a.compareTo(b)", "negative if a < b, 0 if equal, positive if a > b: trust only the SIGN",
                "Integer.compare(a, b) | Double.compare | Boolean.compare | Character.compare", "static versions for primitives"
        );
        show(() -> Integer.compare(3, 7));
        show(() -> Integer.valueOf(3).compareTo(7));
        show(() -> "apple".compareTo("cherry"));                       // -2: the difference of the first different chars
        show(() -> "a".compareTo("A"));                                // 32: lowercase sorts AFTER uppercase
        show(() -> "a".compareToIgnoreCase("A"));                      // 0
        show(() -> Boolean.compare(true, false));                      // true is bigger than false
        show(() -> Double.compare(Double.NaN, 1.0));                   // 1: compare() puts NaN above every number
        show(() -> Double.compare(0.0, -0.0));                         // 1: and 0.0 above -0.0, though 0.0 == -0.0 is true

        section("Sorting text");
        List<String> fruit = List.of("banana", "Cherry", "apple");
        List<String> natural = new ArrayList<>(fruit);
        Collections.sort(natural);
        show(() -> natural);                                           // TRAP: uppercase letters sort before lowercase ones
        List<String> ignoreCase = new ArrayList<>(fruit);
        ignoreCase.sort(String.CASE_INSENSITIVE_ORDER);
        show(() -> ignoreCase);                                        // case-insensitive order
        note("Comparable, Comparator and the a - b overflow trap: lesson 13, ComparableAndComparator");

        section("Range checks and clamping");
        syntax("lo <= x && x <= hi", "Java has no range operator (no 1..10): write both comparisons");
        int x = 7;
        show(() -> 1 <= x && x <= 10);                                 // x is in 1..10
        show(() -> 'q' >= 'a' && 'q' <= 'z');
        show(() -> Math.clamp(x, 1, 5));                               // Java 21: clamp into a range
        show(() -> Math.max(1, Math.min(x, 5)));                       // the same before Java 21
        note("A loop range is written with a condition (for i < n) or IntStream.range: lesson 2");
    }
}
