package review.methods;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 10b. PARAMETERS and RETURN VALUES: no defaults, pass by value, several results
 *      Run: mvn -q compile exec:java -Dmain.class=review.methods.ParametersAndReturn
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ParametersAndReturn {

    // no default arguments: the short overload calls the full one
    static String greet(String name) { return greet(name, "Hello"); }
    static String greet(String name, String word) { return word + ", " + name; }

    /** Many optional parts: a builder instead of a constructor with ten parameters. */
    record Greeting(String name, String word, boolean loud) {
        static Builder builder(String name) { return new Builder(name); }

        String text() {
            String t = word + ", " + name;
            return loud ? t.toUpperCase() + "!" : t;
        }

        static final class Builder {
            private final String name;
            private String word = "Hello";
            private boolean loud;

            Builder(String name) { this.name = name; }
            Builder word(String word) { this.word = word; return this; }
            Builder loud(boolean loud) { this.loud = loud; return this; }
            Greeting build() { return new Greeting(name, word, loud); }
        }
    }

    static void swapInts(int a, int b) { int t = a; a = b; b = t; }
    static void swapRefs(StringBuilder a, StringBuilder b) { StringBuilder t = a; a = b; b = t; }
    static void swapContent(int[] arr) { int t = arr[0]; arr[0] = arr[1]; arr[1] = t; }
    static void append(StringBuilder sb) { sb.append("!"); }
    static void reassign(StringBuilder sb) { sb = new StringBuilder("new"); }

    record MinMax(int min, int max) { }

    static MinMax minMax(int[] xs) {
        int min = xs[0];
        int max = xs[0];
        for (int x : xs) {
            if (x < min) min = x;
            if (x > max) max = x;
        }
        return new MinMax(min, max);
    }

    public static void main(String[] args) {
        title("PARAMETERS AND RETURN  -  no defaults, pass by value, several results");

        section("No default values, no named arguments");
        wontCompile("void greet(String n, String w = \"Hello\")", "Java has no default parameter values (javac: ',', ')', or '[' expected)");
        syntax(
                "greet(name)  { return greet(name, \"Hello\"); }", "an OVERLOAD chain: the short form calls the full one with the default",
                "Type.builder(a).opt(x).opt(y).build()", "a builder when there are many optional parts: the call names each one",
                "record + withX(...) method", "an immutable value copied with one part changed (records have no copy())"
        );
        show(() -> greet("Ann"));
        show(() -> greet("Ann", "Hi"));
        show(() -> Greeting.builder("Ann").word("Hi").loud(true).build().text());   // each optional part is named at the call
        note("Two String parameters can swap silently: greet(\"Hi\", \"Ann\") compiles. A builder or a small record removes that risk");
        wontCompile("void f(final int x) { x = 1; }", "final parameter x may not be assigned (without final it is allowed)");

        section("Everything is passed BY VALUE (lesson 12 goes deeper)");
        syntax(
                "int / long / boolean ...", "the VALUE is copied: the method cannot change the caller's variable",
                "an object reference", "the REFERENCE is copied: the method can change the object, not rebind the caller's variable"
        );
        int a = 1;
        int b = 2;
        swapInts(a, b);
        show(() -> a + "," + b);                                     // 1,2: the method swapped its own two copies
        StringBuilder x = new StringBuilder("x");
        StringBuilder y = new StringBuilder("y");
        swapRefs(x, y);
        show(() -> x + "," + y);                                     // x,y: it swapped copies of the references; the caller still holds the same two
        int[] pair = {10, 20};
        swapContent(pair);
        show(() -> pair);                                            // [20, 10]: the method changed the SHARED array, so this swap works
        StringBuilder sb = new StringBuilder("hi");
        append(sb);
        show(() -> sb);                                              // hi!: the object was changed through the copied reference
        StringBuilder kept = new StringBuilder("hi");
        reassign(kept);
        show(() -> kept);                                            // hi: pointing the copy at a new object changes nothing out here
        note("To 'swap' two variables, return both in a record, or swap inside one array / list; a method cannot rebind the caller's variable");

        section("Return values");
        syntax(
                "Type name(...) { return expr; }", "the return type is always written: Java has no inferred return type and no expression body",
                "void name(...) { ... }", "no result; a bare return; may leave early",
                "return new Result(a, b);", "one return value only: several results go inside an object"
        );
        int[] data = {3, 1, 2};
        show(() -> minMax(data));                                    // a record is the clean way to return two values
        MinMax mm = minMax(data);
        show(() -> mm.max());                                        // read a component through its accessor
        String range = minMax(data) instanceof MinMax(int lo, int hi) ? lo + ".." + hi : "none";
        show(() -> range);                                           // a record pattern takes the result apart
        note("Map.entry(a, b) also carries two values (getKey, getValue), but names nothing; the JDK has no Pair or Triple: a record is clearer");
        note("A void method cannot go inside show(() -> ...): call it on its own line, then show the object it changed");
    }
}
