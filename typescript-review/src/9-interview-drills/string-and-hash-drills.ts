/**
 * 9c. STRING AND HASH DRILLS: stack, map as key, bucket count, sliding window, counting by Map
 *     Run: npx tsx src/9-interview-drills/string-and-hash-drills.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

/**
 * VALID PARENTHESES. Problem: are (), [] and {} closed in the right order?
 * Idea: a stack (an array with push and pop); push every opener, and every closer must match the top. At the end the stack must be empty.
 * Time O(n), space O(n).
 */
function validParentheses(s: string): boolean {
  const closers = new Map([[")", "("], ["]", "["], ["}", "{"]]);   // closer -> its opener
  const stack: string[] = [];
  for (const c of s) {
    const opener = closers.get(c);
    if (opener === undefined) {
      if (c === "(" || c === "[" || c === "{") stack.push(c);
    } else if (stack.pop() !== opener) {                 // pop() on an empty stack is undefined, which never matches
      return false;
    }
  }
  return stack.length === 0;
}

/**
 * GROUP ANAGRAMS. Problem: put words made of the same letters in one group.
 * Idea: the sorted letters of a word are the same for all its anagrams, so use them as the Map key.
 * Time O(n * k log k) for n words of length k, space O(n * k). A Map keeps the groups in order of first appearance.
 */
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

/** The same with a plain object: TRAP, integer-like keys such as "123" are listed FIRST, whatever the insertion order. */
function groupAnagramsObject(words: string[]): string[][] {
  const groups: Record<string, string[]> = {};
  for (const w of words) {
    const key = [...w].sort().join("");
    (groups[key] ??= []).push(w);
  }
  return Object.values(groups);
}

/**
 * TOP K FREQUENT. Problem: the k values that appear most often.
 * Idea: count with a Map, then bucket by count (a value seen c times goes to buckets[c]) and read from the top bucket down.
 * Time O(n), space O(n). Ties come out in the order the values were first seen.
 */
function topKFrequent(nums: number[], k: number): number[] {
  const counts = new Map<number, number>();
  for (const x of nums) counts.set(x, (counts.get(x) ?? 0) + 1);
  const buckets: number[][] = Array.from({ length: nums.length + 1 }, () => []);   // one NEW array per slot
  for (const [x, c] of counts) buckets[c].push(x);
  const result: number[] = [];
  for (let c = buckets.length - 1; c >= 1; c--) {
    for (const x of buckets[c]) {
      if (result.length === k) return result;
      result.push(x);
    }
  }
  return result;
}

/** TRAP: Array(n).fill([]) fills every slot with the SAME array, so every value lands in every bucket. */
function topKFrequentShared(nums: number[], k: number): number[] {
  const counts = new Map<number, number>();
  for (const x of nums) counts.set(x, (counts.get(x) ?? 0) + 1);
  const buckets = new Array<number[]>(nums.length + 1).fill([]);
  for (const [x, c] of counts) buckets[c].push(x);
  const result: number[] = [];
  for (let c = buckets.length - 1; c >= 1; c--) {
    for (const x of buckets[c]) {
      if (result.length === k) return result;
      result.push(x);
    }
  }
  return result;
}

/** TRAP: counts in a plain object. Object.keys gives STRINGS in ascending order for integer-like keys, so ties come out smallest first. */
function topKFrequentObject(nums: number[], k: number): number[] {
  const counts: Record<number, number> = {};
  for (const x of nums) counts[x] = (counts[x] ?? 0) + 1;
  return Object.keys(counts).map(Number).sort((a, b) => counts[b] - counts[a]).slice(0, k);
}

/** A word counter on a plain object: TRAP, "constructor" and "toString" are inherited keys that already "exist". */
function wordCountObject(text: string): Record<string, number> {
  const counts: Record<string, number> = {};
  for (const w of text.split(" ")) counts[w] = (counts[w] ?? 0) + 1;
  return counts;
}

/**
 * LONGEST SUBSTRING WITHOUT REPEATING CHARACTERS. Problem: the length of the longest run of distinct characters.
 * Idea: sliding window [left, right]; remember the last index of each character and jump left past an earlier copy.
 * Time O(n), space O(min(n, alphabet)).
 */
function longestUniqueSubstring(s: string): number {
  const lastSeen = new Map<string, number>();
  let left = 0;
  let best = 0;
  for (let right = 0; right < s.length; right++) {
    const prev = lastSeen.get(s[right]);
    if (prev !== undefined && prev >= left) left = prev + 1;   // only if the copy is INSIDE the window: left never moves back
    lastSeen.set(s[right], right);
    best = Math.max(best, right - left + 1);
  }
  return best;
}

/**
 * VALID ANAGRAM, any characters. Problem: do two texts use exactly the same characters the same number of times?
 * Idea: one count Map over code points ([...s] walks code points, not UTF-16 units); +1 for a, -1 for b; all zero means anagram.
 * Time O(n), space O(k) for k distinct characters. A Map has no alphabet limit (an array of 26 does).
 */
