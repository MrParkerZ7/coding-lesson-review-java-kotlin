package review.classes;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntBinaryOperator;

/**
 * 11e. ENUMS: a fixed set of values, with fields, methods, EnumMap and exhaustive switch
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.Enums
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class Enums {

    enum Level {
        LOW("low", 1), MEDIUM("medium", 5), HIGH("high", 10);        // the constants come first; ';' ends them when members follow

        private final String label;
        private final int weight;

        Level(String label, int weight) {                            // an enum constructor is implicitly private
            this.label = label;
            this.weight = weight;
        }

        String label() { return label; }
        boolean isUrgent() { return weight >= 10; }

        /** The safe lookup by another field: valueOf only knows the constant name. */
        static Optional<Level> byLabel(String text) {
            return Arrays.stream(values()).filter(l -> l.label.equals(text)).findFirst();
        }
    }

    /** Each constant may carry its own body; the enum implements an interface. */
    enum Op implements IntBinaryOperator {
        PLUS("+") { @Override public int applyAsInt(int a, int b) { return a + b; } },
        TIMES("*") { @Override public int applyAsInt(int a, int b) { return a * b; } };

        private final String symbol;

        Op(String symbol) { this.symbol = symbol; }

        String symbol() { return symbol; }
    }

    static String advice(Level level) {
        return switch (level) {                                      // a switch EXPRESSION over an enum needs every constant (or default)
            case LOW -> "relax";
            case MEDIUM -> "plan";
            case HIGH -> "act now";
        };
    }

    public static void main(String[] args) {
        title("ENUMS  -  a fixed set of values");

        section("Declaring and using an enum");
        syntax(
                "enum Name { A(1), B(2); fields, constructor, methods }", "constants are objects created once; the constructor is private",
                "Name.values()", "a NEW array of all constants in declaration order (every call copies)",
                "Name.valueOf(\"A\")", "by exact name; IllegalArgumentException when missing, NullPointerException for null",
                "e.name()  |  e.ordinal()", "the constant's name  |  its position, from 0"
        );
        show(() -> Level.HIGH);
        show(() -> Level.values());                                  // [LOW, MEDIUM, HIGH]
        show(() -> Level.values() == Level.values());                // false: every call returns a fresh copy of the array
        show(() -> Level.MEDIUM.ordinal());                          // the second constant: 1
        show(() -> Level.MEDIUM.name());
        show(() -> Level.valueOf("LOW").label());
        show(() -> thrown(() -> Level.valueOf("low")));              // TRAP: the name is case-sensitive, so this throws
        show(() -> thrown(() -> Level.valueOf(null)));
        show(() -> Level.byLabel("high"));                           // found by label, not by name
        show(() -> Level.byLabel("none"));                           // Optional.empty instead of an exception
        show(() -> Level.LOW.compareTo(Level.HIGH) < 0);             // true: enums compare by ordinal
        show(() -> Level.valueOf("HIGH") == Level.HIGH);             // true: == is correct for enums, there is one object per constant
        show(() -> Level.HIGH.isUrgent());
        show(() -> Op.TIMES.applyAsInt(6, 7));                       // a constant with its own body
        show(() -> Op.PLUS.symbol());
        wontCompile("new Level(\"x\", 1);", "enum classes may not be instantiated");
        note("Do not use ordinal() for stored data: inserting a constant renumbers everything. Store name() or a field of your own");

        section("switch over an enum");
        show(() -> advice(Level.HIGH));
        wontCompile("return switch (l) { case LOW -> \"a\"; };", "the switch expression does not cover all possible input values");
        note("Add a constant later and every exhaustive switch expression stops compiling: the compiler lists the places to fix");

        section("EnumMap and EnumSet");
        syntax(
                "new EnumMap<>(Name.class)", "a Map keyed by enum constants: an array inside, iterates in declaration order, O(1)",
                "EnumSet.of(...) | range | allOf | noneOf | complementOf", "a Set of constants stored as bits: very fast and compact"
        );
        Map<Level, Integer> counts = new EnumMap<>(Level.class);
        counts.put(Level.HIGH, 2);
        counts.put(Level.LOW, 5);
        show(() -> counts);                                          // {LOW=5, HIGH=2}: in constant order, not insertion order
        show(() -> EnumSet.range(Level.MEDIUM, Level.HIGH));
        show(() -> EnumSet.complementOf(EnumSet.of(Level.LOW)));
        note("Enum or sealed? enum = fixed VALUES with the same fields; sealed + records = fixed KINDS with different data (SealedAndPatterns)");
    }
}
