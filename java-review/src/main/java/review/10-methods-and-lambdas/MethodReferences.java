package review.methods;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 10e. METHOD REFERENCES and HIGHER-ORDER METHODS: the 4 kinds, andThen / compose, functions as parameters and results
 *      Run: mvn -q compile exec:java -Dmain.class=review.methods.MethodReferences
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class MethodReferences {

    record Tag(String name) { }

    static int inc(int x) { return x + 1; }

    static int applyTwice(Function<Integer, Integer> f, int x) { return f.apply(f.apply(x)); }

    /** What `filter` is: take a test as a parameter. */
    static <T> List<T> keepIf(List<T> xs, Predicate<T> test) {
        List<T> kept = new ArrayList<>();
        for (T x : xs) if (test.test(x)) kept.add(x);
        return kept;
    }

    /** Return a function chosen by data (the strategy pattern). */
    static IntBinaryOperator operation(String sign) {
        return switch (sign) {
            case "+" -> Integer::sum;
            case "*" -> (a, b) -> a * b;
            default -> throw new IllegalArgumentException("unknown operator " + sign);
        };
    }

    static Function<Integer, Integer> multiplier(int factor) { return x -> x * factor; }

    static Supplier<Integer> boundLength(String s) { return s::length; }
    static Supplier<Integer> lazyLength(String s) { return () -> s.length(); }

    public static void main(String[] args) {
        title("METHOD REFERENCES  -  the 4 kinds, combining functions, higher-order methods");

        section("The four kinds of method reference");
        syntax(
                "Class::staticMethod", "static: Integer::parseInt  is  s -> Integer.parseInt(s)",
                "object::method", "bound: the receiver is fixed when the reference is made: word::contains  is  s -> word.contains(s)",
                "Class::instanceMethod", "unbound: the FIRST argument is the receiver: String::length  is  s -> s.length()",
                "Class::new  |  Type[]::new", "constructor: ArrayList::new  is  () -> new ArrayList<>();  int[]::new  is  n -> new int[n]"
        );
        Function<String, Integer> parse = Integer::parseInt;
        show(() -> parse.apply("42"));                               // static
        String word = "banana";
        Predicate<String> insideBanana = word::contains;
        show(() -> insideBanana.test("nan"));                        // bound: "banana" is fixed, the argument is what contains() looks for
        Function<String, Integer> length = String::length;
        show(() -> length.apply("hello"));                           // unbound: the argument "hello" becomes the receiver
        BiFunction<String, String, Boolean> sameText = String::equalsIgnoreCase;
        show(() -> sameText.apply("Ab", "aB"));                      // unbound with 2 arguments: first = receiver, second = parameter
        Supplier<List<String>> makeList = ArrayList::new;
        show(() -> makeList.get());                                  // constructor reference
        Function<String, Tag> makeTag = Tag::new;
        show(() -> makeTag.apply("x"));
        IntFunction<int[]> makeArray = int[]::new;
        show(() -> makeArray.apply(3));                              // array constructor reference
        List<String> words = List.of("a", "bc");
        show(() -> words.stream().map(String::toUpperCase).toList());   // the everyday use: pass a reference where a lambda would go
        show(() -> thrown(() -> boundLength(null)));                 // TRAP: a bound reference checks its receiver at once ...
        show(() -> thrown(() -> lazyLength(null)));                  // ... the lambda fails only when it is called
        wontCompile("Function<Integer, String> f = Integer::toString;", "reference to toString is ambiguous: static toString(int) and instance toString()");
        note("Use a reference when the lambda only forwards its arguments; otherwise a lambda reads better");

        section("Combining functions");
        syntax(
                "f.andThen(g)  |  f.compose(g)", "g(f(x)): f first  |  f(g(x)): g first",
                "p.negate()  |  p.and(q)  |  p.or(q)", "Predicate combinators; Predicate.not(String::isBlank) reads best with a reference",
                "Function.identity()", "x -> x, the do-nothing function (handy as a key mapper)"
        );
        Function<Integer, Integer> plusOne = x -> x + 1;
        Function<Integer, Integer> timesTwo = x -> x * 2;
        show(() -> plusOne.andThen(timesTwo).apply(5));              // (5 + 1) * 2 = 12: plusOne runs first
        show(() -> plusOne.compose(timesTwo).apply(5));              // 5 * 2 + 1 = 11: timesTwo runs first
        Predicate<String> isBlank = String::isBlank;
        show(() -> isBlank.negate().test("x"));
        show(() -> Predicate.not(String::isBlank).test("  "));       // Predicate.not (Java 11) takes a method reference directly
        Predicate<String> longWord = s -> s.length() > 3;
        show(() -> longWord.and(s -> s.startsWith("a")).test("apple"));
        show(() -> longWord.or(s -> s.startsWith("a")).test("ax"));
        Function<String, String> same = Function.identity();
        show(() -> same.apply("unchanged"));

        section("Higher-order methods: take a function, return a function");
        syntax(
                "static int f(Function<A, B> g, ...)", "takes a function as a parameter (map, filter, forEach are such methods)",
                "static Function<A, B> f(...)", "returns a function: the lambda remembers the effectively final values it captured",
                "Function<A, Function<B, C>>", "currying: take one argument at a time (Java has no (A) -> (B) -> C function types)"
        );
        show(() -> applyTwice(x -> x * 2, 3));                       // f(f(3)) = 12
        show(() -> applyTwice(MethodReferences::inc, 3));            // a named method goes in as a reference
        show(() -> keepIf(List.of(1, 2, 3, 4), x -> x > 2));         // this is all that filter does
        show(() -> operation("*").applyAsInt(6, 7));                 // the strategy pattern: behaviour picked by data
        show(() -> thrown(() -> operation("^")));                    // the default branch throws
        Function<Integer, Integer> triple = multiplier(3);
        show(() -> triple.apply(5));                                 // the returned lambda remembers factor = 3
        Function<Integer, Function<Integer, Integer>> add = a -> b -> a + b;
        show(() -> add.apply(3).apply(4));                           // one argument at a time
        Function<Integer, Integer> add10 = add.apply(10);
        show(() -> add10.apply(5));                                  // partial application: the first argument is fixed
        note("Every function value needs a functional interface: Function<Integer, Integer> where Kotlin writes (Int) -> Int");
    }
}
