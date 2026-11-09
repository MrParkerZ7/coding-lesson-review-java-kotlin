package review.sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 5e. COUNTING AND GROUPING with maps and sets: the patterns behind most interview problems
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.CountingAndGrouping
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class CountingAndGrouping {

    /** Two Sum: one pass, remember each value's index in a HashMap. Returns the two indexes, or an empty array. O(n). */
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();                           // value -> index
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) return new int[] {j, i};
            seen.put(nums[i], i);
        }
        return new int[0];
    }

    /** Does the array contain a duplicate? add() is false for a value already seen. O(n). */
    static boolean hasDuplicate(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int x : nums) if (!seen.add(x)) return true;
        return false;
    }

    /** First character that appears only once, or null. Count first, then scan. O(n). */
    static Character firstUnique(String s) {
        Map<Character, Integer> counts = new LinkedHashMap<>();
        for (char c : s.toCharArray()) counts.merge(c, 1, Integer::sum);
        for (char c : s.toCharArray()) if (counts.get(c) == 1) return c;
        return null;
    }

    /** Group words that are anagrams of each other: the sorted letters are the key. O(n * k log k) for words of length k. */
    static List<List<String>> groupAnagrams(List<String> words) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String w : words) {
            char[] letters = w.toCharArray();
            Arrays.sort(letters);
            groups.computeIfAbsent(new String(letters), key -> new ArrayList<>()).add(w);
        }
        return new ArrayList<>(groups.values());
    }

    /** The broken counter: get() is null for a new key, and null + 1 throws. */
    static void incrementBroken(Map<Character, Integer> counts, char c) {
        counts.put(c, counts.get(c) + 1);
    }

    public static void main(String[] args) {
        title("COUNTING AND GROUPING  -  the map and set patterns");

        section("Counting characters: four ways");
        syntax(
                "m.put(k, m.getOrDefault(k, 0) + 1)", "the classic loop",
                "m.merge(k, 1, Integer::sum)", "one call: absent -> 1, present -> old + 1",
                "stream.collect(groupingBy(c -> c, counting()))", "one expression, the counts are Long (lesson 4)",
                "int[] counts = new int[26]; counts[c - 'a']++", "fastest, lowercase letters only (lesson 6)"
        );
        String word = "banana";
        Map<Character, Integer> classic = new TreeMap<>();
        for (char c : word.toCharArray()) classic.put(c, classic.getOrDefault(c, 0) + 1);
        show(() -> classic);                                                     // the classic loop
        Map<Character, Integer> merged = new TreeMap<>();
        for (char c : word.toCharArray()) merged.merge(c, 1, Integer::sum);
        show(() -> merged);                                                      // merge: the same counts
        show(() -> word.chars().mapToObj(c -> (char) c).collect(Collectors.groupingBy(c -> c, TreeMap::new, Collectors.counting())));
        int[] letters = new int[26];
        for (char c : word.toCharArray()) letters[c - 'a']++;
        show(() -> letters[0]);                                                  // the count of 'a' sits at index 0
        show(() -> Collections.max(classic.entrySet(), Map.Entry.comparingByValue()).getKey());   // the most common character
        show(() -> thrown(() -> { incrementBroken(classic, 'z'); return 0; }));  // TRAP: map.get(k) + 1 on a new key is null + 1

        section("Grouping");
        syntax(
                "m.computeIfAbsent(k, x -> new ArrayList<>()).add(v)", "by hand: create the list on first use, then add",
                "stream.collect(groupingBy(keyFn))", "the same in one expression (lesson 4)"
        );
        List<String> words = List.of("apple", "avocado", "banana", "blueberry", "cherry");
        Map<Character, List<String>> byFirst = new TreeMap<>();
        for (String w : words) byFirst.computeIfAbsent(w.charAt(0), key -> new ArrayList<>()).add(w);
        show(() -> byFirst);
        show(() -> words.stream().collect(Collectors.groupingBy(String::length, TreeMap::new, Collectors.toList())));
        Map<String, Integer> big = Map.of("a", 1000, "b", 1000);
        show(() -> big.get("a") == big.get("b"));                                // TRAP: false, == on two Integer objects compares references
        show(() -> big.get("a").equals(big.get("b")));                           // true: use equals() for boxed values
        Map<String, Integer> small = Map.of("a", 100, "b", 100);
        show(() -> small.get("a") == small.get("b"));                            // true only because -128..127 are cached (lesson 0): never rely on it

        section("Interview patterns");
        show(() -> twoSum(new int[] {2, 7, 11, 15}, 9));                         // the indexes of the two numbers that add up to 9
        show(() -> twoSum(new int[] {3, 3}, 6));                                 // the same value twice works: the map is checked BEFORE storing
        show(() -> twoSum(new int[] {3}, 6));                                    // one 3 is not a pair: an empty array
        show(() -> twoSum(new int[] {1, 2, 3}, 100));                            // no pair
        show(() -> hasDuplicate(new int[] {1, 2, 3, 1}));
        show(() -> hasDuplicate(new int[] {1, 2, 3}));
        show(() -> hasDuplicate(new int[] {}));                                  // empty: no duplicate
        show(() -> firstUnique("swiss"));
        show(() -> firstUnique("aabb"));                                         // null when every character repeats
        show(() -> groupAnagrams(List.of("eat", "tea", "tan", "ate", "nat", "bat")));
        note("A Character (boxed) can be null where a char cannot: the return type of firstUnique says so");

        section("Cost");
        bigO(
                "twoSum with a HashMap", "O(n) time, O(n) space (nested loops: O(n^2) time, O(1) space)",
                "hasDuplicate with a HashSet", "O(n) time, O(n) space (sort first: O(n log n), O(1) extra space)",
                "counting with a map", "O(n) time, O(distinct keys) space",
                "counting with int[26]", "O(n) time, O(1) space",
                "groupAnagrams", "O(n * k log k): every word is sorted once"
        );
    }
}
