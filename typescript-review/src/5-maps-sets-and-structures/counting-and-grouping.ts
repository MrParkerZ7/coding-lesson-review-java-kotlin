/**
 * 5d. COUNTING AND GROUPING: the Map and Set patterns behind most interview problems
 *     Run: npx tsx src/5-maps-sets-and-structures/counting-and-grouping.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

/** Two Sum: one pass, remember each value's index in a Map. O(n) time, O(n) space. Returns [i, j] or null. */
function twoSum(nums: number[], target: number): [number, number] | null {
  const seen = new Map<number, number>();               // value -> index
  for (let i = 0; i < nums.length; i++) {
    const j = seen.get(target - nums[i]!);
    if (j !== undefined) return [j, i];                 // `!== undefined`, not `if (j)`: index 0 is falsy
    seen.set(nums[i]!, i);
  }
  return null;
}

/** Does the array contain a duplicate? A Set drops them, so the size changes. O(n). */
function hasDuplicate(nums: number[]): boolean {
  return new Set(nums).size !== nums.length;
}

/** Frequency of every item, in first-seen order. O(n). */
function countBy<T>(items: Iterable<T>): Map<T, number> {
  const counts = new Map<T, number>();
  for (const x of items) counts.set(x, (counts.get(x) ?? 0) + 1);
  return counts;
}

/** First character that appears only once, or undefined. Count first, then scan. O(n). */
function firstUnique(s: string): string | undefined {
  const counts = countBy(s);
  return [...s].find((c) => counts.get(c) === 1);
}

/** Group words that are anagrams of each other: the sorted letters are the key. O(n * k log k) for words of length k. */
function groupAnagrams(words: string[]): string[][] {
  const groups = new Map<string, string[]>();
  for (const w of words) {
    const key = [...w].sort().join("");
    const group = groups.get(key);
    if (group === undefined) groups.set(key, [w]);
    else group.push(w);
  }
  return [...groups.values()];
}

/** Length of the longest run of consecutive integers, in any order: start counting only at the START of a run. O(n). */
function longestConsecutive(nums: number[]): number {
  const all = new Set(nums);
  let best = 0;
  for (const x of all) {
    if (all.has(x - 1)) continue;                       // not the start of a run: skip it
    let end = x;
    while (all.has(end + 1)) end++;
    best = Math.max(best, end - x + 1);
  }
  return best;
}

title("COUNTING AND GROUPING  -  the Map and Set patterns");

section("Counting with a Map");
syntax(
  ["counts.set(x, (counts.get(x) ?? 0) + 1)", "the classic update: ?? 0 supplies the first count"],
  ["Map.groupBy(xs, f) | Object.groupBy(xs, f)", "group in one call (4-array-methods/grouping-and-counting.ts)"],
);
show(() => countBy("banana"));                          // b: 1, a: 3, n: 2: first-seen order, not sorted
show(() => countBy([] as string[]));                    // empty input: an empty Map
show(() => [...countBy("banana")].sort(([, a], [, b]) => b - a)[0]);   // the most common character: ['a', 3]
show(() => countBy([1, 1, 1, 2]).get(1));
show(() => countBy([1, 1, 1, 2]).get(9));               // undefined for an item that never appeared
note("Use a Map, not {} : keys like 'constructor' or '__proto__' break a plain object (objects-as-dictionaries.ts)");

section("Interview patterns: Map and Set at work");
show(() => twoSum([2, 7, 11, 15], 9));                  // indexes of the two numbers that add up to 9
show(() => twoSum([3, 3], 6));                          // duplicates are fine: the second 3 finds the first
show(() => twoSum([3, 2], 6));                          // null: an element may not be used twice
show(() => twoSum([-1, -2, -3, -4], -6));               // negatives work the same way
show(() => twoSum([], 5));                              // empty input
show(() => hasDuplicate([1, 2, 3, 1]));
show(() => hasDuplicate([1, 2, 3]));
show(() => hasDuplicate([]));                           // false
show(() => hasDuplicate([NaN, NaN]));                   // true: a Set treats NaN as equal to NaN (=== would say no)
show(() => firstUnique("swiss"));                       // "w": s appears 3 times, w once
show(() => firstUnique("aabb"));                        // undefined: every character repeats
show(() => firstUnique(""));
show(() => longestConsecutive([100, 4, 200, 1, 3, 2])); // 4: 1, 2, 3, 4
show(() => longestConsecutive([1, 2, 2, 3]));           // 3: duplicates do not lengthen the run
show(() => longestConsecutive([-1, 0, 1]));
show(() => longestConsecutive([]));

section("Grouping");
show(() => groupAnagrams(["eat", "tea", "tan", "ate", "nat", "bat"]));   // the sorted letters are the key: "aet", "ant", "abt"
show(() => groupAnagrams([]));
show(() => groupAnagrams(["a", "a"]));                  // equal words are in the same group
const byLength = new Map<number, string[]>();
for (const w of ["apple", "fig", "kiwi", "pear", "fig"]) {
  const list = byLength.get(w.length) ?? [];            // get the list or start one, then push and store it
  list.push(w);
  byLength.set(w.length, list);
}
show(() => byLength);                                   // the get-or-create idiom: Map has no computeIfAbsent (Node 24)

section("Cost");
bigO(
  ["twoSum with a Map", "O(n) time, O(n) space (nested loops: O(n^2) time, O(1) space)"],
  ["hasDuplicate with a Set", "O(n) time, O(n) space (sort first: O(n log n), O(1) extra space)"],
  ["countBy | firstUnique", "O(n) time, O(distinct keys) space"],
  ["groupAnagrams", "O(n * k log k): sorting each word of length k"],
  ["longestConsecutive with a Set", "O(n): each run is walked once, from its start (sorting: O(n log n))"],
);
