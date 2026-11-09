package review.strings

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** Runs the block and returns the simple name of the exception it threw, or null when it worked. */
private fun failure(block: () -> Any?): String? = runCatching(block).exceptionOrNull()?.let { it::class.simpleName }

/**
 * 6a. STRING OPERATIONS: split, slice, search, transform, convert, compare
 *     (the String type itself is lesson 0 StringType; regular expressions are 6b RegexBasics)
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.StringOperationsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STRING OPERATIONS  -  the tools of every string problem")
    note("A String is immutable: every operation below returns a NEW String and leaves the original alone")

    val s = "Hello, Kotlin"

    section("split and joinToString")
    syntax(
        "s.split(delimiter, ..., limit = n)" to "List<String>; a delimiter is plain text, NOT a regex",
        "s.split(Regex(\"...\"))" to "split on a pattern",
        "xs.joinToString(separator)" to "the reverse: List -> String",
    )
    show { "a,b,c".split(",") }
    show { "a,,b".split(",") }                                // an empty piece stays in the list
    show { "a, b;c".split(", ", ";") }                        // several delimiters at once
    show { "a,b,c,d".split(",", limit = 2) }                  // at most 2 pieces: the rest stays in the last one
    show { "a.b.c".split(".") }                               // "." is plain text here, so this works
    show { "a.b.c".split(Regex(".")).size }                   // TRAP: as a regex "." matches every character: 6 empty pieces
    show { "  a  b ".split(" ") }                             // TRAP: every single space splits, empty pieces appear
    show { "  a  b ".trim().split(Regex("""\s+""")) }         // trim, then split on runs of whitespace
    show { "".split(",").size }                               // 1: ONE empty string, not an empty list
    show { "a-b-c".split("-").joinToString("+") }

    section("Slicing: substring, take, drop")
    syntax(
        "s.substring(start, endExclusive)" to "start included, end EXCLUDED; throws when out of range",
        "s.take(n) | s.takeLast(n)" to "the first n | the last n; safe when n is too big",
        "s.drop(n) | s.dropLast(n)" to "everything except those",
    )
    show { s.substring(7) }
    show { s.substring(0, 5) }                                // indexes 0..4
    show { s.substring(7, 7) }                                // start == end: the empty String
    show { failure { s.substring(5, 20) } }                   // TRAP: end beyond the length throws
    show { s.take(99) }                                       // take never throws
    show { s.takeLast(6) }
    show { s.drop(7) }
    show { s.dropLast(8) }
    show { s.substringBefore(", ") }
    show { s.substringAfter(", ") }

    section("Search and test")
    show { s.contains("Kot") }
    show { s.contains("kot", ignoreCase = true) }
    show { s.startsWith("Hell") }
    show { s.endsWith("lin") }
    show { s.indexOf('o') }                                   // first match
    show { s.lastIndexOf('o') }                               // last match
    show { s.indexOf('o', 5) }                                // search from index 5
    show { s.indexOf("xyz") }                                 // -1 when missing, never an exception
    show { s.first() }
    show { "".firstOrNull() }                                 // first() on "" throws NoSuchElementException

    section("Case, trim, replace, repeat, pad, lines")
    show { s.lowercase() }
    show { s.uppercase() }
    show { "  hi \n".trim() }                                 // trim removes whitespace on both ends, newline included
    show { "xxhixx".trim('x') }                               // or the characters you name
    show { s.replace("l", "L") }                              // replaces ALL matches
    show { s.replaceFirst("l", "L") }
    show { "ab".repeat(3) }
    show { "7".padStart(3, '0') }
    show { "ab".padEnd(5, '.') }
    show { "a\nb\r\nc".lines() }                              // lines() understands both \n and \r\n

    section("A String is a sequence of Char: filter, count, reversed")
    show { s.reversed() }
    show { s.filter { it.isLetter() } }                       // keep only the letters
    show { s.count { it in "aeiou" } }                        // e, o, o, i
    show { s.toSet().size }                                   // distinct characters (the space and comma count)

    section("String <-> CharArray <-> List<Char>")
    syntax(
        "s.toCharArray()" to "a CharArray you CAN change in place",
        "String(chars) | chars.concatToString()" to "back to a String",
        "s.toList()" to "List<Char>; there is no sorted() on a String itself",
    )
    wontCompile("\"hello\".sorted()", "there is no sorted() on a String: use toList().sorted()")
    val chars = "hello".toCharArray()
    chars.sort()
    show { chars }                                            // sorted in place
    show { String(chars) }
    show { chars.concatToString() }
    show { "hello".toList().sorted().joinToString("") }       // sorting the letters of a word: the anagram key
    show { chars.toString().startsWith("[C@") }               // TRAP: toString() of a CharArray is an address like [C@1b2c3d

    section("Comparing text")
    show { "abc".compareTo("abd") }                           // negative: the first different char decides, 'c' < 'd'
    show { "ab" < "abc" }                                     // a prefix is smaller than the longer text
    show { "10" < "9" }                                       // TRAP: text order compares char by char, '1' < '9'
    show { listOf("10", "9", "2").sorted() }
    show { listOf("10", "9", "2").sortedBy { it.toInt() } }   // sort by the number inside
    note("Case-sensitive order and compareTo values: lesson 1 Equality")

    section("Cost")
    bigO(
        "s[i] | s.length" to "O(1)",
        "substring | take | drop | reversed | replace | s + t" to "O(n): a new String is copied",
        "contains | indexOf" to "O(n) typical, O(n * m) worst case for a pattern of length m",
        "s == t | compareTo" to "O(n): char by char, stops at the first difference",
        "split | lines | toCharArray | filter" to "O(n) time and O(n) space",
        "s += x in a loop" to "O(n^2) in total: see 6c StringBuilderAndChars",
    )
}
