/**
 * 9b. IN-PLACE AND SEARCH DRILLS: two pointers, a write index, reverse tricks, binary search
 *     Run: npx tsx src/9-interview-drills/in-place-and-search-drills.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

/**
 * MERGE TWO SORTED ARRAYS. Problem: one sorted array out of two sorted arrays.
 * Idea: two pointers, always take the smaller front element; then copy what is left of the longer one.
 * Time O(n + m), space O(n + m) for the result.
 */
function mergeSorted(a: number[], b: number[]): number[] {
  const result: number[] = [];
  let i = 0;
  let j = 0;
  while (i < a.length && j < b.length) {
    result.push(a[i] <= b[j] ? a[i++] : b[j++]);         // <= keeps equal values in their original order
  }
  while (i < a.length) result.push(a[i++]);
  while (j < b.length) result.push(b[j++]);
  return result;
}

/**
 * MOVE ZEROES. Problem: push every 0 to the end, keep the order of the other values, in place.
 * Idea: a write index; copy each non-zero to it, then fill the rest with zeros.
 * Time O(n), space O(1).
 */
function moveZeroes(nums: number[]): void {
  let write = 0;
  for (const x of nums) if (x !== 0) nums[write++] = x;  // write never passes the read position, so changing nums while looping is safe
  while (write < nums.length) nums[write++] = 0;
}

/**
 * REMOVE DUPLICATES FROM A SORTED ARRAY. Problem: keep one of each value at the front, in place; return how many.
 * Idea: a write index again; copy an element only when it differs from the one before it.
 * Time O(n), space O(1). Everything after the returned length is leftover garbage.
 */
function removeDuplicates(nums: number[]): number {
  if (nums.length === 0) return 0;
  let write = 1;
  for (let i = 1; i < nums.length; i++) {
    if (nums[i] !== nums[i - 1]) nums[write++] = nums[i];
  }
  return write;
}

function reverseRange(a: number[], from: number, to: number): void {
  let i = from;
  let j = to;
  while (i < j) {
    [a[i], a[j]] = [a[j], a[i]];                         // swap by destructuring: no temporary variable
    i++;
    j--;
  }
}

/**
 * ROTATE RIGHT BY k. Problem: shift an array right by k places, in place (k may be larger than the length, or negative).
 * Idea: reverse everything, then reverse the first k and the rest. The shift is ((k % n) + n) % n: % keeps the sign of k in JavaScript.
 * Time O(n), space O(1).
 */
function rotateRight(nums: number[], k: number): void {
  if (nums.length === 0) return;
  const shift = ((k % nums.length) + nums.length) % nums.length;
  if (shift === 0) return;
  reverseRange(nums, 0, nums.length - 1);
  reverseRange(nums, 0, shift - 1);
  reverseRange(nums, shift, nums.length - 1);
}

/**
 * BINARY SEARCH, LOWER BOUND. Problem: the first index whose value is >= target in a sorted array
 * (the insertion point; the length when every value is smaller).
 * Idea: halve the half-open range [low, high); keep the left half whenever the middle is already >= target.
 * Time O(log n), space O(1). The array must be sorted in NUMERIC order.
 */
function lowerBound(sorted: number[], target: number): number {
  let low = 0;
  let high = sorted.length;
  while (low < high) {
    const mid = low + Math.floor((high - low) / 2);      // Math.floor: "/" is a floating-point division, (low + high) / 2 can be x.5
    if (sorted[mid] < target) low = mid + 1;
    else high = mid;
  }
  return low;
}

title("IN-PLACE AND SEARCH DRILLS  -  O(1) extra space, O(log n) search");

