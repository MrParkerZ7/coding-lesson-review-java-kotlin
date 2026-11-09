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

/** A nickname, or null when there is none. */
private fun nicknameOf(name: String): String? = if (name == "robert") "bob" else null

/** Scores of a student, or null when there is no record at all. */
private fun scoresOf(known: Boolean): List<Int>? = if (known) listOf(90, 80) else null

/** The exception class a block throws (its simple name), or null when it throws nothing. */
private fun errorName(block: () -> Any?): String? = runCatching { block() }.exceptionOrNull()?.javaClass?.simpleName

/**
 * 7a. NULLABLE TYPES: Int vs Int?, !!, nullable collections
 *     Run: mvn -q compile exec:java -Dmain.class=review.nullsafety.NullableTypesKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("NULLABLE TYPES  -  the type says whether null is allowed")

    section("T versus T?")
    syntax(
        "val name: Type = value" to "never null: the compiler enforces it",
        "val name: Type? = value" to "may be null: the ? is part of the type",
    )
    val whole: Int = 5;  show { whole }                                        // Int can never be null
    val unknown: Int? = ageOf("zed");  show { unknown }                        // null: no age on file (the type column says null)
    val known: Int? = ageOf("ann");  show { known }                            // an Int? holding a value is a boxed Int at run time
    wontCompile("val n: Int = null", "null cannot be a value of the non-null type Int")
    wontCompile("val total = whole + unknown", "unknown is Int?, but + wants an Int")
    wontCompile("nick.length   (nick: String?)", "only ?. or !! may reach a member of a nullable value")

    section("Checking for null")
    syntax(
        "x == null | x != null" to "the plain check, works for every type",
        "if (x != null) { x... }" to "inside the branch x counts as the non-null type",
    )
    show { unknown == null }
    show { known != null }
    show { known == 31 }                                                       // == is null-safe: Int? compared with Int is fine
    show { unknown == 31 }                                                     // false, no exception: == never throws on null

    section("!! : trust me, it is not null")
    syntax("x!!" to "x as the non-null type, or NullPointerException when x is null")
    show { known!! + 1 }                                                       // fine: known holds 31
    show { errorName { unknown!! } }                                           // the crash an interviewer wants you to name
    note("!! is a crash waiting to happen: prefer ?., ?: or an early return (next file)")

    section("List<Int?> versus List<Int>?")
    syntax(
        "List<Int?>" to "the list always exists, each ELEMENT may be null",
        "List<Int>?" to "the list itself may be null, its elements never",
    )
    val holes: List<Int?> = listOf(1, null, 3);  show { holes }                // elements may be null
    val noRecord: List<Int>? = scoresOf(false);  show { noRecord }             // the list itself is null
    show { holes.filterNotNull() }                                             // List<Int>: nulls dropped, element type no longer nullable
    show { holes.sumOf { it ?: 0 } }                                           // treat null as 0
    show { holes.count { it == null } }
    wontCompile("holes.sum()", "sum() exists for Iterable<Int>, not for Iterable<Int?>")
    show { holes.filterNotNull().sum() }                                       // so drop the nulls first
    show { noRecord.orEmpty() }                                                // List<Int>? becomes a real, empty List<Int>

    section("Functions that accept a null receiver")
    syntax(
        "String?.isNullOrEmpty()" to "true for null or empty text",
        "String?.isNullOrBlank()" to "true for null, empty or only spaces",
        "String?.orEmpty()" to "null becomes an empty String",
    )
    val nick: String? = nicknameOf("ann");  show { nick }                      // null: Ann has no nickname
    show { nick.isNullOrEmpty() }
    show { "   ".isNullOrBlank() }                                             // blank = only whitespace
    show { nick.orEmpty().length }                                             // safe: orEmpty() turned String? into String

    section("null in text, Nothing?, ?: versus if")
    show { "age = $unknown" }                                                  // TRAP: null prints as the text null, no exception
    show { unknown.toString() }                                                // toString() works on a null receiver
    val nothing: Nothing? = null;  show { nothing }                            // Nothing? has exactly one value: null (the type of the literal null)
    show { if (unknown != null) unknown else 0 }                               // the long form
    show { unknown ?: 0 }                                                      // ?: takes the right side only when the left is null
    note("Next: SafeCallElvis (?. and ?:), then SmartCastsAndParsing")
}
