/**
 * 6d. STRING PATTERNS: the string problems interviewers ask again and again
 *     Run: npx tsx src/6-strings-in-problems/string-patterns.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

/** Is this one-character string a letter or a digit (ASCII)? */
const isAlnum = (c: string): boolean => /[a-z0-9]/i.test(c);

/**
 * Palindrome that ignores case and every character that is not a letter or digit.
 * Idea: two pointers walk toward each other and skip the characters that do not count. O(n) time, O(1) space.
 */
function isPalindrome(s: string): boolean {
  let i = 0;
  let j = s.length - 1;
  while (i < j) {
    if (!isAlnum(s[i]!)) i++;
    else if (!isAlnum(s[j]!)) j--;
    else if (s[i]!.toLowerCase() !== s[j]!.toLowerCase()) return false;
    else { i++; j--; }
  }
  return true;
}

/** Anagram check by sorting both texts: equal sorted letters means anagrams. O(n log n) time, O(n) space. */
const isAnagramSorted = (a: string, b: string): boolean => a.length === b.length && [...a].sort().join("") === [...b].sort().join("");

/** Anagram check by counting, lowercase a-z only: +1 for a letter of a, -1 for a letter of b, all zero at the end. O(n) time, O(1) space. */
function isAnagramCounts(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  const counts = new Array<number>(26).fill(0);
  for (let i = 0; i < a.length; i++) {
    counts[a.charCodeAt(i) - 97]++;
    counts[b.charCodeAt(i) - 97]--;
  }
  return counts.every((n) => n === 0);
}

/** Index of the first character that appears exactly once, or -1. Idea: count with a Map, then scan again. O(n) time, O(k) space. */
function firstUniqueChar(s: string): number {
  const counts = new Map<string, number>();
  for (const c of s) counts.set(c, (counts.get(c) ?? 0) + 1);
  for (let i = 0; i < s.length; i++) if (counts.get(s[i]!) === 1) return i;
  return -1;
}

/** Words in reverse order, extra spaces removed: trim, split on whitespace runs, reverse, join. O(n) time, O(n) space. */
const reverseWords = (s: string): string => s.trim().split(/\s+/).reverse().join(" ");

/** Longest common prefix: walk column by column and stop at the first word that differs or ends. O(n * m) time (n words, m = first word's length), O(1) space. */
function longestCommonPrefix(words: string[]): string {
  if (words.length === 0) return "";
  const first = words[0]!;
  for (let i = 0; i < first.length; i++) {
    for (const w of words) if (i >= w.length || w[i] !== first[i]) return first.slice(0, i);
  }
  return first;
}

/** Number of vowels, ignoring case. One pass. O(n) time, O(1) space. */
const countVowels = (s: string): number => [...s.toLowerCase()].filter((c) => "aeiou".includes(c)).length;

/** Run-length encoding: "aaabb" becomes "a3b2" (a single letter gets a 1, so the result can be longer). O(n) time, O(n) space. */
function compress(s: string): string {
  const parts: string[] = [];
  let count = 1;
  for (let i = 1; i <= s.length; i++) {
    if (i < s.length && s[i] === s[i - 1]) {
      count++;
    } else {
      parts.push(s[i - 1]! + count);
      count = 1;
    }
  }
  return parts.join("");
}

/**
 * Length of the longest substring without a repeated character. Sliding window: remember the last index of every
 * character and jump the window start past an earlier copy. O(n) time, O(k) space for k distinct characters.
 */
function lengthOfLongestSubstring(s: string): number {
  const lastSeen = new Map<string, number>();
  let start = 0;
  let best = 0;
  for (let end = 0; end < s.length; end++) {
    const c = s[end]!;
    const previous = lastSeen.get(c);
    if (previous !== undefined && previous >= start) start = previous + 1;   // the copy must be inside the window
    lastSeen.set(c, end);
    best = Math.max(best, end - start + 1);
  }
  return best;
}

/** Is b a rotation of a? If it is, it appears inside a + a. O(n) space; time O(n) with a linear search. */
const isRotation = (a: string, b: string): boolean => a.length === b.length && (a + a).includes(b);

title("STRING PATTERNS  -  palindrome, anagram, window, run-length");

