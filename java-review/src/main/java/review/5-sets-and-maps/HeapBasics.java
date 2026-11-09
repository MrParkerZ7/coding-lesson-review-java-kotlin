package review.sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

/**
 * 5g. HEAP: PriorityQueue always hands out the smallest (or largest) first
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.HeapBasics
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class HeapBasics {

    /** Empties the heap with poll(): the values come out in priority order. O(n log n). */
    static List<Integer> drain(PriorityQueue<Integer> heap) {
        List<Integer> out = new ArrayList<>();
        while (!heap.isEmpty()) out.add(heap.poll());
        return out;
    }

    /** The k largest values, biggest first: keep a MIN-heap of size k, so the smallest of the k is the one to throw out. O(n log k). */
    static List<Integer> kLargest(int[] nums, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int x : nums) {
            heap.offer(x);
            if (heap.size() > k) heap.poll();                                    // drop the smallest of the k + 1
        }
        List<Integer> out = drain(heap);                                         // ascending
        Collections.reverse(out);
        return out;
    }

    public static void main(String[] args) {
        title("HEAP  -  PriorityQueue");

        section("Min-heap and max-heap");
        syntax(
                "new PriorityQueue<>()", "min-heap: the smallest comes out first (natural order)",
                "new PriorityQueue<>(Comparator.reverseOrder())", "max-heap: the largest comes out first",
                "offer(x) | poll() | peek()", "insert | remove the top (null if empty) | look at the top (null if empty)"
        );
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(List.of(5, 1, 4, 2, 3));
        show(() -> minHeap.peek());                                              // 1: the smallest
        show(() -> minHeap);                                                     // TRAP: printing / iterating shows the heap array, NOT sorted order
        show(() -> minHeap.size());
        show(() -> drain(minHeap));                                              // poll() until empty gives sorted order
        show(() -> minHeap.size());                                              // and the heap is now empty
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.addAll(List.of(5, 1, 4));     show(() -> maxHeap.peek());        // 5: the largest comes first
        show(() -> new PriorityQueue<Integer>().poll());                         // null on an empty heap
        note("Comparator (a, b) -> b - a can overflow for big values: use Comparator.reverseOrder() or Integer.compare(b, a) (lesson 13)");

        section("A heap of arrays: order by one field");
        PriorityQueue<int[]> jobs = new PriorityQueue<int[]>(Comparator.comparingInt(job -> job[1]));   // job = {id, priority}
        jobs.offer(new int[] {1, 30});
        jobs.offer(new int[] {2, 10});
        jobs.offer(new int[] {3, 20});
        show(() -> jobs.poll()[0]);                                              // job 2 has the smallest priority number
        show(() -> jobs.poll()[0]);
        note("The heap never re-sorts after a field of an element changes: remove the element and offer it again");

        section("Interview pattern: k largest");
        show(() -> kLargest(new int[] {5, 1, 9, 3, 7}, 2));                      // [9, 7]
        show(() -> kLargest(new int[] {4, 4, 2}, 2));                            // duplicates count as separate values
        show(() -> kLargest(new int[] {3, 1}, 5));                               // k bigger than the array: everything, sorted
        show(() -> kLargest(new int[] {}, 3));                                   // empty input

        section("Cost");
        bigO(
                "PriorityQueue offer | poll", "O(log n)",
                "PriorityQueue peek | size", "O(1)",
                "new PriorityQueue<>(collection)", "O(n): the heap is built in one pass",
                "contains | remove(Object)", "O(n): the heap is not searchable",
                "kLargest with a heap of size k", "O(n log k) time, O(k) space (sorting everything: O(n log n))"
        );
    }
}
