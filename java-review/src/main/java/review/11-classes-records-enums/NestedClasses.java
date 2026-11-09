package review.classes;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 11g. NESTED CLASSES: static nested, inner, local, anonymous; what an inner class holds on to
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.NestedClasses
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class NestedClasses {

    private int counter = 100;                                       // an instance field of the OUTER class

    static class StaticNested {                                      // no link to any NestedClasses object
        int twice(int x) { return x * 2; }
    }

    class Inner {                                                    // not static: every Inner belongs to one NestedClasses object
        int read() { return counter; }                               // reads the outer object's field directly
        int readQualified() { return NestedClasses.this.counter; }   // Outer.this = the enclosing object, spelled out
    }

    class InnerIgnoringOuter {
        int one() { return 1; }                                      // never touches the outer object
    }

    record Pair(int a, int b) { }                                    // a nested record, enum or interface is implicitly static

    interface Greeter { String greet(); }

    static int sumWithBase(int base) {
        class Adder {                                                // local class: lives inside one method
            int add(int x) { return x + base; }                      // may use effectively final locals and parameters
        }
        return new Adder().add(5);
    }

    static List<String> fieldNames(Class<?> type) {
        List<String> names = new ArrayList<>();
        for (Field f : type.getDeclaredFields()) names.add(f.getName());
        return names;
    }

    public static void main(String[] args) {
        title("NESTED CLASSES  -  static nested, inner, local, anonymous");

        section("Static nested vs inner");
        syntax(
                "static class N { }", "a class that just lives inside another: use it like a top-level class, named Outer.N",
                "class I { }  (no static)", "an INNER class: each object holds a reference to the outer object that made it",
                "outer.new I()", "how main (static) makes an inner object: it needs an outer object first",
                "Outer.this.field", "the outer object, when the inner class has a member with the same name"
        );
        show(() -> new StaticNested().twice(4));
        NestedClasses outer = new NestedClasses();
        NestedClasses.Inner inner = outer.new Inner();
        show(() -> inner.read());                                    // 100: read through the outer object
        outer.counter = 200;
        show(() -> inner.read());                                    // 200: the inner object sees the outer's CURRENT state
        show(() -> inner.readQualified());
        wontCompile("Inner i = new Inner();   (inside static main)", "non-static variable this cannot be referenced from a static context");
        wontCompile("static class N { int f() { return counter; } }", "non-static variable counter cannot be referenced from a static context");
        show(() -> Modifier.isStatic(Pair.class.getModifiers()));    // true: records, enums and interfaces nested in a class are static
        note("Make a nested class static unless it needs the outer object. Most helper classes (Node, Builder, Entry) should be static");

        section("The hidden reference: what an inner class holds on to");
        show(() -> fieldNames(Inner.class));                         // [this$0]: the synthetic field that points at the outer object
        show(() -> fieldNames(InnerIgnoringOuter.class));            // []: javac in JDK 21 leaves the reference out when the class never uses it
        show(() -> fieldNames(StaticNested.class));                  // []: a static nested class never has one
        note("An inner (or anonymous) object that USES its outer object keeps it alive: a long-lived listener can pin a whole screen in memory");

        section("Local and anonymous classes");
        syntax(
                "class Local { ... }  inside a method", "a named class visible only in that method; captures effectively final locals",
                "new Type(args) { ... }", "an anonymous subclass or implementation, created on the spot (one use)"
        );
        show(() -> sumWithBase(10));                                 // 15: Adder captured base
        Greeter anonymous = new Greeter() {
            @Override public String greet() { return "hi"; }
        };
        show(() -> anonymous.greet());
        show(() -> anonymous.getClass().isAnonymousClass());         // true: the class has no name of its own
        Greeter lambda = () -> "hi";
        show(() -> lambda.getClass().isAnonymousClass());            // false: a lambda is not an anonymous class (it is a hidden class made at run time)
        List<String> words = new ArrayList<>(List.of("ccc", "a", "bb"));
        words.sort(new Comparator<String>() {                        // several methods or its own state would justify this over a lambda
            @Override public int compare(String x, String y) { return Integer.compare(x.length(), y.length()); }
        });
        show(() -> words);
        note("One abstract method: write a lambda. Its own fields, several methods, or its own `this`: an anonymous class (CaptureAndThis in lesson 10)");
        note("Avoid 'double brace initialization' (new ArrayList<>() {{ add(1); }}): it creates a whole extra anonymous subclass for one list; use List.of or add()");
    }
}
