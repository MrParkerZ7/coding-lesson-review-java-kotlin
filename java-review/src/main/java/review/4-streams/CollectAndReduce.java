package review.streams;

import java.util.Arrays;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
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
 * 4b. STREAMS: sum, max, average, reduce, and collecting into a String, Set or Map
 *     Run: mvn -q compile exec:java -Dmain.class=review.streams.CollectAndReduce
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class CollectAndReduce {

    public static void main(String[] args) {
        title("COLLECT AND REDUCE  -  collapse a stream into one value, a String, a Map");

        List<Integer> xs = List.of(1, 2, 3, 4);
        List<String> ws = List.of("kiwi", "fig", "apple", "fig");

        section("Numbers: sum, max, average on an IntStream");
        syntax(
                "mapToInt(f).sum() | .count()", "total (0 when empty) | how many, a long",
                ".max() | .min() | .average()", "OptionalInt | OptionalInt | OptionalDouble: EMPTY when there are no elements",
                ".summaryStatistics()", "count, sum, min, max and average in ONE pass"
        );
        show(() -> xs.stream().mapToInt(Integer::intValue).sum());
        show(() -> IntStream.empty().sum());                                     // 0, no exception
        show(() -> IntStream.of(3, 9, 2).max());                                 // an OptionalInt, not an int
        show(() -> IntStream.of(3, 9, 2).max().getAsInt());                      // unwrap it
        show(() -> IntStream.empty().max());
        show(() -> thrown(() -> IntStream.empty().max().getAsInt()));            // TRAP: unwrapping an empty OptionalInt throws
        show(() -> xs.stream().mapToInt(Integer::intValue).average());           // an OptionalDouble
        show(() -> IntStream.empty().average().orElse(0));                       // the empty case: a default, not NaN
        IntSummaryStatistics stats = xs.stream().mapToInt(Integer::intValue).summaryStatistics();
        show(() -> stats.getMax());
        show(() -> stats.getAverage());
        show(() -> Stream.of(Integer.MAX_VALUE, 1).mapToInt(Integer::intValue).sum());     // TRAP: int overflow wraps to a negative number
        show(() -> Stream.of(Integer.MAX_VALUE, 1).mapToLong(Integer::longValue).sum());   // sum as a long when the total can be big
        show(() -> ws.stream().max(Comparator.comparingInt(String::length)));    // Stream<T>.max takes a Comparator: the longest word
        show(() -> Stream.of("bb", "aa").max(Comparator.comparingInt(String::length)));    // a tie: the FIRST one wins

        section("reduce: fold the elements into one value");
        syntax(
                "reduce(identity, (acc, x) -> next)", "start from identity: the result is always there",
                "reduce((acc, x) -> next)", "start from the first element: an Optional (empty without elements)",
                "reduce(identity, accumulate, combine)", "the result may have another type than the elements"
        );
        show(() -> xs.stream().reduce(0, Integer::sum));
        show(() -> xs.stream().reduce(1, (acc, x) -> acc * x));                  // product: the identity is 1, not 0
        show(() -> xs.stream().reduce(Integer::max));                            // a hand-made max
        show(() -> Stream.<Integer>empty().reduce(0, Integer::sum));             // safe on empty: returns the identity
        show(() -> Stream.<Integer>empty().reduce(Integer::sum));                // TRAP: an empty Optional, not 0
        show(() -> xs.stream().reduce("", (acc, x) -> acc + x, String::concat)); // Integer elements, String result
        show(() -> ws.stream().reduce(0, (acc, w) -> acc + w.length(), Integer::sum));    // total letters; the combiner is for parallel streams
        int[] prefix = {1, 2, 3, 4};
        Arrays.parallelPrefix(prefix, Integer::sum);   show(() -> prefix);       // running totals in place (no runningFold in Java)

        section("collect: Collectors");
        syntax(
                "toList() | toSet() | toCollection(TreeSet::new)", "into a List | a Set | the collection you choose",
                "joining(sep, prefix, suffix)", "one String from the elements (they must be Strings)",
                "counting() | summingInt(f) | averagingInt(f)", "Long | Integer | Double"
        );
        show(() -> ws.stream().collect(Collectors.toCollection(TreeSet::new)));  // a sorted set without duplicates
        show(() -> ws.stream().collect(Collectors.joining(", ", "[", "]")));
        show(() -> ws.stream().collect(Collectors.counting()));                  // the same number as count()
        show(() -> ws.stream().collect(Collectors.summingInt(String::length)));
        show(() -> ws.stream().collect(Collectors.averagingInt(String::length)));
        show(() -> Stream.<String>empty().collect(Collectors.averagingInt(String::length)));   // 0.0 here, an OptionalDouble on IntStream
        note("The type column says Long for counting(): a counts map is Map<K, Long>, not Map<K, Integer>");

        section("toMap: one value per key");
        syntax(
                "toMap(keyFn, valueFn)", "a HashMap; a repeated key THROWS",
                "toMap(keyFn, valueFn, merge, TreeMap::new)", "merge decides what a repeated key does; the last argument picks the map"
        );
        show(() -> ws.stream().distinct().collect(Collectors.toMap(w -> w, String::length)));   // HashMap: order not promised
        show(() -> thrown(() -> ws.stream().collect(Collectors.toMap(w -> w, String::length))));   // TRAP: "fig" twice
        show(() -> ws.stream().collect(Collectors.toMap(w -> w, String::length, (a, b) -> a, TreeMap::new)));   // keep the first value
        show(() -> ws.stream().collect(Collectors.toMap(w -> w.charAt(0), w -> 1, Integer::sum, TreeMap::new)));   // merge = count by first letter

        section("Cost");
        bigO(
                "sum | max | average | reduce | count", "O(n) time, O(1) extra space: one pass",
                "summaryStatistics", "O(n): all five numbers from one pass",
                "collect into a List | Set | Map", "O(n) time, O(n) space",
                "sorted", "O(n log n); it must see every element before it passes one on"
        );
    }
}
