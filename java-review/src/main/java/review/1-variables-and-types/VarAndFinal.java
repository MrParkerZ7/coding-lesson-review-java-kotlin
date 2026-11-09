package review.variables;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;

/**
 * 1a. VAR AND FINAL: type inference, read-only variables, effectively final, constants
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.VarAndFinal
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class VarAndFinal {

    /** A compile-time constant: a final primitive or String with a constant value. Usable as a case label. */
    static final int ONE = 1;

    static final int MAX_USERS = 100;
    static final String PREFIX = "user-";
    static final List<String> ROLES = List.of("admin", "user");   // final AND immutable (lesson 12)

    private static String named(int n) {
        return switch (n) {
            case ONE -> "one";
            default -> "other";
        };
    }

    /** A final FIELD must be assigned exactly once: at its declaration or in every constructor. */
    static class Account {
        private final String id;

        Account(String id) {
            this.id = id;
        }

        String id() {
            return id;
        }
    }

    public static void main(String[] args) {
        title("VAR AND FINAL  -  type inference and read-only variables");

        section("var: the compiler works out the type (Java 10+)");
        syntax(
                "Type name = value;", "explicit type (always allowed)",
                "var name = value;", "type inferred from the value: local variables only",
                "final var name = value;", "inferred AND read-only"
        );
        var i = 42;                       show(() -> i);               // int (printed as Integer)
        var big = 3_000_000_000L;         show(() -> big);             // long, because of the L
        var ratio = 1.5;                  show(() -> ratio);           // decimals are double
        var letter = 'k';                 show(() -> letter);          // char
        var names = new ArrayList<String>();   show(() -> names);      // the type is ArrayList<String>, not List<String>
        var guess = new ArrayList<>();
        guess.add("a");   guess.add(1);
        show(() -> guess);                                             // TRAP: new ArrayList<>() with var infers ArrayList<Object>: anything fits
        wontCompile("var x = 1; x = \"text\";", "incompatible types: String cannot be converted to int (var is not dynamic typing)");
        note("var is for readability when the type is obvious on the right side; do not use it to hide a type");
        note("var also works in for headers, for-each (for (var x : xs)) and lambda parameters ((var a, var b) -> a + b)");

        section("What var cannot do");
        wontCompile("var x;", "cannot infer type: cannot use 'var' on variable without initializer");
        wontCompile("var n = null;", "cannot infer type: variable initializer is 'null'");
        wontCompile("var f = () -> 1;", "cannot infer type: lambda expression needs an explicit target-type");
        wontCompile("var arr = {1, 2};", "cannot infer type: array initializer needs an explicit target-type");
        wontCompile("var a = 1, b = 2;", "'var' is not allowed in a compound declaration");
        wontCompile("class Box { var field = 1; }", "'var' is not allowed here: no fields, parameters or return types");

        section("final: assigned once");
        syntax(
                "final int x = 10;", "a final local: cannot be assigned again",
                "final int x;  x = 10;", "blank final: assigned exactly once later (also in a constructor for a field)",
                "void f(final int n)", "a final parameter (rarely written: just do not reassign parameters)"
        );
        final int limit = 10;   show(() -> limit);
        final int later;
        later = 5;              show(() -> later);                     // a blank final may be assigned once
        wontCompile("final int x = 10; x = 11;", "cannot assign a value to final variable x");
        wontCompile("final int x; x = 1; x = 2;", "variable x might already have been assigned");
        wontCompile("void f(final int n) { n++; }", "final parameter n may not be assigned");
        show(() -> new Account("A-1").id());                           // a final field is set once, in the constructor
        wontCompile("class A { final int x; }", "variable x not initialized in the default constructor");
        note("final on a class means no subclass, on a method no override (lesson 11)");

        section("final is NOT immutability");
        syntax("final List<Integer> list = new ArrayList<>();", "final fixes the REFERENCE; the object behind it may still change");
        final List<Integer> list = new ArrayList<>(List.of(1));
        list.add(2);   show(() -> list);                               // the final variable still points at the same list, which grew
        final int[] arr = {1, 2};
        arr[0] = 9;    show(() -> arr);                                // array elements are never final
        wontCompile("list = new ArrayList<>();", "cannot assign a value to final variable list");
        note("For a list that cannot change use List.of / List.copyOf (lesson 13), not just final");

        section("Effectively final: what a lambda may capture");
        syntax("int count = 0; Runnable r = () -> use(count);", "legal only while count is never reassigned anywhere in the method");
        int[] hits = {0};
        Runnable tick = () -> hits[0]++;                               // the array reference never changes, only its content
        tick.run();
        tick.run();
        show(() -> hits[0]);                                           // the workaround for a counter inside a lambda
        wontCompile("int c = 0; c++; Runnable r = () -> f(c);", "local variables referenced from a lambda must be final or effectively final");
        wontCompile("int total = 0; xs.forEach(x -> total += x);", "same error: a lambda cannot change a local (use a loop or a stream sum)");
        note("Also usable for the same job: AtomicInteger (lesson 16). An inner class has the same rule");

        section("Constants: static final");
        syntax("static final TYPE NAME = value;", "UPPER_SNAKE_CASE; a primitive/String constant is inlined by the compiler");
        show(() -> MAX_USERS);
        show(() -> PREFIX + 7);
        show(() -> ROLES.contains("admin"));                           // final reference to an immutable list
        show(() -> named(1));                                          // a constant is allowed as a case label
        wontCompile("int one = 1; switch (n) { case one -> ...", "constant expression required: a normal variable cannot be a case label");
        note("Naming: camelCase for variables and methods, PascalCase for classes, UPPER_SNAKE_CASE for constants");
    }
}
