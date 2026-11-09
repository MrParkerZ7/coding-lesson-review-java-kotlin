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

/** Caesar shift of one lowercase letter, wrapping from z back to a: the alphabet index trick. O(1). */
fun shiftLetter(c: Char, k: Int): Char = 'a' + (c - 'a' + k) % 26

/**
 * 6c. STRINGBUILDER AND CHARS: build text fast, work with letters
 *     (Char basics are lesson 0 CharType; this file is the problem-solving side)
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.StringBuilderAndCharsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STRINGBUILDER AND CHARS  -  build text fast, count letters")

    section("StringBuilder: a text you can change")
    syntax(
        "val sb = StringBuilder()" to "a mutable text buffer",
        "sb.append(x) | sb.insert(i, x) | sb.reverse()" to "add at the end | add at index i | reverse in place",
        "sb.deleteCharAt(i) | sb.setCharAt(i, c) | sb[i] = c" to "remove one char | replace one char",
        "sb.toString()" to "the finished String",
    )
    val sb = StringBuilder("hello")
    sb.append(" world");  show { sb }
    sb.insert(0, ">> ");  show { sb }
    sb.deleteCharAt(0);  show { sb }                          // removes the first >
    sb.setCharAt(0, '*');  show { sb }                        // replaces one char in place
    sb[2] = 'H';  show { sb }                                 // sb[i] = c is the same as setCharAt
    sb.reverse();  show { sb }                                // reverses in place and returns the same builder
    show { sb.length }
    wontCompile("val s = \"abc\"; s[0] = 'x'", "a String has no set operator; a StringBuilder has one")
    show { StringBuilder("a") == StringBuilder("a") }         // TRAP: false, StringBuilder has no equals(): compare toString()
    show { buildString { append("a"); append(1) } }           // buildString { } = StringBuilder + toString()

    section("s += x in a loop: a new String every round")
    syntax("text += x" to "copies the old text into a NEW String: O(n) per round, O(n^2) for the loop")
    var text = ""
    var plusCopies = 0
    for (i in 1..1000) {
        text += "x"
        plusCopies += text.length                             // the characters this round had to copy
    }
    show { plusCopies }                                       // 1 + 2 + ... + 1000: about n^2 / 2 characters copied
    val builder = StringBuilder()
    for (i in 1..1000) builder.append("x")
    show { builder.length }                                   // the builder wrote each character once: about n
    val before = text
    text += "!"
    show { before === text }                                  // false: += made a NEW String, the old one is untouched
    val same = builder
    builder.append("!")
    show { same === builder }                                 // true: append changes the one object

    section("Char helpers")
    syntax(
        "c.isDigit() | isLetter() | isWhitespace()" to "what kind of character (also isLetterOrDigit())",
        "isUpperCase() | uppercaseChar() | lowercaseChar()" to "case test and conversion",
        "c.code | c.digitToInt()" to "the number of the Char | '7' becomes 7",
    )
    show { '7'.isDigit() }
    show { 'x'.isLetter() }
    show { '_'.isLetterOrDigit() }                            // underscore is neither
    show { 'Q'.isUpperCase() }
    show { 'Q'.lowercaseChar() }
    show { 'a'.code }
    show { '7'.digitToInt() }
    show { '7' - '0' }                                        // the manual way: 7
    show { failure { 'x'.digitToInt() } }                     // TRAP: a non-digit throws
    show { 'x'.digitToIntOrNull() }                           // the safe version

    section("Char arithmetic: the alphabet index trick")
    syntax("Char + Int = Char | Char - Char = Int" to "c - 'a' is the position of c in the alphabet (a = 0)")
    show { 'a' + 1 }
    show { 'z' - 'a' }
    show { 'a' + 25 }
    show { shiftLetter('c', 3) }                              // Caesar shift by 3
    show { shiftLetter('x', 3) }                              // wraps around from z to a
    show { 'a' + "b" }                                        // TRAP: Char + String is a String
    show { "" + 'a' + 'b' }                                   // start from a String to join Chars
    wontCompile("'a' + 'b'", "Char + Char is not defined (Char + Int and Char + String are)")

    section("Counting letters with IntArray(26)")
    syntax("val counts = IntArray(26); counts[c - 'a']++" to "one counter per letter a..z: O(1) space, no hashing")
    val counts = IntArray(26)
    for (c in "banana") counts[c - 'a']++
    show { counts['a' - 'a'] }                                // how many a
    show { counts['n' - 'a'] }                                // how many n
    show { counts.withIndex().filter { it.value > 0 }.map { ('a' + it.index) to it.value } }   // index back to the letter
    show { failure { IntArray(26)['A' - 'a'] } }              // TRAP: 'A' - 'a' is -32, an index error: lowercase first
    note("For any ASCII character use IntArray(128) and counts[c.code]; use a HashMap when the characters are unknown")

    section("Cost")
    bigO(
        "sb.append(x) | sb[i] | sb.setCharAt" to "O(1) (append is O(1) amortized)",
        "sb.insert(0, x) | sb.deleteCharAt(0)" to "O(n): the rest of the text shifts",
        "sb.reverse() | sb.toString()" to "O(n)",
        "text += x in a loop of n rounds" to "O(n^2) in total",
        "IntArray(26) counting" to "O(n) time, O(1) space: 26 counters whatever n is",
        "HashMap<Char, Int> counting" to "O(n) time, O(k) space for k distinct characters",
    )
}
