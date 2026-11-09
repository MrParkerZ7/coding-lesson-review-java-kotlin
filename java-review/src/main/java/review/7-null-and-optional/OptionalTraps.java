package review.nullsafety;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 7c. OPTIONAL TRAPS: get(), Optional as a parameter, Optional vs OptionalInt, equals, null instead of empty
 *     Run: mvn -q compile exec:java -Dmain.class=review.nullsafety.OptionalTraps
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class OptionalTraps {

    /** BAD design: the Optional parameter can itself be null, so the caller can still crash it. */
    static String greet(Optional<String> name) {
        return "Hello " + name.orElse("stranger");
    }

    /** BAD: promises an Optional and returns null. The caller trusts the type and calls a method on null. */
    static Optional<String> brokenLookup(boolean found) {
        return found ? Optional.of("hit") : null;
    }

    /** GOOD: "no result" is Optional.empty(), never null. */
    static Optional<String> goodLookup(boolean found) {
        return found ? Optional.of("hit") : Optional.empty();
    }

    public static void main(String[] args) {
        title("OPTIONAL TRAPS  -  how Optional code goes wrong");

        section("get() without a check");
        syntax(
                "opt.get()", "the value, or NoSuchElementException: nothing forces you to check first",
                "opt.orElseThrow()", "the same behaviour with an honest name (Java 10): prefer it to get()",
                "opt.orElse(null)", "gives the null back: you are where you started"
        );
        Optional<Integer> age = Optional.of(31);
        Optional<Integer> noAge = Optional.empty();
        show(() -> thrown(() -> noAge.get()));
        show(() -> age.isPresent() ? age.get() : -1);                   // works, but it is a null check in disguise
        show(() -> age.orElse(-1));                                     // the same in one call
        show(() -> noAge.orElse(null));                                 // TRAP: null again, and the next call on it throws

        section("Not for parameters, fields or collections");
        show(() -> thrown(() -> greet(null)));                          // TRAP: the Optional parameter itself was null
        Object box = Optional.of(1);
        show(() -> box instanceof Serializable);                        // false: Optional is not Serializable, so a field of that type breaks serialization
        note("Use Optional as a RETURN type only. For 'maybe a value' parameters use an overload; for collections return an empty one");

        section("Optional<Integer> versus OptionalInt");
        syntax(
                "OptionalInt | OptionalLong | OptionalDouble", "primitive boxes returned by IntStream: getAsInt(), no boxing, no map/filter",
                "Optional<Integer>", "the generic box: get(), map, filter, but each value is a boxed Integer"
        );
        show(() -> IntStream.of(4, 8, 1).max());
        show(() -> IntStream.of().max());                               // empty stream: empty box
        show(() -> IntStream.of(4, 8, 1).average());                    // average() is always an OptionalDouble (empty input has no average)
        show(() -> IntStream.of(4, 8, 1).max().getAsInt());
        show(() -> thrown(() -> IntStream.of().max().getAsInt()));
        show(() -> IntStream.of().max().orElse(-1));
        show(() -> Stream.of(4, 8, 1).max(Integer::compare));           // a Stream<Integer> needs a Comparator and gives Optional<Integer>
        wontCompile("Optional<int> box;", "a type argument must be a reference type: Optional<Integer>, or OptionalInt");
        wontCompile("Integer v = Optional.of(5).orElse(\"none\");", "orElse takes the Optional's own type: a String is not an Integer");

        section("equals, and null instead of empty");
        show(() -> Optional.of(1).equals(Optional.of(1)));              // equal when both hold equal values
        show(() -> Optional.empty().equals(Optional.empty()));
        show(() -> Optional.of(1).equals(1));                           // false: a box is never equal to its bare value
        show(() -> Optional.of(1) == Optional.of(1));                   // TRAP: false, two boxes: compare with equals, never ==
        show(() -> thrown(() -> brokenLookup(false).isPresent()));      // TRAP: the caller trusted the Optional type; the method returned null
        show(() -> goodLookup(false).isPresent());
        note("Never return null from a method that returns Optional: return Optional.empty()");
    }
}
