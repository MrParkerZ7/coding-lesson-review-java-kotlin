package review.nullsafety

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

private val ages = mapOf("ann" to 31, "bob" to 25)

/** An age from the table: null when the name is unknown. */
private fun ageOf(name: String): Int? = ages[name]

/** Some text, or null when switched off. */
private fun maybeText(on: Boolean): String? = if (on) "kotlin" else null

/** The exception class a block throws (its simple name), or null when it throws nothing. */
private fun errorName(block: () -> Any?): String? = runCatching { block() }.exceptionOrNull()?.javaClass?.simpleName

class Holder { var name: String? = null }
class Fixed(val name: String?)

/** Smart cast by is: inside the branch x is a String. */
fun lengthOrMinus(x: Any): Int = if (x is String) x.length else -1

/** Smart cast in when: each branch sees its own type. */
fun measure(x: Any): Int = when (x) {
    is String -> x.length
    is List<*> -> x.size
    else -> -1
}

/** Guard with !is: after the early return, x is a String. */
fun shout(x: Any): String {
    if (x !is String) return "not text"
    return x.uppercase()
}

/** requireNotNull also smart casts: after it, a is an Int. */
fun nextAge(name: String): Int {
    val a = ageOf(name)
    requireNotNull(a) { "unknown name: $name" }
    return a + 1
}

/**
 * 7c. SMART CASTS, PARSING TO NULL, THE OrNull FAMILY
 *     Run: mvn -q compile exec:java -Dmain.class=review.nullsafety.SmartCastsAndParsingKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("SMART CASTS AND PARSING  -  let the compiler narrow the type")

    section("Smart casts")
    syntax(
        "if (x != null) { x... }" to "x becomes the non-null type inside the branch",
        "if (x is Type) { x... }" to "x becomes Type inside the branch",
        "if (x !is Type) return" to "after the guard, x is Type for the rest of the function",
        "x != null && x.member" to "the right side of && sees the narrowed type",
    )
    val word = maybeText(true)
    show { if (word != null) word.length else -1 }                             // 6: word is a String inside the branch, no ?. needed
    show { word != null && word.length > 3 }
    show { lengthOrMinus("hello") }
    show { lengthOrMinus(42) }
    show { measure(listOf(1, 2, 3)) }
    show { shout("hi") }
    show { shout(5) }                                                          // the guard returned early

    section("Where a smart cast does NOT work")
    val h = Holder()
    h.name = "Ann"
    wontCompile("if (h.name != null) h.name.length", "h.name is a mutable property: it could change after the check")
    show { h.name?.length }                                               // 3: ?. is the simple fix
    val copy = h.name
    show { if (copy != null) copy.length else 0 }                              // 3: or copy it into a local val first
    val f = Fixed("Bo")
    show { if (f.name != null) f.name.length else 0 }                  // 2: a plain val property in the same module is stable
    var current: String? = maybeText(true)
    val size = if (current != null) current.length else 0
    show { size }                                                              // 6: a local var is smart-cast in plain code (nothing can change it in between)
    current = null
    wontCompile("{ if (current != null) current.length }", "inside a LAMBDA the var is not smart-cast when it is assigned after the lambda (current = null)")
    show { current }
    note("Rule: smart casts need a value that cannot change: val, a local var in plain code, or a val property without a custom getter")

    section("Parsing to null instead of throwing")
    syntax(
        "s.toIntOrNull()" to "an Int, or null when s is not a valid Int",
        "s.toLongOrNull() | s.toDoubleOrNull()" to "the same idea for the other numbers",
        "s.toBooleanStrictOrNull()" to "true or false exactly (lower case), else null",
        "s.toInt()" to "same parse, but throws NumberFormatException",
    )
    show { "42".toIntOrNull() }
    show { "4x2".toIntOrNull() }                                               // null: not a number
    show { " 42".toIntOrNull() }                                               // null: no trimming, a space makes it invalid
    show { "+5".toIntOrNull() }                                                // a leading plus is accepted
    show { "2147483648".toIntOrNull() }                                        // null: one past Int.MAX_VALUE
    show { "2147483648".toLongOrNull() }                                       // fits in a Long
    show { "3.5".toDoubleOrNull() }
    show { "true".toBooleanStrictOrNull() }
    show { "True".toBooleanStrictOrNull() }                                    // null: strict means exactly true or false, lower case
    show { "yes".toBoolean() }                                                 // TRAP: toBoolean() never fails, anything but true is false
    show { errorName { "abc".toInt() } }
    show { "abc".toIntOrNull() ?: 0 }                                          // the idiom: parse with a default

    section("The OrNull family and map lookups")
    syntax(
        "list.getOrNull(i) | firstOrNull() | lastOrNull()" to "an element, or null instead of an exception",
        "list.maxOrNull() | minOrNull()" to "null on an empty list (max() throws)",
        "map[key]" to "V?: null for a missing key",
    )
    val nums = listOf(4, 8, 1)
    show { nums.getOrNull(10) }                                                // null; nums[10] would throw
    show { emptyList<Int>().firstOrNull() }
    show { nums.maxOrNull() }
    show { emptyList<Int>().maxOrNull() }
    show { errorName { emptyList<Int>().max() } }                              // max() on an empty list throws
    val stock = mapOf("apple" to 3)
    show { stock["pear"] }                                                     // null: a map lookup is always V?, even for Int values
    show { stock["pear"] ?: 0 }
    show { errorName { stock.getValue("pear") } }                              // getValue throws on a missing key

    section("requireNotNull and checkNotNull")
    syntax(
        "requireNotNull(x) { message }" to "x as non-null, or IllegalArgumentException (a bad argument)",
        "checkNotNull(x) { message }" to "x as non-null, or IllegalStateException (a bad state)",
    )
    show { requireNotNull(ageOf("ann")) }
    show { errorName { requireNotNull(ageOf("zed")) } }
    show { errorName { checkNotNull(ageOf("zed")) } }
    show { nextAge("ann") }                                                    // requireNotNull(a) smart-cast a to Int
    note("lateinit var (non-null later) is lesson 18; Java values arrive as platform types String! with unknown nullability: lesson 16")
}
