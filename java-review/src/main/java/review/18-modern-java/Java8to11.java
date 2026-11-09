package review.modern;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 18a. WHAT IS NEW IN JAVA 8 TO 11
 *      Run: mvn -q compile exec:java -Dmain.class=review.modern.Java8to11
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class Java8to11 {

    /** One abstract method, so a lambda can implement it. default (Java 8) and private (Java 9) methods share code. */
    interface Greeter {
        String name();

        default String greet() {
            return prefix() + name();
        }

        private String prefix() {
            return "Hello, ";
        }

        static Greeter of(String name) {
            return () -> name;
        }
    }

    public static void main(String[] args) {
        title("JAVA 8 TO 11  -  lambdas, collections factories, var, String methods");

        section("Version table: feature -> Java version");
        syntax(
                "Java 8  (LTS)", "lambdas, method references, streams, Optional, default and static interface methods, java.time",
                "Java 9", "modules (JPMS), List.of / Set.of / Map.of, private interface methods, takeWhile / dropWhile, Optional.or / ifPresentOrElse",
                "Java 10", "var for local variables, List.copyOf, Collectors.toUnmodifiableList, Optional.orElseThrow()",
                "Java 11  (LTS)", "String isBlank / strip / lines / repeat, Predicate.not, Files.readString, HTTP client, var in lambda parameters",
                "after 11", "Java 17 (LTS) is Java14to17, Java 21 (LTS) is Java21; dates and times are in JavaTime"
        );

        section("Java 8: lambdas, streams, Optional, interface methods");
        note("Lambdas and functional interfaces are lesson 10, streams are lesson 4, Optional is lesson 7: this is only the recap");
        show(() -> Greeter.of("Ann").greet());                             // a static factory, a lambda as the implementation, a default method
        show(() -> Optional.<String>empty().map(String::length).orElse(-1));   // Optional: a value that may be absent, without null
        show(() -> Stream.of("b", "a", "c").sorted().collect(Collectors.toList()));   // the Java 8 way to collect (Java 16 added toList())
        note("The default method lets an interface grow without breaking its implementors (that is why List got sort and forEach)");

        section("Java 9: collection factories, takeWhile, Optional");
        syntax(
                "List.of(..) | Set.of(..) | Map.of(k, v, ..)", "immutable collections in one line; no null allowed",
                "Stream.iterate(seed, hasNext, next)", "a stream that ends, like a for loop",
                "stream.takeWhile(p) | dropWhile(p)", "the prefix while p holds | everything after it"
        );
        show(() -> thrown(() -> List.of(1).add(2)));                       // immutable: UnsupportedOperationException
        show(() -> thrown(() -> List.of(1, null)));                        // null elements are refused
        show(() -> Stream.iterate(1, n -> n < 100, n -> n * 2).collect(Collectors.toList()));   // 1, 2, 4 ... while n < 100
        show(() -> Stream.of(1, 2, 3, 10, 1).takeWhile(n -> n < 5).collect(Collectors.toList()));   // stops at the first 10
        show(() -> Stream.of(1, 2, 3, 10, 1).dropWhile(n -> n < 5).collect(Collectors.toList()));   // starts at the first 10
        show(() -> Optional.<String>empty().or(() -> Optional.of("fallback")).get());   // or: an alternative Optional
        List<String> seen = new ArrayList<>();
        Optional.of("x").ifPresentOrElse(seen::add, () -> seen.add("empty"));
        show(() -> seen);                                                  // ifPresentOrElse runs exactly one of the two actions
        show(() -> Objects.requireNonNullElse(null, "default"));
        note("Map.of / Set.of iterate in an order that changes from run to run: never depend on it (use LinkedHashMap or TreeMap)");

        section("Java 10: var");
        syntax("var name = expression;", "the compiler infers the type from the right side; local variables only, not fields or parameters");
        var list = new ArrayList<String>();
        list.add("kept as ArrayList<String>");
        show(() -> list);                                                  // var is NOT dynamic typing: list has the exact static type ArrayList<String>
        wontCompile("var x;", "cannot use 'var' on variable without initializer");
        wontCompile("var n = null;", "variable initializer is 'null'");
        wontCompile("var f = () -> 1;", "lambda expression needs an explicit target-type");
        wontCompile("var arr = {1, 2};", "array initializer needs an explicit target-type");
        List<Integer> original = new ArrayList<>(List.of(1, 2));
        List<Integer> copy = List.copyOf(original);
        original.add(3);
        show(() -> copy);                                                  // List.copyOf is an immutable snapshot: the later add is not in it
        show(() -> thrown(() -> Optional.empty().orElseThrow()));          // orElseThrow() with no argument: NoSuchElementException
        note("Use var when the type is obvious from the right side (new ArrayList<String>(), a constructor); spell it out when it is not");

        section("Java 11: String methods, Predicate.not, var in lambdas");
        syntax(
                "isBlank() | strip() | lines() | repeat(n)", "blank test (spaces too) | trim that knows Unicode spaces | split into lines | repeat",
                "Predicate.not(pred)", "negate a method reference: filter(Predicate.not(String::isBlank))",
                "(var a, var b) -> ...", "var in lambda parameters, mainly to put an annotation on them"
        );
        show(() -> "   ".isBlank());                                       // true: isEmpty() would say false
        show(() -> "  hi  ".strip());
        show(() -> "a\nb\nc".lines().count());
        show(() -> "ab".repeat(3));
        show(() -> Stream.of("a", " ", "", "b").filter(Predicate.not(String::isBlank)).collect(Collectors.toList()));
        BinaryOperator<Integer> add = (var a, var b) -> a + b;
        show(() -> add.apply(2, 3));
        show(() -> Optional.empty().isEmpty());                            // Optional.isEmpty() arrived in 11 (isPresent was the only form before)
        note("Also in 11: Files.readString / writeString, the standard HTTP client (java.net.http), and java File.java to run one source file");
        note("Removed in 11: the Java EE modules (JAXB, JAX-WS) left the JDK, so old code needs them as separate dependencies");
    }
}
