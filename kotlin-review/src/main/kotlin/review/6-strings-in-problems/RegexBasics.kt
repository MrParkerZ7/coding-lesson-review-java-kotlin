package review.strings

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 6b. REGEX BASICS: matches, find, findAll, groups, replace
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.RegexBasicsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("REGEX BASICS  -  patterns for parsing and cleaning text")

    section("Making a Regex")
    syntax(
        "Regex(\"\"\"\\d+\"\"\")" to "a raw string needs no doubled backslash",
        "\"\\\\d+\".toRegex()" to "the same pattern from a normal string: the backslash is doubled",
    )
    val digits = Regex("""\d+""")
    show { digits.pattern }
    show { "\\d+".toRegex().pattern }                         // the same pattern, written with an escaped backslash
    wontCompile("Regex(\"\\d+\")", "in a normal string a lone backslash-d is an unsupported escape: use a raw string")

    section("matches, containsMatchIn, find, findAll")
    syntax(
        "re.matches(s)" to "does the WHOLE string match?",
        "re.containsMatchIn(s)" to "does it match anywhere inside?",
        "re.find(s) | re.findAll(s)" to "the first MatchResult? | a Sequence of all of them",
        "match.value | match.range" to "the matched text | where it sits",
    )
    show { digits.matches("123") }
    show { digits.matches("a123") }                           // TRAP: false, matches() needs the whole string to fit
    show { digits.containsMatchIn("a123") }                   // true: a match exists somewhere inside
    show { digits.find("ab 42 cd 7")?.value }                 // the first match only
    show { digits.find("ab 42 cd 7")?.range }                 // indexes of that match
    show { digits.find("none") }                              // null when there is no match
    show { digits.findAll("ab 42 cd 7").map { it.value }.toList() }   // findAll is lazy: toList() runs it
    show { "x@y.com" matches Regex(".+@.+") }                 // the infix form
    show { "abc".contains(Regex("b")) }                       // contains accepts a Regex too

    section("Groups: parentheses capture parts")
    syntax(
        "match.groupValues" to "List<String>: index 0 is the whole match, then group 1, 2, ...",
        "match.destructured" to "val (y, m, d) = match.destructured",
        "(?<name>...) | match.groups[\"name\"]" to "named groups",
    )
    val date = Regex("""(\d{4})-(\d{2})-(\d{2})""")
    val found = date.find("on 2026-11-09 ok")
    show { found?.groupValues?.toList() }                     // index 0 is the whole match
    show { found?.groupValues?.get(1)?.toInt() }              // group 1 is the year
    val (year, month, day) = found?.destructured ?: error("no match")
    show { "$day/$month/$year" }                              // destructured skips the whole match and gives the groups
    val named = Regex("""(?<year>\d{4})-(?<month>\d{2})""").find("2026-11")
    show { named?.groups?.get("year")?.value }                // look a group up by name

    section("replace and split with a Regex")
    show { digits.replace("ab 42 cd 7", "#") }
    show { digits.replace("ab 42 cd 7") { (it.value.toInt() * 2).toString() } }   // compute the replacement from each match
    show { "Hello, World 42!".replace(Regex("[^A-Za-z0-9]"), "") }   // drop everything except letters and digits
    show { "Hello, World 42!".replace(Regex("[aeiou]"), "*") }
    show { "one  two   three".split(Regex("""\s+""")) }       // split on runs of spaces
    show { "a.b.c".replace(".", "-") }                        // a String pattern is literal text, no regex
    show { "a.b.c".replace(Regex("."), "-") }                 // TRAP: as a regex "." matches every character
    show { Regex.escape("a.b") }                              // turn user text into a literal pattern
    note("Common pieces: \\d digit, \\w word char, \\s space, . any char, + one or more, * zero or more, ^ start, $ end")

    section("Cost")
    bigO(
        "matches | find | replace" to "O(n) in the text for ordinary patterns",
        "nested quantifiers like (a+)+" to "can backtrack exponentially on bad input: keep patterns simple",
        "Regex(\"...\") itself" to "compiles the pattern: build it once, not inside a loop",
    )
}
