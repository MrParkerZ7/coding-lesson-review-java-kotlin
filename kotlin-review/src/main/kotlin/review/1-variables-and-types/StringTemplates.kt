package review.variables

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile
import java.util.Locale

/** Runs the block and returns the simple name of the exception it threw, or null when it worked. */
private fun failure(block: () -> Any?): String? = runCatching(block).exceptionOrNull()?.let { it::class.simpleName }

/**
 * 1c. STRING TEMPLATES: $name, ${expr}, raw strings, format, buildString, + on Strings
 *     (the String type is lesson 0 StringType; string operations for problems are lesson 6)
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.StringTemplatesKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STRING TEMPLATES  -  put values into text")

    section("Templates: \$name and \${expression}")
    syntax(
        "\"Hi \$name\"" to "put a variable into the text",
        "\"\${expression}\"" to "braces for anything else: a property, a call, a calculation, an if",
    )
    val name = "Kotlin"
    val n = 3
    val items = listOf("a", "b")
    show { "Hi $name" }
    show { "${n * 2} items" }
    show { "${name.length} letters" }                         // braces are needed for a property or a call
    show { "$name.length" }                                   // TRAP: $name stops at the dot, ".length" stays as plain text
    show { "first: ${items[0]}" }
    show { "$items[0]" }                                      // TRAP: the whole list, then the plain text [0]
    show { "n is ${if (n > 2) "big" else "small"}" }          // any expression, even an if with its own quotes

    section("A literal dollar sign")
    show { "Price: \$5" }                                     // a backslash escape
    show { "Price: ${'$'}5" }                                 // or the Char '$' inside a template
    show { """Price: ${'$'}5""" }                             // raw strings have no backslash escapes, so use the template form
    show { $$"Price: $5 and $${name}" }                       // prefix $$: only $$name and $${...} are templates

    section("Raw strings: triple quotes")
    syntax(
        "\"\"\"text\"\"\"" to "may span lines, a backslash is just a backslash, templates still work",
        "text.trimIndent()" to "remove the common leading spaces and the blank first and last line",
        "text.trimMargin()" to "remove each line up to and including its | margin",
    )
    show { """C:\temp\new""" }                                // no escape processing: \t and \n stay as typed
    show { """C:\temp\new""".length }                         // 11: the backslash is a real character
    show { """Hi $name!""" }                                  // templates work inside raw strings
    val raw = """
        roses
          are red
        """
    show { raw.lines().size }                                 // 4: blank first line, two text lines, the closing-quote line
    val trimmed = raw.trimIndent()
    show { trimmed.replace("\n", "|") }                       // newlines shown as |: common indent gone, relative indent kept
    val margin = """
        |first
        |  second
        """.trimMargin()
    show { margin.replace("\n", "|") }

    section("String.format")
    syntax("\"%d %s %.2f\".format(a, b, c)" to "printf style: %d integer, %s text, %.2f decimals, %05d zero padded, %% a percent sign")
    show { "%d + %d = %d".format(2, 3, 5) }
    show { "%05d".format(42) }
    show { "%-6s|".format("ab") }                             // left-aligned in 6 columns
    show { "%6s|".format("ab") }                              // right-aligned
    show { "%x".format(255) }                                 // hexadecimal
    show { "%d%%".format(50) }
    show { "%.2f".format(Locale.ROOT, 3.14159) }              // pass a Locale: the default one may print 3,14
    show { failure { "%d".format(2.5) } }                     // %d with a Double fails at run time, not at compile time

    section("buildString and joinToString")
    show { buildString { append("n="); append(5); append('!') } }
    val csv = buildString { for (i in 1..3) { append(i); append(',') } }
    show { csv }                                              // TRAP: a trailing comma
    show { (1..3).joinToString(",") }                         // no trailing separator
    show { buildString { appendLine("x"); append("y") }.lines() }   // appendLine adds a line break
    show { "line1\nline2".lines() }                           // \n is ONE character; lines() splits on it

    section("+ on Strings")
    syntax("string + anything" to "String.plus(Any?): the other side is turned into text with toString()")
    show { "a" + 1 }
    show { "a" + 1 + 2 }                                      // left to right: ("a" + 1) + 2, every step is a String
    show { "a" + (1 + 2) }                                    // parentheses do the number math first
    show { "a".plus(1) }                                      // + is the plus() function
    show { "a" + DataPoint(1, 2) }                            // toString() of a data class
    wontCompile("1 + \"a\"", "Int.plus() has no overload that takes a String: write \"\${1}a\" or 1.toString() + \"a\"")

    section("null in text")
    val none = nameOrNull(false)?.length                       // an Int? that is null at run time
    show { "value: $none" }                                   // the four letters null
    show { none.toString() }                                  // "null": toString() on a nullable is safe
    show { none?.toString() }                                 // the real null, not the text
    show { "a" + none }                                       // the text null is glued on
    note("Anything that prints a null prints the text null; check for null first when that matters (lesson 7)")
}
