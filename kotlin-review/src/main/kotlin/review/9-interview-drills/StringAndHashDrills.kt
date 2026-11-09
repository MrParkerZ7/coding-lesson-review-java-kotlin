package review.drills

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** The exception class a block throws (its simple name), or null when it throws nothing. */
private fun errorName(block: () -> Any?): String? = runCatching { block() }.exceptionOrNull()?.javaClass?.simpleName

/**
 * VALID PARENTHESES. Problem: are (), [] and {} closed in the right order?
 * Idea: a stack; push every opener, and every closer must match the top. At the end the stack must be empty.
 * Time O(n), space O(n).
 */
fun validParentheses(s: String): Boolean {
    val stack = ArrayDeque<Char>()
    for (c in s) {
        when (c) {
            '(', '[', '{' -> stack.addLast(c)
            ')' -> if (stack.removeLastOrNull() != '(') return false
            ']' -> if (stack.removeLastOrNull() != '[') return false
            '}' -> if (stack.removeLastOrNull() != '{') return false
        }
    }
    return stack.isEmpty()
}

/**
 * GROUP ANAGRAMS. Problem: put words made of the same letters in one group.
 * Idea: the sorted letters of a word are the same for all its anagrams, so use them as the map key.
 * Time O(n * k log k) for n words of length k, space O(n * k).
 */
fun groupAnagrams(words: List<String>): List<List<String>> =
    words.groupBy { it.toCharArray().sorted().joinToString("") }.values.toList()

/**
 * TOP K FREQUENT. Problem: the k values that appear most often.
 * Idea: count with a map, then bucket by count (a value seen c times goes to buckets[c]) and read from the top bucket down.
 * Time O(n), space O(n). Ties come out in the order the values were first seen.
 */
fun topKFrequent(nums: IntArray, k: Int): List<Int> {
    val counts = mutableMapOf<Int, Int>()
    for (x in nums) counts[x] = (counts[x] ?: 0) + 1
    val buckets = Array(nums.size + 1) { mutableListOf<Int>() }
    for ((x, c) in counts) buckets[c].add(x)
    val result = ArrayList<Int>()
    for (c in buckets.lastIndex downTo 1) {
        for (x in buckets[c]) {
            if (result.size == k) return result
            result.add(x)
        }
    }
    return result
}

/**
 * LONGEST SUBSTRING WITHOUT REPEATING CHARACTERS. Problem: the length of the longest run of distinct characters.
 * Idea: sliding window [left, right]; remember the last index of each character and jump left past an earlier copy.
 * Time O(n), space O(min(n, alphabet)).
 */
fun longestUniqueSubstring(s: String): Int {
    val lastSeen = HashMap<Char, Int>()
    var left = 0
    var best = 0
    for ((right, c) in s.withIndex()) {
        val prev = lastSeen[c]
        if (prev != null && prev >= left) left = prev + 1     // only if the copy is INSIDE the window: left never moves back
        lastSeen[c] = right
        best = maxOf(best, right - left + 1)
    }
    return best
}

/**
 * VALID ANAGRAM. Problem: do two lower-case words use exactly the same letters?
 * Idea: one count array; +1 for each letter of a, -1 for each letter of b; all zero means anagram.
 * Time O(n), space O(1) (26 counters). Assumes a..z: ask the interviewer about the alphabet.
 */
fun isAnagram(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    val counts = IntArray(26)
    for (i in a.indices) {
        counts[a[i] - 'a']++
        counts[b[i] - 'a']--
    }
    return counts.all { it == 0 }
}

/**
 * FIRST UNIQUE CHARACTER. Problem: the index of the first character that appears exactly once, or -1.
 * Idea: count every character, then scan again and stop at the first one with count 1.
 * Time O(n), space O(alphabet).
 */
fun firstUniqueChar(s: String): Int {
    val counts = s.groupingBy { it }.eachCount()
    return s.indexOfFirst { counts[it] == 1 }
}

/**
 * 9c. STRING AND HASH DRILLS: stack, map as key, bucket count, sliding window, count array
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.StringAndHashDrillsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STRING AND HASH DRILLS  -  a map, a set or a stack does the work")

    section("Valid parentheses (stack)")
    show { validParentheses("()[]{}") }
    show { validParentheses("([)]") }                                          // closed in the wrong order
    show { validParentheses("") }                                              // empty text is balanced
    show { validParentheses("(") }                                             // an opener never closed: the stack is not empty
    show { validParentheses("]") }                                             // a closer with nothing open: removeLastOrNull() gives null

    section("Group anagrams (sorted letters as the key)")
    show { groupAnagrams(listOf("eat", "tea", "tan", "ate", "nat", "bat")) }   // groups keep the order of first appearance
    show { groupAnagrams(listOf("a", "a")) }                                   // duplicates are grouped, not dropped
    show { groupAnagrams(listOf("ab", "ba", "abc")) }
    show { groupAnagrams(emptyList()) }

    section("Top K frequent (count, then bucket by count)")
    show { topKFrequent(intArrayOf(1, 1, 1, 2, 2, 3), 2) }
    show { topKFrequent(intArrayOf(5), 1) }
    show { topKFrequent(intArrayOf(4, 4, 5, 5, 6), 2) }                        // a tie for the top: first seen first
    show { topKFrequent(intArrayOf(1, 2, 2), 5) }                              // k larger than the distinct values: all of them
    show { topKFrequent(intArrayOf(), 3) }

    section("Longest substring without repeats (sliding window)")
    syntax("left = prev + 1  (only when prev >= left)" to "jump the window start past the earlier copy")
    show { longestUniqueSubstring("abcabcbb") }
    show { longestUniqueSubstring("bbbbb") }
    show { longestUniqueSubstring("pwwkew") }
    show { longestUniqueSubstring("abba") }                                    // TRAP: without the prev >= left test this gives 3
    show { longestUniqueSubstring("") }

    section("Valid anagram (count array) and first unique character")
    show { isAnagram("listen", "silent") }
    show { isAnagram("rat", "car") }
    show { isAnagram("a", "ab") }                                              // different lengths: no need to count
    show { isAnagram("", "") }
    show { errorName { isAnagram("A", "B") } }                                 // TRAP: upper case falls outside the 26 counters
    show { firstUniqueChar("leetcode") }
    show { firstUniqueChar("loveleetcode") }                                   // l and o repeat later; v at index 2 is the first one that does not
    show { firstUniqueChar("aabb") }                                           // nothing unique: -1
    show { firstUniqueChar("") }
    note("Always ask: which characters (a-z, any Unicode)? A HashMap handles everything; IntArray(26) is faster but narrower")

    section("Cost")
    bigO(
        "validParentheses" to "O(n) time, O(n) space (the stack)",
        "groupAnagrams" to "O(n * k log k) time: each word of length k is sorted",
        "topKFrequent (buckets)" to "O(n) time, O(n) space; a heap of size k gives O(n log k)",
        "longestUniqueSubstring" to "O(n) time, O(alphabet) space",
        "isAnagram | firstUniqueChar" to "O(n) time, O(1) space for a fixed alphabet",
    )
}
