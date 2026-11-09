package review.strings

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/**
 * Palindrome that ignores case and every character that is not a letter or digit.
 * Idea: two pointers walk toward each other and skip the characters that do not count. O(n) time, O(1) space.
 */
fun isPalindrome(s: String): Boolean {
    var i = 0
    var j = s.lastIndex
    while (i < j) {
        when {
            !s[i].isLetterOrDigit() -> i++
            !s[j].isLetterOrDigit() -> j--
            s[i].lowercaseChar() != s[j].lowercaseChar() -> return false
            else -> { i++; j-- }
        }
    }
    return true
}

/** Anagram check by sorting both texts: equal sorted letters means anagrams. O(n log n) time, O(n) space. */
fun isAnagramSorted(a: String, b: String): Boolean =
    a.length == b.length && a.toList().sorted() == b.toList().sorted()

/** Anagram check by counting, lowercase a-z only: +1 for a letter of a, -1 for a letter of b, all zero at the end. O(n) time, O(1) space. */
fun isAnagramCounts(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    val counts = IntArray(26)
    for (i in a.indices) {
        counts[a[i] - 'a']++
        counts[b[i] - 'a']--
    }
    return counts.all { it == 0 }
}

/** Index of the first character that appears exactly once (lowercase a-z), or -1. Idea: count, then scan again. O(n) time, O(1) space. */
fun firstUniqueChar(s: String): Int {
    val counts = IntArray(26)
    for (c in s) counts[c - 'a']++
    for (i in s.indices) if (counts[s[i] - 'a'] == 1) return i
    return -1
}

/** Words in reverse order, extra spaces removed: trim, split on whitespace runs, reverse, join. O(n) time, O(n) space. */
fun reverseWords(s: String): String = s.trim().split(Regex("""\s+""")).reversed().joinToString(" ")

/** Longest common prefix: walk column by column and stop at the first word that differs or ends. O(n * m) time (n words, m = first word's length), O(1) space. */
fun longestCommonPrefix(words: List<String>): String {
    if (words.isEmpty()) return ""
    val first = words[0]
    for (i in first.indices) {
        for (w in words) if (i >= w.length || w[i] != first[i]) return first.take(i)
    }
    return first
}

/** Number of vowels, ignoring case. One pass. O(n) time, O(1) space. */
fun countVowels(s: String): Int = s.count { it.lowercaseChar() in "aeiou" }

/** Run-length encoding: "aaabb" becomes "a3b2" (a single letter gets a 1, so the result can be longer). O(n) time, O(n) space. */
fun compress(s: String): String {
    val sb = StringBuilder()
    var count = 1
    for (i in 1..s.length) {
        if (i < s.length && s[i] == s[i - 1]) {
            count++
        } else {
            sb.append(s[i - 1]).append(count)
            count = 1
        }
    }
    return sb.toString()
}

/**
 * Length of the longest substring without a repeated character. Sliding window: remember the last index of every
 * character and jump the window start past an earlier copy. O(n) time, O(k) space for k distinct characters.
 */
fun lengthOfLongestSubstring(s: String): Int {
    val lastSeen = HashMap<Char, Int>()
    var start = 0
    var best = 0
    for ((end, c) in s.withIndex()) {
        val previous = lastSeen[c]
        if (previous != null && previous >= start) start = previous + 1   // the copy must be inside the window
        lastSeen[c] = end
        best = maxOf(best, end - start + 1)
    }
    return best
}

/** Is b a rotation of a? If it is, it appears inside a + a. O(n) space; time O(n) with a linear search. */
fun isRotation(a: String, b: String): Boolean = a.length == b.length && (a + a).contains(b)

