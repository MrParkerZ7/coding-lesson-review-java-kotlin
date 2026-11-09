package review.control;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 8c. PATTERN MATCHING: instanceof patterns, switch patterns with guards, record patterns, sealed types (Java 21)
 *     (records and sealed interfaces themselves are lesson 11; here they are the thing being matched)
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.PatternMatching
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class PatternMatching {

    sealed interface Shape permits Circle, Rect, Dot {
    }

    record Circle(int r) implements Shape {
    }

    record Rect(int w, int h) implements Shape {
    }

    record Dot() implements Shape {
    }

    record Point(int x, int y) {
    }

    record Line(Point from, Point to) {
    }

    /** instanceof pattern plus a condition: s exists only where the test succeeded. */
    static String longText(Object o) {
        if (o instanceof String s && s.length() > 3) return "long text " + s.toUpperCase();
        return "something else";
    }

    /** Early exit with a negated test: afterwards s is in scope for the rest of the method. */
    static int lengthOrMinus(Object o) {
        if (!(o instanceof String s)) return -1;
        return s.length();
    }

    /** Type patterns, guards (when) and a null label. The first matching case wins. */
    static String describe(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i when i > 100 -> "big int " + i;
            case Integer i -> "int " + i;
            case String s when s.length() > 3 -> "long text " + s;
            case String s -> "short text " + s;
            case int[] arr -> "int array of " + arr.length;
            default -> "other " + o.getClass().getSimpleName();
        };
    }

    /** The same idea without a case null: a null selector throws. */
    static String describeNoNull(Object o) {
        return switch (o) {
            case String s -> "text";
            default -> "other";
        };
    }

    /** A sealed interface: every permitted subtype listed, so no default is needed. */
    static int corners(Shape s) {
        return switch (s) {
            case Circle c -> 0;
            case Rect r -> 4;
            case Dot d -> 0;
        };
    }

    /** Record patterns take the record apart in the pattern itself. */
    static String describeShape(Shape s) {
        return switch (s) {
            case Circle(int r) -> "circle of radius " + r;
            case Rect(int w, int h) when w == h -> "square " + w;
            case Rect(int w, int h) -> "rect " + w + "x" + h;
            case Dot() -> "dot";
        };
    }

    /** Nested record patterns: the inner points are taken apart too. */
    static String where(Object o) {
        return switch (o) {
            case Point(int x, int y) when x == 0 && y == 0 -> "origin";
            case Point(int x, int y) -> "point " + x + "," + y;
            case Line(Point(var x1, var y1), Point(var x2, var y2)) -> "line, squared length " + ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            default -> "unknown";
        };
    }

    public static void main(String[] args) {
        title("PATTERN MATCHING  -  test the type and take the value in one step");

        section("instanceof pattern");
        syntax(
                "o instanceof String s", "test AND cast in one step: s is a String wherever the test is true",
                "if (!(o instanceof String s)) return ...;", "after the early exit s is in scope for the rest of the method",
                "o instanceof Point(int x, int y)", "record pattern: also takes the record apart (Java 21)"
        );
        show(() -> longText("hello"));
        show(() -> longText("hi"));                                     // a String, but the condition after && fails
        show(() -> longText(42));
        show(() -> longText(null));                                     // null is never an instance: no exception
        show(() -> lengthOrMinus("kotlin"));
        show(() -> lengthOrMinus(5));
        Object point = new Point(3, 4);
        show(() -> point instanceof Point(int x, int y) && x + y == 7); // x and y exist inside the && chain
        wontCompile("if (o instanceof String s || s.length() > 3)", "s exists only where the test is TRUE; after || it may not be");

        section("switch with type patterns, guards and null");
        syntax(
                "case Type t -> ...", "type pattern: t has the Type inside the branch",
                "case Type t when cond -> ...", "a guard: the branch also needs cond (Java 21)",
                "case null -> ... | case null, default -> ...", "handle null inside the switch; without it a null selector throws",
                "default -> ...", "needed unless the compiler can see that every case is covered"
        );
        show(() -> describe("hello"));
        show(() -> describe("hi"));                                     // the guard failed, so the next String case matched
        show(() -> describe(7));
        show(() -> describe(700));
        show(() -> describe(new int[3]));
        show(() -> describe(2.5));
        show(() -> describe(null));
        show(() -> thrown(() -> describeNoNull(null)));                 // TRAP: no case null, so the null selector throws
        wontCompile("case CharSequence cs -> ...;  case String s -> ...;", "this case label is dominated by a preceding case label: specific type FIRST");
        note("Order matters: the first match wins, so specific before general, guarded before unguarded");

        section("Sealed types: exhaustive without default");
        show(() -> corners(new Rect(2, 3)));
        show(() -> corners(new Dot()));
        show(() -> describeShape(new Circle(2)));
        show(() -> describeShape(new Rect(2, 2)));                      // the guard w == h matches first
        show(() -> describeShape(new Rect(2, 3)));
        show(() -> describeShape(new Dot()));
        wontCompile("switch (shape) { case Circle c -> 1; }", "the switch expression does not cover all possible input values (Rect and Dot are missing)");
        note("Add a record to `permits` and every switch over the sealed type without default stops compiling: the compiler finds what you forgot");

        section("Record patterns, nested");
        show(() -> where(new Point(0, 0)));
        show(() -> where(new Point(2, 5)));
        show(() -> where(new Line(new Point(0, 0), new Point(3, 4))));  // 3 * 3 + 4 * 4
        show(() -> where("text"));
    }
}
