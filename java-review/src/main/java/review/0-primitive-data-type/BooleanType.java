package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;

/**
 * 0c. BOOLEAN
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.BooleanType
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class BooleanType {

    /** Records which side was evaluated, so short-circuiting becomes visible. */
    private static final List<String> LOG = new ArrayList<>();

    /** Logs its name and returns true. */
    private static boolean yes(String name) {
        LOG.add(name);
        return true;
    }

    /** Logs its name and returns false. */
    private static boolean no(String name) {
        LOG.add(name);
        return false;
    }

    public static void main(String[] args) {
        title("BOOLEAN");

        section("Logic operators");
        syntax(
                "boolean flag = true;", "only two values: true and false (default of a field: false)",
                "a && b | a || b | !a | a ^ b", "AND | OR | NOT | XOR",
                "a & b | a | b", "AND | OR without short-circuit (both sides always run)"
        );
        show(() -> true && false);                                     // AND
        show(() -> true || false);                                     // OR
        show(() -> !true);                                             // NOT
        show(() -> true ^ false);                                      // XOR: true when exactly one side is true
        show(() -> true ^ true);                                       // equal sides give false

        section("Short-circuit: && and || stop early, & and | do not");
        syntax("String s = ...; s != null && s.length() > 3", "the classic guard: the right side only runs when it is safe");
        String text = null;
        show(() -> text != null && text.length() > 3);                 // right side is skipped when text is null: no crash
        show(() -> thrown(() -> text.length() > 3));                   // without the guard: NullPointerException
        note("yes(name) and no(name) log their name and return true / false: the log shows which sides ran");
        LOG.clear();
        boolean a1 = no("L") && yes("R");   show(() -> LOG);           // && skips R: false already decides the answer
        LOG.clear();
        boolean a2 = no("L") & yes("R");    show(() -> LOG);           // & runs BOTH sides
        LOG.clear();
        boolean o1 = yes("L") || yes("R");  show(() -> LOG);           // || skips R: true already decides the answer
        LOG.clear();
        boolean o2 = yes("L") | yes("R");   show(() -> LOG);           // | runs BOTH sides
        note("Use && and || in conditions; & and | on booleans are for when the right side must always run");

        section("Comparisons return boolean");
        syntax("a > b | a >= b | a < b | a <= b | a == b | a != b", "every comparison is a boolean");
        show(() -> 5 > 3);
        show(() -> 5 == 5);
        show(() -> 5 != 5);
        show(() -> 'a' < 'b');                                         // chars compare by their code
        show(() -> 1 < 2 == true);                                     // < binds tighter than ==, so this is (1 < 2) == true
        wontCompile("if (1) { }", "incompatible types: int cannot be converted to boolean (no truthy/falsy values)");
        wontCompile("String s = \"a\"; if (s) { }", "incompatible types: String cannot be converted to boolean");
        wontCompile("int x = 5; if (x = 6) { }", "incompatible types: int cannot be converted to boolean");
        note("A typo like if (flag = true) compiles ONLY for a boolean flag: it assigns true and is always true");

        section("From text: Boolean.parseBoolean never fails");
        syntax(
                "Boolean.parseBoolean(s)", "true only for \"true\" in any case, everything else is false",
                "Boolean.valueOf(s) | Boolean.toString(b)", "wrapper object from text | text from a boolean"
        );
        show(() -> Boolean.parseBoolean("true"));
        show(() -> Boolean.parseBoolean("TRUE"));                      // case does not matter
        show(() -> Boolean.parseBoolean("yes"));                       // TRAP: no exception, just false
        show(() -> Boolean.parseBoolean("1"));                         // false as well
        show(() -> Boolean.parseBoolean(null));                        // null is false too
        show(() -> Boolean.toString(5 > 3));
        note("Boolean (the wrapper) can be null: unboxing a null Boolean throws NullPointerException (WrapperTypes)");
    }
}
