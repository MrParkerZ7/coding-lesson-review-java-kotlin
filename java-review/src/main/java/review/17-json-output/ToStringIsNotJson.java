package review.json;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 17a. toString() IS NOT JSON
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.ToStringIsNotJson
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ToStringIsNotJson {

    /** A plain class without its own toString: the default is class name + @ + hash code. */
    static class Point {
        int x = 1;
        int y = 2;
    }

    public static void main(String[] args) {
        title("TO JSON  -  System.out.println(obj) is not JSON");

        section("What println / toString gives you");
        User user = User.sample();
        show(() -> user);                                                  // record: User[id=1, ...], square brackets, NOT JSON
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", "x");
        show(() -> map);                                                   // Map: {a=1, b=x}  keys and strings have no quotes
        show(() -> List.of("a", "b"));                                     // List: [a, b]  the strings lost their quotes
        show(() -> user.address());                                        // nested records print the same way
        show(() -> user.toString().contains("\""));                        // no double quote anywhere: invalid JSON
        show(() -> new Point().toString().startsWith("review.json.ToStringIsNotJson$Point@"));   // a plain class: name@hash, the hash is not printed (it changes)

        section("What JSON needs");
        syntax(
                "{ \"key\": value, ... }", "object: every key is a double-quoted string, then a colon",
                "[ value, value ]", "array: commas BETWEEN items, never after the last",
                "\"text\"", "string: double quotes only; escape the quote, the backslash and newlines",
                "123 | 1.5 | true | false | null", "number, boolean, null: lowercase; no NaN, no Infinity"
        );
        note("Interview trap: a record prints User[id=1, name=Ann, ...]: that is Java, not JSON. Say it before you print.");
        note("The same is true of Object.toString(), Arrays.toString and every collection: they are for humans and logs");

        section("Three ways to get real JSON (the next files)");
        syntax(
                "HandWrittenJson", "no library: escape + a recursive toJson(value). Works on any judge, no imports",
                "ReflectionJson", "no library: read the fields with Java reflection, then reuse toJson",
                "LibraryJson", "Jackson | Gson: one call, but the library must be available"
        );
        note("On HackerRank-style judges assume NO library: learn the hand-written one first");
    }
}
