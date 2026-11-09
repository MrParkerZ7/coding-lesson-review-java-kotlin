package review.json

import kotlinx.serialization.Serializable
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** Sample data shared by the files of this lesson. `@Serializable` is only read by kotlinx.serialization (LibraryJson); the rest ignore it. */
@Serializable
data class Address(val city: String, val zip: String? = null)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val active: Boolean,
    val score: Double,
    val tags: List<String>,
    val address: Address?,
)

fun sampleUser() = User(1, "Ann", true, 9.5, listOf("kotlin", "jvm"), Address("Bangkok", null))

/**
 * 21a. toString() IS NOT JSON
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.ToStringIsNotJsonKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("TO JSON  -  println(obj) is not JSON")

    section("What println / toString gives you")
    val user = sampleUser()
    show { user }                                                              // data class: Kotlin syntax, User(id=1, ...), NOT JSON
    show { mapOf("a" to 1, "b" to "x") }                                       // Map: {a=1, b=x}  keys and strings have no quotes
    show { listOf("a", "b") }                                                  // List: [a, b]  the strings lost their quotes
    show { user.address }                                                      // nested objects print the same way
    show { user.toString().contains('"') }                                     // no double quote anywhere: invalid JSON

    section("What JSON needs")
    syntax(
        "{ \"key\": value, ... }" to "object: every key is a double-quoted string, then a colon",
        "[ value, value ]" to "array: commas BETWEEN items, never after the last",
        "\"text\"" to "string: double quotes only; escape the quote, the backslash and newlines",
        "123 | 1.5 | true | false | null" to "number, boolean, null: lowercase; no NaN, no Infinity",
    )
    note("Interview trap: a data class prints User(id=1, name=Ann): that is Kotlin, not JSON. Say it before you print.")

    section("Three ways to get real JSON (the next files)")
    syntax(
        "HandWrittenJson" to "no library: escape + a recursive toJson(value). Works on any judge, no imports",
        "ReflectionJson" to "no library: read the fields with Java reflection, then reuse toJson",
        "LibraryJson" to "Jackson | Gson | kotlinx.serialization: one call, but the library must be available",
    )
    note("On HackerRank-style judges assume NO library: learn the hand-written one first")
}