section("Merge two sorted arrays (two pointers)");
show(() => mergeSorted([1, 3, 5], [2, 4, 6]));
show(() => mergeSorted([1, 1], [1]));                   // duplicates are all kept
show(() => mergeSorted([], [2, 3]));                    // one side empty: the other is copied
show(() => mergeSorted([-5, 0], [-3, 4]));              // negatives
show(() => mergeSorted([], []));
show(() => [...[2, 10], ...[3, 9]].sort());             // TRAP: the shortcut with the default sort gives [ 10, 2, 3, 9 ]: text order

section("Move zeroes and remove duplicates (write index)");
syntax(["for (const x of nums) if (x !== 0) nums[write++] = x;", "compact the wanted values to the front, then fill the rest"]);
const a = [0, 1, 0, 3, 12];  moveZeroes(a);  show(() => a);   // the order of the non-zeros is kept
const b = [4, 5];  moveZeroes(b);  show(() => b);       // no zeros: unchanged
const c = [0, 0, 0];  moveZeroes(c);  show(() => c);
const d: number[] = [];  moveZeroes(d);  show(() => d);
const dup = [1, 1, 2, 2, 2, 3];
const keep = removeDuplicates(dup);
show(() => keep);                                       // 3 distinct values
show(() => dup.slice(0, keep));                         // the first 3 slots hold them
show(() => dup);                                        // the tail is leftover: only the returned length counts
show(() => removeDuplicates([]));

section("Rotate right by k (three reversals)");
syntax(["reverse all, reverse [0, k), reverse [k, end)", "k is first reduced to ((k % n) + n) % n: any k, even a negative one"]);
const r1 = [1, 2, 3, 4, 5, 6, 7];  rotateRight(r1, 3);  show(() => r1);   // the last 3 move to the front
const r2 = [1, 2, 3];  rotateRight(r2, 10);  show(() => r2);              // 10 % 3 = 1 step
const r3 = [1, 2, 3];  rotateRight(r3, 3);  show(() => r3);               // k equal to the length: a full turn, unchanged
const r4: number[] = [];  rotateRight(r4, 5);  show(() => r4);           // empty: no work (and no % 0, which would be NaN)
const r5 = [1, 2, 3];  rotateRight(r5, -1);  show(() => r5);              // a negative k rotates left by 1
show(() => -1 % 3);                                     // TRAP: -1, so k % n alone is not a valid index shift for negative k
show(() => ((-1 % 3) + 3) % 3);                         // 2: the always-non-negative modulo

section("Binary search: lower bound");
syntax(["if (a[mid] < target) low = mid + 1; else high = mid;", "loop while low < high: ends at the first index with value >= target"]);
const sorted = [1, 3, 3, 3, 5];
show(() => lowerBound(sorted, 3));                      // the FIRST of the equal values
show(() => lowerBound(sorted, 4));                      // not present: where it would be inserted
show(() => lowerBound(sorted, 0));                      // smaller than everything
show(() => lowerBound(sorted, 9));                      // larger than everything: the length
show(() => lowerBound([], 7));
show(() => lowerBound(sorted, 4) - lowerBound(sorted, 3));   // how many 3s there are: two lower bounds
show(() => (0 + 5) / 2);                                // TRAP: 2.5, there is no integer division, so mid needs Math.floor
show(() => sorted[2.5]);                                // undefined: a fractional index is not an element
const lexical = [10, 9, 1].sort();                      // [ 1, 10, 9 ]: text order, NOT sorted numerically
show(() => lowerBound(lexical, 9));                     // TRAP: 1, but 9 sits at index 2: binary search needs the same order as the sort
note("Sort numbers with (a, b) => a - b before you binary search; the plain sort() is for text");

section("Cost");
bigO(
  ["mergeSorted", "O(n + m) time, O(n + m) space"],
  ["moveZeroes | removeDuplicates | rotateRight", "O(n) time, O(1) extra space"],
  ["lowerBound", "O(log n) time, O(1) space (needs SORTED input)"],
  ["sorting first to make binary search possible", "O(n log n)"],
  ["arr.shift() / splice(0, 1) to delete the front", "O(n) each: the write index avoids it"],
);
