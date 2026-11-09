package review.extensions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** Extension on a non-null receiver: inside, `this` is the String. */
fun String.shout(): String = this.uppercase() + "!"

/** Extension on a NULLABLE receiver: it can be called on null, so the body must handle null. */
fun String?.orDash(): String = this ?: "-"

/** Generic extension: works for a List of anything. Throws on a list with fewer than two elements. */
fun <T> List<T>.second(): T = this[1]

/** infix: call it without dot and parentheses, `2 pow 10`. */
infix fun Int.pow(exponent: Int): Int {
    var result = 1
    repeat(exponent) { result *= this }
    return result
}

/** A small value type to show operator extensions on. */
data class Vec(val x: Int, val y: Int)

operator fun Vec.plus(other: Vec): Vec = Vec(x + other.x, y + other.y)
operator fun Vec.times(k: Int): Vec = Vec(x * k, y * k)

/** Animal.sound is a MEMBER (virtual); kind() below is an EXTENSION (static). */
open class Animal {
    open fun sound() = "..."
}

class Dog : Animal() {
    override fun sound() = "Woof"
}

fun Animal.kind() = "animal"
fun Dog.kind() = "dog"

class Greeter {
    fun hello() = "member hello"
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")           // the compiler warns about this on purpose: that is the lesson
fun Greeter.hello() = "extension hello"

fun Greeter.hello(name: String) = "extension hello $name"

class Secret(private val code: Int) {
    fun masked() = "*".repeat(code.toString().length)
}

/**
 * 14a. EXTENSION FUNCTIONS
 *      Run: mvn -q compile exec:java -Dmain.class=review.extensions.ExtensionFunctionsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("EXTENSION FUNCTIONS  -  add a function to a type you do not own")

    section("Declaring and calling")
    syntax(
        "fun Type.name(params): R { ... this ... }" to "extension function; `this` is the receiver object",
        "fun Type?.name()" to "nullable receiver: callable on null",
        "fun <T> List<T>.name()" to "generic extension",
        "infix fun Int.name(other: Int)" to "call as `a name b`",
        "operator fun Vec.plus(o: Vec)" to "makes `a + b` work (more in lesson 20)",
    )
    show { "kotlin".shout() }                                       // looks like a member, is not one
    show { listOf("a", "b").map(String::shout) }                    // an extension has a function reference too
    val none: String? = null
    show { none.orDash() }                                          // nullable receiver: no NPE, the body decides
    show { "x".orDash() }
    show { none.toString() }                                        // Any?.toString() is itself an extension: null prints "null"
    show { listOf(1, 2, 3).second() }                               // generic: T is Int here
    show { listOf("a", "b").second() }                              // and String here
    show { runCatching { emptyList<Int>().second() }.exceptionOrNull()?.javaClass?.simpleName }   // the extension does not guard the index
    show { 2 pow 10 }                                               // infix call
    show { Vec(1, 2) + Vec(3, 4) }                                  // operator extension
    show { Vec(1, 2) * 3 }

    section("Resolved STATICALLY: the declared type decides, not the object")
    val pet: Animal = Dog()
    show { pet.sound() }                                            // member: virtual, the run-time type (Dog) decides
    show { pet.kind() }                                             // extension: the declared type (Animal) decides
    show { Dog().kind() }                                           // declared as Dog: the Dog extension is chosen
    show { (pet as Dog).kind() }                                    // a cast changes the declared type
    note("An extension is a static function in disguise: it is NOT virtual and cannot be overridden")

    section("A member always wins over an extension of the same signature")
    show { Greeter().hello() }                                      // the member is called; the extension is shadowed (compiler warning)
    show { Greeter().hello("Ann") }                                 // a different signature: the extension is used
    note("The compiler warns \"This extension is shadowed by a member\": the extension can never be called by that name")

    section("What an extension cannot do")
    wontCompile("fun Secret.reveal() = code", "an extension sees only the public API, not private members")
    show { Secret(1234).masked() }                                  // only members reach the private code
    note("No new fields either: an extension adds behavior, not state (see the next file for properties)")
    note("Why Kotlin has them: add helpers to String, List, Int without inheriting or editing the class")
}
