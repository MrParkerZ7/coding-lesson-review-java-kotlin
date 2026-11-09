package review.exceptions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.typeOf

/** A function that never returns normally has the return type Nothing. */
fun fail(message: String): Nothing = throw IllegalStateException(message)

/** The else branch is Nothing, a subtype of Int, so the whole if is an Int. */
fun positive(n: Int): Int = if (n > 0) n else fail("not positive: $n")

/** Returns Unit: the one value that means "nothing useful". */
fun noop() {}

/** Shows the compile-time type of the argument (typeOf adds a note about kotlin-reflect, cut off here). */
inline fun <reified T> staticType(@Suppress("UNUSED_PARAMETER") x: T): String = typeOf<T>().toString().substringBefore(" (")

/** A resource that records when it is closed. */
class Res(private val name: String, private val log: MutableList<String>) : java.io.Closeable {
    override fun close() {
        log += "close $name"
    }
}

/**
 * 15d. Result, Nothing / Unit / Any, and use { }
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.ResultAndNothingKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("RESULT, NOTHING, USE  -  exceptions as values")

    section("runCatching and Result")
    syntax(
        "runCatching { ... }" to "Result<T>: Success(value) or Failure(exception)",
        "r.getOrNull() | exceptionOrNull()" to "the value or null | the exception or null",
        "r.getOrDefault(x) | getOrElse { e -> x }" to "a fallback value, fixed or computed",
        "r.map { } | mapCatching { }" to "transform a success (mapCatching also catches inside)",
        "r.recover { e -> x }" to "turn a failure into a success",
        "r.fold(onSuccess, onFailure)" to "one value from either side",
        "r.onSuccess { } | onFailure { }" to "side effects, return r again",
        "r.getOrThrow()" to "the value, or rethrow the exception",
    )
    val ok = runCatching { "42".toInt() }
    val bad = runCatching { "x".toInt() }
    show { ok }                                                       // a Result prints as Success(value)
    show { ok.isSuccess }
    show { bad.getOrNull() }                                          // failure: null
    show { bad.exceptionOrNull()?.let { it::class.simpleName } }      // the exception object is kept
    show { bad.getOrDefault(-1) }
    show { bad.getOrElse { e -> if (e is NumberFormatException) 0 else -1 } }   // the fallback can look at the exception
    show { ok.map { it * 2 }.getOrNull() }                            // map works on the success only
    show { bad.map { it * 2 }.isFailure }                             // a failure passes through map unchanged
    show { bad.recover { -1 }.getOrNull() }                           // recover: failure becomes success
    show { bad.fold({ "got $it" }, { "failed" }) }
    show { thrown { bad.getOrThrow() } }
    val seen = mutableListOf<String>()
    ok.onSuccess { seen += "success" }
    bad.onFailure { seen += "failure" }
    show { seen }
    show { thrown { ok.map { error("inside map") } } }                // trap: map does NOT catch what its own lambda throws
    show { ok.mapCatching { error("inside map") }.isFailure }          // mapCatching does

    section("Trap: runCatching catches everything, even cancellation")
    show { runCatching { TODO() }.isFailure }                         // Errors too: it catches Throwable, not just Exception
    show { runCatching { throw CancellationException("stop") }.isFailure }   // swallowed: in coroutines this breaks cancellation
    note("In coroutine code rethrow it: onFailure { if (it is CancellationException) throw it } (lesson 17)")

    section("Nothing, Unit, Any")
    syntax(
        "Any" to "supertype of every non-null type",
        "Unit" to "one value, Unit: the 'nothing useful' result (Java void)",
        "Nothing" to "no value at all: throw, error(), TODO(), an endless loop",
        "Nothing?" to "the type of null alone",
    )
    show { noop() }                                                   // Unit is a real value you can print
    show { staticType(noop()) }
    show { staticType(null) }                                         // null alone has the type Nothing?
    show { positive(5) }
    show { thrown { positive(-5) } }                                  // the Nothing branch threw
    wontCompile("val n: Nothing = 1", "no value has the type Nothing")
    note("Nothing is a subtype of EVERY type, which is why throw / TODO() fit anywhere an expression is expected")

    section("use { } closes a Closeable: try-finally in one call")
    syntax("resource.use { r -> ... }" to "calls close() afterwards, on success AND failure; returns the block's value")
    val log = mutableListOf<String>()
    show { Res("a", log).use { log += "work"; 42 } }                  // the value of the block comes back
    show { log }                                                      // work first, then the close
    val failing = mutableListOf<String>()
    show { thrown { Res("b", failing).use { fail("boom") } } }        // the exception still propagates ...
    show { failing }                                                  // ... but close ran first
    val nested = mutableListOf<String>()
    Res("outer", nested).use { Res("inner", nested).use { nested += "work" } }
    show { nested }                                                   // closed in reverse order, like Java try-with-resources
    note("use replaces try { ... } finally { r.close() } in one call (Java: try-with-resources)")
}
