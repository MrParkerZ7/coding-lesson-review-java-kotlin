package review.drills;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 9c. STRING AND HASH DRILLS: stack, map as key, bucket count, sliding window, count array
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.StringAndHashDrills
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class StringAndHashDrills {

    /**
     * VALID PARENTHESES. Problem: are (), [] and {} closed in the right order?
     * Idea: a stack; push every opener, and every closer must match the top. At the end the stack must be empty.
     * Time O(n), space O(n).
     */
    static boolean validParentheses(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(', '[', '{' -> stack.push(c);
                case ')' -> {
                    if (stack.isEmpty() || stack.pop() != '(') return false;   // isEmpty() first: pop() on an empty deque throws
                }
                case ']' -> {
                    if (stack.isEmpty() || stack.pop() != '[') return false;
                }
                case '}' -> {
                    if (stack.isEmpty() || stack.pop() != '{') return false;
                }
                default -> {
                }
            }
        }
        return stack.isEmpty();
    }

    /**
     * GROUP ANAGRAMS. Problem: put words made of the same letters in one group.
     * Idea: the sorted letters of a word are the same for all its anagrams, so use them as the map key.
     * Time O(n * k log k) for n words of length k, space O(n * k).
     */
    static List<List<String>> groupAnagrams(List<String> words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();       // keeps the order of first appearance
        for (String w : words) {
            char[] letters = w.toCharArray();
            Arrays.sort(letters);
            groups.computeIfAbsent(new String(letters), key -> new ArrayList<>()).add(w);   // a String key; a char[] key would not work
        }
        return new ArrayList<>(groups.values());
    }

    /**
     * TOP K FREQUENT. Problem: the k values that appear most often.
     * Idea: count with a map, then bucket by count (a value seen c times goes to bucket c) and read from the top bucket down.
     * Time O(n), space O(n). Ties come out in the order the values were first seen.
     */
    static List<Integer> topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        for (int x : nums) counts.merge(x, 1, Integer::sum);
        List<List<Integer>> buckets = new ArrayList<>();                // a list of lists: Java cannot create a generic array
        for (int c = 0; c <= nums.length; c++) buckets.add(new ArrayList<>());
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) buckets.get(e.getValue()).add(e.getKey());
        List<Integer> result = new ArrayList<>();
        for (int c = buckets.size() - 1; c >= 1; c--) {
            for (int x : buckets.get(c)) {
                if (result.size() == k) return result;
                result.add(x);
            }
        }
        return result;
    }

    /**
     * LONGEST SUBSTRING WITHOUT REPEATING CHARACTERS. Problem: the length of the longest run of distinct characters.
     * Idea: sliding window [left, right]; remember the last index of each character and jump left past an earlier copy.
     * Time O(n), space O(min(n, alphabet)).
     */
    static int longestUniqueSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int left = 0;
        int best = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            left = Math.max(left, lastSeen.getOrDefault(c, -1) + 1);    // max: a copy OUTSIDE the window must not pull left back
            lastSeen.put(c, right);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /**
     * VALID ANAGRAM. Problem: do two lower-case words use exactly the same letters?
     * Idea: one count array; +1 for each letter of a, -1 for each letter of b; all zero means anagram.
     * Time O(n), space O(1) (26 counters). Assumes a..z: ask the interviewer about the alphabet.
     */
    static boolean isAnagram(String a, String b) {
        if (a.length() != b.length()) return false;
        int[] counts = new int[26];
        for (int i = 0; i < a.length(); i++) {
            counts[a.charAt(i) - 'a']++;
            counts[b.charAt(i) - 'a']--;
        }
        for (int n : counts) {
            if (n != 0) return false;
        }
        return true;
    }

    /**
     * FIRST UNIQUE CHARACTER. Problem: the index of the first character that appears exactly once, or -1.
     * Idea: count every character in a map (any Unicode char), then scan again and stop at the first one with count 1.
     * Time O(n), space O(alphabet).
     */
    static int firstUniqueChar(String s) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : s.toCharArray()) counts.merge(c, 1, Integer::sum);
        for (int i = 0; i < s.length(); i++) {
            if (counts.get(s.charAt(i)) == 1) return i;                 // Integer == int unboxes: a number test, not an identity test
        }
        return -1;
    }

    public static void main(String[] args) {
        title("STRING AND HASH DRILLS  -  a map, a set or a stack does the work");

        section("Valid parentheses (stack)");
        syntax("Deque<Character> stack = new ArrayDeque<>(); push | pop | isEmpty", "ArrayDeque is the stack; the old Stack class is not used (lesson 5)");
        show(() -> validParentheses("()[]{}"));
        show(() -> validParentheses("([)]"));                           // closed in the wrong order
        show(() -> validParentheses(""));                               // empty text is balanced
        show(() -> validParentheses("("));                              // an opener never closed: the stack is not empty
        show(() -> validParentheses("]"));                              // a closer with nothing open: the isEmpty() check catches it
        Deque<Character> empty = new ArrayDeque<>();
        show(() -> thrown(() -> empty.pop()));                          // pop() on an empty ArrayDeque: NoSuchElementException
        show(() -> thrown(() -> empty.poll() != '('));                  // TRAP: poll() gives null, and comparing null with a char unboxes it

        section("Group anagrams (sorted letters as the key)");
        show(() -> groupAnagrams(List.of("eat", "tea", "tan", "ate", "nat", "bat")));   // groups keep the order of first appearance
        show(() -> groupAnagrams(List.of("a", "a")));                   // duplicates are grouped, not dropped
        show(() -> groupAnagrams(List.of("ab", "ba", "abc")));
        show(() -> groupAnagrams(List.of()));
        Map<char[], Integer> byArray = new HashMap<>();
        byArray.put(new char[] {'a'}, 1);
        show(() -> byArray.get(new char[] {'a'}));                      // TRAP: null, an array uses identity equals/hashCode: key by a String
        wontCompile("List<Integer>[] buckets = new List<Integer>[n];", "generic array creation: use a List<List<Integer>>");

        section("Top K frequent (count, then bucket by count)");
        show(() -> topKFrequent(new int[] {1, 1, 1, 2, 2, 3}, 2));
        show(() -> topKFrequent(new int[] {5}, 1));
        show(() -> topKFrequent(new int[] {4, 4, 5, 5, 6}, 2));         // a tie for the top: first seen first
        show(() -> topKFrequent(new int[] {1, 2, 2}, 5));               // k larger than the distinct values: all of them
        show(() -> topKFrequent(new int[0], 3));
        note("map.merge(x, 1, Integer::sum) is the one-line count. A HashMap would also work, but its tie order is not guaranteed");

        section("Longest substring without repeats (sliding window)");
        syntax("left = Math.max(left, lastSeen.getOrDefault(c, -1) + 1)", "jump the window start past the earlier copy, never backwards");
        show(() -> longestUniqueSubstring("abcabcbb"));
        show(() -> longestUniqueSubstring("bbbbb"));
        show(() -> longestUniqueSubstring("pwwkew"));
        show(() -> longestUniqueSubstring("abba"));                     // TRAP: without Math.max the window start jumps back and it says 3
        show(() -> longestUniqueSubstring(""));

        section("Valid anagram (count array) and first unique character");
        show(() -> isAnagram("listen", "silent"));
        show(() -> isAnagram("rat", "car"));
        show(() -> isAnagram("a", "ab"));                               // different lengths: no need to count
        show(() -> isAnagram("", ""));
        show(() -> thrown(() -> isAnagram("A", "B")));                  // TRAP: upper case falls outside the 26 counters
        show(() -> firstUniqueChar("leetcode"));
        show(() -> firstUniqueChar("loveleetcode"));                    // l and o repeat later; v at index 2 is the first one that does not
        show(() -> firstUniqueChar("aabb"));                            // nothing unique: -1
        show(() -> firstUniqueChar(""));
        note("Always ask: which characters (a-z, any Unicode)? A HashMap handles everything; int[26] is faster but narrower");

        section("Cost");
        bigO(
                "validParentheses", "O(n) time, O(n) space (the stack)",
                "groupAnagrams", "O(n * k log k) time: each word of length k is sorted",
                "topKFrequent (buckets)", "O(n) time, O(n) space; a heap of size k gives O(n log k)",
                "longestUniqueSubstring", "O(n) time, O(alphabet) space",
                "isAnagram | firstUniqueChar", "O(n) time, O(1) space for a fixed alphabet"
        );
    }
}
