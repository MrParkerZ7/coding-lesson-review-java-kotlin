package review.modern;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedMap;
import java.util.SequencedSet;

/**
 * 18d. WHAT IS NEW IN JAVA 21 (LTS): pattern matching for switch, record patterns, sequenced collections, small APIs
 *      Run: mvn -q compile exec:java -Dmain.class=review.modern.Java21
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class Java21 {

    sealed interface Shape permits Circle, Rect {
    }

    record Circle(double radius) implements Shape {
    }

    record Rect(double width, double height) implements Shape {
    }

    record Point(int x, int y) {
    }

    record Line(Point from, Point to) {
    }

    /** Exhaustive over a sealed type: no default needed, and a new subtype would make this a compile error. */
    static String shape(Shape s) {
        return switch (s) {
            case Circle c -> "circle r=" + c.radius();
            case Rect r when r.width() == r.height() -> "square " + r.width();   // a guard: when <condition>
            case Rect r -> "rect " + r.width() + "x" + r.height();
        };
    }

    /** Type patterns on a plain Object need a default, and null gets its own case. */
    static String kind(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i when i > 10 -> "big int " + i;
            case Integer i -> "int " + i;
            case String s -> "string of " + s.length();
            case int[] a -> "int array of " + a.length;
            default -> "something else";
        };
    }

    /** The same idea WITHOUT case null: a null selector throws before any case is tried. */
    static String noNullCase(Object o) {
        return switch (o) {
            case String s -> "string";
            default -> "other";
        };
    }

    /** A record pattern takes the record apart; patterns can nest. */
    static int manhattan(Object o) {
        if (o instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
            return Math.abs(x2 - x1) + Math.abs(y2 - y1);
        }
        return -1;
    }

    public static void main(String[] args) {
        title("JAVA 21 (LTS)  -  patterns everywhere, sequenced collections");

        section("Version table: feature -> Java version");
        syntax(
                "Java 21  (LTS)", "pattern matching for switch, record patterns, virtual threads, sequenced collections (final)",
                "Java 21 APIs", "List.getFirst / getLast / reversed, Math.clamp, StringBuilder.repeat, String.indexOf(ch, from, to), splitWithDelimiters",
                "Java 21 PREVIEW", "string templates, unnamed patterns and variables (_), unnamed classes, scoped values, structured concurrency",
                "Java 25  (LTS)", "the next long-term release after 21"
        );

        section("Pattern matching for switch");
        syntax(
                "case Type t ->", "matches when the value is a Type; t is the cast value",
                "case Type t when cond ->", "a guard: the case also needs cond",
                "case null ->", "null is handled by the switch itself (without it a null throws NullPointerException)"
        );
        show(() -> shape(new Circle(1.5)));
        show(() -> shape(new Rect(2, 2)));                                 // the guarded case
        show(() -> shape(new Rect(2, 3)));
        show(() -> kind(42));
        show(() -> kind(7));
        show(() -> kind("abc"));
        show(() -> kind(new int[] {1, 2}));
        show(() -> kind(null));                                            // handled by case null
        show(() -> kind(2.5));                                             // default
        show(() -> thrown(() -> noNullCase(null)));                        // no case null: NullPointerException, even with a default
        wontCompile("int n = switch (s) { case Circle c -> 1; };", "the switch expression does not cover all possible input values (s is a Shape with two subtypes)");
        wontCompile("case CharSequence cs -> 1; case String s -> 2;", "this case label is dominated by a preceding case label");
        note("Order matters: a general case before a specific one is an error, so write the specific cases first");

        section("Record patterns");
        syntax("o instanceof Line(Point(var x1, var y1), Point(var x2, var y2))", "test the type AND extract the components, nested as deep as the data");
        show(() -> manhattan(new Line(new Point(1, 2), new Point(4, 6))));   // |4-1| + |6-2|
        show(() -> manhattan("not a line"));

        section("Sequenced collections");
        syntax(
                "getFirst() | getLast() | addFirst(x) | addLast(x)", "the same names on List, Deque, LinkedHashSet, SortedSet",
                "removeFirst() | removeLast()", "NoSuchElementException on an empty collection",
                "reversed()", "a reverse-order VIEW, not a copy",
                "SequencedMap: firstEntry() | lastEntry() | putFirst() | reversed()", "the same for LinkedHashMap and TreeMap"
        );
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        show(() -> list.getFirst());                                       // before Java 21: list.get(0)
        show(() -> list.getLast());                                        // before Java 21: list.get(list.size() - 1)
        List<Integer> backwards = list.reversed();
        list.addFirst(0);
        show(() -> backwards);                                             // a VIEW: it shows the element added to the original
        show(() -> list.removeLast());
        show(() -> thrown(() -> new ArrayList<Integer>().getFirst()));     // empty: NoSuchElementException
        show(() -> thrown(() -> List.of(1, 2).reversed().add(3)));         // List.of stays immutable, also reversed
        SequencedSet<String> ordered = new LinkedHashSet<>(List.of("b", "a", "c"));
        show(() -> ordered.getLast());                                     // LinkedHashSet is sequenced too (it had no way to get the last element)
        show(() -> new ArrayList<>(ordered.reversed()));                   // copied into a list only to print it
        wontCompile("Set<String> s = new LinkedHashSet<>(); s.reversed();", "cannot find symbol: the plain Set type has no reversed(); declare SequencedSet");
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("x", 1);
        map.put("y", 2);
        map.putFirst("w", 0);
        show(() -> map);                                                   // putFirst moved w to the front
        show(() -> map.lastEntry().getKey());                              // lastEntry / firstEntry exist on the sequenced map
        show(() -> new ArrayList<>(map.reversed().keySet()));

        section("Small additions");
        syntax(
                "Math.clamp(value, min, max)", "value limited to min..max",
                "sb.repeat(text, count)", "StringBuilder.repeat",
                "s.indexOf(ch, from, to)", "search only inside [from, to)",
                "s.splitWithDelimiters(regex, limit)", "split and KEEP the separators"
        );
        show(() -> Math.clamp(15, 0, 10));                                 // 15 is above the maximum
        show(() -> Math.clamp(-5, 0, 10));
        show(() -> new StringBuilder("go").repeat("!", 3).toString());
        show(() -> "hello".indexOf('l', 0, 3));                            // the first l, inside [0,3)
        show(() -> "hello".indexOf('o', 0, 3));                            // the only o is at 4: outside the range
        show(() -> String.join("|", "a,b;c".splitWithDelimiters("[,;]", 0)));   // the separators are kept as elements of their own
        note("Virtual threads are final in 21: see lesson 16 (VirtualThreads). Preview features need --enable-preview and may change");
        note("Not in 21: string templates (STR.\"...\") and the unnamed variable _ are PREVIEW here; do not write them in an interview");
    }
}
