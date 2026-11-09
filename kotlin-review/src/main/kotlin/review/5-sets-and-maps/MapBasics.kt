package review.sets

import review.bigO
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 5b. MAPS: key -> value
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.MapBasicsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("MAPS  -  look a value up by key")

    section("Creating")
    syntax(
        "mapOf(k1 to v1, k2 to v2)" to "read-only Map; `to` makes a Pair; keeps insertion order",
        "mutableMapOf(...) | linkedMapOf(...) | sortedMapOf(...)" to "mutable | insertion order | sorted by key",
    )
    val ages = mapOf("ann" to 30, "bob" to 25);   show { ages }
    show { sortedMapOf("b" to 1, "a" to 2) }                                   // a TreeMap: sorted by key
    show { ages.size }
    show { ages.keys }
    show { ages.values }

    section("Reading")
    syntax(
        "m[key] | m.getValue(key)" to "value or null | value or an exception",
        "m.getOrDefault(key, d) | m[key] ?: d" to "value or a default",
        "key in m | m.containsKey(key)" to "is there such a key?",
    )
    show { ages["ann"] }
    show { ages["zed"] }                                                       // null when the key is missing
    show { ages.getOrDefault("zed", 0) }
    show { ages["zed"] ?: -1 }                                                 // the Kotlin way: elvis
    show { "bob" in ages }
    show { runCatching { ages.getValue("zed") }.exceptionOrNull()?.javaClass?.simpleName }   // getValue throws on a missing key
    wontCompile("val n: Int = ages[\"ann\"]", "m[key] is Int? (null when missing): use ?: or getValue")

    section("Changing (MutableMap only)")
    syntax(
        "m[key] = v | m += key to v | m.put(key, v)" to "add or overwrite",
        "m.remove(key) | m.getOrPut(key) { default }" to "remove, returns the old value | read, or compute and store",
    )
    val m = mutableMapOf("a" to 1)
    m["b"] = 2;                            show { m }
    m["a"] = 10;                           show { m }                          // the same key overwrites
    m += "c" to 3;                         show { m }
    show { m.remove("b") }                                                     // returns the removed value
    show { m.getOrPut("d") { 4 } }                                             // missing: computes 4 AND stores it
    show { m }
    wontCompile("val r = mapOf(\"a\" to 1); r[\"b\"] = 2", "a read-only Map has no set operator")

    section("Looping")
    syntax("for ((key, value) in map)" to "destructure each entry")
    for ((k, v) in ages) show { "$k=$v" }
    show { ages.entries.joinToString { "${it.key}:${it.value}" } }

    section("Transforming")
    show { ages.filter { (_, v) -> v > 26 } }                                  // keeps matching entries
    show { ages.mapValues { it.value * 10 } }
    show { ages.maxByOrNull { it.value }?.key }                                // the key with the biggest value
    show { ages.entries.sortedBy { it.value }.map { it.key } }                 // keys ordered by their values
    show { ages.toList() }                                                     // list of Pairs

    section("Cost")
    bigO(
        "HashMap get | put | containsKey" to "O(1) on average",
        "TreeMap (sortedMapOf) get | put" to "O(log n), sorted by key",
        "containsValue | value search" to "O(n)",
    )
}
