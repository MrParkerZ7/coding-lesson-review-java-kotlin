package review.drills;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 9a. ARRAY DRILLS: hash map, set, running best, prefix and suffix
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.ArrayDrills
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ArrayDrills {

    /**
     * TWO SUM. Problem: which two numbers add up to target? Return their indexes (an empty array when there is no pair).
     * Idea: one pass; remember value -> index of everything seen; for each x ask "is target - x already there?".
     * Time O(n), space O(n). Brute force checks every pair: O(n^2).
     */
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();                   // value -> index
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);                     // Integer, not int: a missing key gives null
            if (j != null) return new int[] {j, i};
            seen.put(nums[i], i);                                       // store AFTER the check: an element never pairs with itself
        }
        return new int[0];
    }

    /**
     * CONTAINS DUPLICATE. Problem: does any value appear twice?
     * Idea: Set.add returns false when the value is already in the set, so stop at the first repeat.
     * Time O(n), space O(n).
     */
    static boolean containsDuplicate(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int x : nums) {
            if (!seen.add(x)) return true;
        }
        return false;
    }

    /** BUG TO AVOID: == on two Integer objects compares references. It works only inside the Integer cache (-128 to 127). */
    static boolean containsDuplicateWrong(List<Integer> nums) {
        for (int i = 0; i < nums.size(); i++) {
            for (int j = i + 1; j < nums.size(); j++) {
                if (nums.get(i) == nums.get(j)) return true;
            }
        }
        return false;
    }

    /**
     * MAXIMUM SUBARRAY (Kadane). Problem: the largest sum of a contiguous, non-empty run.
     * Idea: current = best sum of a run ENDING here: extend the run, or start fresh at this element.
     * Time O(n), space O(1). The sums are int: they overflow past 2.1 billion (see maxSubarrayLong).
     */
    static int maxSubarray(int[] nums) {
        if (nums.length == 0) throw new IllegalArgumentException("need at least one element");
        int best = nums[0];
        int current = nums[0];
        for (int i = 1; i < nums.length; i++) {
            current = Math.max(nums[i], current + nums[i]);
            best = Math.max(best, current);
        }
        return best;
    }

    /** The same drill with long sums: the overflow-safe answer when the values are big. */
    static long maxSubarrayLong(int[] nums) {
        long best = nums[0];
        long current = nums[0];
        for (int i = 1; i < nums.length; i++) {
            current = Math.max(nums[i], current + nums[i]);
            best = Math.max(best, current);
        }
        return best;
    }

    /**
     * BEST TIME TO BUY AND SELL STOCK. Problem: one buy, then one sell later: the biggest profit (0 if none).
     * Idea: walk the prices, remember the cheapest so far, and see what selling today would earn.
     * Time O(n), space O(1).
     */
    static int maxProfit(int[] prices) {
        int cheapest = Integer.MAX_VALUE;
        int best = 0;
        for (int p : prices) {
            cheapest = Math.min(cheapest, p);
            best = Math.max(best, p - cheapest);
        }
        return best;
    }

    /**
     * PRODUCT OF ARRAY EXCEPT SELF. Problem: result[i] = product of all the other elements, without division.
     * Idea: a pass left to right keeps the product of everything BEFORE i, a pass right to left the product AFTER i.
     * Time O(n), extra space O(1) besides the result. int products overflow quickly (see the last examples).
     */
    static int[] productExceptSelf(int[] nums) {
        int[] result = new int[nums.length];
        Arrays.fill(result, 1);
        int left = 1;
        for (int i = 0; i < nums.length; i++) {
            result[i] = left;
            left *= nums[i];
        }
        int right = 1;
        for (int i = nums.length - 1; i >= 0; i--) {
            result[i] *= right;
            right *= nums[i];
        }
        return result;
    }

    public static void main(String[] args) {
        title("ARRAY DRILLS  -  one pass beats two nested loops");

        section("Two Sum (hash map)");
        syntax("seen.put(x, i); seen.get(target - x)", "value -> index map: O(n) instead of O(n^2)");
        show(() -> twoSum(new int[] {2, 7, 11, 15}, 9));
        show(() -> twoSum(new int[] {3, 3}, 6));                        // duplicates: both 3s are used (indexes 0 and 1)
        show(() -> twoSum(new int[] {-3, 4, 3, 90}, 0));                // negatives work the same way
        show(() -> twoSum(new int[] {3}, 6));                           // the single 3 must not pair with itself
        show(() -> twoSum(new int[0], 5));                              // empty input: no pair
        note("Keep the lookup an Integer: int j = seen.get(...) throws NullPointerException when the key is missing");

        section("Contains Duplicate (set)");
        show(() -> containsDuplicate(new int[] {1, 2, 3, 1}));
        show(() -> containsDuplicate(new int[] {1, 2, 3}));
        show(() -> containsDuplicate(new int[0]));                      // empty: nothing repeats
        show(() -> containsDuplicate(new int[] {7}));
        show(() -> containsDuplicateWrong(List.of(127, 127)));          // true by luck: 127 is in the Integer cache, same object
        show(() -> containsDuplicateWrong(List.of(1000, 1000)));        // TRAP: false, two different Integer objects: use equals
        int[] sample = {1, 2, 2, 3};
        show(() -> Arrays.asList(sample).size());                       // TRAP: 1, an int[] is ONE object: you get a List<int[]>
        show(() -> Arrays.stream(sample).distinct().count());           // 3 distinct values for 4 elements: a duplicate exists

        section("Maximum Subarray (Kadane)");
        syntax("current = max(x, current + x)", "best run ENDING here: extend it, or start fresh; best = max(best, current)");
        show(() -> maxSubarray(new int[] {-2, 1, -3, 4, -1, 2, 1, -5, 4}));   // the run 4, -1, 2, 1
        show(() -> maxSubarray(new int[] {-3, -1, -2}));                // TRAP: all negative, the answer is -1, not 0
        show(() -> maxSubarray(new int[] {5}));
        show(() -> thrown(() -> maxSubarray(new int[0])));              // empty input is rejected
        show(() -> maxSubarray(new int[] {2_000_000_000, 2_000_000_000}));       // TRAP: 2000000000, the int sum 4 billion overflowed silently
        show(() -> maxSubarrayLong(new int[] {2_000_000_000, 2_000_000_000}));   // 4000000000 with long sums

        section("Best time to buy and sell stock");
        show(() -> maxProfit(new int[] {7, 1, 5, 3, 6, 4}));            // buy at 1, sell at 6
        show(() -> maxProfit(new int[] {7, 6, 4, 3, 1}));               // always falling: do not trade, profit 0
        show(() -> maxProfit(new int[] {5}));
        show(() -> maxProfit(new int[0]));

        section("Product of array except self (no division)");
        show(() -> productExceptSelf(new int[] {1, 2, 3, 4}));
        show(() -> productExceptSelf(new int[] {1, 0, 3, 4}));          // one zero: only the zero's own slot is non-zero
        show(() -> productExceptSelf(new int[] {-1, 2, -3}));           // negatives
        show(() -> productExceptSelf(new int[] {5}));                   // nothing else to multiply: the empty product is 1
        show(() -> productExceptSelf(new int[] {50_000, 50_000, 2}));   // TRAP: the last slot is 50000 * 50000 = 2.5 billion, wrapped
        note("Dividing the total product by nums[i] fails on zeros, which is why the prefix/suffix version is the expected answer");
        note("Ask the interviewer for the value range: if sums or products may pass 2.1 billion, use long");

        section("Cost");
        bigO(
                "twoSum | containsDuplicate", "O(n) time, O(n) space (the map or set)",
                "maxSubarray | maxProfit", "O(n) time, O(1) space",
                "productExceptSelf", "O(n) time, O(1) extra space besides the result",
                "brute force: compare every pair", "O(n^2) time"
        );
    }
}
