package review.drills;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.Arrays;

/**
 * 9b. IN-PLACE AND SEARCH DRILLS: two pointers, a write index, reverse tricks, binary search
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.InPlaceAndSearchDrills
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class InPlaceAndSearchDrills {

    /**
     * MERGE TWO SORTED ARRAYS. Problem: one sorted array out of two sorted arrays.
     * Idea: two pointers, always take the smaller front element; then copy what is left of the longer one.
     * Time O(n + m), space O(n + m) for the result.
     */
    static int[] mergeSorted(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < a.length && j < b.length) {
            result[k++] = a[i] <= b[j] ? a[i++] : b[j++];              // <= keeps equal values in their original order
        }
        while (i < a.length) result[k++] = a[i++];
        while (j < b.length) result[k++] = b[j++];
        return result;
    }

    /**
     * MOVE ZEROES. Problem: push every 0 to the end, keep the order of the other values, in place.
     * Idea: a write index; copy each non-zero to it, then fill the rest with zeros.
     * Time O(n), space O(1).
     */
    static void moveZeroes(int[] nums) {
        int write = 0;
        for (int x : nums) {
            if (x != 0) nums[write++] = x;
        }
        while (write < nums.length) nums[write++] = 0;
    }

    /**
     * ROTATE RIGHT BY k. Problem: shift an array right by k places, in place (k >= 0).
     * Idea: reverse everything, then reverse the first k and the rest; k % size handles k larger than the array.
     * Time O(n), space O(1).
     */
    static void rotateRight(int[] nums, int k) {
        if (nums.length == 0) return;
        int shift = k % nums.length;
        if (shift == 0) return;
        reverseRange(nums, 0, nums.length - 1);
        reverseRange(nums, 0, shift - 1);
        reverseRange(nums, shift, nums.length - 1);
    }

    private static void reverseRange(int[] a, int from, int to) {
        int i = from;
        int j = to;
        while (i < j) {
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
            i++;
            j--;
        }
    }

    /**
     * BINARY SEARCH, LOWER BOUND. Problem: the first index whose value is >= target in a sorted array
     * (the insertion point; the length when every value is smaller).
     * Idea: halve the half-open range [low, high); keep the left half whenever the middle is already >= target.
     * Time O(log n), space O(1).
     */
    static int lowerBound(int[] sorted, int target) {
        int low = 0;
        int high = sorted.length;
        while (low < high) {
            int mid = low + (high - low) / 2;                           // not (low + high) / 2: that can overflow
            if (sorted[mid] < target) low = mid + 1;
            else high = mid;
        }
        return low;
    }

    public static void main(String[] args) {
        title("IN-PLACE AND SEARCH DRILLS  -  O(1) extra space, O(log n) search");

        section("Merge two sorted arrays (two pointers)");
        show(() -> mergeSorted(new int[] {1, 3, 5}, new int[] {2, 4, 6}));
        show(() -> mergeSorted(new int[] {1, 1}, new int[] {1}));       // duplicates are all kept
        show(() -> mergeSorted(new int[0], new int[] {2, 3}));          // one side empty: the other is copied
        show(() -> mergeSorted(new int[] {-5, 0}, new int[] {-3, 4}));  // negatives
        show(() -> mergeSorted(new int[0], new int[0]));

        section("Move zeroes (write index)");
        syntax("for (int x : nums) if (x != 0) nums[write++] = x;", "compact the non-zeros to the front, then fill zeros");
        int[] a = {0, 1, 0, 3, 12};  moveZeroes(a);  show(() -> a);    // order of the non-zeros is kept
        int[] b = {4, 5};  moveZeroes(b);  show(() -> b);               // no zeros: unchanged
        int[] c = {0, 0, 0};  moveZeroes(c);  show(() -> c);
        int[] d = {};  moveZeroes(d);  show(() -> d);

        section("Rotate right by k (three reversals)");
        syntax("reverse all, reverse [0, k), reverse [k, end)", "k % length first, so k may exceed the length");
        int[] r1 = {1, 2, 3, 4, 5, 6, 7};  rotateRight(r1, 3);  show(() -> r1);   // the last 3 move to the front
        int[] r2 = {1, 2, 3};  rotateRight(r2, 10);  show(() -> r2);   // 10 % 3 = 1 step
        int[] r3 = {1, 2, 3};  rotateRight(r3, 3);  show(() -> r3);    // k equal to the length: a full turn, unchanged
        int[] r4 = {};  rotateRight(r4, 5);  show(() -> r4);           // empty: no division by zero thanks to the early return

        section("Binary search: lower bound");
        syntax("if (a[mid] < target) low = mid + 1; else high = mid;", "loop while low < high: ends at the first index with value >= target");
        int[] sorted = {1, 3, 3, 3, 5};
        show(() -> lowerBound(sorted, 3));                              // the FIRST of the equal values
        show(() -> lowerBound(sorted, 4));                              // not present: where it would be inserted
        show(() -> lowerBound(sorted, 0));                              // smaller than everything
        show(() -> lowerBound(sorted, 9));                              // larger than everything: the length
        show(() -> lowerBound(new int[0], 7));
        show(() -> lowerBound(sorted, 4) - lowerBound(sorted, 3));      // how many 3s there are: two lower bounds
        show(() -> Arrays.binarySearch(sorted, 3));                     // TRAP: the index of SOME 3 (here the middle one), not the first
        show(() -> Arrays.binarySearch(sorted, 4));                     // not found: -(insertion point) - 1 = -5
        int low = 1_500_000_000;
        int high = 2_000_000_000;
        show(() -> (low + high) / 2);                                   // TRAP: low + high is 3.5 billion, it overflowed: negative
        show(() -> low + (high - low) / 2);                             // the safe midpoint
        note("Arrays.binarySearch from lesson 3 returns -(insertion point) - 1 for a missing value; the lower bound returns the insertion point itself");

        section("Cost");
        bigO(
                "mergeSorted", "O(n + m) time, O(n + m) space",
                "moveZeroes | rotateRight", "O(n) time, O(1) extra space",
                "lowerBound", "O(log n) time, O(1) space (needs SORTED input)",
                "sorting first to make binary search possible", "O(n log n)"
        );
    }
}
