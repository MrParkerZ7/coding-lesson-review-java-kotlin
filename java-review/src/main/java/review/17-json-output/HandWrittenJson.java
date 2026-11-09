package review.json;

import static review.Output.bigO;
import static review.Output.block;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 17b. JSON BY HAND  (no library)
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.HandWrittenJson
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class HandWrittenJson {

    enum Level { LOW, HIGH }

    /** Escape a string for JSON: the quote, the backslash and control characters. O(n). */
    public static String escapeJson(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < ' ') out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }

    /** Any value to compact JSON: null, Boolean, Number, String, Character, Enum, Map, Iterable, arrays. */
    public static String toJson(Object value) {
        return switch (value) {
            case null -> "null";
            case Boolean b -> b.toString();
            case Double d when d.isNaN() || d.isInfinite() -> "null";    // JSON has no NaN or Infinity
            case Float f when f.isNaN() || f.isInfinite() -> "null";
            case Number n -> n.toString();
            case String s -> "\"" + escapeJson(s) + "\"";
            case Character c -> "\"" + escapeJson(c.toString()) + "\"";
            case Enum<?> e -> "\"" + e.name() + "\"";
            case Map<?, ?> m -> {
                List<String> parts = new ArrayList<>();
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    parts.add("\"" + escapeJson(String.valueOf(entry.getKey())) + "\":" + toJson(entry.getValue()));
                }
                yield "{" + String.join(",", parts) + "}";
            }
            case Iterable<?> items -> {
                List<String> parts = new ArrayList<>();
                for (Object item : items) parts.add(toJson(item));
                yield "[" + String.join(",", parts) + "]";
            }
            case Object[] array -> toJson(Arrays.asList(array));          // Arrays.asList (not List.of) so null elements are allowed
            case int[] array -> {
                List<Integer> boxed = new ArrayList<>();
                for (int n : array) boxed.add(n);
                yield toJson(boxed);
            }
            default -> "\"" + escapeJson(value.toString()) + "\"";         // unknown type: its toString() as a string
        };
    }

    /** Same data, indented: two spaces per level. */
    public static String toPrettyJson(Object value, int indent) {
        String pad = "  ".repeat(indent + 1);
        String closePad = "  ".repeat(indent);
        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) return "{}";
            List<String> lines = new ArrayList<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                lines.add(pad + "\"" + escapeJson(String.valueOf(e.getKey())) + "\": " + toPrettyJson(e.getValue(), indent + 1));
            }
            return "{\n" + String.join(",\n", lines) + "\n" + closePad + "}";
        }
        if (value instanceof Iterable<?> items) {
            List<String> lines = new ArrayList<>();
            for (Object item : items) lines.add(pad + toPrettyJson(item, indent + 1));
            return lines.isEmpty() ? "[]" : "[\n" + String.join(",\n", lines) + "\n" + closePad + "]";
        }
        return toJson(value);
    }

    /** An object becomes a LinkedHashMap (keeps the order you write), then toJson does the rest. */
    static Map<String, Object> toMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.id());
        map.put("name", user.name());
        map.put("active", user.active());
        map.put("score", user.score());
        map.put("tags", user.tags());
        Address a = user.address();
        map.put("address", a == null ? null : toMap(a));
        return map;
    }

    static Map<String, Object> toMap(Address address) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("city", address.city());
        map.put("zip", address.zip());
        return map;
    }

    /** The other hand-written style: String.formatted with templates. Fine for one small class, tedious for many. */
    static String toJsonByHand(Address a) {
        return "{\"city\":%s,\"zip\":%s}".formatted(toJson(a.city()), toJson(a.zip()));
    }

    public static void main(String[] args) {
        title("JSON BY HAND  -  escape + one recursive function");

        section("1. Escape the text");
        syntax("\" -> \\\"   \\ -> \\\\   newline -> \\n   tab -> \\t   other control char -> \\u00XX", "what a JSON string may NOT contain raw");
        note("show wraps every String result in its own quotes: the OUTER pair is show's, the inner ones belong to the JSON");
        show(() -> escapeJson("say \"hi\""));                              // the quotes get a backslash
        show(() -> escapeJson("C:\\temp"));                                // a backslash becomes two
        show(() -> escapeJson("line1\nline2"));                            // a real newline becomes the two characters \n
        show(() -> escapeJson(String.valueOf((char) 1)));                  // control character 1 becomes a six-character escape
        note("Forgetting to escape is THE bug: a name like O\"Brien breaks the whole document");

        section("2. One recursive function for every value");
        syntax("String toJson(Object value) { return switch (value) { case null -> ...; case Map<?, ?> m -> ...; } }", "a pattern-matching switch over the type (Java 21); Map and Iterable call toJson again");
        show(() -> toJson(null));                                          // the literal null, no quotes
        show(() -> toJson(true));
        show(() -> toJson(42));
        show(() -> toJson("text"));                                        // strings get quotes
        show(() -> toJson('c'));                                           // Character is a string in JSON
        show(() -> toJson(Level.HIGH));                                    // enum: its name as a string
        show(() -> toJson(List.of(1, 2, 3)));
        show(() -> toJson(new int[] {1, 2, 3}));                           // int[] is not an Object[]: it needs its own case
        show(() -> toJson(new String[] {"x", "y"}));                       // Object[] covers every reference array
        show(() -> toJson(new Object[] {1, "two", null}));                 // a mixed array with a null inside
        show(() -> toJson(new ArrayList<>()));                             // [] : join of nothing is safe on empty
        show(() -> toJson(0.1 + 0.2));                                     // Double.toString: the shortest text that reads back the same
        show(() -> toJson(Double.NaN));                                    // JSON cannot hold NaN: this version writes null
        show(() -> toJson(1.0 / 0));                                       // Infinity: also null
        show(() -> toJson(9_007_199_254_740_993L));                        // exact here; JavaScript readers round it to ...992 (above 2^53)
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("a", 1);
        nested.put("b", List.of("x", "y"));
        show(() -> toJson(nested));                                        // maps and lists nest freely

        section("3. Your own class: build a Map, then toJson");
        User user = User.sample();
        show(() -> toJson(toMap(user)));                                   // the whole User on one line
        show(() -> toJson(toJsonByHand(user.address())));                  // a JSON text is a String, so it was quoted AGAIN: trap
        show(() -> toJsonByHand(user.address()));                          // the template style: correct for one nested class
        show(() -> toJson(user));                                          // TRAP: a record is not a Map: the default case quotes its toString()
        Map<String, Integer> insertion = new LinkedHashMap<>();
        insertion.put("b", 2);
        insertion.put("a", 1);
        show(() -> toJson(insertion));                                     // LinkedHashMap: insertion order
        show(() -> toJson(new TreeMap<>(insertion)));                      // TreeMap: sorted keys, stable output for a test
        show(() -> toJson(new HashMap<>(insertion)));                      // HashMap: order not guaranteed (hash order; a before b here)

        section("4. Pretty print");
        syntax("String.join(\",\\n\", lines)  with  pad = \"  \".repeat(indent + 1)", "one line per entry; indent grows by two spaces per level");
        block(toPrettyJson(toMap(user), 0));

        section("Cost");
        bigO(
                "toJson / toPrettyJson over n values", "O(n) time: every value is visited once",
                "StringBuilder / String.join", "O(n) total",
                "s += piece in a loop", "O(n^2): every + copies the whole string. Use StringBuilder"
        );
        note("Interview order: escape first, then the switch, then the object-to-Map step, then pretty print if asked");
    }
}
