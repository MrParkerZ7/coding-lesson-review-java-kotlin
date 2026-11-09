package review.functions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

fun applyTwice(x: Int, f: (Int) -> Int): Int = f(f(x))

fun inc(x: Int): Int = x + 1

/** What `filter` is: take a test as a parameter. */
fun <T> keepIf(xs: List<T>, test: (T) -> Boolean): List<T> {
    val kept = mutableListOf<T>()
    for (x in xs) if (test(x)) kept.add(x)
    return kept
}

/** Return a function chosen by data (the strategy pattern). */
fun operation(sign: String): (Int, Int) -> Int = when (sign) {
    "+" -> Int::plus
    "-" -> Int::minus
    "*" -> Int::times
    else -> error("unknown operator $sign")
}

fun multiplier(factor: Int): (Int) -> Int = { it * factor }

/** compose(f, g)(a) == f(g(a)): g runs FIRST. */
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a -> f(g(a)) }

/** Turn a two-parameter function into a function that takes one parameter at a time. */
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a, b) } }

fun firstNegative(xs: List<Int>): Int? {
    xs.forEach { if (it < 0) return it }           // non-local return: leaves firstNegative, not just the lambda
    return null
}

fun sumPositive(xs: List<Int>): Int {
    var total = 0
    xs.forEach { if (it < 0) return@forEach; total += it }   // return@forEach: leaves only the lambda, like continue
    return total
}

fun notInline(block: () -> Unit) = block()

/** The simple name of the exception a block throws, or null when it does not throw. */
fun errorOf(block: () -> Any?): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName

/** An inline function: its body and the lambda are copied into every call site. */
inline fun <T> logged(log: MutableList<String>, block: () -> T): T {
    log.add("start")
    val result = block()
    log.add("end")
    return result
}

fun interface Validator {
    fun check(s: String): Boolean
}

/**
 * 10c. HIGHER-ORDER FUNCTIONS: functions as parameters and results, return@label, inline
 *      Run: mvn -q compile exec:java -Dmain.class=review.functions.HigherOrderKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("HIGHER-ORDER FUNCTIONS  -  take a function, return a function")

    section("Taking a function")
    syntax(
        "fun f(g: (A) -> B)" to "a higher-order function: takes a function",
        "fun f(): (A) -> B" to "...or returns one (or both)",
    )
    show { applyTwice(3) { it * 2 } }                                          // f(f(3)) = 12
    show { applyTwice(3, ::inc) }                                              // a named function goes in with ::
    show { keepIf(listOf(1, 2, 3, 4)) { it > 2 } }                             // this is all that filter does
    show { operation("*")(6, 7) }                                              // the strategy pattern: behaviour picked by data
    show { errorOf { operation("^") } }                                       // error() throws IllegalStateException
    note("Q: What is a higher-order function?  A: one that takes a function as a parameter, returns one, or both (map, filter, fold)")

    section("Returning a function: factory, compose, currying")
    val triple = multiplier(3);   show { triple(5) }                           // the returned lambda remembers factor = 3
    val incThenDouble = compose({ x: Int -> x * 2 }, { x: Int -> x + 1 })
    show { incThenDouble(5) }                                                  // f(g(5)): g runs first, (5 + 1) * 2 = 12
    val times = curry { a: Int, b: Int -> a * b }
    show { times(4)(5) }                                                       // (A, B) -> C becomes (A) -> (B) -> C
    val times4 = times(4);   show { times4(10) }                               // partial application: the first argument is fixed

    section("return inside a lambda")
    syntax(
        "return@label value" to "leave only the lambda (the label is the function name, or name@{ ... })",
        "return value" to "inside an INLINE lambda: leave the ENCLOSING function (non-local return)",
    )
    show { firstNegative(listOf(3, -1, -5)) }                                  // the return inside forEach ended firstNegative itself
    show { firstNegative(listOf(1, 2)) }                                       // no negative: the loop ends, the function returns null
    show { sumPositive(listOf(4, -1, 5)) }                                     // return@forEach skips one element (see lesson 2)
    show { listOf(1, -2, 3).map { if (it < 0) return@map 0; it * 10 } }       // return@map gives THIS element its result
    show { listOf(1, 2, 3).map lit@{ if (it == 2) return@lit 0; it } }         // your own label: lit@{ ... return@lit }
    wontCompile("notInline { return 1 }", "return is prohibited in the lambda of a function that is not inline")

    section("inline: why return works in forEach")
    syntax("inline fun f(block: () -> T)" to "the compiler copies f's body AND the lambda into the call site: no lambda object")
    val log = mutableListOf<String>()
    val answer = logged(log) { log.add("work"); 42 }
    show { log }                                                               // [start, work, end]: the lambda ran inside the copied body
    show { answer }
    note("map, filter, forEach, let, run, apply and repeat are all inline: that is why return works inside them")
    note("Inline copies code into every caller: keep inline functions small (lesson 20: InlineFunctions)")

    section("Function values are objects: Function1, fun interface")
    syntax(
        "(A) -> R  is  Function1<A, R>" to "(A, B) -> R is Function2 ...; all extend kotlin.Function<R>",
        "fun interface Name { fun check(...) }" to "an interface with ONE method: a lambda converts to it (SAM)",
    )
    val fn: Any = ::inc
    show { fn is Function1<*, *> }                                             // a one-parameter function value is a Function1
    show { fn is Function2<*, *, *> }
    val notBlank = Validator { it.isNotBlank() }
    show { notBlank.check("  ") }                                             // false: a blank string
    val doubler = java.util.function.Function<Int, Int> { it * 2 }
    show { doubler.apply(4) }                                                  // a Java Function has apply(); Kotlin function values use invoke()
    note("Kotlin function types are not the Java functional interfaces; a lambda converts to a Java one by SAM conversion (lesson 16)")
}
