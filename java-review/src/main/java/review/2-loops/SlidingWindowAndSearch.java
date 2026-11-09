package review.loops;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 2e. SLIDING WINDOW, PREFIX SUMS, BINARY SEARCH (more loop patterns; the first ones are 2d, LoopPatterns)
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.SlidingWindowAndSearch
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class SlidingWindowAndSearch {

    /** Sliding window: best sum of k neighbours, updating the sum instead of recomputing it. O(n). */
    public static int maxWindowSum(int[] nums, int k) {
        if (k <= 0 || k > nums.length) throw new IllegalArgumentException("k must be 1.." + nums.length);
        int window = 0;
        for (int i = 0; i < k; i++) window += nums[i];
        int best = window;
        for (int i = k; i < nums.length; i++) {
            window += nums[i] - nums[i - k];           // add the new element, drop the old one
            if (window > best) best = window;
        }
        return best;
    }

    /** Prefix sums: prefix[i] is the sum of the first i elements; any range sum is one subtraction. O(n). */
    public static int[] prefixSums(int[] nums) {
        int[] prefix = new int[nums.length + 1];
        for (int i = 0; i < nums.length; i++) prefix[i + 1] = prefix[i] + nums[i];
        return prefix;
    }

    /** Binary search with a loop: halve the range each round. Returns the index or -1. O(log n). */
    public static int binarySearch(int[] sorted, int target) {
        int low = 0;
        int high = sorted.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;          // avoids the int overflow of (low + high) / 2
            if (sorted[mid] == target) return mid;
            if (sorted[mid] < target) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }

    public static void main(String[] args) {
        title("SLIDING WINDOW, PREFIX SUMS, BINARY SEARCH");

        section("Sliding window and prefix sums");
        syntax(
                "window += nums[i] - nums[i - k];", "slide: add the new element, drop the old one",
                "prefix[i + 1] = prefix[i] + nums[i];", "range sum (a..b) = prefix[b + 1] - prefix[a]"
        );
        show(() -> maxWindowSum(new int[] {2, 1, 5, 1, 3, 2}, 3));     // best 3 in a row: 5 + 1 + 3
        show(() -> thrown(() -> maxWindowSum(new int[] {1, 2}, 3)));   // k larger than the array: guard it
        int[] prefix = prefixSums(new int[] {3, 1, 4, 1, 5});
        show(() -> prefix);
        show(() -> prefix[4] - prefix[1]);                             // sum of elements 1..3 = 1 + 4 + 1

        section("Binary search");
        syntax("while (low <= high) { mid = low + (high - low) / 2; ... }", "halve the range each round: needs SORTED input");
        int[] sorted = {1, 3, 5, 7, 9, 11};
        show(() -> binarySearch(sorted, 7));
        show(() -> binarySearch(sorted, 1));
        show(() -> binarySearch(sorted, 11));
        show(() -> binarySearch(sorted, 4));                           // -1 when missing
        int low = 1_500_000_000;
        int high = 2_000_000_000;
        show(() -> (low + high) / 2);                                  // TRAP: low + high overflows to a negative number
        show(() -> low + (high - low) / 2);                            // the safe middle
        show(() -> (low + high) >>> 1);                                // also safe for non-negative ints: the sum is read as unsigned
        note("Arrays.binarySearch returns -(insertion point) - 1 for a missing value (lesson 3)");

        section("Cost");
        bigO(
                "sliding window", "O(n) time, O(1) space (recomputing every window: O(n * k))",
                "prefix sums", "O(n) to build, then O(1) per range sum",
                "binary search", "O(log n) time, O(1) space; the input must be sorted"
        );
    }
}
