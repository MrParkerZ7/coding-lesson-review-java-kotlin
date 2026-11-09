/**
 * 2d. LOOP PATTERNS: the loops behind most coding-interview problems
 *     Run: npx tsx src/2-loops/loop-patterns.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

/** Total of an array with an accumulator. O(n). */
function sumAll(xs: number[]): number {
  let total = 0;
  for (const x of xs) total += x;
  return total;
}

/** Largest value, or undefined for an empty array. One pass, O(n). */
function largest(xs: number[]): number | undefined {
  if (xs.length === 0) return undefined;
  let best = xs[0];
  for (let i = 1; i < xs.length; i++) if (xs[i] > best) best = xs[i];
  return best;
}

/** Reverse in place with two pointers moving toward each other. O(n) time, O(1) extra space. */
function reverseInPlace(arr: number[]): void {
  let left = 0;
  let right = arr.length - 1;
  while (left < right) {
    [arr[left], arr[right]] = [arr[right], arr[left]];   // the destructuring swap: no temp variable
    left++;
    right--;
  }
}

/** Brute force Two Sum: every pair i < j. O(n^2) time, O(1) space. */
function twoSumBrute(nums: number[], target: number): [number, number] | null {
  for (let i = 0; i < nums.length; i++) {
    for (let j = i + 1; j < nums.length; j++) {
      if (nums[i] + nums[j] === target) return [i, j];
    }
  }
  return null;
}

/** Two pointers on a SORTED array: is there a pair adding up to target? O(n). */
function hasPairWithSum(sorted: number[], target: number): boolean {
  let left = 0;
  let right = sorted.length - 1;
  while (left < right) {
    const sum = sorted[left] + sorted[right];
    if (sum === target) return true;
    if (sum < target) left++;
    else right--;
  }
  return false;
}

/** Sliding window: best sum of k neighbours, updating the sum instead of recomputing it. O(n). */
function maxWindowSum(nums: number[], k: number): number | undefined {
  if (k <= 0 || k > nums.length) return undefined;       // edge cases are graded: no window fits
  let window = 0;
  for (let i = 0; i < k; i++) window += nums[i];
  let best = window;
  for (let i = k; i < nums.length; i++) {
    window += nums[i] - nums[i - k];                     // add the new element, drop the old one
    if (window > best) best = window;
  }
  return best;
}

/** Prefix sums: prefix[i] is the sum of the first i elements; any range sum is one subtraction. O(n). */
function prefixSums(nums: number[]): number[] {
  const prefix: number[] = new Array(nums.length + 1).fill(0);
  for (let i = 0; i < nums.length; i++) prefix[i + 1] = prefix[i] + nums[i];
  return prefix;
}

/** Binary search with a loop: halve the range each round. O(log n). */
function binarySearch(sorted: number[], target: number): number {
  let low = 0;
  let high = sorted.length - 1;
  while (low <= high) {
    const mid = low + Math.floor((high - low) / 2);      // / is a decimal division in JavaScript: floor it
    if (sorted[mid] === target) return mid;
    if (sorted[mid] < target) low = mid + 1;
    else high = mid - 1;
  }
  return -1;
}

/** Palindrome check with two pointers, ignoring case. O(n). */
function isPalindrome(s: string): boolean {
  let i = 0;
  let j = s.length - 1;
  while (i < j) {
    if (s[i].toLowerCase() !== s[j].toLowerCase()) return false;
    i++;
    j--;
  }
  return true;
}

/** FizzBuzz: the classic loop. */
function fizzBuzz(n: number): string[] {
  const out: string[] = [];
  for (let i = 1; i <= n; i++) {
    if (i % 15 === 0) out.push("FizzBuzz");
    else if (i % 3 === 0) out.push("Fizz");
    else if (i % 5 === 0) out.push("Buzz");
    else out.push(String(i));
  }
  return out;
}

title("LOOP PATTERNS  -  accumulator, two pointers, sliding window, binary search");

