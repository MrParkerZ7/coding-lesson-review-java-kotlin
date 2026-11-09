package review.streams;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

/**
 * 4d. STREAMS: no zip, no windowed, no chunked in Java. IntStream.range gives the index, and the rest is built by hand
 *     Run: mvn -q compile exec:java -Dmain.class=review.streams.ZipAndWindow
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ZipAndWindow {

    /** zip: pair up the elements of two lists; stops at the SHORTER one. O(n). */
    static <A, B> List<Map.Entry<A, B>> zip(List<A> as, List<B> bs) {
        return IntStream.range(0, Math.min(as.size(), bs.size())).mapToObj(i -> Map.entry(as.get(i), bs.get(i))).toList();
    }

    /** windowed: every run of k neighbours (n - k + 1 windows); none when k is bigger than the list. O(n * k) with the copies. */
    static <T> List<List<T>> windows(List<T> xs, int k) {
        return IntStream.rangeClosed(0, xs.size() - k).mapToObj(i -> List.copyOf(xs.subList(i, i + k))).toList();
    }

    /** chunked: blocks of k that do not overlap; the last one may be shorter. O(n). */
    static <T> List<List<T>> chunks(List<T> xs, int k) {
        return IntStream.range(0, (xs.size() + k - 1) / k).mapToObj(i -> List.copyOf(xs.subList(i * k, Math.min(xs.size(), (i + 1) * k)))).toList();
    }

    static int sum(List<Integer> xs) {
        return xs.stream().mapToInt(Integer::intValue).sum();
    }

    public static void main(String[] args) {
        title("ZIP AND WINDOW  -  pairs, neighbours, sliding windows, join");

        List<Integer> nums = List.of(1, 2, 3, 4, 5);
        List<String> words = List.of("kiwi", "fig", "apple");

        section("Index tricks: IntStream.range");
        syntax(
                "IntStream.range(0, n).mapToObj(i -> ...)", "a loop with an index, as a stream: withIndex, zip",
                "IntStream.range(0, n).filter(i -> ...)", "pick positions: every second element",
                "IntStream.range(1, n).map(i -> xs.get(i) - xs.get(i - 1))", "neighbours: zipWithNext, the differences"
        );
        show(() -> IntStream.range(0, words.size()).mapToObj(i -> i + ":" + words.get(i)).toList());   // withIndex
        show(() -> IntStream.range(0, words.size()).filter(i -> i % 2 == 0).mapToObj(words::get).toList());   // every second element
        show(() -> IntStream.range(1, nums.size()).map(i -> nums.get(i) - nums.get(i - 1)).boxed().toList());   // differences of neighbours
        show(() -> IntStream.range(1, nums.size()).allMatch(i -> nums.get(i - 1) <= nums.get(i)));   // is it sorted? (an empty or 1-element list: true)
        show(() -> zip(nums, words));                                            // TRAP: stops at the shorter list, the 4 and 5 are dropped silently
        show(() -> Stream.of(0, 2, 4).map(nums::get).toList());                  // pick positions in any order
        show(() -> words.reversed());                                            // Java 21; before: Collections.reverse on a copy

        section("Sliding windows and chunks");
        syntax(
                "windows(xs, k)", "the helper above: sliding windows of k (Kotlin: windowed)",
                "chunks(xs, k)", "the helper above: blocks that do not overlap (Kotlin: chunked)"
        );
        show(() -> windows(nums, 3));                                            // n - k + 1 = 3 windows
        show(() -> windows(nums, 7));                                            // TRAP: a window bigger than the list gives an EMPTY list
        show(() -> windows(nums, 3).stream().map(ZipAndWindow::sum).toList());   // sliding-window sums (lesson 2 does it in O(n) with a running total)
        show(() -> windows(nums, 2).stream().allMatch(w -> w.get(0) < w.get(1)));   // strictly increasing?
        show(() -> chunks(nums, 2));                                             // the last block is shorter
        show(() -> chunks(nums, 2).stream().map(ZipAndWindow::sum).toList());    // sum per block
        note("subList gives a window without copying, but it is a VIEW of the list (3f): copy it (List.copyOf) if the list can change");

        section("Join into a String");
        syntax(
                "String.join(sep, strings)", "for a List<String> (or any Iterable of CharSequence)",
                "stream.map(String::valueOf).collect(Collectors.joining(sep))", "for anything else"
        );
        show(() -> String.join("-", words));
        show(() -> nums.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]")));   // prefix and suffix
        show(() -> String.join(",", List.of()));                                 // an empty list: the empty string, no exception
        show(() -> nums.toString());                                             // List.toString: the quickest debug print

        section("Cost");
        bigO(
                "zip | withIndex | differences | pick positions", "O(n): one pass, get(i) is O(1) on an ArrayList or List.of",
                "chunks(xs, k)", "O(n): each element is copied once",
                "windows(xs, k)", "O(n * k): k elements copied per window; use a running total to get O(n)",
                "get(i) inside range(...) on a LinkedList", "O(n) per call, O(n^2) overall: use an ArrayList"
        );
    }
}
