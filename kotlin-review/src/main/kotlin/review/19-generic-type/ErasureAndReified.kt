package review.generics

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** reified: an inline function can see T at run time. */
inline fun <reified T> isInstance(x: Any?): Boolean = x is T

/** The same trick the standard library uses for filterIsInstance. */
inline fun <reified T> List<Any?>.onlyOfType(): List<T> = filter { it is T }.map { it as T }

/** T::class works only with reified. */
inline fun <reified T> typeName(): String? = T::class.simpleName

/**
 * 19c. ERASURE, STAR PROJECTION, REIFIED
 *      Run: mvn -q compile exec:java -Dmain.class=review.generics.ErasureAndReifiedKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("ERASURE, STAR PROJECTION, REIFIED")

    section("Type erasure: generics are checked at compile time and gone at run time")
    syntax("x is List<*>" to "the only List check allowed at run time: element type unknown")
    val things: Any = listOf("a", "b")
    val ints = listOf(1, 2)
    val strs = listOf("a", "b")
    show { things is List<*> }                                // star projection: a List of some unknown element type
    show { ints::class == strs::class }                       // List<Int> and List<String> are the same class at run time
    wontCompile("things is List<String>", "cannot check for an erased type")

    section("Star projection: List<*> and MutableList<*>")
    syntax("List<*>" to "a List of SOME type: you may read (as Any?), not write")
    val star: List<*> = listOf(1, "two", 3.0)
    show { star[0] }                                          // reads come out as Any?: you cannot know the real type
    show { star.size }
    wontCompile("(star as MutableList<*>).add(1)", "writing into an unknown type is unsafe")

    section("reified: inline + reified keeps T at run time")
    syntax("inline fun <reified T> name()" to "inline + reified: T can be used in `is T` and `T::class`")
    val mixed: List<Any?> = listOf(1, "two", 3.0, "four", 5)
    show { isInstance<String>("hi") }
    show { isInstance<Int>("hi") }
    show { typeName<Double>() }                               // T::class works only with reified
    show { mixed.onlyOfType<String>() }                       // keeps only the Strings
    show { mixed.filterIsInstance<Int>() }                    // the standard library does the same with reified

    section("What a type parameter cannot do")
    wontCompile("fun <T> f(x: Any) = x is T", "T is erased: needs inline + reified")
    wontCompile("fun <T> f() = T::class", "T is erased: needs inline + reified")
    wontCompile("fun <T> f() = T()", "a type parameter has no known constructor")
    note("reified needs inline, so it cannot be used on a normal (non-inline) function")
}
