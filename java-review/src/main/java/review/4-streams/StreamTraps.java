package review.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 4e. STREAM TRAPS: single use, lazy order, peek, changing the source, parallel, infinite streams
 *     Run: mvn -q compile exec:java -Dmain.class=review.streams.StreamTraps
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class StreamTraps {

    public static void main(String[] args) {
        title("STREAM TRAPS  -  what surprises people in interviews");

        section("A stream is used once");
        Stream<Integer> once = Stream.of(1, 2, 3);
        show(() -> once.count());                                                // the first terminal step works
        show(() -> thrown(() -> once.count()));                                  // TRAP: the second throws (stream has already been operated upon)
        Stream<Integer> reused = Stream.of(1, 2, 3);
        reused.filter(x -> x > 1);
        show(() -> thrown(() -> reused.map(x -> x * 2)));                        // even a second INTERMEDIATE step on the same variable throws
        Supplier<Stream<Integer>> fresh = () -> Stream.of(1, 2, 3);
        show(() -> fresh.get().count());                                         // the fix: make a new stream each time (list.stream() again)
        show(() -> fresh.get().mapToInt(x -> x).sum());

        section("Lazy: nothing runs until the terminal step");
        List<String> log = new ArrayList<>();
        Stream<Integer> pipeline = Stream.of(1, 2, 3, 4)
                .filter(x -> { log.add("filter " + x); return x % 2 == 0; })
                .map(x -> { log.add("map " + x); return x * 10; });
        show(() -> log);                                                         // [] : the steps are only described so far
        List<Integer> out = pipeline.toList();
        show(() -> log);                                                         // element by element, not step by step: filter 1, filter 2, map 2 ...
        show(() -> out);
        List<String> order = new ArrayList<>();
        Stream.of(3, 1, 2).peek(x -> order.add("a" + x)).sorted().peek(x -> order.add("b" + x)).toList();
        show(() -> order);                                                       // sorted() must see EVERY element before it passes one on
        List<String> seen = new ArrayList<>();
        show(() -> Stream.of(5, 6, 7, 8).peek(x -> seen.add("saw " + x)).filter(x -> x > 5).findFirst());
        show(() -> seen);                                                        // findFirst stopped early: 7 and 8 were never visited

        section("peek is for debugging, not for logic");
        List<String> peeked = new ArrayList<>();
        show(() -> List.of(1, 2, 3).stream().peek(x -> peeked.add("peek " + x)).count());
        show(() -> peeked);                                                      // TRAP: []. count() knows the size already, so peek never ran (Java 9+)
        List<String> filtered = new ArrayList<>();
        show(() -> List.of(1, 2, 3).stream().filter(x -> x > 0).peek(x -> filtered.add("peek " + x)).count());
        show(() -> filtered);                                                    // after a filter the size is unknown, so the elements ARE visited
        note("Do the work in map / filter / collect, and use peek only to print while debugging");

        section("Do not change the source while streaming");
        List<Integer> source = new ArrayList<>(List.of(1, 2, 3));
        show(() -> thrown(() -> source.stream().map(x -> source.add(x)).toList()));   // the source must not be modified by the pipeline
        note("forEach(list::add) into an outside list is a side effect: use collect or toList(), which are safe in parallel too");

        section("Parallel streams: a tool, not a default");
        show(() -> IntStream.rangeClosed(1, 1000).parallel().sum());             // a stateless sum is safe and exact
        show(() -> IntStream.rangeClosed(1, 5).parallel().boxed().toList());     // toList and collect keep the encounter order
        note("forEach on a parallel stream has no order (use forEachOrdered); adding to a shared ArrayList from it is a data race");
        note("All parallel streams share ONE common pool: blocking tasks starve each other, and small data gets slower. Measure first");

        section("Infinite streams need a limit");
        syntax(
                "Stream.iterate(seed, f)", "seed, f(seed), f(f(seed)) ... never ends: add limit() or takeWhile()",
                "Stream.iterate(seed, hasNext, f)", "Java 9: finite, like for (x = seed; hasNext(x); x = f(x))",
                "Stream.generate(supplier)", "an endless stream of supplied values"
        );
        show(() -> Stream.iterate(1, x -> x * 2).limit(6).toList());
        show(() -> Stream.iterate(1, x -> x < 100, x -> x * 3).toList());
        show(() -> Stream.generate(() -> "x").limit(3).toList());
        show(() -> Stream.iterate(1, x -> x + 1).takeWhile(x -> x * x < 30).toList());
        show(() -> Stream.iterate(1, x -> x + 1).filter(x -> x % 7 == 0).findFirst());   // short-circuiting ends it: Optional[7]
        note("limit() must come BEFORE sorted(), count() or collect() on an infinite stream, or the program never finishes");

        section("Checked exceptions inside a lambda");
        wontCompile("Stream.of(1).map(x -> { throw new Exception(); })", "a lambda for map may not throw a checked exception: catch it inside the lambda");
        note("No zip, windowed or chunked in the JDK: see 4d. Streams and exceptions: lesson 15");

        section("Cost");
        bigO(
                "each stage (map, filter, ...)", "O(n) in total: ONE pass for the whole pipeline",
                "sorted | distinct", "O(n log n) | O(n) with a set: both keep every element in memory",
                "Stream<Integer> vs IntStream", "boxing every element costs memory and time: prefer mapToInt for numbers",
                "a stream on a few elements", "the setup (lambdas, pipeline objects) costs more than a loop"
        );
    }
}
