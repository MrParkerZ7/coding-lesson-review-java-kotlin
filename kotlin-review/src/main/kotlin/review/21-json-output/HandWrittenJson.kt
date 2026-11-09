package review.json

import review.bigO
import review.block
import review.note
import review.section
import review.show
import review.syntax
import review.title

enum class Level { LOW, HIGH }

/** Escape a string for JSON: the quote, the backslash and control characters. O(n). */
fun escapeJson(s: String): String = buildString {
    for (c in s) when (c) {
        '"' -> append("\\\"")
        '\\' -> append("\\\\")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
    }
}

/** Any value to compact JSON: null, Boolean, Number, String, Char, Enum, Map, Iterable, Array. */
fun toJson(value: Any?): String = when (value) {
    null -> "null"
    is Boolean -> value.toString()
    is Double -> if (value.isNaN() || value.isInfinite()) "null" else value.toString()   // JSON has no NaN or Infinity
    is Number -> value.toString()
    is String -> "\"" + escapeJson(value) + "\""
    is Char -> "\"" + escapeJson(value.toString()) + "\""
    is Enum<*> -> "\"" + value.name + "\""
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") { (k, v) -> "\"" + escapeJson(k.toString()) + "\":" + toJson(v) }
    is Iterable<*> -> value.joinToString(",", "[", "]") { toJson(it) }
    is Array<*> -> value.joinToString(",", "[", "]") { toJson(it) }
    is IntArray -> value.joinToString(",", "[", "]")
    else -> "\"" + escapeJson(value.toString()) + "\""                         // unknown type: its toString() as a string
}

/** Same data, indented: two spaces per level. */
fun toPrettyJson(value: Any?, indent: Int = 0): String {
    val pad = "  ".repeat(indent + 1)
    val closePad = "  ".repeat(indent)
    return when (value) {
        is Map<*, *> ->
            if (value.isEmpty()) "{}"
            else value.entries.joinToString(",\n", "{\n", "\n$closePad}") { (k, v) ->
                pad + "\"" + escapeJson(k.toString()) + "\": " + toPrettyJson(v, indent + 1)
            }
        is Iterable<*> -> {
            val items = value.toList()
            if (items.isEmpty()) "[]" else items.joinToString(",\n", "[\n", "\n$closePad]") { pad + toPrettyJson(it, indent + 1) }
        }
        else -> toJson(value)
    }
}

/** An object becomes a LinkedHashMap (keeps the order you write), then toJson does the rest. */
fun User.asMap(): Map<String, Any?> = linkedMapOf(
    "id" to id,
    "name" to name,
    "active" to active,
    "score" to score,
    "tags" to tags,
    "address" to address?.let { linkedMapOf("city" to it.city, "zip" to it.zip) },
)

/** The other hand-written style: a raw string with templates. Fine for one small class, tedious for many. */
fun Address.toJsonByHand(): String = """{"city":${toJson(city)},"zip":${toJson(zip)}}"""

/**
 * 21b. JSON BY HAND  (no library)
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.HandWrittenJsonKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("JSON BY HAND  -  escape + one recursive function")

    section("1. Escape the text")
    syntax("\" -> \\\"   \\ -> \\\\   newline -> \\n   tab -> \\t   other control char -> \\u00XX" to "what a JSON string may NOT contain raw")
    show { escapeJson("say \"hi\"") }                                          // the quotes get a backslash
    show { escapeJson("C:\\temp") }                                            // a backslash becomes two
    show { escapeJson("line1\nline2") }                                        // a real newline becomes the two characters \n
    show { escapeJson(1.toChar().toString()) }                                 // control character 1 becomes \u0001
    note("Forgetting to escape is THE bug: a name like O\"Brien breaks the whole document")

    section("2. One recursive function for every value")
    syntax("fun toJson(value: Any?): String = when (value) { null -> ..., is Map<*, *> -> ... }" to "a when over the type; Map and Iterable call toJson again")
    show { toJson(null) }                                                      // the literal null, no quotes
    show { toJson(true) }
    show { toJson(42) }
    show { toJson("text") }                                                    // strings get quotes
    show { toJson('c') }                                                       // Char is a string in JSON
    show { toJson(Level.HIGH) }                                                // enum: its name as a string
    show { toJson(listOf(1, 2, 3)) }
    show { toJson(mapOf("a" to 1, "b" to listOf("x", "y"))) }                  // maps and lists nest freely
    show { toJson(arrayOf<Any?>(1, "two", null)) }                             // mixed array: say the element type, or the compiler guesses
    show { toJson(emptyList<Int>()) }                                          // [] : joinToString with prefix and postfix is safe on empty
    show { toJson(0.1 + 0.2) }                                                 // Double.toString: the shortest text that reads back the same
    show { toJson(Double.NaN) }                                                // JSON cannot hold NaN: this version writes null
    show { toJson(1.0 / 0) }                                                   // Infinity: also null
    show { toJson(10_000_000_000L) }                                           // Long is fine here; JavaScript readers lose digits above 2^53

    section("3. Your own class: build a Map, then toJson")
    val user = sampleUser()
    show { toJson(user.asMap()) }                                              // the whole User on one line
    show { toJson(user.address?.toJsonByHand()) }                              // a JSON text is a String, so it was quoted AGAIN: trap
    show { user.address?.toJsonByHand() }                                      // the raw-string style: correct for one nested class
    show { toJson(emptyMap<String, Any>()) }                                   // {}
    show { toJson(sortedMapOf("b" to 2, "a" to 1)) }                           // sortedMapOf (TreeMap) sorts the keys: stable output for a test

    section("4. Pretty print")
    syntax("joinToString(separator = \",\\n\", prefix = \"{\\n\", postfix = \"\\n}\")" to "one line per entry; indent grows by two spaces per level")
    block(toPrettyJson(user.asMap()))

    section("Cost")
    bigO(
        "toJson / toPrettyJson over n values" to "O(n) time: every value is visited once",
        "joinToString or buildString" to "O(n) total",
        "s += piece in a loop" to "O(n^2): every + copies the whole string. Use buildString",
    )
    note("Interview order: escape first, then the when, then the object-to-Map step, then pretty print if asked")
}
