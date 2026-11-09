package review.extensions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** A read-only extension property: no backing field, so it needs a getter and cannot have an initializer. */
val String.firstWord: String get() = trim().substringBefore(' ')

/** Generic extension property: the second to last element. */
val <T> List<T>.penultimate: T get() = this[size - 2]

/** A `var` extension property needs BOTH a getter and a setter. */
var StringBuilder.lastChar: Char
    get() = this[length - 1]
    set(value) { setCharAt(length - 1, value) }

/** An empty companion is enough to hang extensions on it. */
class Temperature(val degrees: Int) {
    companion object
}

fun Temperature.Companion.freezing() = Temperature(0)

/** A member extension: declared inside a class, usable only where that class is in scope. */
class Formatter(private val prefix: String) {
    fun String.tagged() = "$prefix$this"                      // `this` = the String; `this@Formatter` = the Formatter
    fun tag(text: String) = text.tagged()
}

/**
 * 14b. EXTENSION PROPERTIES, COMPANION EXTENSIONS, MEMBER EXTENSIONS, SCOPE
 *      Run: mvn -q compile exec:java -Dmain.class=review.extensions.ExtensionPropertiesKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("EXTENSION PROPERTIES  -  and where an extension is visible")

    section("Extension properties: a getter, never a field")
    syntax(
        "val Type.name: R get() = ..." to "read-only extension property",
        "var Type.name: R { get() = ...; set(v) { ... } }" to "needs getter AND setter",
        "val <T> List<T>.name: T get() = ..." to "generic receiver",
    )
    show { "hello big world".firstWord }                            // computed on every read
    show { listOf(1, 2, 3, 4).penultimate }
    val sb = StringBuilder("cat")
    sb.lastChar = 'r';  show { sb }                                 // the setter replaced the last char
    show { sb.lastChar }                                            // and the getter reads it back
    wontCompile("val String.len: Int = 5", "an extension property has no backing field, so no initializer")
    note("The getter runs on every access: there is nothing stored, so it is a function in property clothes")

    section("The standard library has some: lastIndex and indices")
    show { listOf(5, 6, 7).lastIndex }                              // size - 1
    show { listOf(5, 6, 7).indices }                                // 0..lastIndex, an IntRange
    show { "kotlin".lastIndex }                                     // works on String too
    show { emptyList<Int>().lastIndex }                             // trap: -1 for an empty collection
    show { emptyList<Int>().indices.isEmpty() }                     // 0..-1 is an empty range: a loop over indices never runs

    section("Extension on a companion object")
    syntax("fun Type.Companion.name()" to "needs a declared companion object, even an empty one")
    show { Temperature.freezing().degrees }                         // looks like a static factory
    show { String.format("%03d", 7) }                               // the standard library does this: format is String.Companion.format
    show { Int.MAX_VALUE }                                          // a constant in Int.Companion

    section("Member extension: declared inside a class")
    val fmt = Formatter(">> ")
    show { fmt.tag("hi") }                                          // the class can use its own extension
    show { with(fmt) { "hi".tagged() } }                            // from outside: bring the class into scope with with/run
    wontCompile("\"hi\".tagged()", "a member extension is visible only inside its class (or with(fmt) { })")

    section("Scope: where is an extension visible?")
    fun Int.twice() = this * 2                                      // a local extension: visible only inside main()
    show { 21.twice() }
    note("Top level in the same package: usable at once. In another package: import it (import pkg.name).")
    note("A private top-level extension is visible in its own file only.")
    note("Standard-library extensions (map, filter, let, joinToString ...) are imported automatically.")
    note("Two imports with the same name clash: rename with import pkg.name as other")
}
