package review.classes

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

enum class Level(val label: String, val weight: Int) {
    LOW("low", 1), MEDIUM("medium", 5), HIGH("high", 10);       // ';' ends the constants when members follow

    fun isUrgent(): Boolean = weight >= 10
}

enum class Op(val symbol: String) {
    PLUS("+") { override fun apply(a: Int, b: Int): Int = a + b },
    TIMES("*") { override fun apply(a: Int, b: Int): Int = a * b };   // each constant has its own body

    abstract fun apply(a: Int, b: Int): Int
}

fun advice(level: Level): String = when (level) {              // no else: every constant is covered
    Level.LOW -> "relax"
    Level.MEDIUM -> "plan"
    Level.HIGH -> "act now"
}

sealed interface Payment {
    data class Card(val last4: String) : Payment
    data class Cash(val amount: Int) : Payment
    data object Pending : Payment                                // one instance, readable toString
}

fun payInfo(p: Payment): String = when (p) {                   // no else: the compiler knows every subtype
    is Payment.Card -> "card ending ${p.last4}"
    is Payment.Cash -> "cash ${p.amount}"
    Payment.Pending -> "waiting"
}

sealed class Expr
data class Num(val value: Int) : Expr()
data class Add(val left: Expr, val right: Expr) : Expr()
data class Mul(val left: Expr, val right: Expr) : Expr()

fun eval(e: Expr): Int = when (e) {
    is Num -> e.value
    is Add -> eval(e.left) + eval(e.right)
    is Mul -> eval(e.left) * eval(e.right)
}

sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Failure(val reason: String) : Outcome<Nothing>
}

fun parseAge(text: String): Outcome<Int> {
    val n = text.toIntOrNull() ?: return Outcome.Failure("not a number: $text")
    return if (n in 0..150) Outcome.Ok(n) else Outcome.Failure("out of range: $n")
}

/**
 * 11e. ENUM and SEALED: a closed set of values, a closed set of kinds
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.EnumAndSealedKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 *      (errorOf, used below, is declared in ClassBasics.kt: same package.)
 */
fun main() {
    title("ENUM AND SEALED  -  exhaustive when, data-carrying cases")

    section("enum class: a fixed set of values")
    syntax(
        "enum class Name(val p: T) { A(1), B(2); fun f() ... }" to "constants are objects",
        "Name.entries" to "all constants in order (Kotlin 1.9+; replaces values())",
        "Name.valueOf(\"A\")" to "by exact name; IllegalArgumentException when missing",
        "e.name | e.ordinal" to "the constant's name | its position, from 0",
    )
    show { Level.HIGH }
    show { Level.entries.toList() }
    show { Level.MEDIUM.ordinal }                                              // the second constant: 1
    show { Level.MEDIUM.name }
    show { Level.valueOf("LOW").label }
    show { errorOf { Level.valueOf("low") } }                                  // TRAP: the name is case-sensitive, so this throws
    show { Level.entries.firstOrNull { it.label == "none" } }                  // null: the safe lookup by another field
    show { Level.LOW < Level.HIGH }                                            // true: enums compare by ordinal
    show { Level.HIGH.isUrgent() }
    show { Op.TIMES.apply(6, 7) }                                              // a constant with its own body
    show { Op.entries.map { it.symbol } }

    section("when over an enum needs no else")
    show { advice(Level.HIGH) }
    wontCompile("fun f(l: Level): String = when (l) { Level.LOW -> \"a\" }", "a when used as an expression must cover every constant")
    note("Add a constant later and every exhaustive when stops compiling: the compiler lists the places to fix")

    section("sealed: a closed set of KINDS, each with its own data")
    syntax(
        "sealed interface Name { ... }" to "every direct subtype is known at compile time (same package and module)",
        "data class A(...) : Name  |  data object B : Name" to "a kind with data | a kind with none",
        "when (x) { is A -> ...; B -> ... }" to "exhaustive without else",
    )
    show { payInfo(Payment.Card("4242")) }
    show { payInfo(Payment.Cash(30)) }
    show { payInfo(Payment.Pending) }
    show { Payment.Cash(30) }                                                  // each kind has its own fields
    show { Payment.Pending }                                                   // a data object prints its name
    wontCompile("Payment()", "a sealed type cannot be instantiated")
    wontCompile("fun f(p: Payment): Int = when (p) { is Payment.Cash -> 1 }", "a missing subtype: the when is not exhaustive")

    section("A sealed class as an expression tree")
    val tree = Add(Num(2), Mul(Num(3), Num(4)))
    show { tree }
    show { eval(tree) }                                                        // 2 + 3 * 4

    section("A sealed type as a result: Ok or Failure")
    show { parseAge("42") }
    show { parseAge("x") }
    show { parseAge("200") }

    section("Enum or sealed?")
    note("enum = a fixed set of VALUES: every constant has the same fields (Level.HIGH); it has valueOf, entries, ordinal")
    note("sealed = a fixed set of KINDS: each kind has its own fields (Card has last4, Cash has amount) and can have many instances")
    note("Sealed beats enum when the cases carry different data; enum beats sealed when you need to read a name or all values")
    note("A new kind breaks every exhaustive when at compile time: that is the point")
}
