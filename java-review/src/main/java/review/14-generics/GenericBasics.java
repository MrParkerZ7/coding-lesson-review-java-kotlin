package review.generics;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 14a. GENERIC BASICS: generic class, generic method, bounds, diamond
 *      Run: mvn -q compile exec:java -Dmain.class=review.generics.GenericBasics
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class GenericBasics {

    /** A generic class: the user of the class fills in T. */
    static class Box<T> {
        private final T value;

        Box(T value) {
            this.value = value;
        }

        T get() {
            return value;
        }

        /** A generic method inside a generic class: R is new, T belongs to the class. */
        <R> Box<R> map(Function<? super T, ? extends R> f) {
            return new Box<>(f.apply(value));
        }

        /** A static method cannot use the class's T (there is no instance), so it declares its own. */
        static <T> Box<T> of(T value) {
            return new Box<>(value);
        }

        @Override
        public String toString() {
            return "Box(" + value + ")";
        }
    }

    /** A generic method: T is declared in angle brackets BEFORE the return type, and inferred from the arguments. */
    static <T> T firstOrDefault(List<T> list, T fallback) {
        return list.isEmpty() ? fallback : list.get(0);
    }

    /** Upper bound: T must be Comparable<T>, so compareTo can be called. */
    static <T extends Comparable<T>> T maxOfTwo(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    /** Several bounds with &: a class bound (if any) comes FIRST, then interfaces. */
    static <T extends CharSequence & Comparable<T>> T longerOrLarger(T a, T b) {
        if (a.length() != b.length()) return a.length() > b.length() ? a : b;
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static void main(String[] args) {
        title("GENERIC TYPES  -  classes, methods, bounds, diamond");

        section("Generic class: Box<T>");
        syntax(
                "class Box<T> { ... }", "T is a placeholder type, filled in by the user",
                "Box<Integer> b = new Box<>(42);", "type argument written once, the diamond <> infers the right side",
                "Box<int>", "a type argument must be a reference type: use the wrapper (Integer)"
        );
        Box<Integer> intBox = new Box<>(42);   show(() -> intBox);        // run-time class is just Box: T is erased (14c)
        Box<String> strBox = Box.of("java");   show(() -> strBox);          // static generic factory: T inferred as String
        show(() -> strBox.get().length());                                 // T is String here, so String methods work, no cast
        show(() -> intBox.map(n -> n * 2));                                // Box<Integer> -> Box<Integer>
        show(() -> intBox.map(n -> "n=" + n));                             // Box<Integer> -> Box<String>: map changes T
        wontCompile("List<int> list;", "type arguments must be reference types (int is a primitive): use List<Integer>");

        section("Generic method: <T> returnType name(...)");
        syntax(
                "static <T> T name(List<T> list)", "T is declared BEFORE the return type; inferred from the arguments",
                "Class.<Type>name(args)", "explicit type argument (rarely needed)"
        );
        show(() -> firstOrDefault(List.of(1, 2), 0));                      // T = Integer
        show(() -> firstOrDefault(List.of(), "none"));                     // T = String, inferred from the second argument
        show(() -> GenericBasics.<String>firstOrDefault(List.of(), "x"));  // explicit type argument
        note("A generic method works in a non-generic class too; the angle brackets go before the return type");

        section("Upper bound: T extends Comparable<T>");
        syntax(
                "<T extends Bound>", "T must be a Bound (or a subtype): extends is also used for interfaces here",
                "<T extends A & B & C>", "several bounds: at most one class, and it must come first"
        );
        show(() -> maxOfTwo(3, 9));
        show(() -> maxOfTwo("apple", "pear"));                             // Strings are Comparable: alphabetical
        show(() -> longerOrLarger("kiwi", "fig"));                         // longer wins
        show(() -> longerOrLarger("fig", "yam"));                          // same length: the larger one wins
        wontCompile("maxOfTwo(new Box<>(1), new Box<>(2))", "inference variable T has incompatible bounds: Box is not Comparable");
        wontCompile("<X extends Comparable<X> & Number>", "interface expected here: the class bound (Number) must come first");

        section("The diamond <> and var");
        List<String> names = new ArrayList<>();
        names.add("Ann");
        show(() -> names);                                                 // diamond: the type argument comes from the left side
        var guess = new ArrayList<>();
        guess.add(1);
        guess.add("two");
        show(() -> guess);                                                 // TRAP: var + diamond = ArrayList<Object>, anything goes in
        var typed = new ArrayList<String>();
        typed.add("ok");
        show(() -> typed);                                                 // with var, write the type argument on the right
        wontCompile("typed.add(1);", "int cannot be converted to String: the compiler checks the type argument");
        note("Generics exist only at compile time: they give you checks and no casts, not a different object at run time");
    }
}
