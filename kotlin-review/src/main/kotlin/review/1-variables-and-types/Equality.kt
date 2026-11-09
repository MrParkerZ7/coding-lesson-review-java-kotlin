package review.variables

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** A data class: equals, hashCode, toString and copy come from the primary constructor. */
data class DataPoint(val x: Int, val y: Int)

/** A plain class: equals() is the default one from Any, which is identity. */
class PlainPoint(val x: Int, val y: Int)

/** Implements Comparable, so it gets <, >, <= and >= and can be used in a range. Compares major first, then minor. */
data class Version(val major: Int, val minor: Int) : Comparable<Version> {
    override fun compareTo(other: Version): Int = compareValuesBy(this, other, { it.major }, { it.minor })
}

/** A String? that is really null at run time, so the compiler cannot fold the comparisons below. */
fun nameOrNull(known: Boolean): String? = if (known) "Ann" else null

/**
 * 1b. EQUALITY: == (equals) vs === (identity), NaN, Comparable
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.EqualityKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("EQUALITY  -  == compares content, === compares the object")

    section("== (equals) vs === (identity)")
    syntax(
        "a == b | a != b" to "structural: a.equals(b), null-safe (no NullPointerException)",
        "a === b | a !== b" to "referential: the very same object in memory",
    )
    val lit = "kotlin"
    val built = String(charArrayOf('k', 'o', 't', 'l', 'i', 'n'))
    show { lit == built }                                     // same characters: equal
    show { lit === built }                                    // but two different String objects
    show { lit === "kotlin" }                                 // two identical literals are ONE shared object
    show { built.intern() === lit }                           // intern() returns the shared literal
    note("Java's == is Kotlin's ===, and Java's .equals() is Kotlin's == (the String case is in lesson 0)")

    section("The boxed Integer trap")
    val small1: Any = 127
    val small2: Any = 127
    show { small1 === small2 }                                // true: the JVM caches boxed Integers from -128 to 127
    val big1: Any = 1000
    val big2: Any = 1000
    show { big1 === big2 }                                    // TRAP: false outside the cache, each boxing makes a new object
    show { big1 == big2 }                                     // true: == compares the values
    note("The compiler warns about === on Int and Int?: for numbers always use ==")

    section("== is null-safe")
    show { nameOrNull(false) == null }                        // a null String? is equal to null
    show { nameOrNull(false) == "Ann" }                       // false, no NullPointerException (Java's null.equals(...) would throw)
    show { nameOrNull(true) == "Ann" }
    show { nameOrNull(false)?.equals("Ann") }                 // TRAP: ?. gives null, a third answer that is neither true nor false

    section("Data class vs plain class")
    syntax("data class P(val x: Int)" to "equals / hashCode / toString / copy are generated from the primary constructor")
    show { DataPoint(1, 2) == DataPoint(1, 2) }               // true: compares the properties
    show { DataPoint(1, 2) === DataPoint(1, 2) }              // false: still two objects
    show { PlainPoint(1, 2) == PlainPoint(1, 2) }             // false: the default equals() is identity
    val p = PlainPoint(1, 2)
    val alias = p
    show { p == alias }                                       // true: the same object
    val dataSet = setOf(DataPoint(1, 2), DataPoint(1, 2))
    show { dataSet.size }                                     // 1: a data class works as a Set element or a Map key
    val plainSet = setOf(PlainPoint(1, 2), PlainPoint(1, 2))
    show { plainSet.size }                                    // 2: two different objects
    note("A plain class needs its own equals AND hashCode to work in a HashSet or HashMap (lesson 11)")

    section("Collections and arrays")
    show { listOf(1, 2) == mutableListOf(1, 2) }              // true: List equality compares the elements, not the class
    show { setOf(1, 2) == setOf(2, 1) }                       // true: a Set ignores order
    val xs = intArrayOf(1, 2)
    val ys = intArrayOf(1, 2)
    show { xs == ys }                                         // TRAP: false, arrays compare by identity (the compiler warns)
    show { xs.contentEquals(ys) }                             // compare the contents of arrays like this

    section("Numbers of different types are never equal")
    val one = 1
    val oneLong = 1L
    wontCompile("one == oneLong", "Operator == cannot be applied to Int and Long")
    wontCompile("\"a\" == 1", "== between unrelated types is rejected (Java would allow it)")
    show { one.toLong() == oneLong }                          // convert first
    show { one.equals(oneLong) }                              // TRAP: false, equals(Any?) boxes an Integer and a Long

    section("Double: NaN and -0.0")
    show { Double.NaN == Double.NaN }                         // false: IEEE says NaN is never equal to anything
    show { Double.NaN.equals(Double.NaN) }                    // true: equals() treats NaN as equal to itself
    show { 0.0 == -0.0 }                                      // true: IEEE comparison
    show { 0.0.equals(-0.0) }                                 // false: equals() tells them apart
    show { listOf(Double.NaN) == listOf(Double.NaN) }         // true: collections use equals(), so NaN is found in a list
    val nanA: Any = Double.NaN
    val nanB: Any = Double.NaN
    show { nanA == nanB }                                     // true: once the static type is Any, equals() is used

    section("compareTo")
    syntax("a.compareTo(b)" to "negative | 0 | positive; a < b means a.compareTo(b) < 0")
    show { 3.compareTo(7) }
    show { "apple".compareTo("cherry") }                      // -2: the difference of the first different chars; trust only the sign
    show { "a".compareTo("A") }                               // 32: lowercase sorts AFTER uppercase
    show { "a".compareTo("A", ignoreCase = true) }            // 0
    val fruit = listOf("banana", "Cherry", "apple")
    show { fruit.sorted() }                                   // TRAP: uppercase letters sort before lowercase ones
    show { fruit.sortedBy { it.lowercase() } }                // case-insensitive order
    show { Double.NaN.compareTo(1.0) }                        // 1: compareTo puts NaN above every number
    show { 0.0.compareTo(-0.0) }                              // 1: and 0.0 above -0.0, though 0.0 == -0.0 is true

    section("Comparable and ranges")
    syntax("a..b" to "a range of any Comparable: x in a..b means a <= x && x <= b")
    show { 5 in 1..10 }
    show { 'q' in 'a'..'z' }
    show { 2.5 in 1.0..3.0 }
    show { "bb" in "a".."c" }                                 // Strings are Comparable: "bb" sorts between "a" and "c"
    show { "d" in "a".."c" }
    show { Version(1, 5) < Version(1, 10) }                   // true: major equal, then minor 5 < 10
    show { Version(1, 5) in Version(1, 0)..Version(2, 0) }
    show { maxOf(Version(1, 5), Version(1, 10)) }
    show { 7.coerceIn(1, 5) }                                 // clamp into a range: 5
    note("A range with start > end is empty, so x in 5..1 is always false (lesson 2)")
}
