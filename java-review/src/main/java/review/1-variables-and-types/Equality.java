package review.variables;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 1c. EQUALITY: == vs equals(), null, arrays, collections, NaN
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.Equality
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class Equality {

    /** An enum: every constant exists exactly once, so == is the right test. */
    enum Light { RED, GREEN }

    /** A record: equals, hashCode and toString are generated from the components. */
    record DataPoint(int x, int y) {
    }

    /** A plain class: equals() is the one from Object, which is identity. */
    static class PlainPoint {
        final int x;
        final int y;

        PlainPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        title("EQUALITY  -  == compares identity, equals() compares content");

        section("== vs equals()");
        syntax(
                "a == b | a != b", "primitives: the VALUES; references: the very same object in memory",
                "a.equals(b)", "content comparison, if the class overrides equals (String, wrappers, records, collections do)",
                "Objects.equals(a, b)", "null-safe equals: true for two nulls, never a NullPointerException"
        );
        String lit = "java";
        String built = new String("java");
        show(() -> lit.equals(built));                                 // same characters
        show(() -> lit == built);                                      // TRAP: two different String objects
        show(() -> lit == "java");                                     // identical literals are ONE pooled object (StringType)
        Integer big1 = 1000;
        Integer big2 = 1000;
        show(() -> big1 == big2);                                      // TRAP: two Integer objects outside the cache -128..127
        show(() -> big1.equals(big2));                                 // compare wrappers with equals (WrapperTypes)
        Light light = Light.valueOf("RED");
        Light none = null;
        show(() -> light == Light.RED);                                // an enum constant exists once, so == is correct for enums
        show(() -> none == Light.RED);                                 // and == on null never throws (none.equals(...) would)

        section("null");
        String missing = null;
        show(() -> thrown(() -> missing.equals("Ann")));               // TRAP: calling equals on null throws NullPointerException
        show(() -> "Ann".equals(missing));                             // literal first: false, no exception
        show(() -> Objects.equals(missing, "Ann"));                    // null-safe on both sides
        show(() -> Objects.equals(missing, null));                     // two nulls are equal
        show(() -> missing == null);                                   // == null is the right test for null

        section("Record vs plain class");
        syntax("record P(int x, int y) { }", "equals / hashCode / toString are generated from the components");
        DataPoint d1 = new DataPoint(1, 2);
        DataPoint d2 = new DataPoint(1, 2);
        PlainPoint p1 = new PlainPoint(1, 2);
        PlainPoint p2 = new PlainPoint(1, 2);
        PlainPoint alias = p1;
        show(() -> d1.equals(d2));                                     // true: compares the components
        show(() -> d1 == d2);                                          // false: still two objects
        show(() -> p1.equals(p2));                                     // false: the default equals() is identity
        show(() -> p1.equals(alias));                                  // true: the same object
        Set<DataPoint> recordSet = new HashSet<>(List.of(d1, d2));
        show(() -> recordSet.size());                                  // 1: a record works as a Set element or a Map key
        Set<PlainPoint> plainSet = new HashSet<>(List.of(p1, p2));
        show(() -> plainSet.size());                                   // 2: two different objects
        note("A plain class needs its own equals AND hashCode to work in a HashSet or HashMap (lesson 12)");

        section("Collections and arrays");
        List<Integer> listA = List.of(1, 2);
        List<Integer> listB = new ArrayList<>(List.of(1, 2));
        show(() -> listA.equals(listB));                               // true: List equality compares the elements, not the class
        show(() -> listA.equals(List.of(2, 1)));                       // false: a List cares about order
        show(() -> Set.of(1, 2).equals(Set.of(2, 1)));                 // true: a Set ignores order
        show(() -> listA.equals(Set.of(1, 2)));                        // false: a List is never equal to a Set
        int[] xs = {1, 2};
        int[] ys = {1, 2};
        show(() -> xs == ys);                                          // false: identity
        show(() -> xs.equals(ys));                                     // TRAP: false too, arrays do NOT override equals
        show(() -> Arrays.equals(xs, ys));                             // compare the contents of arrays like this
        int[][] grid1 = {{1}, {2}};
        int[][] grid2 = {{1}, {2}};
        show(() -> Arrays.equals(grid1, grid2));                       // TRAP: false, the inner arrays are compared by identity
        show(() -> Arrays.deepEquals(grid1, grid2));                   // deepEquals for nested arrays
        StringBuilder sb1 = new StringBuilder("a");
        StringBuilder sb2 = new StringBuilder("a");
        show(() -> sb1.equals(sb2));                                   // TRAP: StringBuilder does not override equals either
        show(() -> sb1.toString().equals(sb2.toString()));

        section("Different types");
        show(() -> 1 == 1L);                                           // primitives are promoted to one type first
        show(() -> 1 == 1.0);
        show(() -> 'a' == 97);                                         // a char is a number
        show(() -> "1".equals(1));                                     // TRAP: compiles, always false (String vs Integer), nothing warns you
        wontCompile("Integer i = 1; String s = \"1\"; i == s", "incomparable types: Integer and String");
        wontCompile("boolean b = \"a\" == 1;", "bad operand types for binary operator '=='");
        note("Integer.equals(Long) is false too: equals() never converts types (WrapperTypes)");

        section("double: NaN and -0.0");
        show(() -> Double.NaN == Double.NaN);                          // false: IEEE says NaN is never equal to anything
        show(() -> Double.valueOf(Double.NaN).equals(Double.NaN));     // true: equals() treats NaN as equal to itself
        show(() -> 0.0 == -0.0);                                       // true: the IEEE comparison
        show(() -> Double.valueOf(0.0).equals(-0.0));                  // false: equals() tells them apart
        show(() -> List.of(Double.NaN).contains(Double.NaN));          // true: collections use equals(), so NaN is found
        Object nanA = Double.NaN;
        Object nanB = Double.NaN;
        show(() -> nanA.equals(nanB));                                 // true
        show(() -> nanA == nanB);                                      // false: two boxed objects
        note("Test for NaN with Double.isNaN(x) (DecimalTypes); compare doubles with a tolerance, not ==");
    }
}
