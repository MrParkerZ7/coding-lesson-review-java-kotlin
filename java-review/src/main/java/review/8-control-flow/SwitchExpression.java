package review.control;

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
 * 8b. SWITCH: the old statement with fall-through, and the arrow switch EXPRESSION (Java 14)
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.SwitchExpression
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class SwitchExpression {

    enum Light { RED, YELLOW, GREEN }

    /** Old style: colon labels. Without break the code FALLS THROUGH into the next label. */
    static List<String> oldFallThrough(int n) {
        List<String> out = new ArrayList<>();
        switch (n) {
            case 1:
                out.add("one");
            case 2:
                out.add("two");
            case 3:
                out.add("three");
                break;
            default:
                out.add("other");
        }
        return out;
    }

    /** Arrow style: each branch ends by itself. */
    static List<String> arrowNoFallThrough(int n) {
        List<String> out = new ArrayList<>();
        switch (n) {
            case 1 -> out.add("one");
            case 2 -> out.add("two");
            case 3 -> out.add("three");
            default -> out.add("other");
        }
        return out;
    }

    /** Several values per branch. There are no ranges in a case label. */
    static String dayKind(int d) {
        return switch (d) {
            case 6, 7 -> "weekend";
            case 1, 2, 3, 4, 5 -> "weekday";
            default -> "invalid";
        };
    }

    /** A String selector is compared with equals, so "start" and "go" can share a branch. */
    static String command(String cmd) {
        return switch (cmd) {
            case "start", "go" -> "starting";
            case "stop" -> "stopping";
            default -> "unknown";
        };
    }

    static String letterKind(char c) {
        char lower = Character.toLowerCase(c);
        return switch (lower) {
            case 'a', 'e', 'i', 'o', 'u' -> "vowel";
            default -> lower >= 'a' && lower <= 'z' ? "consonant" : "other";
        };
    }

    /** A block branch: yield gives the value of the block. */
    static int price(String code) {
        return switch (code) {
            case "A" -> {
                int base = 10;
                yield base * 2;
            }
            case "B" -> 5;
            default -> 0;
        };
    }

    /** The old colon style also works as an expression: yield takes the place of break. */
    static String oldYield(int n) {
        return switch (n) {
            case 1:
                yield "one";
            case 2:
                yield "two";
            default:
                yield "many";
        };
    }

    /** A branch can throw instead of giving a value. */
    static int mustBePositive(int n) {
        return switch (Integer.signum(n)) {
            case 1 -> n;
            default -> throw new IllegalArgumentException("not positive: " + n);
        };
    }

    /** Every enum constant listed: no default needed. */
    static Light nextLight(Light l) {
        return switch (l) {
            case RED -> Light.GREEN;
            case GREEN -> Light.YELLOW;
            case YELLOW -> Light.RED;
        };
    }

    public static void main(String[] args) {
        title("SWITCH  -  from fall-through statement to arrow expression");

        section("Old switch: colon labels fall through");
        syntax(
                "switch (x) { case 1: ...; break; default: ... }", "old style: a label does NOT end by itself, break does",
                "switch (x) { case 1 -> ...; default -> ... }", "arrow style (Java 14): no fall-through, no break"
        );
        show(() -> oldFallThrough(1));                                  // TRAP: no break after case 1 and 2, so all three run
        show(() -> oldFallThrough(2));
        show(() -> oldFallThrough(3));
        show(() -> oldFallThrough(9));
        show(() -> arrowNoFallThrough(1));                              // only one: the arrow form cannot fall through
        wontCompile("case 1 -> 10;  case 2: yield 20;", "different case kinds used in the switch: pick arrows OR colons, not both");
        note("Prefer the arrow form. The colon form still shows up in old code and in interview puzzles");

        section("Switch as an expression");
        syntax(
                "var v = switch (x) { case a, b -> r1; default -> r2; };", "an expression: it gives a value, and every possible x needs a branch",
                "case a -> { stmts; yield value; }", "a block branch: yield gives the value of the block",
                "case a -> throw new X();", "a branch may throw instead"
        );
        show(() -> dayKind(7));                                         // 6, 7 -> several values in one branch
        show(() -> dayKind(3));
        show(() -> dayKind(9));
        show(() -> command("go"));                                      // Strings compare by value
        show(() -> letterKind('E'));
        show(() -> letterKind('z'));
        show(() -> letterKind('7'));
        show(() -> price("A"));                                         // 20: the block ran, yield base * 2 is its value
        show(() -> oldYield(2));
        show(() -> thrown(() -> mustBePositive(-3)));
        show(() -> thrown(() -> command(null)));                        // TRAP: a null selector throws (case null: next file)
        note("No ranges in a case label (Kotlin has in 1..5): list the values, or use if / else if, or a guard (next file)");

        section("Exhaustive: a default, or every enum constant");
        wontCompile("int r = switch (n) { case 1 -> 10; };", "a switch EXPRESSION must cover every value: add default");
        wontCompile("switch (big) { ... }   // long", "selector type long is not allowed (also float, double and boolean)");
        wontCompile("switch (flag) { ... }   // boolean", "selector type boolean is not allowed: use if / else");
        wontCompile("case 1 -> ...;  case 1 -> ...;", "duplicate case label");
        wontCompile("int k = 2; switch (n) { case k -> ... }", "constant expression required: k is a variable (final int k = 2 would be a constant and compile)");
        show(() -> nextLight(Light.RED));
        show(() -> nextLight(Light.YELLOW));
        note("Add a Light constant and every switch EXPRESSION without default stops compiling. A default would hide it");
        note("A Kotlin 'when' with no subject becomes an if / else if chain in Java (see sizeWord in 8a)");
    }
}
