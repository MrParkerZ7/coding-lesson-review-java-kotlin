package review.streams;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 4a. STREAMS: map, filter, flatMap, match, find, slice, the everyday operations
 *     Run: mvn -q compile exec:java -Dmain.class=review.streams.MapAndFilter
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class MapAndFilter {

    public static void main(String[] args) {
        title("MAP AND FILTER  -  transform, select, test, find, slice");

        List<Integer> xs = List.of(1, 2, 3, 4, 5, 6);
        List<String> ws = List.of("kiwi", "fig", "apple", "fig");

        section("The shape of a pipeline");
        syntax(
                "source.stream()", "start: collection.stream(), Arrays.stream(a), Stream.of(...), IntStream.range(a, b)",
                ".map(...).filter(...) ...", "INTERMEDIATE steps: lazy, each returns another stream",
                ".toList() | .count() | .sum() ...", "TERMINAL step: runs the whole pipeline and returns the result"
        );
        note("A List has no map() or filter(): go in with stream() and come back with toList(). Nothing runs before the terminal step (4e)");

        section("Transform: map, mapToInt, boxed");
        syntax(
                "xs.stream().map(x -> f(x))", "Stream<R>: every element transformed, same count",
                "mapToInt(f) | mapToObj(f) | boxed()", "to IntStream (no boxing, has sum and max) | IntStream to Stream<R> | to Stream<Integer>"
        );
        show(() -> xs.stream().map(x -> x * x).toList());
        show(() -> ws.stream().map(String::length).toList());                    // a method reference instead of a lambda
        show(() -> ws.stream().mapToInt(String::length).sum());                  // total letters: an IntStream has sum()
        show(() -> IntStream.rangeClosed(1, 5).boxed().toList());                // boxed(): IntStream -> Stream<Integer>
        show(() -> IntStream.range(0, 0).boxed().toList());                      // an empty range is an empty stream
        note("The type column shows Integer for an int and Long for a long: primitives are shown boxed (lesson 0)");
        show(() -> "kiwi".chars().mapToObj(c -> (char) c).toList());             // String.chars() is an IntStream of char codes

        section("Select: filter, distinct, sorted");
        show(() -> xs.stream().filter(x -> x % 2 == 0).toList());
        show(() -> Stream.of("a", "", "b").filter(Predicate.not(String::isEmpty)).toList());   // no filterNot: Predicate.not (Java 11)
        show(() -> Stream.of(1, null, 3).filter(Objects::nonNull).toList());     // drops the nulls
        show(() -> Stream.of(1, "a", 2.5, "b").filter(String.class::isInstance).toList());     // keeps one type (map(String.class::cast) to get List<String>)
        show(() -> ws.stream().distinct().toList());                             // the first occurrence stays, order is kept
        show(() -> ws.stream().sorted().toList());                               // natural order (Strings: alphabetical)
        show(() -> ws.stream().sorted(Comparator.reverseOrder()).toList());

        section("Flatten: flatMap");
        syntax("flatMap(x -> stream)", "map each element to a STREAM, then join them all: map + flatten");
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3), List.of());
        show(() -> nested.stream().flatMap(List::stream).toList());              // a list of lists to one list
        show(() -> Stream.of(1, 2).flatMap(a -> Stream.of(10, 20).map(b -> a + b)).toList());   // two nested loops in one line

        section("Test: anyMatch, allMatch, noneMatch, count");
        show(() -> xs.stream().anyMatch(x -> x > 5));
        show(() -> xs.stream().allMatch(x -> x > 0));
        show(() -> xs.stream().noneMatch(x -> x > 6));
        show(() -> xs.stream().filter(x -> x % 2 == 0).count());                 // count() returns a long
        List<Integer> none = List.of();
        show(() -> none.stream().allMatch(x -> x > 0));                          // TRAP: true on an empty stream, nothing breaks the rule
        show(() -> none.stream().anyMatch(x -> x > 0));                          // nothing can match: false
        show(() -> none.stream().noneMatch(x -> x > 0));                         // and noneMatch is true

        section("Find: findFirst, findAny");
        syntax("findFirst() | findAny()", "an Optional: the match or Optional.empty (not null, not an exception)");
        show(() -> xs.stream().filter(x -> x > 3).findFirst());                  // the match, wrapped in an Optional
        show(() -> xs.stream().filter(x -> x > 99).findFirst());                 // nothing matched: an empty Optional
        show(() -> xs.stream().filter(x -> x > 99).findFirst().orElse(-1));      // a default instead (lesson 7)
        show(() -> thrown(() -> xs.stream().filter(x -> x > 99).findFirst().get()));   // TRAP: get() on an empty Optional throws
        note("findAny is free to return ANY match (that only matters on a parallel stream); findFirst always returns the first");

        section("Slice: limit, skip, takeWhile, dropWhile");
        show(() -> xs.stream().limit(2).toList());
        show(() -> xs.stream().skip(4).toList());
        show(() -> xs.stream().limit(99).toList());                              // no exception: takes what exists
        show(() -> xs.stream().skip(99).toList());                               // an empty list, no exception
        List<Integer> mixed = List.of(1, 2, 5, 1, 6);
        show(() -> mixed.stream().takeWhile(x -> x < 4).toList());               // stops at the FIRST failure (the 5) ...
        show(() -> mixed.stream().filter(x -> x < 4).toList());                  // ... filter keeps scanning: not the same
        show(() -> mixed.stream().dropWhile(x -> x < 4).toList());               // drops while true, then keeps EVERYTHING

        section("toList() is immutable");
        syntax("stream.toList() | collect(Collectors.toList())", "unmodifiable list (Java 16+) | a mutable ArrayList today, but not promised");
        show(() -> thrown(() -> xs.stream().toList().add(7)));                   // read-only
        show(() -> thrown(() -> xs.stream().collect(Collectors.toList()).add(7)));   // works: an ArrayList (do not rely on it)
        show(() -> Stream.of("a", null).toList());                               // toList() accepts null elements, List.of does not

        section("Cost");
        bigO(
                "map | filter | distinct | sorted | toList", "O(n) | O(n) | O(n) with a set | O(n log n) | O(n)",
                "anyMatch | allMatch | findFirst | limit", "O(n) worst case, stop at the first decision",
                "a whole pipeline of map / filter ...", "ONE pass: each element flows through every step (no list per step)",
                "a stream over a few elements", "the setup costs more than a loop: choose by readability"
        );
    }
}
