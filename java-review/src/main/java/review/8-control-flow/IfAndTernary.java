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
 * 8a. IF, ELSE IF AND THE TERNARY OPERATOR
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.IfAndTernary
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class IfAndTernary {

    /** A chain of else if: the first true condition decides. */
    static String sizeWord(int n) {
        if (n < 0) return "negative";
        else if (n == 0) return "zero";
        else if (n < 10) return "small";
        else return "large";
    }

    /** The same rule as one chained ternary: fine for two levels, hard to read beyond that. */
    static String sizeWordTernary(int n) {
        return n < 0 ? "negative" : n == 0 ? "zero" : n < 10 ? "small" : "large";
    }

    /** TRAP: the else belongs to the NEAREST if, whatever the indentation says. */
    static String dangling(boolean a, boolean b) {
        if (a)
            if (b) return "a and b";
        else return "else of b";
        return "end";
    }

    /** TRAP: = assigns and == compares. With a boolean variable the typo still compiles. */
    static String assignTrap() {
        boolean ready = false;
        if (ready = true) return "branch taken, ready is now " + ready;
        return "branch skipped";
    }

    /** A call with a side effect, to see whether the right side of a condition runs. */
    static boolean check(List<String> log, String name) {
        log.add(name);
        return true;
    }

    public static void main(String[] args) {
        title("IF AND TERNARY  -  choose with a statement or with an expression");

        section("if / else if / else");
        syntax(
                "if (cond) { ... } else if (cond) { ... } else { ... }", "the first true condition wins; the condition MUST be a boolean",
                "cond ? a : b", "the ternary operator: an EXPRESSION that gives a or b (if is only a statement)"
        );
        show(() -> sizeWord(-4));
        show(() -> sizeWord(0));
        show(() -> sizeWord(7));
        show(() -> sizeWord(10));                                       // boundary: n < 10 is false, so the last branch
        show(() -> sizeWordTernary(7));                                 // the same answer from the chained ternary
        int n = 7;
        wontCompile("int r = if (n > 5) 1 else 2;", "if is a statement, not an expression: write n > 5 ? 1 : 2");
        wontCompile("if (n) { ... }   // n is an int", "a condition must be a boolean: there are no truthy values, write n != 0");
        wontCompile("if (n = 5) { ... }", "an int assignment is an int, not a boolean (with a boolean variable this typo DOES compile: see below)");

        section("Ternary ?:");
        String parity = n % 2 == 0 ? "even" : "odd";
        show(() -> parity);
        show(() -> n > 5 ? "big" : "small");
        show(() -> Math.max(3, 9));                                     // the library already has it
        show(() -> Math.clamp(15, 0, 10));                              // Java 21: clamp into a range
        show(() -> n > 5 ? 1 : 2.0);                                    // TRAP: 1.0, both branches are promoted to a common type (double)
        char letter = 'b';
        int zero = 0;
        show(() -> n > 5 ? letter : zero);                              // TRAP: 98, char and an int VARIABLE promote to int
        show(() -> n > 5 ? letter : 0);                                 // 'b': the constant 0 fits in a char, so the type stays char
        note("A ternary with a boxed value can throw: Integer maybe = null; flag ? maybe : 0 unboxes (lesson 7 NullBasics)");

        section("Conditions: && || ! and short-circuit");
        syntax(
                "a && b | a || b | !a", "and | or | not: the right side runs only when it can change the answer",
                "a & b, a | b", "the same on booleans, but BOTH sides always run"
        );
        List<String> log = new ArrayList<>();
        boolean viaAnd = n > 100 && check(log, "right side of &&");
        boolean viaBitAnd = n > 100 & check(log, "right side of &");
        show(() -> log);                                                // only the & call ran
        show(() -> n > 100 && 10 / (n - 7) > 0);                        // false, no ArithmeticException: && skips the right side
        show(() -> thrown(() -> n > 100 & 10 / (n - 7) > 0));           // & evaluates both sides: division by zero
        show(() -> n > 0 && n < 10);                                    // a range test needs both comparisons
        wontCompile("boolean b = 0 < n < 10;", "(0 < n) is a boolean, which cannot be compared with 10: write 0 < n && n < 10");

        section("Traps: dangling else and = instead of ==");
        show(() -> dangling(true, true));
        show(() -> dangling(true, false));                              // TRAP: else of b, although it lines up with if (a)
        show(() -> dangling(false, true));                              // a is false: neither else is reached
        show(() -> assignTrap());                                       // TRAP: ready = true assigns, and the value of the assignment is true
        note("Always write braces, even for one statement: it removes the dangling else. == on objects and Strings: lesson 1 Equality");
    }
}
