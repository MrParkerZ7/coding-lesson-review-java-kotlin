package review.primitive

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 0h. STRING: not a primitive, but the basic type every program uses
 *     (string operations used in interview problems get their own lesson, 6)
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.StringTypeKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STRING  -  an immutable sequence of Chars")

    section("Literals and templates")
    syntax(
        "\"text\"" to "String literal, double quotes",
        "\"Hi \$name\" | \"\${expression}\"" to "template: put a variable or an expression into the text",
        "\"\"\"raw text\"\"\"" to "raw string: keeps line breaks, no escapes",
    )
    val name = "Kotlin"
    val n = 3
    val raw = """line1
        |line2""".trimMargin()
    show { "Hi $name" }                                       // $name puts a variable into the text
    show { "${n * 2} items" }                                 // ${...} holds any expression
    show { "a\tb".length }                                    // an escape like \t is ONE char
    show { raw.lines() }                                      // triple quotes keep line breaks; trimMargin() strips the | margin

    section("Immutable")
    val s = "abc"
    show { s.uppercase() }                                    // returns a NEW String
    show { s }                                                // the original is unchanged
    wontCompile("s[0] = 'x'", "a String cannot be changed in place")

    section("Length and indexing")
    syntax("s[i] | s.length | s.lastIndex | s.getOrNull(i)" to "index from 0, safe access with getOrNull")
    show { s.length }
    show { s[0] }                                             // a String is indexed like a list of Char
    show { s.last() }
    show { s[s.lastIndex] }
    show { s.getOrNull(10) }                                  // null instead of StringIndexOutOfBoundsException

    section("Equality: == compares the content")
    syntax("a == b | a === b" to "same content | same object")
    val a = "hello"
    val c = String(charArrayOf('h', 'e', 'l', 'l', 'o'))
    show { a == c }                                           // Kotlin's == is equals(): same text -> true (Java's == would be false)
    show { a === c }                                          // === is identity: two different objects
    show { "a".equals("A", ignoreCase = true) }
    show { "apple".compareTo("banana") }                      // negative: apple comes first
    show { "b" > "a" }                                        // < and > use compareTo

    section("Everyday operations")
    val h = "Hello, Kotlin"
    show { "  Hello  ".trim() }
    show { h.substring(7) }                                   // from index 7 to the end
    show { h.substring(0, 5) }                                // end index is exclusive
    show { h.indexOf("K") }                                   // -1 when not found
    show { h.contains("Kot") }
    show { h.replace("l", "L") }                              // replaces ALL matches
    show { h.split(", ") }
    show { h.reversed() }
    show { h.count { it == 'l' } }
    show { "ab".repeat(3) }
    show { "7".padStart(3, '0') }

    section("String <-> Char <-> number")
    show { "a" + 1 }                                          // String + anything = String
    show { 1.toString() + 2 }                                 // convert first: text 12, not number 3
    show { "5".toInt() + 1 }
    show { "abc".toList() }                                   // a List<Char>
    show { listOf('a', 'b').joinToString("") }                // chars back to a String
    wontCompile("1 + \"a\"", "Int has no plus(String)")

    section("Building a String in a loop")
    show { buildString { for (i in 1..3) append(i) } }        // one StringBuilder underneath
    note("text += \"x\" in a loop makes a new String every time (slow for big loops): use buildString / StringBuilder")
}
