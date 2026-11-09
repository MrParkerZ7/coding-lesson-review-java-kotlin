/**
 * 9a. ARRAY DRILLS: hash map, set, running best, prefix and suffix, sorting numbers
 *     Run: npx tsx src/9-interview-drills/array-drills.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title } from "../output";

/**
 * TWO SUM. Problem: which two numbers add up to target? Return their indexes.
 * Idea: one pass; remember value -> index of everything seen; for each x ask "is target - x already there?".
 * Time O(n), space O(n). Brute force checks every pair: O(n^2).
 */
function twoSum(nums: number[], target: number): [number, number] | undefined {
  const seen = new Map<number, number>();               // value -> index
  for (let i = 0; i < nums.length; i++) {
    const j = seen.get(target - nums[i]);
    if (j !== undefined) return [j, i];                  // compare with undefined: index 0 is a valid answer
    seen.set(nums[i], i);                                // store AFTER the check: an element never pairs with itself
  }
  return undefined;
}

/** The same with `if (j)`: TRAP, index 0 is falsy, so a pair that starts at index 0 is missed. */
function twoSumWrong(nums: number[], target: number): [number, number] | undefined {
  const seen = new Map<number, number>();
  for (let i = 0; i < nums.length; i++) {
    const j = seen.get(target - nums[i]);
    if (j) return [j, i];
    seen.set(nums[i], i);
  }
  return undefined;
}

/**
 * CONTAINS DUPLICATE. Problem: does any value appear twice?
 * Idea: a Set remembers what was seen; stop at the first repeat. Set uses SameValueZero, so NaN equals NaN.
 * Time O(n), space O(n).
 */
function containsDuplicate(nums: number[]): boolean {
  const seen = new Set<number>();
  for (const x of nums) {
    if (seen.has(x)) return true;
    seen.add(x);
  }
  return false;
}

/**
 * MAXIMUM SUBARRAY (Kadane). Problem: the largest sum of a contiguous, non-empty run.
 * Idea: current = best sum of a run ENDING here: extend the run, or start fresh at this element.
 * Time O(n), space O(1).
 */
function maxSubarray(nums: number[]): number {
  if (nums.length === 0) throw new RangeError("need at least one element");
  let best = nums[0];
  let current = nums[0];
  for (let i = 1; i < nums.length; i++) {
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
function maxProfit(prices: number[]): number {
  let cheapest = Infinity;
  let best = 0;
  for (const p of prices) {
    cheapest = Math.min(cheapest, p);
    best = Math.max(best, p - cheapest);
  }
  return best;
}

/**
 * PRODUCT OF ARRAY EXCEPT SELF. Problem: result[i] = product of all the other elements, without division.
 * Idea: a pass left to right keeps the product of everything BEFORE i, a pass right to left the product AFTER i.
 * Time O(n), extra space O(1) besides the result.
 */
function productExceptSelf(nums: number[]): number[] {
  const result = new Array<number>(nums.length).fill(1);
  let left = 1;
  for (let i = 0; i < nums.length; i++) {
    result[i] = left;
    left *= nums[i];
  }
  let right = 1;
  for (let i = nums.length - 1; i >= 0; i--) {
    result[i] *= right;
    right *= nums[i];
  }
  return result;
}

/**
 * K-TH LARGEST. Problem: the k-th largest value (k = 1 is the maximum), or undefined when k is out of range.
 * Idea: sort a COPY in descending order with a comparator and read index k - 1. Time O(n log n), space O(n).
 * (A min-heap of size k gives O(n log k); quickselect O(n) on average.)
 */
const kthLargest = (nums: number[], k: number): number | undefined => [...nums].sort((a, b) => b - a)[k - 1];

/** TRAP: the default sort() compares the numbers as TEXT, so 100 comes before 9. */
const kthLargestWrong = (nums: number[], k: number): number | undefined => [...nums].sort().reverse()[k - 1];

title("ARRAY DRILLS  -  one pass beats two nested loops");

section("Two Sum (hash map)");
syntax(["seen.set(x, i); seen.get(target - x)", "value -> index map: O(n) instead of O(n^2)"]);
show(() => twoSum([2, 7, 11, 15], 9));
show(() => twoSum([3, 3], 6));                          // duplicates: both 3s are used (indexes 0 and 1)
show(() => twoSum([-3, 4, 3, 90], 0));                  // negatives work the same way
show(() => twoSum([3], 6));                             // the single 3 must not pair with itself
show(() => twoSum([], 5));                              // empty input: no pair
show(() => twoSumWrong([3, 3], 6));                     // TRAP: undefined, "if (j)" treats index 0 as "not found"

section("Contains Duplicate (set)");
show(() => containsDuplicate([1, 2, 3, 1]));
show(() => containsDuplicate([1, 2, 3]));
show(() => containsDuplicate([]));                      // empty: nothing repeats
show(() => containsDuplicate([NaN, NaN]));              // true: a Set treats NaN as equal to NaN (indexOf would not find it)

section("Maximum Subarray (Kadane)");
syntax(["current = Math.max(x, current + x)", "best run ENDING here: extend it, or start fresh; best = Math.max(best, current)"]);
show(() => maxSubarray([-2, 1, -3, 4, -1, 2, 1, -5, 4]));   // the run 4, -1, 2, 1
show(() => maxSubarray([-3, -1, -2]));                  // TRAP: all negative, the answer is -1, not 0
show(() => maxSubarray([5]));
show(() => thrown(() => maxSubarray([])));              // empty input is rejected with a RangeError

section("Best time to buy and sell stock");
show(() => maxProfit([7, 1, 5, 3, 6, 4]));              // buy at 1, sell at 6
show(() => maxProfit([7, 6, 4, 3, 1]));                 // always falling: do not trade, profit 0
show(() => maxProfit([5]));
show(() => maxProfit([]));                              // cheapest stays Infinity, profit stays 0

section("Product of array except self (no division)");
show(() => productExceptSelf([1, 2, 3, 4]));
show(() => productExceptSelf([1, 0, 3, 4]));            // one zero: only the zero's own slot is non-zero
show(() => productExceptSelf([-1, 2, -3]));             // negatives
show(() => productExceptSelf([5]));                     // nothing else to multiply: the empty product is 1
show(() => productExceptSelf([3, 0, -2]));              // TRAP: a -0 appears (1 * -0), === says it equals 0, Object.is and inspect tell them apart
note("Dividing the total product by nums[i] fails on zeros, which is why the prefix/suffix version is the expected answer");

section("K-th largest: sort numbers with a comparator");
show(() => kthLargest([3, 2, 1, 5, 6, 4], 2));
show(() => kthLargest([10, 9, 100, 1], 1));
show(() => kthLargestWrong([10, 9, 100, 1], 1));        // TRAP: 9, because [1, 10, 100, 9] is the text order
show(() => [10, 9, 100, 1].sort());                     // the default sort() is lexicographic
show(() => kthLargest([1], 5));                         // k out of range: undefined, no exception
show(() => kthLargest([], 1));

section("Cost");
bigO(
  ["twoSum | containsDuplicate", "O(n) time, O(n) space (the Map or Set)"],
  ["maxSubarray | maxProfit", "O(n) time, O(1) space"],
  ["productExceptSelf", "O(n) time, O(1) extra space besides the result"],
  ["kthLargest by sorting", "O(n log n) time, O(n) space for the copy; heap O(n log k), quickselect O(n) average"],
  ["brute force: compare every pair", "O(n^2) time"],
);
