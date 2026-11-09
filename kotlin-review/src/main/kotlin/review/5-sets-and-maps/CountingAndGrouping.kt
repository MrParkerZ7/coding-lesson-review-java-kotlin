package review.sets

import review.bigO
import review.section
import review.show
import review.syntax
import review.title

/** Two Sum: one pass, remember each value's index in a HashMap. O(n). */
fun twoSum(nums: List<Int>, target: Int): Pair<Int, Int>? {
    val seen = HashMap<Int, Int>()                 // value -> index
    for ((i, x) in nums.withIndex()) {
        val j = seen[target - x]
        if (j != null) return j to i
        seen[x] = i
    }
    return null
}

/** Does the list contain a duplicate? A set drops duplicates, so the size changes. O(n). */
fun hasDuplicate(nums: List<Int>): Boolean = nums.size != nums.toSet().size

/** First character that appears only once, or null. Count first, then scan. O(n). */
fun firstUnique(s: String): Char? {
    val counts = s.groupingBy { it }.eachCount()
    return s.firstOrNull { counts.getValue(it) == 1 }
}

/** Group words that are anagrams of each other: the sorted letters are the key. */
fun groupAnagrams(words: List<String>): List<List<String>> =
    words.groupBy { it.toList().sorted().joinToString("") }.values.toList()

/**
 * 5c. COUNTING AND GROUPING with maps and sets: the patterns behind most interview problems
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.CountingAndGroupingKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("COUNTING AND GROUPING  -  the map and set patterns")

    section("Counting characters: four ways")
    syntax(
        "map[k] = (map[k] ?: 0) + 1" to "the classic loop",
        "map.merge(k, 1, Int::plus)" to "same, Java style",
        "list.groupingBy { it }.eachCount()" to "one line",
    )
    val word = "banana"
    val classic = mutableMapOf<Char, Int>()
    for (c in word) classic[c] = (classic[c] ?: 0) + 1
    show { classic }                                                           // the classic loop
    val merged = mutableMapOf<Char, Int>()
    for (c in word) merged.merge(c, 1, Int::plus)
    show { merged }                                                            // merge: the same counts
    show { word.groupingBy { it }.eachCount() }                                // one line
    show { word.groupBy { it }.mapValues { it.value.size } }                   // groupBy then size: builds the lists first, slower
    show { word.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key }  // the most common character

    section("Grouping")
    syntax(
        "xs.groupBy { key }" to "Map<key, List<element>>",
        "xs.associateBy { key } | xs.associateWith { value }" to "one element per key | element -> value",
    )
    val words = listOf("apple", "avocado", "banana", "blueberry", "cherry")
    show { words.groupBy { it.first() } }
    show { words.groupBy({ it.first() }, { it.length }) }                      // key, then what to store
    show { words.associateBy { it.first() } }                                  // a repeated key keeps the LAST element
    show { words.associateWith { it.length } }
    val byLength = mutableMapOf<Int, MutableList<String>>()
    for (w in words) byLength.getOrPut(w.length) { mutableListOf() }.add(w)
    show { byLength }                                                          // getOrPut: build the lists by hand

    section("Interview patterns")
    show { twoSum(listOf(2, 7, 11, 15), 9) }                                   // indexes of the two numbers that add up to 9
    show { twoSum(listOf(1, 2, 3), 100) }                                      // null when there is no pair
    show { hasDuplicate(listOf(1, 2, 3, 1)) }
    show { hasDuplicate(listOf(1, 2, 3)) }
    show { firstUnique("swiss") }
    show { firstUnique("aabb") }
    show { groupAnagrams(listOf("eat", "tea", "tan", "ate", "nat", "bat")) }

    section("Cost")
    bigO(
        "twoSum with a HashMap" to "O(n) time, O(n) space (nested loops: O(n^2) time)",
        "hasDuplicate with a set" to "O(n) time, O(n) space (sort first: O(n log n), O(1) space)",
        "counting with a map" to "O(n)",
    )
}