section("Accumulator");
syntax(["let result = start; for (const x of xs) result = combine(result, x);", "one pass, keep a running answer"]);
show(() => sumAll([4, 8, 1, 9]));
show(() => largest([4, 8, 1, 9]));
show(() => largest([]));                                 // undefined: handle the empty input, edge cases are graded
show(() => Math.max(...[]));                             // TRAP: -Infinity, Math.max of nothing is not an error
show(() => fizzBuzz(15).slice(-6));
const nums = [1, 2, 3, 4, 5, 6];
let squares = 0;
for (const x of nums) if (x % 2 === 0) squares += x * x;
show(() => squares);                                     // sum of squares of the evens: 4 + 16 + 36
show(() => nums.filter((x) => x % 2 === 0).reduce((sum, x) => sum + x * x, 0));   // the same without a loop (lesson 4)

section("Two pointers");
syntax(["let left = 0, right = last; while (left < right) { ...; left++ or right-- }", "walk toward each other"]);
const arr = [1, 2, 3, 4, 5];
reverseInPlace(arr);
show(() => arr);                                         // [ 5, 4, 3, 2, 1 ]: the same array, changed
show(() => hasPairWithSum([1, 3, 4, 6, 9], 10));         // 1 + 9 = 10 on a sorted array
show(() => hasPairWithSum([1, 3, 4, 6, 9], 100));
show(() => hasPairWithSum([], 5));                       // false: an empty array has no pair
show(() => isPalindrome("Level"));
show(() => isPalindrome("TypeScript"));
show(() => isPalindrome(""));                            // true: nothing to compare

section("Nested loops (brute force) vs one pass");
show(() => twoSumBrute([2, 7, 11, 15], 9));              // [ 0, 1 ]: correct but O(n^2); the hash map version is lesson 5
show(() => twoSumBrute([1, 2, 3], 100));                 // null when no pair exists
show(() => twoSumBrute([3, 3], 6));                      // [ 0, 1 ]: duplicates are fine, i < j never reuses the same element

section("Sliding window and prefix sums");
syntax(
  ["window += nums[i] - nums[i - k]", "slide: add the new element, drop the old one"],
  ["prefix[i + 1] = prefix[i] + nums[i]", "range sum (a..b) = prefix[b + 1] - prefix[a]"],
);
show(() => maxWindowSum([2, 1, 5, 1, 3, 2], 3));         // best 3 in a row: 5 + 1 + 3
show(() => maxWindowSum([-1, -2, -3], 2));               // -3: works for negatives because best starts at the first window
show(() => maxWindowSum([1, 2], 5));                     // undefined: the window is bigger than the array
const prefix = prefixSums([3, 1, 4, 1, 5]);
show(() => prefix);                                      // [ 0, 3, 4, 8, 9, 14 ]
show(() => prefix[4] - prefix[1]);                       // sum of elements 1..3 = 1 + 4 + 1

section("Binary search");
syntax(["while (low <= high) { mid = low + Math.floor((high - low) / 2); ... }", "halve the range each round: needs SORTED input"]);
const sorted = [1, 3, 5, 7, 9, 11];
show(() => binarySearch(sorted, 7));
show(() => binarySearch(sorted, 1));
show(() => binarySearch(sorted, 11));
show(() => binarySearch(sorted, 4));                     // -1 when missing
show(() => binarySearch([], 4));                         // -1: the loop never starts
show(() => (0 + 5) / 2);                                 // TRAP: 2.5 for low 0, high 5: / never truncates, so floor the middle
note("(low + high) >> 1 also works while the sum stays below 2^31; low + Math.floor((high - low) / 2) always works");

section("Cost");
bigO(
  ["accumulator | two pointers | sliding window", "O(n) time, O(1) space"],
  ["nested loops over all pairs", "O(n^2) time"],
  ["binary search", "O(log n) time (the array must be sorted first: O(n log n))"],
  ["prefix sums", "O(n) to build, then O(1) per range sum"],
);