/**
 * 6d. STRING PATTERNS: the string problems interviewers ask again and again
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.StringPatternsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STRING PATTERNS  -  palindrome, anagram, window, run-length")

    section("Palindrome: two pointers")
    syntax("i = 0; j = last; while (i < j) { ...; i++; j-- }" to "compare from both ends, skip what does not count")
    show { isPalindrome("No 'x' in Nixon") }                  // letters only: noxinnixon reads the same both ways
    show { isPalindrome("race a car") }
    show { isPalindrome("") }                                 // edge: empty is a palindrome
    show { isPalindrome(".,;") }                              // edge: nothing to compare, so true
    show { isPalindrome("0P") }                               // edge: digits count, '0' differs from 'p'

    section("Anagram: sort, or count")
    syntax("counts[a[i] - 'a']++; counts[b[i] - 'a']--" to "IntArray(26): all counts back to zero = anagram")
    show { isAnagramSorted("listen", "silent") }
    show { isAnagramSorted("rat", "car") }
    show { isAnagramSorted("", "") }
    show { isAnagramCounts("listen", "silent") }
    show { isAnagramCounts("aab", "abb") }                    // same length and letters, different counts: false
    show { isAnagramCounts("abc", "ab") }                     // different lengths are rejected first

    section("First unique character: count, then scan")
    show { firstUniqueChar("leetcode") }                      // 'l' at index 0
    show { firstUniqueChar("loveleetcode") }                  // 'l' and 'o' repeat, so 'v' at 2
    show { firstUniqueChar("aabb") }                          // none: -1
    show { firstUniqueChar("") }

    section("Reverse words")
    show { reverseWords("the sky is blue") }
    show { reverseWords("  hello   world  ") }                // edge: extra spaces vanish
    show { reverseWords("one") }
    show { reverseWords("") }

    section("Longest common prefix: column by column")
    show { longestCommonPrefix(listOf("flower", "flow", "flight")) }
    show { longestCommonPrefix(listOf("dog", "racecar", "car")) }   // no common start: empty String
    show { longestCommonPrefix(listOf("solo")) }              // edge: one word is its own prefix
    show { longestCommonPrefix(emptyList()) }                 // edge: no words at all

    section("Count vowels and run-length compress")
    show { countVowels("Hello World") }
    show { countVowels("rhythm") }
    show { countVowels("AEIOU") }                             // case is ignored
    show { compress("aaabb") }
    show { compress("aabcccccaaa") }                          // a repeated letter later starts a new run
    show { compress("abc") }                                  // every letter once: longer than the input
    show { compress("a".repeat(12)) }                         // counts can have several digits
    show { compress("") }

    section("Longest substring without repeats: sliding window + map")
    syntax("start jumps to lastSeen[c] + 1" to "the window [start..end] never holds a repeated character")
    show { lengthOfLongestSubstring("abcabcbb") }             // abc
    show { lengthOfLongestSubstring("bbbbb") }                // b
    show { lengthOfLongestSubstring("pwwkew") }               // wke
    show { lengthOfLongestSubstring("abba") }                 // TRAP: 2; without the previous >= start check the window start jumps back and it says 3
    show { lengthOfLongestSubstring("") }

    section("Rotation: look inside a + a")
    show { isRotation("waterbottle", "erbottlewat") }
    show { isRotation("abc", "acb") }                         // same letters, but not a rotation
    show { isRotation("ab", "abc") }                          // different lengths
    show { isRotation("", "") }
    note("Interview routine: clarify the input (case? spaces? empty?), try an example, say the brute force, then optimize")

    section("Cost")
    bigO(
        "isPalindrome | countVowels" to "O(n) time, O(1) space",
        "isAnagramSorted" to "O(n log n) time, O(n) space",
        "isAnagramCounts | firstUniqueChar" to "O(n) time, O(1) space: 26 counters",
        "reverseWords | compress" to "O(n) time, O(n) space",
        "longestCommonPrefix" to "O(n * m) time, O(1) space",
        "lengthOfLongestSubstring" to "O(n) time, O(k) space for k distinct chars",
        "isRotation" to "O(n) space; the JVM's contains is O(n * m) worst case",
    )
}
