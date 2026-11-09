package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 0i. WRAPPER TYPES: Integer, Long, Double, Boolean, Character ... (the object version of each primitive)
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.WrapperTypes
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class WrapperTypes {

    /** Boxes the same number twice and compares the two objects with ==. */
    private static boolean sameObject(int n) {
        Integer first = n;
        Integer second = n;
        return first == second;
    }

    public static void main(String[] args) {
        title("WRAPPER TYPES  -  Integer, Long, Double, Boolean, Character");

        section("Primitive vs wrapper");
        syntax(
                "int | long | double | boolean | char | ...", "primitive: a plain value, never null, no methods",
                "Integer | Long | Double | Boolean | Character | ...", "wrapper: an object, can be null, has methods"
        );
        wontCompile("List<int> xs = new ArrayList<>();", "unexpected type, required: reference, found: int (generics need wrappers)");
        note("Collections hold wrappers: List<Integer>. A wrapper field that is never set is null, an int field is 0");

        section("Autoboxing and unboxing");
        syntax(
                "Integer boxed = 5;", "autoboxing: the compiler writes Integer.valueOf(5)",
                "int prim = boxed;", "unboxing: the compiler writes boxed.intValue()"
        );
        Integer boxed = 5;   show(() -> boxed);
        int prim = boxed;    show(() -> prim);
        show(() -> boxed + 1);                                         // arithmetic unboxes first: the result is an int
        show(() -> Integer.valueOf("42"));                             // valueOf gives an Integer ...
        show(() -> Integer.parseInt("42"));                            // ... parseInt gives an int (both look the same here)
        wontCompile("Long big = 5;", "incompatible types: int cannot be converted to Long (write 5L)");
        wontCompile("Double d = 1;", "incompatible types: int cannot be converted to Double (write 1.0)");
        note("new Integer(5) is deprecated for removal (javac warns): use Integer.valueOf(5) or autoboxing");

        section("TRAP: unboxing null is a NullPointerException");
        Integer missing = null;
        Map<String, Integer> counts = new HashMap<>();
        counts.put("a", 1);
        show(() -> thrown(() -> missing + 1));                         // unboxing null
        show(() -> thrown(() -> counts.get("zzz") + 1));               // Map.get returns null for a missing key
        show(() -> counts.getOrDefault("zzz", 0) + 1);                 // the safe way
        Boolean flag = null;
        show(() -> thrown(() -> flag ? "yes" : "no"));                 // a null Boolean in a condition

        section("TRAP: == on wrappers compares objects, and small values are cached");
        syntax("a.equals(b) | Objects.equals(a, b)", "compare wrapper VALUES (the second one is null-safe)");
        show(() -> sameObject(127));                                   // the JVM caches boxed Integers from -128 to 127
        show(() -> sameObject(128));                                   // outside the cache every boxing makes a new object
        show(() -> sameObject(-128));                                  // lowest cached value
        show(() -> sameObject(-129));
        Integer big1 = 1000;
        Integer big2 = 1000;
        show(() -> big1 == big2);                                      // two objects
        show(() -> big1.equals(big2));                                 // same value
        show(() -> big1 == 1000);                                      // Integer == int unboxes: compares values
        show(() -> Long.valueOf(127L) == Long.valueOf(127L));          // Long caches -128..127 too
        show(() -> Long.valueOf(128L) == Long.valueOf(128L));
        show(() -> Double.valueOf(1.0) == Double.valueOf(1.0));        // Double never caches
        show(() -> Boolean.valueOf(true) == Boolean.valueOf(true));    // Boolean has just two objects
        note("Never compare wrappers with ==; the cache makes a wrong program pass its small tests");

        section("equals() never converts between types");
        show(() -> Integer.valueOf(1).equals(1));                      // the int is boxed to Integer: same type, true
        show(() -> Integer.valueOf(1).equals(1L));                     // TRAP: a Long is not an Integer: false
        show(() -> Long.valueOf(1L).equals(1));
        show(() -> Objects.equals(1, 1L));
        Integer one = 1;
        show(() -> one == 1L);                                         // but Integer == long unboxes and widens: true
        wontCompile("Integer i = 1; Long l = 1L; boolean b = i == l;", "incomparable types: Integer and Long");
        Map<Integer, String> names = Map.of(1, "one");
        show(() -> names.get(1));
        show(() -> names.get(1L));                                     // TRAP: Long key on an Integer map: null, no error
        List<Integer> numbers = new ArrayList<>(List.of(10, 20, 30));
        show(() -> numbers.contains(10L));                             // false for the same reason
        wontCompile("List<Long> xs = ...; xs.add(5);", "incompatible types: int cannot be converted to Long (write 5L)");

        section("TRAP: list.remove(int) vs list.remove(Object)");
        syntax("list.remove(1) | list.remove(Integer.valueOf(1))", "remove the element AT index 1 | remove the VALUE 1");
        List<Integer> byIndex = new ArrayList<>(List.of(10, 20, 30));
        byIndex.remove(1);   show(() -> byIndex);                      // int argument: remove(int index) removes 20
        List<Integer> byValue = new ArrayList<>(List.of(10, 20, 30));
        byValue.remove(Integer.valueOf(10));   show(() -> byValue);    // Integer argument: remove(Object) removes the value 10
        List<Integer> byCast = new ArrayList<>(List.of(10, 20, 30));
        byCast.remove((Integer) 10);   show(() -> byCast);             // a cast to Integer picks remove(Object) as well
        int[] prim3 = {1, 2, 3};
        show(() -> Arrays.asList(prim3).size());                       // TRAP: a list with ONE element, the whole int[] (no boxing of arrays)
        show(() -> Arrays.asList(1, 2, 3).size());

        section("Cost");
        note("A boxed Integer is a separate heap object plus a reference to it: int[] is far smaller and faster than List<Integer>");
        note("Integer total = 0; total += i; in a loop boxes a new Integer every round (above 127 each one is a new object): use an int accumulator");
    }
}
