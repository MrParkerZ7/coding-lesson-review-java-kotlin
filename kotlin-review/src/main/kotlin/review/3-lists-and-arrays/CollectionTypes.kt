package review.lists

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 3b. COLLECTION TYPES: the family tree, read-only vs mutable, converting between them
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.CollectionTypesKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("COLLECTION TYPES  -  Iterable, Collection, List, Set, Map")

    section("The family")
    note("Iterable<T>  : anything you can loop with for (x in it)")
    note("  Collection<T> : + size, contains, isEmpty")
    note("    List<T>     : ordered, index access, duplicates allowed")
    note("    Set<T>      : no duplicates")
    note("Map<K, V>      : key -> value. NOT an Iterable (loop over .entries, .keys or .values)")
    note("Array<T>, IntArray : not collections, but have almost the same functions")

    section("Read-only vs mutable")
    syntax(
        "listOf | setOf | mapOf" to "read-only: cannot add, remove or set",
        "mutableListOf | mutableSetOf | mutableMapOf" to "mutable: add, remove, set allowed",
    )
    val readOnly = listOf(1, 2, 3)
    wontCompile("readOnly.add(4)", "a read-only List has no add")
    wontCompile("readOnly[0] = 9", "a read-only List has no set operator")
    wontCompile("val m: MutableList<Int> = listOf(1)", "a List is not a MutableList")
    val backing = mutableListOf(1, 2)
    val view: List<Int> = backing
    backing.add(3);                        show { view }                       // read-only is a VIEW: the owner can still change it

    section("Converting")
    syntax(
        "toList() | toSet() | toMutableList()" to "between List, Set and a mutable copy",
        "toIntArray() | toTypedArray()" to "List -> IntArray | Array<T>",
        "toMap() | associateWith { } | associateBy { }" to "Pairs or a key function -> Map",
    )
    val items = listOf(3, 1, 3, 2)
    show { items.toSet() }                                                     // removes duplicates, keeps first-seen order
    show { items.toIntArray() }
    show { items.toTypedArray() }
    show { items.zip(listOf("c", "a", "c", "b")).toMap() }                     // pairs -> Map; a repeated key keeps the last value
    show { items.associateWith { it * it } }                                   // element -> value
    show { mapOf("a" to 1).toList() }                                          // Map -> List of Pairs
    show { (1..5).toList() }                                                   // a range is an Iterable too
    show { "hello".toList() }                                                  // a String is a sequence of Char

    section("Which one to use")
    note("List: order matters, duplicates ok, access by index        Set: unique elements, fast contains")
    note("Map: look a value up by key (counting, grouping, cache)    Array: fixed size, fastest, primitives")
    note("ArrayDeque: stack and queue    PriorityQueue: always hands out the smallest (or largest) first")
}
