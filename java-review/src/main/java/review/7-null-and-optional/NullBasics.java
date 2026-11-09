package review.nullsafety;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import review.Output.Risky;

/**
 * 7a. NULL BASICS: where a NullPointerException comes from, and how to defend against it
 *     (Java has no null-safe types like Kotlin's String?: any reference may be null and the compiler does not check)
 *     Run: mvn -q compile exec:java -Dmain.class=review.nullsafety.NullBasics
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class NullBasics {

    /** The message of the exception a call throws, or "no exception". */
    static String messageOf(Risky call) {
        try {
            call.run();
            return "no exception";
        } catch (Throwable t) {
            return String.valueOf(t.getMessage());
        }
    }

    public static void main(String[] args) {
        title("NULL BASICS  -  where an NPE comes from, and how to defend against it");
        note("A primitive (int, boolean ...) can never be null. Every reference type (String, Integer, arrays, your classes) can");

        section("Where a NullPointerException comes from");
        syntax(
                "x.method() | x.field | x[i] | x.length", "a call, a field read or an index on null",
                "int n = integerOrNull", "unboxing null (also in arithmetic, a ternary, a method argument)",
                "switch (x) { ... }", "switching on a null String, enum or wrapper (a case null label is lesson 8)"
        );
        String name = null;
        int[] none = null;
        Integer boxed = null;
        show(() -> thrown(() -> name.length()));
        show(() -> thrown(() -> none[0]));
        show(() -> thrown(() -> boxed + 1));                            // unboxing: boxed.intValue() on null
        show(() -> thrown(() -> switch (name) { case "a" -> 1; default -> 0; }));
        show(() -> name == null);                                       // == null is always safe
        show(() -> name instanceof String);                             // false: instanceof is false for null, never an exception
        show(() -> name + "!");                                         // TRAP: concatenation prints the text null, no exception
        wontCompile("int n = null;", "a primitive can never be null; an Integer can");
        Map<String, Integer> stock = Map.of("apple", 3);
        show(() -> stock.get("pear"));                                  // a missing key gives null, even when the values are Integer
        show(() -> messageOf(() -> stock.get("pear") + 1));             // the helpful message (Java 15+) names the exact call that returned null

        section("Defending: Objects helpers and the yoda condition");
        syntax(
                "Objects.requireNonNull(x, \"message\")", "x itself, or NullPointerException with your message: fail fast at the door",
                "Objects.requireNonNullElse(x, other)", "x, or other when x is null (other must not be null)",
                "Objects.equals(a, b) | Objects.toString(x, dflt)", "null-safe equals | null-safe toString with a default",
                "\"a\".equals(x)", "known non-null value on the LEFT: no exception when x is null"
        );
        show(() -> messageOf(() -> Objects.requireNonNull(name, "name must not be null")));
        show(() -> Objects.requireNonNullElse(name, "anonymous"));
        show(() -> Objects.toString(name, "none"));
        show(() -> Objects.equals(name, null));                         // true: two nulls are equal
        show(() -> Objects.equals(name, "a"));                          // false, no exception
        show(() -> "a".equals(name));                                   // false: the receiver is the literal
        show(() -> thrown(() -> name.equals("a")));                     // the receiver is null: NullPointerException
        show(() -> String.valueOf((Object) name));                      // the text null
        show(() -> thrown(() -> String.valueOf(null)));                 // TRAP: the literal null picks the valueOf(char[]) overload, which throws

        section("The ternary and boxing trap");
        Integer maybe = null;
        boolean useIt = true;
        show(() -> thrown(() -> useIt ? maybe : 0));                    // TRAP: Integer and int mix, so the type is int and maybe is unboxed
        show(() -> useIt ? maybe : Integer.valueOf(0));                 // both sides Integer: no unboxing, null comes through
        Boolean flag = null;
        show(() -> thrown(() -> flag ? "yes" : "no"));                  // TRAP: a null Boolean used as a condition is unboxed too
        note("Prefer the primitive (int, boolean) wherever null makes no sense: it cannot be null");

        section("null in collections");
        syntax(
                "accepts null", "ArrayList, LinkedList, Arrays.asList, HashSet, HashMap (one null key), LinkedHashMap, stream().toList()",
                "rejects null (NPE)", "List.of, Set.of, Map.of, TreeMap and TreeSet keys, ArrayDeque, PriorityQueue, ConcurrentHashMap, Hashtable"
        );
        Map<String, Integer> hash = new HashMap<>();
        hash.put(null, 1);
        hash.put("a", null);
        show(() -> hash);                                               // null key and null value both stored
        show(() -> hash.containsKey("a"));                              // true: the key exists...
        show(() -> hash.get("a"));                                      // ...and its value is null
        show(() -> hash.getOrDefault("a", 9));                          // TRAP: null, not 9: getOrDefault only replaces a MISSING key
        show(() -> Arrays.asList(1, null));
        show(() -> thrown(() -> List.of(1, null)));                     // List.of refuses null at creation
        show(() -> thrown(() -> List.of(1, 2).contains(null)));         // TRAP: even asking contains(null) throws on an immutable list
        show(() -> thrown(() -> new TreeMap<String, Integer>().put(null, 1)));
        show(() -> thrown(() -> Map.of("a", null)));
        show(() -> thrown(() -> new ArrayDeque<String>().add(null)));
        show(() -> thrown(() -> new ConcurrentHashMap<String, Integer>().put("a", null)));
        note("Return an empty list or an empty String instead of null; use Optional for 'maybe a value' (next file)");
    }
}
