package review.generics

import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** A generic class: the user of the class fills in T. */
class Box<T>(val value: T) {
    fun <R> map(transform: (T) -> R): Box<R> = Box(transform(value))
    override fun toString() = "Box($value)"
}

/** A generic function: T is inferred from the arguments. */
fun <T> firstOrDefault(list: List<T>, default: T): T = if (list.isEmpty()) default else list[0]

/** Upper bound: T must be Comparable<T>, so compareTo (and >=) can be used. */
fun <T : Comparable<T>> maxOfTwo(a: T, b: T): T = if (a >= b) a else b

/** Several bounds need a where clause: T is both a CharSequence (has length) and Comparable. */
fun <T> longerOrLarger(a: T, b: T): T where T : CharSequence, T : Comparable<T> =
    if (a.length != b.length) (if (a.length > b.length) a else b) else maxOf(a, b)

/** A plain T has the upper bound Any?, so null is allowed. */
fun <T> describe(x: T): String = x.toString()

/** T : Any forbids null. */
fun <T : Any> describeNonNull(x: T): String = x.toString()

/**
 * 19a. GENERIC BASICS: generic class, generic function, bounds, where, nullability of T
 *      Run: mvn -q compile exec:java -Dmain.class=review.generics.GenericBasicsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("GENERIC TYPES  -  classes, functions, bounds")

    section("Generic class: Box<T>")
    syntax(
        "class Box<T>(val value: T)" to "T is a placeholder type, filled in by the user",
        "Box<Int>(42) | Box(42)" to "type argument written, or inferred",
    )
    val intBox = Box(42)
    val strBox = Box("kotlin")
    show { intBox }                                           // T is inferred as Int; the run-time type is just Box (T is erased)
    show { strBox.value.length }                              // T is String here, so String methods work
    show { intBox.map { it * 2 } }                            // Box<Int> -> Box<Int>
    show { intBox.map { "n=$it" } }                           // Box<Int> -> Box<String>: map changes T

    section("Generic function: fun <T> ...")
    syntax(
        "fun <T> name(x: T): T" to "T is declared in angle brackets BEFORE the function name",
        "name<Type>(x)" to "explicit type argument (usually inferred)",
    )
    show { firstOrDefault(listOf(1, 2), 0) }                  // T = Int
    show { firstOrDefault(emptyList(), "none") }              // T = String, inferred from the default
    show { firstOrDefault<String>(emptyList(), "x") }         // explicit type argument

    section("Upper bound: T : Comparable<T>")
    syntax("fun <T : Bound> name(x: T)" to "T must be a Bound (or a subtype of it)")
    show { maxOfTwo(3, 9) }
    show { maxOfTwo("apple", "pear") }                        // Strings are Comparable: alphabetical
    wontCompile("maxOfTwo(Box(1), Box(2))", "Box is not Comparable")

    section("Several bounds: where T : A, T : B")
    syntax("fun <T> name(x: T): T where T : A, T : B" to "T must satisfy every bound")
    show { longerOrLarger("kiwi", "fig") }                   // longer wins
    show { longerOrLarger("fig", "yam") }                     // same length: the larger one wins

    section("Is null allowed?")
    syntax(
        "fun <T> f(x: T)" to "T means T : Any?, so null is allowed",
        "fun <T : Any> f(x: T)" to "T : Any forbids null",
    )
    show { describe(null) }                                   // a plain T means T : Any?, so null is fine
    show { describeNonNull(5) }
    wontCompile("describeNonNull(null)", "T : Any forbids null")
}
