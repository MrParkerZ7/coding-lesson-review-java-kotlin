package review.primitive

import review.section
import review.show
import review.syntax
import review.title

/**
 * 0d. CHAR
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.CharTypeKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CHAR  -  single quotes 'A' = Char, double quotes \"A\" = String")

    section("A Char is a number underneath")
    syntax(
        "'A'" to "Char literal, single quotes",
        "c.code" to "the number (Unicode code point) of a Char",
    )
    show { 'A'.code }                                         // the code of 'A'
    show { 'a'.code }                                         // lowercase is 32 higher
    show { '0'.code }                                         // the digit zero is 48, not 0

    section("Char arithmetic")
    syntax(
        "Char + Int" to "Char",
        "Char - Char" to "Int",
        "n.toChar()" to "number -> Char",
    )
    show { 'A' + 1 }                                          // Char + Int = Char
    show { 'A' + 2 }
    show { 'C' - 'A' }                                        // Char - Char = Int
    show { 'e' - 'a' }                                        // alphabet index of 'e' (a = 0)
    show { 65.toChar() }                                      // code -> Char

    section("Digits")
    syntax("c.digitToInt() | n.digitToChar()" to "'7' <-> 7")
    show { '7'.digitToInt() }                                 // Char '7' -> Int 7
    show { 7.digitToChar() }                                  // Int 7 -> Char '7'

    section("Tests and case")
    syntax("c.isLetter() | isDigit() | isUpperCase() | uppercaseChar() | lowercaseChar()" to "")
    show { 'x'.isLetter() }
    show { 'x'.isDigit() }
    show { 'x'.isUpperCase() }
    show { 'x'.uppercaseChar() }
    show { 'X'.lowercaseChar() }

    section("Escape sequences (shown as codes)")
    syntax("'\\t' | '\\n' | '\\\\' | '\\'' | '\\$'" to "tab | newline | backslash | single quote | dollar sign")
    show { '\t'.code }                                        // tab
    show { '\n'.code }                                        // newline
    show { '\\'.code }                                        // backslash
    show { '\''.code }                                        // single quote
    show { '$'.code }                                         // dollar sign (must be escaped inside a String)
}