section("Palindrome: two pointers");
syntax(["i = 0; j = s.length - 1; while (i < j) { ...; i++; j-- }", "compare from both ends, skip what does not count"]);
show(() => isPalindrome("No 'x' in Nixon"));            // letters only: noxinnixon reads the same both ways
show(() => isPalindrome("race a car"));
show(() => isPalindrome(""));                           // edge: empty is a palindrome
show(() => isPalindrome(".,;"));                        // edge: nothing to compare, so true
show(() => isPalindrome("0P"));                         // edge: digits count, "0" differs from "p"
const word = "level";
show(() => word === [...word].reverse().join(""));      // the short version when the text is already clean

section("Anagram: sort, or count");
syntax(["counts[a.charCodeAt(i) - 97]++; counts[b.charCodeAt(i) - 97]--", "array of 26: all counts back to zero = anagram"]);
show(() => isAnagramSorted("listen", "silent"));
show(() => isAnagramSorted("rat", "car"));
show(() => isAnagramSorted("", ""));
show(() => isAnagramCounts("listen", "silent"));
show(() => isAnagramCounts("aab", "abb"));              // same length and letters, different counts: false
show(() => isAnagramCounts("abc", "ab"));               // different lengths are rejected first
show(() => isAnagramCounts("A", "B"));                  // TRAP: true! upper case gives index -32 / -31: no error, the 26 counters are never touched
note("Kotlin throws on a bad index; JavaScript silently corrupts: lower-case the input or use a Map for 'any character'");

section("First unique character: count, then scan");
show(() => firstUniqueChar("leetcode"));                // "l" at index 0
show(() => firstUniqueChar("loveleetcode"));            // "l" and "o" repeat, so "v" at 2
show(() => firstUniqueChar("aabb"));                    // none: -1
show(() => firstUniqueChar(""));

section("Reverse words");
show(() => reverseWords("the sky is blue"));
show(() => reverseWords("  hello   world  "));          // edge: extra spaces vanish
show(() => reverseWords("one"));
show(() => reverseWords(""));                           // "": "".split(/\s+/) is [""], not []

section("Longest common prefix: column by column");
show(() => longestCommonPrefix(["flower", "flow", "flight"]));
show(() => longestCommonPrefix(["dog", "racecar", "car"]));   // no common start: empty string
show(() => longestCommonPrefix(["solo"]));              // edge: one word is its own prefix
show(() => longestCommonPrefix([]));                    // edge: no words at all

section("Count vowels and run-length compress");
show(() => countVowels("Hello World"));
show(() => countVowels("rhythm"));
show(() => countVowels("AEIOU"));                       // case is ignored
show(() => compress("aaabb"));
show(() => compress("aabcccccaaa"));                    // a repeated letter later starts a new run
show(() => compress("abc"));                            // every letter once: longer than the input
show(() => compress("a".repeat(12)));                   // counts can have several digits
show(() => compress(""));

section("Longest substring without repeats: sliding window + Map");
syntax(["start = lastSeen.get(c) + 1", "the window [start..end] never holds a repeated character"]);
show(() => lengthOfLongestSubstring("abcabcbb"));       // abc
show(() => lengthOfLongestSubstring("bbbbb"));          // b
show(() => lengthOfLongestSubstring("pwwkew"));         // wke
show(() => lengthOfLongestSubstring("abba"));           // TRAP: 2; without the previous >= start check the window start jumps back and it says 3
show(() => lengthOfLongestSubstring(""));

section("Rotation: look inside a + a");
show(() => isRotation("waterbottle", "erbottlewat"));
show(() => isRotation("abc", "acb"));                   // same letters, but not a rotation
show(() => isRotation("ab", "abc"));                    // different lengths
show(() => isRotation("", ""));
note("Interview routine: clarify the input (case? spaces? empty? Unicode?), try an example, say the brute force, then optimize");
note("Building the answer: collect pieces in an array and join once; s += x is fine in V8 but join is the idiom (6c)");

section("Cost");
bigO(
  ["isPalindrome", "O(n) time, O(1) space"],
  ["countVowels", "O(n) time; O(n) space as written (the spread), O(1) with a plain loop"],
  ["isAnagramSorted", "O(n log n) time, O(n) space"],
  ["isAnagramCounts | firstUniqueChar", "O(n) time; 26 counters | a Map of k distinct characters"],
  ["reverseWords | compress", "O(n) time, O(n) space"],
  ["longestCommonPrefix", "O(n * m) time, O(1) space"],
  ["lengthOfLongestSubstring", "O(n) time, O(k) space for k distinct characters"],
  ["isRotation", "O(n) space; includes is O(n * m) worst case, linear in practice"],
);
