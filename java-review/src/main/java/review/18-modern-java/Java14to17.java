package review.modern;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 18c. WHAT IS NEW IN JAVA 14 TO 17
 *      Run: mvn -q compile exec:java -Dmain.class=review.modern.Java14to17
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class Java14to17 {

    /** Java 16: a record is an immutable data carrier; equals, hashCode, toString and accessors are generated. */
    record Point(int x, int y) {
        Point {                                                            // compact constructor: validate, no field assignments needed
            if (x < 0 || y < 0) throw new IllegalArgumentException("negative coordinate");
        }
    }

    /** Java 17: a sealed type lists every allowed subtype, so the compiler knows all the cases. */
    sealed interface Shape permits Circle, Rect {
    }

    record Circle(double radius) implements Shape {
    }

    record Rect(double width, double height) implements Shape {
    }

    static int dayLength(String day) {
        return switch (day) {                                              // Java 14: switch EXPRESSION with arrows
            case "MON", "TUE", "FRI" -> 6;                                 // several labels in one case
            case "WED" -> {
                int base = 4;
                yield base + 1;                                            // a block gives its value with yield
            }
            default -> 0;
        };
    }

    static String describe(Object o) {
        if (o instanceof String s && s.length() > 3) return "long string " + s.toUpperCase();   // Java 16: test, cast and name in one
        if (o instanceof Integer i) return "int " + (i + 1);
        return "other";
    }

    static String nullField;

    /** Tries to make a private field of java.lang.String readable: allowed in Java 8, refused since 17. */
    static int openStringInternals() throws NoSuchFieldException {
        String.class.getDeclaredField("value").setAccessible(true);
        return 0;
    }

    public static void main(String[] args) {
        title("JAVA 14 TO 17  -  switch expressions, text blocks, records, sealed");

        section("Version table: feature -> Java version");
        syntax(
                "Java 14", "switch expressions (final); helpful NullPointerException messages (on by default from 15)",
                "Java 15", "text blocks (final); String.formatted",
                "Java 16", "records (final); pattern matching for instanceof (final); Stream.toList()",
                "Java 17  (LTS)", "sealed classes and interfaces (final); HexFormat; JDK internals sealed off for good (strong encapsulation)"
        );

        section("Switch expressions (14)");
        syntax(
                "int v = switch (x) { case A, B -> 1; default -> 2; };", "an expression: it has a value; no fall-through; arrows",
                "case C -> { ...; yield value; }", "a block case gives its value with yield"
        );
        show(() -> dayLength("MON"));
        show(() -> dayLength("WED"));                                      // the block with yield
        show(() -> dayLength("SUN"));                                      // default
        wontCompile("int r = switch (n) { case 1 -> 10; };", "the switch expression does not cover all possible input values");
        note("The full tour of switch (old vs new, patterns) is lesson 8");

        section("Text blocks (15)");
        syntax("\"\"\" newline text \"\"\"", "a multi-line string; quotes need no escape; the common indentation is removed");
        String json = """
                {
                  "name": "Ann"
                }
                """;
        show(() -> json.lines().count());                                  // 3 lines of text
        show(() -> json.endsWith("}\n"));                                  // the closing delimiter on its own line leaves a final newline
        show(() -> json.lines().toList().get(1));                          // the 2-space indent inside the braces is kept, the block's own indent is gone
        String joined = """
                one \
                line""";
        show(() -> joined);                                                // a backslash at the end of a line joins it with the next
        show(() -> "%s is %d".formatted("Ann", 30));                       // Java 15: formatted() is String.format as a method
        wontCompile("String s = \"\"\"hello\"\"\";", "illegal text block open delimiter sequence, missing line terminator");

        section("Records (16)");
        syntax("record Point(int x, int y) { }", "final fields, constructor, accessors x() and y(), equals, hashCode, toString");
        Point p = new Point(1, 2);
        show(() -> p);                                                     // Point[x=1, y=2]
        show(() -> p.x());                                                 // an accessor is x(), not getX()
        show(() -> p.equals(new Point(1, 2)));                             // equals compares the components
        show(() -> Point.class.isRecord());
        show(() -> thrown(() -> new Point(-1, 0)));                        // the compact constructor validated
        wontCompile("record P(int x) { int y; }", "field declaration must be static (consider replacing field with record component)");
        note("A record is shallowly immutable and cannot extend a class; it may implement interfaces (lesson 11)");

        section("Pattern matching for instanceof (16)");
        syntax("if (o instanceof String s && s.length() > 3)", "s exists only where the test is known to be true");
        show(() -> describe("hello"));
        show(() -> describe("hi"));                                        // too short: falls through
        show(() -> describe(41));
        show(() -> describe(2.5));

        section("Sealed classes (17)");
        syntax("sealed interface Shape permits Circle, Rect", "only the listed types may implement it; each must be final, sealed or non-sealed (records are final)");
        show(() -> Shape.class.isSealed());
        show(() -> Arrays.stream(Shape.class.getPermittedSubclasses()).map(Class::getSimpleName).toList());
        wontCompile("record Tri(double b) implements Shape { }", "class is not allowed to extend sealed class: Shape (as it is not listed in its 'permits' clause)");
        note("Sealed + records + a switch over the type is the Java 21 payoff: the compiler checks every case (see Java21)");

        section("More from 14 to 17");
        show(() -> thrown(() -> Stream.of(1, 2).toList().add(3)));         // Java 16 Stream.toList() is unmodifiable ...
        show(() -> Stream.of(1, 2).collect(Collectors.toList()).add(3));   // ... Collectors.toList() gives an ArrayList you may change
        show(() -> HexFormat.of().formatHex(new byte[] {1, (byte) 255}));  // Java 17: bytes to hex text without the old & 0xFF loops
        List<String> npeMessage = new ArrayList<>();
        try {
            nullField.length();
        } catch (NullPointerException e) {
            npeMessage.add(e.getMessage());
        }
        show(() -> npeMessage);                                            // Java 14/15: the NPE says WHAT was null (field name, method call, array index)
        show(() -> thrown(() -> openStringInternals()));                   // Java 17: reflection into java.base internals is refused (Java 9 to 15 only warned, 16 denied by default)
        note("That last line is why hand-made reflection (lesson 17, ReflectionJson) cannot read JDK classes such as LocalDate");
    }
}
