package review.exceptions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** A custom exception that carries data. */
class InsufficientFundsException(val missing: Int) : Exception("missing $missing")

/** Returns 0, but the compiler cannot see it: `x / 0` written literally gets a compile-time warning. */
fun zero() = 0

/** Returns null, hidden from the compiler for the same reason. */
fun nothing(): String? = null

/**
 * Runs the block and reports the simple class name of whatever it threw, or "no exception".
 * It catches Throwable on purpose, so that Errors (StackOverflowError ...) show up too. Used by every file of this lesson.
 */
fun thrown(block: () -> Any?): String =
    try {
        block()
        "no exception"
    } catch (e: Throwable) {
        e::class.simpleName ?: "?"
    }

fun parseOrMinus(s: String): Int = try { s.toInt() } catch (e: NumberFormatException) { -1 }

/** NumberFormatException is an IllegalArgumentException, so the FIRST matching catch takes it. */
fun rightOrder(block: () -> Unit): String =
    try { block(); "ok" } catch (e: IllegalArgumentException) { "IllegalArgument" } catch (e: Exception) { "Exception" }

/** The same two catches in the wrong order: the general one hides the specific one. Kotlin does not stop you. */
fun wrongOrder(block: () -> Unit): String =
    try { block(); "ok" } catch (e: Exception) { "Exception" } catch (e: IllegalArgumentException) { "IllegalArgument" }

fun withFinally(log: MutableList<String>, fail: Boolean): String {
    try {
        log += "try"
        if (fail) throw IllegalStateException("boom")
        return "returned"
    } catch (e: IllegalStateException) {
        log += "catch"
        return "caught"
    } finally {
        log += "finally"
    }
}

fun withdraw(balance: Int, amount: Int): Int {
    if (amount > balance) throw InsufficientFundsException(amount - balance)
    return balance - amount
}

fun describeWithdraw(balance: Int, amount: Int): String =
    try { "left ${withdraw(balance, amount)}" } catch (e: InsufficientFundsException) { "short by ${e.missing}" }

/** throw is an expression of type Nothing, so it fits on the right of ?: */
fun mustBeNumber(text: String): Int = text.toIntOrNull() ?: throw IllegalArgumentException("not a number: $text")

/**
 * 15a. TRY, CATCH, FINALLY, THROW
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.TryCatchKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("TRY / CATCH / FINALLY  -  try is an expression")

    section("try as an expression")
    syntax(
        "val v = try { risky() } catch (e: Type) { fallback }" to "the value of try (or of the catch that ran)",
        "try { ... } catch (e: A) { ... } finally { ... }" to "finally always runs; its value is ignored",
    )
    show { parseOrMinus("42") }                                       // no exception: the try block's value
    show { parseOrMinus("4x") }                                       // NumberFormatException: the catch block's value
    val trace = mutableListOf<String>()
    show { try { 1 } finally { trace += "finally ran" } }             // finally runs, but its value is never the result
    show { thrown { throw java.io.IOException("disk") } }             // a Java checked exception, thrown with no declaration
    note("Kotlin has no checked exceptions: nothing forces you to catch, and there is no `throws` clause")

    section("catch order: the first match wins")
    show { rightOrder { "x".toInt() } }                               // NumberFormatException is an IllegalArgumentException
    show { rightOrder { error("x") } }                                // IllegalStateException is only an Exception
    show { wrongOrder { "x".toInt() } }                               // trap: Exception first, so the specific catch is dead code
    wontCompile("catch (e: IllegalStateException | IllegalArgumentException)", "Kotlin has no multi-catch: use `is` inside one catch")

    section("finally always runs, even after return")
    val ok = mutableListOf<String>()
    val bad = mutableListOf<String>()
    show { withFinally(ok, false) }                                   // the return value is kept
    show { ok }                                                       // finally ran after the return statement
    show { withFinally(bad, true) }
    show { bad }                                                      // try, catch, finally: always in this order

    section("Custom exceptions and throw")
    syntax(
        "class MyException(val data: Int) : Exception(\"text\")" to "extend Exception (or RuntimeException) to carry data",
        "throw MyException(5)" to "throw is an expression of type Nothing",
        "x ?: throw SomeException(...)" to "a guard that also gives the non-null value",
    )
    show { describeWithdraw(100, 30) }
    show { describeWithdraw(10, 25) }                                 // the custom exception carried the missing amount
    show { runCatching { withdraw(10, 25) }.exceptionOrNull()?.message }   // message set by the constructor
    show { mustBeNumber("7") }
    show { thrown { mustBeNumber("seven") } }
    note("Interview answer: Kotlin exceptions are all unchecked; try is an expression; throw has type Nothing")
}
