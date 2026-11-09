package review.loops;

import static review.Output.bigO;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * 2d. LOOP PATTERNS: accumulator, two pointers, brute force (window, prefix sums and binary search are 2e)
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.LoopPatterns
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class LoopPatterns {

    /** Total of an array with a long accumulator (an int total would wrap around silently). O(n) time, O(1) space. */
    public static long sumAll(int[] xs) {
        long total = 0;
        for (int x : xs) total += x;
        return total;
    }

    /** Largest value, or an empty OptionalInt for an empty array. Starts from xs[0], not from 0, so all-negative input works. O(n). */
    public static OptionalInt largest(int[] xs) {
        if (xs.length == 0) return OptionalInt.empty();
        int best = xs[0];
        for (int i = 1; i < xs.length; i++) if (xs[i] > best) best = xs[i];
        return OptionalInt.of(best);
    }

    /** Reverse in place with two pointers moving toward each other. O(n) time, O(1) extra space. */
    public static void reverseInPlace(int[] arr) {
        int left = 0;
        int right = arr.length - 1;
        while (left < right) {
            int tmp = arr[left];
            arr[left] = arr[right];
            arr[right] = tmp;
            left++;
            right--;
        }
    }

    /** Brute force Two Sum: every pair i < j. Returns the two indexes, or an empty array. O(n^2) time, O(1) space. */
    public static int[] twoSumBrute(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) return new int[] {i, j};
            }
        }
        return new int[0];
    }

    /** Two pointers on a SORTED array: is there a pair adding up to target? O(n). */
    public static boolean hasPairWithSum(int[] sorted, int target) {
        int left = 0;
        int right = sorted.length - 1;
        while (left < right) {
            int sum = sorted[left] + sorted[right];
            if (sum == target) return true;
            if (sum < target) left++;
            else right--;
        }
        return false;
    }

    /** Palindrome check with two pointers, ignoring case. O(n). */
    public static boolean isPalindrome(String s) {
        int i = 0;
        int j = s.length() - 1;
        while (i < j) {
            if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j))) return false;
            i++;
            j--;
        }
        return true;
    }

    /** FizzBuzz: the classic loop. */
    public static List<String> fizzBuzz(int n) {
        List<String> out = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            if (i % 15 == 0) out.add("FizzBuzz");
            else if (i % 3 == 0) out.add("Fizz");
            else if (i % 5 == 0) out.add("Buzz");
            else out.add(String.valueOf(i));
        }
        return out;
    }

    public static void main(String[] args) {
        title("LOOP PATTERNS  -  accumulator, two pointers, brute force");

        section("Accumulator");
        syntax("long result = start; for (int x : xs) result = combine(result, x);", "one pass, keep a running answer");
        show(() -> sumAll(new int[] {4, 8, 1, 9}));
        show(() -> sumAll(new int[] {Integer.MAX_VALUE, 1}));          // TRAP: a long accumulator is right; an int total would wrap to -2147483648
        show(() -> largest(new int[] {4, 8, 1, 9}));
        show(() -> largest(new int[] {}));                             // handle the empty input: edge cases are graded
        show(() -> largest(new int[] {-5, -2, -9}));                   // all negative: starting best at 0 would wrongly answer 0
        show(() -> fizzBuzz(5));
        show(() -> fizzBuzz(15).getLast());                            // Java 21: getLast()
        int squares = 0;
        for (int x : new int[] {1, 2, 3, 4, 5, 6}) if (x % 2 == 0) squares += x * x;
        int evenSquares = squares;   show(() -> evenSquares);          // 4 + 16 + 36: sum of squares of the evens
        show(() -> IntStream.rangeClosed(1, 6).filter(x -> x % 2 == 0).map(x -> x * x).sum());   // the same result without a loop

        section("Two pointers");
        syntax("int left = 0, right = last; while (left < right) { ...; left++ or right--; }", "walk toward each other");
        int[] arr = {1, 2, 3, 4, 5};
        reverseInPlace(arr);   show(() -> arr);
        int[] sortedNums = {1, 3, 4, 6, 9};
        show(() -> hasPairWithSum(sortedNums, 10));                    // 1 + 9 = 10 on a sorted array
        show(() -> hasPairWithSum(sortedNums, 100));
        show(() -> isPalindrome("Level"));
        show(() -> isPalindrome("Java"));

        section("Nested loops (brute force) vs one pass");
        show(() -> twoSumBrute(new int[] {2, 7, 11, 15}, 9));          // indexes [0, 1]; correct but O(n^2)
        show(() -> twoSumBrute(new int[] {1, 2, 3}, 100));             // an empty array when no pair exists

        section("Cost");
        bigO(
                "accumulator | two pointers", "O(n) time, O(1) space",
                "nested loops over all pairs (twoSumBrute)", "O(n^2) time",
                "for (i < n) linkedList.get(i)", "O(n^2): every get(i) walks the list, use for-each (lesson 3)"
        );
    }
}
