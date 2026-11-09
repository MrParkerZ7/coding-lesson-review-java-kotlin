package review.methods;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 10c. LAMBDAS and FUNCTIONAL INTERFACES: syntax, java.util.function, your own SAM interface
 *      Run: mvn -q compile exec:java -Dmain.class=review.methods.LambdasAndFunctionalInterfaces
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class LambdasAndFunctionalInterfaces {

    /** One abstract method: a lambda can implement it. */
    @FunctionalInterface
    interface Validator {
        boolean check(String s);

        default Validator negate() { return s -> !check(s); }      // a default method does not count as abstract
    }

    public static void main(String[] args) {
        title("LAMBDAS  -  syntax, java.util.function, your own functional interface");

        section("Lambda syntax");
        syntax(
                "(int a, int b) -> a + b", "typed parameters; an expression body: its value is the result",
                "(a, b) -> a + b", "parameter types inferred from the target type",
                "x -> x * 2", "ONE parameter without a type: the parentheses may go",
                "() -> 42", "no parameters: the parentheses are required",
                "x -> { int y = x * 2; return y; }", "block body: braces and an explicit return"
        );
        Function<Integer, Integer> twice = x -> x * 2;
        show(() -> twice.apply(21));
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        show(() -> add.apply(2, 3));
        Supplier<Integer> answer = () -> 42;
        show(() -> answer.get());
        Function<Integer, String> describe = n -> { int sq = n * n; return "square of " + n + " is " + sq; };
        show(() -> describe.apply(3));
        wontCompile("var f = x -> x * 2;", "cannot infer type for local variable f (lambda expression needs an explicit target-type)");
        wontCompile("Object o = () -> 42;", "Object is not a functional interface");
        note("A lambda has NO type of its own: it becomes the functional interface it is assigned to or passed to");

        section("The standard functional interfaces (java.util.function)");
        syntax(
                "Function<T, R>  |  BiFunction<T, U, R>", "R apply(T)  |  R apply(T, U)      one or two in, one out (map)",
                "UnaryOperator<T>  |  BinaryOperator<T>", "T apply(T)  |  T apply(T, T)      the same type in and out",
                "Predicate<T>  |  BiPredicate<T, U>", "boolean test(T)  |  boolean test(T, U)      (filter)",
                "Supplier<T>", "T get()       nothing in, one out: a lazy value or a factory",
                "Consumer<T>  |  BiConsumer<T, U>", "void accept(T)       one in, nothing out (forEach)",
                "Runnable  |  Callable<V>", "void run()  |  V call() throws Exception       (threads, lesson 16)",
                "Comparator<T>", "int compare(T, T)       sorting (lesson 13)"
        );
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        show(() -> shout.apply("hey"));
        BinaryOperator<Integer> larger = (a, b) -> a > b ? a : b;
        show(() -> larger.apply(4, 9));
        Predicate<String> isLong = s -> s.length() > 3;
        show(() -> isLong.test("lambda"));
        List<String> sink = new ArrayList<>();
        Consumer<String> collect = s -> sink.add(s);
        collect.accept("a");
        collect.accept("b");
        show(() -> sink);                                            // a Consumer works by side effect: the list it filled is the result
        Runnable task = () -> sink.add("ran");
        task.run();
        show(() -> sink);                                            // a Runnable has no result either: it added "ran"
        IntBinaryOperator multiply = (a, b) -> a * b;
        show(() -> multiply.applyAsInt(6, 7));                       // primitive version: no boxing, but the method is applyAsInt, not apply
        note("Int / Long / Double versions exist (IntPredicate, ToIntFunction, IntFunction, ...) to avoid boxing in hot loops");
        note("Callable may throw a checked exception, Runnable and Supplier may not (lesson 15 shows the consequence inside lambdas)");

        section("Your own functional interface");
        syntax(
                "@FunctionalInterface interface V { boolean check(String s); }", "exactly ONE abstract method (SAM): a lambda can implement it",
                "default / static / private methods, Object's methods", "do not count as abstract, so a SAM interface may have them"
        );
        Validator notBlank = s -> !s.isBlank();
        show(() -> notBlank.check("  "));                           // false: a blank string
        Validator blank = notBlank.negate();
        show(() -> blank.check("  "));                              // the default method built a new lambda on top of the first
        wontCompile("@FunctionalInterface interface Two { void a(); void b(); }", "multiple non-overriding abstract methods found in interface Two");
        note("@FunctionalInterface is optional documentation, but the compiler then checks the single-method rule for you");
    }
}
