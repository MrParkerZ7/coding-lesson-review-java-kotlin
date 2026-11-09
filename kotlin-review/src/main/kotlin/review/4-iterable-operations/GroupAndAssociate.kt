package review.iterable

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** A small record for the grouping and sorting demos. */
data class Person(val name: String, val age: Int)

/**
 * 4c. GROUPBY, ASSOCIATE, PARTITION, DISTINCTBY, SORTEDBY: turn a List into a Map, split it, sort it
 *     (lesson 5 CountingAndGrouping uses groupBy and associate for counting problems)
 *     Run: mvn -q compile exec:java -Dmain.class=review.iterable.GroupAndAssociateKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("GROUP AND ASSOCIATE  -  List to Map, split in two, sort")

    val words = listOf("bat", "ant", "ape", "bee", "cow", "bee")
    val nums = listOf(1, 2, 3, 4, 5, 6)
    val people = listOf(Person("Cid", 30), Person("Ann", 25), Person("Bob", 30))

    section("groupBy: one key, many elements")
    syntax(
        "xs.groupBy { key }" to "Map<key, List<element>>, keys in first-seen order",
        "xs.groupBy({ key }, { value })" to "store a transformed value instead of the element",
        "xs.groupingBy { key }.eachCount()" to "Map<key, Int>: only counts, builds no lists",
    )
    show { words.groupBy { it.first() } }
    show { words.groupBy({ it.first() }, { it.uppercase() }) }
    show { words.groupingBy { it }.eachCount() }
    show { people.groupBy({ it.age }, { it.name }) }          // names per age

    section("associate: List to Map, ONE value per key")
    syntax(
        "xs.associateBy { key }" to "the key comes from the element; the value is the element",
        "xs.associateWith { value }" to "the element is the key; the value is computed",
        "xs.associate { k to v }" to "build both the key and the value",
        "pairs.toMap()" to "from a list of Pairs",
    )
    show { words.associateBy { it.first() } }                 // TRAP: a repeated key keeps the LAST element (bee, not bat)
    show { words.associateWith { it.length } }                // "bee" appears twice, but a key exists once
    show { people.associate { it.name to it.age } }
    show { listOf("a" to 1, "b" to 2, "a" to 3).toMap() }     // toMap also keeps the last value of a repeated key
    show { words.distinctBy { it.first() } }                  // distinctBy keeps the FIRST element of each key
    show { words.toSet() }                                    // duplicates removed, first-seen order kept

    section("partition: split in two with one pass")
    syntax("xs.partition { condition }" to "Pair(matching, not matching)")
    show { nums.partition { it % 2 == 0 } }
    val (evens, odds) = nums.partition { it % 2 == 0 }
    show { evens }                                            // destructure the Pair (lesson 1)
    show { odds }

    section("Sorting: sortedBy, sortedWith, compareBy")
    syntax(
        "xs.sortedBy { key } | sortedByDescending { key }" to "a NEW sorted list; the sort is stable",
        "xs.sortedWith(compareBy<T> { a }.thenBy { b })" to "several keys: the next key breaks ties",
        "xs.sorted() | sortedDescending()" to "natural order of the elements",
    )
    show { people.sortedBy { it.age }.map { it.name } }       // stable: Cid and Bob are tied on age and keep their input order
    show { people.sortedByDescending { it.age }.map { it.name } }
    val byAgeThenName = compareByDescending<Person> { it.age }.thenBy { it.name }
    show { people.sortedWith(byAgeThenName).map { it.name } } // age descending, then name ascending
    val fruit = listOf("pear", "fig", "apple", "kiwi")
    show { fruit.sortedWith(compareBy<String> { it.length }.thenBy { it }) }   // shortest first, ties alphabetical
    show { people.maxBy { it.age }.name }                     // a tie again: the FIRST element wins
    show { people.minBy { it.age }.name }
    note("sortedBy returns a NEW sorted list; MutableList.sortBy { } sorts the list itself (lesson 3)")

    section("Cost")
    bigO(
        "groupBy | associate | partition | distinctBy | toSet" to "O(n) time, O(n) space: a hash map underneath",
        "sortedBy | sortedWith | sorted" to "O(n log n) time, O(n) for the new list",
        "maxBy | minBy" to "O(n), one pass",
        "groupingBy { }.eachCount()" to "O(n) and no lists: cheaper than groupBy { } then size",
    )
}
