package review.methods;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 10a. METHODS and OVERLOADING: static vs instance, which overload runs, varargs
 *      Run: mvn -q compile exec:java -Dmain.class=review.methods.MethodsAndOverloading
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class MethodsAndOverloading {

    static int square(int x) { return x * x; }                // static: belongs to the class
    String greeting() { return "hello"; }                      // instance: needs an object

    // one name, five signatures
    static String kind(int x) { return "int"; }
    static String kind(long x) { return "long"; }
    static String kind(Integer x) { return "Integer"; }
    static String kind(Object x) { return "Object"; }
    static String kind(int... x) { return "int..."; }

    static String prefer(long x) { return "long (widening)"; }
    static String prefer(Integer x) { return "Integer (boxing)"; }

    static String specific(Object o) { return "Object"; }
    static String specific(String s) { return "String"; }

    static int sum(int... xs) {
        int total = 0;
        for (int x : xs) total += x;
        return total;
    }

    static int countObjects(Object... xs) { return xs.length; }

    static void zeroFirst(int... xs) { xs[0] = 0; }

    public static void main(String[] args) {
        title("METHODS AND OVERLOADING  -  static vs instance, overload resolution, varargs");

        section("Static and instance methods");
        syntax(
                "static Type name(Type p) { ... }", "belongs to the CLASS: call it as Class.name(x), no object needed",
                "Type name(Type p) { ... }", "belongs to an OBJECT: call it as obj.name(x), it can read the fields",
                "void name() { ... }", "void = no result (a void call cannot be the body of show: call it on its own line)"
        );
        show(() -> square(7));                                       // static, called by its simple name from static main
        MethodsAndOverloading self = new MethodsAndOverloading();
        show(() -> self.greeting());                                 // an instance method needs an object first
        wontCompile("greeting();  (inside static main)", "non-static method greeting() cannot be referenced from a static context");
        wontCompile("int f() {..}  long f() {..}", "method f() is already defined: the return type is not part of the signature");
        note("A method is identified by its NAME + PARAMETER TYPES (the signature). Java has no nested methods: use a lambda or a local class");

        section("Which overload is called (decided by the compiler, from the STATIC types)");
        syntax(
                "phase 1", "exact match, widening (byte -> int -> long -> double) or subtype; no boxing, no varargs",
                "phase 2", "boxing and unboxing allowed (int -> Integer)",
                "phase 3", "varargs; the most specific candidate of the first phase that finds one wins"
        );
        show(() -> kind(5));                                         // the literal 5 is an int: exact match
        show(() -> kind(5L));
        show(() -> kind((short) 5));                                 // short widens to int (the closest), not to long
        show(() -> kind('a'));                                       // char widens to int too
        show(() -> kind(Integer.valueOf(5)));                        // Integer matches exactly, no unboxing needed
        show(() -> kind(5.5));                                       // double never narrows to int: it boxes to Double, an Object
        show(() -> kind("text"));
        show(() -> kind());                                          // only the varargs method fits zero arguments
        show(() -> kind(1, 2));
        show(() -> prefer(5));                                       // TRAP: widening (phase 1) beats boxing (phase 2)
        show(() -> specific(null));                                  // null fits both: the MOST SPECIFIC one (String) wins
        Object text = "text";
        show(() -> specific(text));                                  // TRAP: Object, because the static type is Object, not the runtime String
        wontCompile("amb(null) when amb(String), amb(Integer)", "reference to amb is ambiguous: neither is more specific");
        note("Overloading is chosen at COMPILE time by the declared types; overriding (lesson 11) is chosen at RUN time by the real class");

        section("Varargs");
        syntax(
                "Type... name", "inside the method it is an array: int... is an int[]",
                "f(1, 2, 3)  |  f()  |  f(array)", "any number of arguments, none at all (empty array, never null), or an array",
                "last parameter only", "one varargs parameter per method, and it must come last"
        );
        show(() -> sum(1, 2, 3));
        show(() -> sum());                                           // zero arguments: an empty array
        show(() -> sum(new int[] {4, 5}));                           // an array is passed as it is, no copy
        int[] shared = {1, 2};
        zeroFirst(shared);
        show(() -> shared);                                          // [0, 2]: the method changed the CALLER's array
        show(() -> countObjects(new int[] {1, 2, 3}));               // TRAP: an int[] is ONE object, not an Object[]
        show(() -> countObjects((Object[]) new Integer[] {1, 2, 3}));   // an Integer[] IS an Object[]: it is passed as the array itself, so 3
        wontCompile("void f(int... a, String s) { }", "varargs parameter must be the last parameter");
        note("Every varargs call allocates an array: a hot method often keeps a plain f(int) overload next to f(int...)");
    }
}
