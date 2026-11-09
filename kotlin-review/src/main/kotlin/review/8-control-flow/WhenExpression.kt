package review.control

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

enum class Light { RED, YELLOW, GREEN }

sealed interface Shape {
    data class Circle(val r: Int) : Shape
    data class Rect(val w: Int, val h: Int) : Shape
    data object Dot : Shape
}

/** Several values per branch, then a range. */
fun dayKind(d: Int): String = when (d) {
    6, 7 -> "weekend"
    in 1..5 -> "weekday"
    else -> "invalid"
}

/** Strings are compared with ==, so "start" and "go" can share a branch. */
fun command(cmd: String): String = when (cmd) {
    "start", "go" -> "starting"
    "stop" -> "stopping"
    else -> "unknown"
}

/** First match wins: a vowel is also in 'a'..'z', but the vowel branch comes first. */
fun letterKind(c: Char): String = when (c) {
    'a', 'e', 'i', 'o', 'u' -> "vowel"
    in 'a'..'z' -> "consonant"
    else -> "other"
}

/** is with several types in one branch. */
fun kindOf(x: Any): String = when (x) {
    is Int, is Long -> "integer"
    is String -> "text"
    else -> "other"
}

/** Each branch ends by itself: no break, no fall-through into the next branch. */
fun noFallThrough(n: Int): List<String> {
    val out = mutableListOf<String>()
    when (n) {
        1 -> out += "one"
        2 -> out += "two"
        3 -> out += "three"
    }
    return out
}

/** No subject: every branch is a Boolean condition. Order matters. */
fun temperatureWord(t: Int): String = when {
    t < 0 -> "freezing"
    t < 15 -> "cold"
    t < 25 -> "mild"
    else -> "hot"
}

/** TRAP: the second branch can never run, because n > 0 already matched every n > 100. */
fun wrongOrder(n: Int): String = when {
    n > 0 -> "positive"
    n > 100 -> "huge"
    else -> "not positive"
}

/** when (val x = ...) names the subject once; the name only exists inside the when. */
fun lengthClass(s: String): String = when (val len = s.length) {
    0 -> "empty"
    in 1..3 -> "short ($len)"
    else -> "long ($len)"
}

/** A branch can be a block: its last expression is the value. */
fun price(code: String): Int = when (code) {
    "A" -> { val base = 10; base * 2 }
    "B" -> 5
    else -> 0
}

/** An enum subject listing every constant needs no else. */
fun nextLight(l: Light): Light = when (l) {
    Light.RED -> Light.GREEN
    Light.GREEN -> Light.YELLOW
    Light.YELLOW -> Light.RED
}

/** A sealed subject listing every subtype needs no else either. */
fun corners(s: Shape): Int = when (s) {
    is Shape.Circle -> 0
    is Shape.Rect -> 4
    Shape.Dot -> 0
}

/** A guard condition: the branch also needs the extra test after if. */
fun classify(x: Any): String = when (x) {
    is String if x.length > 3 -> "long text"
    is String -> "short text"
    else -> "other"
}

/**
 * 8b. WHEN: the Kotlin switch, and much more
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.WhenExpressionKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("WHEN EXPRESSION  -  a switch that returns a value")

    section("when with a subject")
    syntax(
        "when (x) { a -> r1; b, c -> r2; else -> r3 }" to "compare x with each branch in order",
        "in a..b -> | !in a..b ->" to "range (or collection) check",
        "is Type -> | !is Type ->" to "type check with smart cast",
    )
    val day = 2
    show { when (day) { 1 -> "Mon"; 2 -> "Tue"; else -> "?" } }
    show { dayKind(7) }                                                        // 6, 7 -> several values in one branch
    show { dayKind(3) }                                                        // in 1..5 -> a range
    show { dayKind(9) }
    show { command("go") }                                                     // strings compare by value
    show { kindOf(5L) }                                                        // is Int, is Long -> one branch for two types
    show { kindOf("x") }
    show { kindOf(2.5) }

    section("No fall-through, and the first match wins")
    syntax("when { cond1 -> r1; cond2 -> r2; else -> r3 }" to "no subject: each branch is a Boolean condition")
    show { noFallThrough(2) }                                                  // only two: no break needed (a Java switch would fall into three)
    show { letterKind('e') }                                                   // vowel: also in 'a'..'z', but the first match wins
    show { letterKind('z') }
    show { letterKind('E') }                                                   // other: 'E' is upper case, neither branch matches
    show { temperatureWord(-5) }
    show { temperatureWord(15) }                                               // boundary: t < 15 is false, so mild
    show { temperatureWord(25) }
    show { wrongOrder(500) }                                                   // TRAP: positive, the huge branch is unreachable

    section("when as an expression: else, or exhaustive")
    syntax(
        "val v = when (x) { ...; else -> ... }" to "as a value, every possible x needs a branch",
        "enum or sealed subject" to "list every case and no else is needed (and none is better)",
    )
    wontCompile("val s = when (n) { 1 -> 10 }", "a when used as a value must be exhaustive: add else")
    wontCompile("val s = when (n) { else -> 0; 1 -> 1 }", "the else entry must be the last one")
    show { nextLight(Light.RED) }
    show { nextLight(Light.YELLOW) }
    show { corners(Shape.Rect(2, 3)) }
    show { corners(Shape.Dot) }
    note("Add a Light constant and every when without else stops compiling, listing what you forgot. An else would hide it")

    section("Blocks, val in the subject, guards")
    syntax(
        "when (val x = f()) { ... }" to "name the subject once; x lives only inside the when",
        "a -> { stmts; value }" to "a block branch: its last expression is the value",
        "is Type if cond -> ..." to "a guard: the branch also needs cond (newer Kotlin)",
    )
    show { lengthClass("") }
    show { lengthClass("abc") }
    show { lengthClass("kotlin") }
    show { price("A") }                                                        // 20: the block ran, base * 2 is its value
    show { classify("hello") }
    show { classify("hi") }                                                    // the guard failed, so the next branch matched
    show { classify(7) }
    note("when is the answer to 'Kotlin has no switch': it is an expression, has no fall-through, and matches values, ranges and types")
}
