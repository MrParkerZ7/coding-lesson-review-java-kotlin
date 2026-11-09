package review.variables;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 1b. SCOPE, SHADOWING, DEFAULT VALUES, VALUE vs REFERENCE
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.ScopeAndDefaults
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ScopeAndDefaults {

    // Fields (here: static fields) get a default value when nothing is assigned. Locals do not.
    static int defaultInt;
    static long defaultLong;
    static double defaultDouble;
    static boolean defaultBoolean;
    static char defaultChar;
    static String defaultString;
    static Integer defaultBoxed;
    static int[] defaultArray;

    static int level = 1;

    /** A local may hide a FIELD of the same name (this is legal, and what "this.x = x" in a constructor is for). */
    private static int hidesField() {
        int level = 2;
        return level;
    }

    /** The same name in two separate blocks is fine: each variable ends at its closing brace. */
    private static int sumTwoLoops() {
        int total = 0;
        for (int i = 1; i <= 3; i++) total += i;
        for (int i = 10; i <= 12; i++) total += i;
        return total;
    }

    /** A variable declared BEFORE the loop is still alive after it. */
    private static int counterAfterLoop() {
        int i;
        for (i = 0; i < 3; i++) {
            // nothing to do: only the counter matters
        }
        return i;
    }

    /** Primitives copy the value; arrays and objects copy the reference. */
    private static int[] copyOfPrimitive() {
        int a = 5;
        int b = a;
        b++;
        return new int[] {a, b};
    }

    public static void main(String[] args) {
        title("SCOPE AND DEFAULTS  -  where a variable lives and what it starts as");

        section("Scope: a variable lives until the end of its block");
        syntax("{ int x = 1; }  // x is gone here", "a block, loop or method body ends the life of what it declared");
        show(() -> sumTwoLoops());                                     // 6 + 33: the two i variables never meet
        show(() -> counterAfterLoop());                                // declared before the loop: still alive, holds 3
        wontCompile("for (int i = 0; i < 3; i++) { } print(i);", "cannot find symbol: i ended with the loop");

        section("Shadowing: Java forbids a local hiding another local");
        wontCompile("int a = 1; { int a = 2; }", "variable a is already defined in method");
        wontCompile("void f(int a) { int a = 2; }", "variable a is already defined: a parameter counts as a local");
        wontCompile("int n = 1; Function<Integer, Integer> f = n -> n;", "variable n is already defined: a lambda parameter too");
        show(() -> level);                                             // the field
        show(() -> hidesField());                                      // a local may hide a FIELD: inside the method level is 2
        show(() -> level);                                             // the field was never touched
        note("Because a local can never hide another local, a long method cannot silently change which variable a name means");

        section("Default values: fields get one, locals do not");
        syntax("int n;  (a local)", "has NO value yet: reading it is a compile error");
        show(() -> defaultInt);                                        // numbers start as 0
        show(() -> defaultLong);
        show(() -> defaultDouble);
        show(() -> defaultBoolean);                                    // booleans start as false
        show(() -> (int) defaultChar);                                 // the char with code 0
        show(() -> defaultString);                                     // null: every reference type starts as null
        show(() -> defaultBoxed);                                      // TRAP: Integer starts as null, int starts as 0
        show(() -> defaultArray);                                      // an array variable is a reference: null until new
        show(() -> new int[3]);                                        // but the ELEMENTS of a new array get defaults too
        show(() -> new boolean[2]);
        show(() -> new String[2]);                                     // null elements
        wontCompile("int n; int m = n + 1;", "variable n might not have been initialized");
        wontCompile("int x; if (flag) { x = 1; } use(x);", "variable x might not have been initialized: one branch is not enough");

        section("Value or reference?");
        syntax(
                "int | long | double | boolean | char ...", "the variable holds the VALUE: = copies it",
                "String | arrays | every class", "the variable holds a REFERENCE: = copies the address, not the object"
        );
        show(() -> copyOfPrimitive());                                 // a stays 5 while b changed to 6
        int[] first = {1, 2};
        int[] alias = first;
        alias[0] = 9;          show(() -> first);                      // TRAP: both names point to ONE array, first changed too
        show(() -> first == alias);                                    // == on references asks: the very same object?
        int[] copy = first.clone();
        copy[1] = 7;           show(() -> copy);                       // clone() made a separate array: only the copy changed
        show(() -> first);                                             // the original keeps [9, 2]
        note("Everything in Java is passed by value, also a reference (the address is copied): lesson 12, PassByValue");
    }
}
