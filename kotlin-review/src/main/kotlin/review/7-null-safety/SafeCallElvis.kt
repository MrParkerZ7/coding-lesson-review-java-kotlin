package review.nullsafety

import review.note
import review.section
import review.show
import review.syntax
import review.title

private val ages = mapOf("ann" to 31, "bob" to 25)

/** An age from the table: null when the name is unknown. */
private fun ageOf(name: String): Int? = ages[name]

/** A nickname, or null when there is none. */
private fun nickOf(name: String): String? = if (name == "robert") "bob" else null

/** A counter, or null when switched off. */
private fun counterIf(on: Boolean): Counter? = if (on) Counter() else null

/** A list of names, or null when there is no list. */
private fun namesIf(on: Boolean): List<String>? = if (on) listOf("Ann", "Bob") else null

/** Either text or a number, typed as Any. */
private fun mystery(kind: Int): Any = if (kind == 0) "text" else 42

/** The exception class a block throws (its simple name), or null when it throws nothing. */
private fun errorName(block: () -> Any?): String? = runCatching { block() }.exceptionOrNull()?.javaClass?.simpleName

class Address(val city: String?)
class Person(val name: String, val address: Address?)
class Counter { var count: Int = 0 }

/** Elvis + return: after the guard line, age is a plain Int. */
fun describeAge(name: String): String {
    val age = ageOf(name) ?: return "no age for $name"
    return "$name is $age"
}

/** Elvis + throw: a real Int or an exception, never null. */
fun mustAgeOf(name: String): Int = ageOf(name) ?: throw IllegalArgumentException("unknown name: $name")

/**
 * 7b. SAFE CALL ?. AND ELVIS ?:
 *     Run: mvn -q compile exec:java -Dmain.class=review.nullsafety.SafeCallElvisKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("SAFE CALL ?. AND ELVIS ?:  -  stop at null, or replace it")

    val ann = Person("Ann", Address("Oslo"))
    val bob = Person("Bob", null)
    val cy = Person("Cy", Address(null))

    section("Safe call ?.")
    syntax(
        "a?.b" to "a.b when a is not null, otherwise null (the result type gets a ?)",
        "a?.b?.c" to "a chain: the first null ends it, the rest is skipped",
        "a?.b = value" to "an assignment that is skipped when a is null",
    )
    show { ann.address?.city }                                                 // every link is non-null, so the chain gives the city
    show { bob.address?.city }                                                 // null: address is null, .city never runs
    show { cy.address?.city?.length }                                          // null: the second ?. stops at city == null
    show { ann.address?.city?.length }                                         // length of "Oslo"
    val counter = counterIf(true)
    val noCounter = counterIf(false)
    counter?.count = 5
    noCounter?.count = 5
    show { counter?.count }                                                    // 5: the assignment ran
    show { noCounter?.count }                                                  // null: the assignment was skipped, no error
    val names = namesIf(false)
    show { names?.size }                                                       // null, not 0: a missing list is not an empty list

    section("Elvis ?: (a default, a return or a throw)")
    syntax(
        "a ?: default" to "a when a is not null, otherwise default",
        "a ?: return value" to "a guard: leave the function when a is null",
        "a ?: throw SomeException()" to "or fail loudly, so the result is never null",
    )
    show { ageOf("zed") ?: -1 }                                                // left side is null, so the default is used
    show { ageOf("ann") ?: -1 }                                                // 31: the left side wins when it is not null
    show { bob.address?.city ?: "unknown" }                                    // chain + default: the everyday combination
    show { names?.size ?: 0 }                                                  // now a missing list counts as 0
    show { describeAge("ann") }
    show { describeAge("zed") }                                                // returned early through ?: return
    show { errorName { mustAgeOf("zed") } }                                    // ?: throw

    section("let, also and as?")
    syntax(
        "a?.let { it... }" to "run the block only when a is not null; the block value is the result",
        "a?.also { it... }" to "run a side effect; the result stays a itself",
        "x as? Type" to "safe cast: the Type, or null instead of a ClassCastException",
    )
    show { ageOf("ann")?.let { it + 1 } }                                      // 32: the block ran with it = 31
    show { ageOf("zed")?.let { it + 1 } }                                      // null: the block is skipped
    val seen = mutableListOf<String>()
    show { ageOf("ann")?.also { seen += "saw $it" } }                          // also returns the receiver (31), not the block value
    show { seen }                                                              // but the side effect ran
    show { mystery(0) as? String }
    show { mystery(1) as? String }                                             // null: 42 is not a String
    show { errorName { mystery(1) as String } }                                // plain as crashes instead

    section("Traps")
    show { ageOf("ann") ?: 0 + 1 }                                             // 31, not 32: + binds tighter, so it is x ?: (0 + 1)
    show { (ageOf("ann") ?: 0) + 1 }                                           // 32: parentheses give the intended meaning
    show { ageOf("ann")?.let { nickOf("ann") } ?: "none" }                      // TRAP: Ann exists, but let returned null, so ?: ran
    show { ageOf("ann")?.let { "age $it" } ?: "none" }                         // fine while the block returns non-null
    note("x?.let { A } ?: B is not if/else: B also runs when A itself is null. Use if (x != null) A else B")
    note("Interview answer: ?. for optional access, ?: for a default or an early exit, !! almost never")
}
