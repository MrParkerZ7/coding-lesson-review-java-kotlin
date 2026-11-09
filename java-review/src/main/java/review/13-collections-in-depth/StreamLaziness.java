package review.collectionsdepth;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 13d. STREAM LAZINESS: eager loops vs lazy pipelines, short-circuiting, endless streams
 *      Run: mvn -q compile exec:java -Dmain.class=review.collectionsdepth.StreamLaziness
 *      Or click the green arrow beside `main` in IntelliJ.
 *      (The stream operators themselves are lesson 4; the single-use trap is in StreamTraps there.)
 */
public class StreamLaziness {

    /** Two loops, one after the other: each step finishes the WHOLE list before the next starts. */
    static String eagerOrder() {
        List<String> steps = new ArrayList<>();
        List<Integer> mapped = new ArrayList<>();
        for (int x : List.of(1, 2, 3, 4)) {
            steps.add("map" + x);
            mapped.add(x * 2);
        }
        List<Integer> kept = new ArrayList<>();
        for (int x : mapped) {
            steps.add("filter" + x);
            if (x > 2) kept.add(x);
        }
        return String.join(" ", steps);
    }

    /** The same two steps as a stream: each element goes through the whole chain, and findFirst stops the work. */
    static String lazyOrder() {
        List<String> steps = new ArrayList<>();
        List.of(1, 2, 3, 4).stream()
                .map(x -> { steps.add("map" + x); return x * 2; })
                .filter(x -> { steps.add("filter" + x); return x > 2; })
                .findFirst();
        return String.join(" ", steps);
    }

    public static void main(String[] args) {
        title("STREAM LAZINESS  -  nothing runs until the terminal operation");

        section("Eager steps vs one lazy pipeline");
        syntax(
                "loop per step", "eager: step 1 finishes for ALL elements, then step 2 starts (and each step builds a whole new list)",
                "stream().map(..).filter(..)", "intermediate operations only DESCRIBE the work; nothing runs yet",
                "findFirst() | toList() | count() | forEach(..)", "a terminal operation starts it: each element runs through the whole chain, one at a time"
        );
        show(() -> eagerOrder());                                    // all 4 maps, then all 4 filters: 8 calls
        show(() -> lazyOrder());                                     // element by element, and findFirst stops at the first match: 4 calls
        List<String> log = new ArrayList<>();
        Stream<Integer> pipeline = Stream.of(1, 2, 3).map(x -> { log.add("map" + x); return x * 2; });
        show(() -> log);                                             // []: building the pipeline ran nothing
        show(() -> pipeline.toList());                               // toList() is terminal: now it runs
        show(() -> log);
        note("Kotlin lists are eager and Sequences lazy. In Java a plain loop is eager and a Stream is the lazy one (like a Sequence)");

        section("Endless and huge sources are fine when something stops them");
        syntax(
                "Stream.iterate(seed, f)  |  iterate(seed, hasNext, f)", "each element from the previous one; the 3-argument form ends by itself",
                "Stream.generate(supplier)", "an endless stream of supplied values",
                "limit(n) | takeWhile(p) | findFirst() | anyMatch(p)", "short-circuiting operations: they can finish an endless stream"
        );
        show(() -> Stream.iterate(1, x -> x * 2).limit(6).toList());   // powers of two: limit(6) ends the endless stream
        show(() -> Stream.iterate(1, x -> x < 100, x -> x * 3).toList());   // the 3-argument form stops by itself
        show(() -> Stream.generate(() -> "z").limit(3).toList());
        show(() -> Stream.iterate(1, x -> x * 2).filter(x -> x > 100).findFirst());   // 128: found after looking at 8 numbers
        show(() -> Stream.iterate(new int[] {0, 1}, f -> new int[] {f[1], f[0] + f[1]}).limit(8).map(f -> f[0]).toList());   // Fibonacci
        show(() -> IntStream.rangeClosed(1, Integer.MAX_VALUE).filter(x -> x % 3 == 0).limit(3).boxed().toList());   // instant: only 1..9 are touched

        section("Short-circuit: stop as soon as the answer is known");
        AtomicInteger visited = new AtomicInteger();
        int firstBig = IntStream.rangeClosed(1, 1_000_000).peek(x -> visited.incrementAndGet()).filter(x -> x > 3).findFirst().getAsInt();
        show(() -> firstBig);
        show(() -> visited.get());                                   // 4 of 1,000,000 elements were touched
        List<Integer> seen = new ArrayList<>();
        int smallest = Stream.of(3, 1, 2).peek(seen::add).sorted().findFirst().get();
        show(() -> smallest);
        show(() -> seen);                                            // [3, 1, 2]: sorted() is a BARRIER, it reads every element before it hands out the first
        note("map, filter, limit, takeWhile, distinct stay lazy; sorted() buffers everything first. peek is for looking while debugging, not for logic");

        section("When a stream wins and when a loop does");
        note("WINS: long chains (filter, map, collect), grouping and counting, endless or huge sources with an early exit; reads like a description");
        note("LOSES: tiny lists (setup cost), a single simple loop, index-based or stateful logic, several results from one pass, breaking out of nested loops");
        note("A stream can be consumed ONCE (a second terminal operation throws IllegalStateException: lesson 4)");

        section("Cost");
        bigO(
                "each intermediate operation (map, filter ...)", "O(1) to add to the pipeline: the work happens at the terminal operation",
                "a whole pipeline of k operations over n elements", "O(n * k) in ONE pass, no intermediate lists",
                "findFirst / limit / anyMatch", "O(m): only the m elements up to the answer",
                "sorted()", "O(n log n) and buffers all n elements"
        );
    }
}
