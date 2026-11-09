package review.methods;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 10d. CAPTURE and `this`: what a lambda may read, effectively final, lambda vs anonymous class
 *      Run: mvn -q compile exec:java -Dmain.class=review.methods.CaptureAndThis
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class CaptureAndThis {

    static int staticCounter = 0;                                   // a field: a lambda may change it freely

    /** Each call makes a counter with its OWN state, kept in a one-element array. */
    static Supplier<Integer> makeCounter() {
        int[] count = {0};
        return () -> ++count[0];
    }

    static class Owner {
        private final String name = "owner";

        String viaLambda() {
            Supplier<String> s = () -> this.name;                    // `this` in a lambda is the enclosing Owner
            return s.get();
        }

        String viaAnonymous() {
            Supplier<String> s = new Supplier<>() {
                private final String name = "anonymous";
                @Override public String get() { return this.name + " / outer: " + Owner.this.name; }   // `this` is the anonymous object
            };
            return s.get();
        }
    }

    public static void main(String[] args) {
        title("CAPTURE AND this  -  effectively final, closures, lambda vs anonymous class");

        section("A lambda captures a COPY: the variable must be effectively final");
        syntax(
                "effectively final", "a local that is assigned once and never changed (the word final is not needed)",
                "a lambda may read", "locals and parameters that are effectively final, and any field (also change fields)",
                "to count inside a lambda", "keep the state in an object: int[1], AtomicInteger, a list, or a field"
        );
        int base = 10;
        Function<Integer, Integer> addBase = x -> x + base;
        show(() -> addBase.apply(5));                                // 15: the lambda holds the VALUE 10
        wontCompile("int n = 0; Runnable r = () -> n++;", "local variables referenced from a lambda expression must be final or effectively final");
        wontCompile("int n = 0; Runnable r = () -> p(n); n = 5;", "the same error: a LATER assignment also breaks 'effectively final'");
        int[] box = {0};
        Runnable inc = () -> box[0]++;
        inc.run();
        inc.run();
        show(() -> box[0]);                                          // 2: the array variable is final, its content is not
        AtomicInteger hits = new AtomicInteger();
        Runnable hit = () -> hits.incrementAndGet();
        hit.run();
        hit.run();
        hit.run();
        show(() -> hits.get());                                      // 3: AtomicInteger is the same trick, and also thread-safe
        Runnable bump = () -> staticCounter++;
        bump.run();
        show(() -> staticCounter);                                   // 1: a lambda can change a FIELD (only locals are restricted)
        note("Kotlin lambdas capture the variable itself and may change it; a Java lambda captures its value, hence the rule");

        section("Closures and loop variables");
        Supplier<Integer> a = makeCounter();
        Supplier<Integer> b = makeCounter();
        a.get();
        a.get();
        show(() -> a.get());                                         // 3: counter a keeps its own count
        show(() -> b.get());                                         // 1: counter b has another array
        wontCompile("for (int i = 0; i < 3; i++) tasks.add(() -> i);", "i changes with i++, so it is not effectively final");
        List<Supplier<Integer>> tasks = new ArrayList<>();
        for (int i : new int[] {1, 2, 3}) tasks.add(() -> i);        // an enhanced for makes a NEW variable each round, which is effectively final
        List<Integer> results = new ArrayList<>();
        for (Supplier<Integer> t : tasks) results.add(t.get());
        show(() -> results);                                         // [1, 2, 3]
        note("In a classic for loop, copy the counter first: int copy = i; then capture copy");

        section("`this` and scope: lambda vs anonymous class");
        syntax(
                "lambda body", "same scope as the code around it: `this` is the enclosing object, no new variable may reuse a local's name",
                "new Interface() { ... }", "a new class with its own `this`, its own fields, and its own scope (a parameter may shadow a local)"
        );
        Owner owner = new Owner();
        show(() -> owner.viaLambda());                               // owner: this.name read the Owner's field
        show(() -> owner.viaAnonymous());                            // the anonymous object's own field, and Owner.this reaches the outer one
        int level = 1;
        Function<Integer, Integer> anon = new Function<>() { @Override public Integer apply(Integer level) { return level * 10; } };
        show(() -> anon.apply(5));                                   // 50: the anonymous class has its own scope, the parameter hides the local
        wontCompile("int x = 1; Function<Integer, Integer> f = x -> x;", "variable x is already defined: a lambda cannot hide a local");
        note("Rule of thumb: a one-method interface = lambda; several methods, or its own state, or its own `this` = anonymous class");
        note("A lambda or anonymous class that USES the outer object keeps it alive while it lives (memory-leak hint, lesson 11 NestedClasses)");
    }
}
