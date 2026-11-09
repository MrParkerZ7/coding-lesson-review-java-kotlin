package review.strings;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 6d. STRING PATTERNS: the string problems interviewers ask again and again
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.StringPatterns
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class StringPatterns {

    /**
     * Palindrome that ignores case and every character that is not a letter or digit.
     * Idea: two pointers walk toward each other and skip the characters that do not count. O(n) time, O(1) space.
     */
    static boolean isPalindrome(String s) {
        int i = 0;
        int j = s.length() - 1;
        while (i < j) {
            if (!Character.isLetterOrDigit(s.charAt(i))) i++;
            else if (!Character.isLetterOrDigit(s.charAt(j))) j--;
            else if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j))) return false;
            else {
                i++;
                j--;
            }
        }
        return true;
    }

    /** Anagram check by sorting both texts: equal sorted letters means anagrams. O(n log n) time, O(n) space. */
    static boolean isAnagramSorted(String a, String b) {
        if (a.length() != b.length()) return false;
        char[] x = a.toCharArray();
        char[] y = b.toCharArray();
        Arrays.sort(x);
        Arrays.sort(y);
        return Arrays.equals(x, y);
    }

    /** Anagram check by counting, lowercase a-z only: +1 for a letter of a, -1 for a letter of b, all zero at the end. O(n) time, O(1) space. */
    static boolean isAnagramCounts(String a, String b) {
        if (a.length() != b.length()) return false;
        int[] counts = new int[26];
        for (int i = 0; i < a.length(); i++) {
            counts[a.charAt(i) - 'a']++;
            counts[b.charAt(i) - 'a']--;
        }
        for (int n : counts) if (n != 0) return false;
        return true;
    }

    /** Index of the first character that appears exactly once (lowercase a-z), or -1. Idea: count, then scan again. O(n) time, O(1) space. */
    static int firstUniqueChar(String s) {
        int[] counts = new int[26];
        for (char c : s.toCharArray()) counts[c - 'a']++;
        for (int i = 0; i < s.length(); i++) if (counts[s.charAt(i) - 'a'] == 1) return i;
        return -1;
    }

    /** Words in reverse order, extra spaces removed: trim, split on whitespace runs, reverse, join. O(n) time, O(n) space. */
    static String reverseWords(String s) {
        String[] words = s.trim().split("\\s+");
        Collections.reverse(Arrays.asList(words));                      // asList is a write-through view: the array itself gets reversed
        return String.join(" ", words);
    }

    /** Longest common prefix: walk column by column and stop at the first word that differs or ends. O(n * m) time (n words, m = first word's length), O(1) space. */
    static String longestCommonPrefix(String... words) {
        if (words.length == 0) return "";
        String first = words[0];
        for (int i = 0; i < first.length(); i++) {
            for (String w : words) {
                if (i >= w.length() || w.charAt(i) != first.charAt(i)) return first.substring(0, i);
            }
        }
        return first;
    }

    /** Number of vowels, ignoring case. One pass. O(n) time, O(1) space. */
    static int countVowels(String s) {
        return (int) s.chars().filter(c -> "aeiou".indexOf(Character.toLowerCase(c)) >= 0).count();
    }

    /** Run-length encoding: "aaabb" becomes "a3b2" (a single letter gets a 1, so the result can be longer). O(n) time, O(n) space. */
    static String compress(String s) {
        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (int i = 1; i <= s.length(); i++) {
            if (i < s.length() && s.charAt(i) == s.charAt(i - 1)) {
                count++;
            } else {
                sb.append(s.charAt(i - 1)).append(count);
                count = 1;
            }
        }
        return sb.toString();
    }

    /**
     * Length of the longest substring without a repeated character. Sliding window: remember the last index of every
     * character and jump the window start past an earlier copy. O(n) time, O(k) space for k distinct characters.
     */
    static int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int start = 0;
        int best = 0;
        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            Integer previous = lastSeen.get(c);
            if (previous != null && previous >= start) start = previous + 1;   // the copy must be inside the window
            lastSeen.put(c, end);
            best = Math.max(best, end - start + 1);
        }
        return best;
    }

    /** Is b a rotation of a? If it is, it appears inside a + a. O(n) space; time O(n) with a linear search. */
    static boolean isRotation(String a, String b) {
        return a.length() == b.length() && (a + a).contains(b);
    }

    public static void main(String[] args) {
        title("STRING PATTERNS  -  palindrome, anagram, window, run-length");

        section("Palindrome: two pointers");
        syntax("i = 0; j = last; while (i < j) { ...; i++; j--; }", "compare from both ends, skip what does not count");
        show(() -> isPalindrome("No 'x' in Nixon"));                    // letters only: noxinnixon reads the same both ways
        show(() -> isPalindrome("A man, a plan, a canal: Panama"));
        show(() -> isPalindrome("race a car"));
        show(() -> isPalindrome(""));                                   // edge: empty is a palindrome
        show(() -> isPalindrome(".,;"));                                // edge: nothing to compare, so true
        show(() -> isPalindrome("0P"));                                 // edge: digits count, '0' differs from 'p'
        String racecar = "racecar";
        show(() -> new StringBuilder(racecar).reverse().toString() == racecar);        // TRAP: false, == compares objects, not text
        show(() -> new StringBuilder(racecar).reverse().toString().equals(racecar));   // true: the short version, but O(n) extra space

        section("Anagram: sort, or count");
        syntax("counts[a.charAt(i) - 'a']++; counts[b.charAt(i) - 'a']--", "int[26]: all counts back to zero = anagram");
        show(() -> isAnagramSorted("listen", "silent"));
        show(() -> isAnagramSorted("rat", "car"));
        show(() -> isAnagramSorted("", ""));
        show(() -> isAnagramCounts("listen", "silent"));
        show(() -> isAnagramCounts("aab", "abb"));                      // same length and letters, different counts: false
        show(() -> isAnagramCounts("abc", "ab"));                       // different lengths are rejected first

        section("First unique character: count, then scan");
        show(() -> firstUniqueChar("leetcode"));                        // 'l' at index 0
        show(() -> firstUniqueChar("loveleetcode"));                    // 'l' and 'o' repeat, so 'v' at 2
        show(() -> firstUniqueChar("aabb"));                            // none: -1
        show(() -> firstUniqueChar(""));

        section("Reverse words");
        show(() -> reverseWords("the sky is blue"));
        show(() -> reverseWords("  hello   world  "));                  // edge: extra spaces vanish
        show(() -> reverseWords("one"));
        show(() -> reverseWords(""));

        section("Longest common prefix: column by column");
        show(() -> longestCommonPrefix("flower", "flow", "flight"));
        show(() -> longestCommonPrefix("dog", "racecar", "car"));       // no common start: empty String
        show(() -> longestCommonPrefix("solo"));                        // edge: one word is its own prefix
        show(() -> longestCommonPrefix());                              // edge: no words at all (varargs gives an empty array)

        section("Count vowels and run-length compress");
        show(() -> countVowels("Hello World"));
        show(() -> countVowels("rhythm"));
        show(() -> countVowels("AEIOU"));                               // case is ignored
        show(() -> compress("aaabb"));
        show(() -> compress("aabcccccaaa"));                            // a repeated letter later starts a new run
        show(() -> compress("abc"));                                    // every letter once: longer than the input
        show(() -> compress("a".repeat(12)));                           // counts can have several digits
        show(() -> compress(""));

        section("Longest substring without repeats: sliding window + map");
        syntax("start = lastSeen[c] + 1", "the window [start..end] never holds a repeated character");
        show(() -> lengthOfLongestSubstring("abcabcbb"));               // abc
        show(() -> lengthOfLongestSubstring("bbbbb"));                  // b
        show(() -> lengthOfLongestSubstring("pwwkew"));                 // wke
        show(() -> lengthOfLongestSubstring("abba"));                   // TRAP: 2; without the previous >= start check the window start jumps back and it says 3
        show(() -> lengthOfLongestSubstring(""));

        section("Rotation: look inside a + a");
        show(() -> isRotation("waterbottle", "erbottlewat"));
        show(() -> isRotation("abc", "acb"));                           // same letters, but not a rotation
        show(() -> isRotation("ab", "abc"));                            // different lengths
        show(() -> isRotation("", ""));
        note("Interview routine: clarify the input (case? spaces? empty?), try an example, say the brute force, then optimize");

        section("Cost");
        bigO(
                "isPalindrome | countVowels", "O(n) time, O(1) space",
                "isAnagramSorted", "O(n log n) time, O(n) space",
                "isAnagramCounts | firstUniqueChar", "O(n) time, O(1) space: 26 counters",
                "reverseWords | compress", "O(n) time, O(n) space",
                "longestCommonPrefix", "O(n * m) time, O(1) space",
                "lengthOfLongestSubstring", "O(n) time, O(k) space for k distinct chars",
                "isRotation", "O(n) space; the JDK's contains is O(n * m) worst case"
        );
    }
}