function isAnagram(a: string, b: string): boolean {
  const counts = new Map<string, number>();
  for (const c of a) counts.set(c, (counts.get(c) ?? 0) + 1);
  for (const c of b) counts.set(c, (counts.get(c) ?? 0) - 1);
  return [...counts.values()].every((n) => n === 0);
}

/**
 * FIRST UNIQUE CHARACTER. Problem: the index of the first character that appears exactly once, or -1.
 * Idea: count every character, then scan again and stop at the first one with count 1.
 * Time O(n), space O(alphabet).
 */
function firstUniqueChar(s: string): number {
  const counts = new Map<string, number>();
  for (const c of s) counts.set(c, (counts.get(c) ?? 0) + 1);
  return [...s].findIndex((c) => counts.get(c) === 1);
}

title("STRING AND HASH DRILLS  -  a Map, a Set or a stack does the work");

section("Valid parentheses (stack)");
show(() => validParentheses("()[]{}"));
show(() => validParentheses("([)]"));                   // closed in the wrong order
show(() => validParentheses(""));                       // empty text is balanced
show(() => validParentheses("("));                      // an opener never closed: the stack is not empty
show(() => validParentheses("]"));                      // a closer with nothing open: pop() gives undefined, which never matches

section("Group anagrams (sorted letters as the key)");
show(() => groupAnagrams(["eat", "tea", "tan", "ate", "nat", "bat"]));   // groups keep the order of first appearance
show(() => groupAnagrams(["a", "a"]));                  // duplicates are grouped, not dropped
show(() => groupAnagrams(["bb", "321", "123"]));
show(() => groupAnagramsObject(["bb", "321", "123"]));  // TRAP: the "123" group jumps to the front: integer-like keys come first, in ascending order
show(() => groupAnagrams([]));
note("An object keeps string keys in insertion order EXCEPT integer-like keys (\"0\", \"7\", \"123\"), which come first in ascending order: use a Map");

section("Top K frequent (count, then bucket by count)");
show(() => topKFrequent([1, 1, 1, 2, 2, 3], 2));
show(() => topKFrequent([5], 1));
show(() => topKFrequent([4, 4, 5, 5, 6], 2));           // a tie for the top: first seen first
show(() => topKFrequent([1, 2, 2], 5));                 // k larger than the distinct values: all of them
show(() => topKFrequent([], 3));
show(() => topKFrequentShared([1, 2, 2, 3, 3, 3], 2));  // TRAP: [ 1, 2 ] instead of [ 3, 2 ]: Array(n).fill([]) shares ONE bucket
show(() => topKFrequent([1, 2, 2, 3, 3, 3], 2));
show(() => topKFrequent([5, 5, 4, 4, 6], 1));           // 5 (first seen) ...
show(() => topKFrequentObject([5, 5, 4, 4, 6], 1));     // TRAP: 4: object keys of a tie come out smallest first, as strings turned back into numbers

section("Counting words: Map versus plain object");
const wordCounts = wordCountObject("a constructor");
show(() => typeof wordCounts["constructor"]);           // TRAP: "string": counts["constructor"] was the inherited Object function, and function + 1 is text
show(() => new Map([["constructor", 1]]).get("constructor"));   // a Map has no inherited keys
note("Counting or grouping by arbitrary text: use a Map (or Object.create(null)); an object literal has inherited keys and integer-like ordering");

section("Longest substring without repeats (sliding window)");
syntax(["left = prev + 1  (only when prev >= left)", "jump the window start past the earlier copy"]);
show(() => longestUniqueSubstring("abcabcbb"));
show(() => longestUniqueSubstring("bbbbb"));
show(() => longestUniqueSubstring("pwwkew"));
show(() => longestUniqueSubstring("abba"));             // TRAP: 2; without the prev >= left test this gives 3
show(() => longestUniqueSubstring(""));

section("Valid anagram and first unique character (Map counts)");
show(() => isAnagram("listen", "silent"));
show(() => isAnagram("rat", "car"));
show(() => isAnagram("a", "ab"));
show(() => isAnagram("", ""));
show(() => isAnagram("A", "B"));                        // false: a Map takes any character (the array-of-26 version in 6d wrongly says true)
show(() => firstUniqueChar("leetcode"));
show(() => firstUniqueChar("loveleetcode"));            // l and o repeat later; v at index 2 is the first one that does not
show(() => firstUniqueChar("aabb"));                    // nothing unique: findIndex gives -1
show(() => firstUniqueChar(""));
note("Always ask: which characters (a-z, any Unicode)? A Map handles everything; an array of 26 is faster but narrower");

section("Cost");
bigO(
  ["validParentheses", "O(n) time, O(n) space (the stack)"],
  ["groupAnagrams", "O(n * k log k) time: each word of length k is sorted"],
  ["topKFrequent (buckets)", "O(n) time, O(n) space; a heap of size k gives O(n log k)"],
  ["longestUniqueSubstring", "O(n) time, O(alphabet) space"],
  ["isAnagram | firstUniqueChar", "O(n) time, O(k) space for k distinct characters"],
);
