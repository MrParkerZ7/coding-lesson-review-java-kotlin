package review.functions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

fun isOdd(n: Int): Boolean = n % 2 != 0

data class Tag(val name: String)

/** A nullable function parameter: call it with ?.invoke, and decide what null means. */
fun callOrMinusOne(f: ((Int) -> Int)?, x: Int): Int = f?.invoke(x) ?: -1

/** A class with `operator fun invoke` can be called like a function. */
class Adder(private val amount: Int) {
    operator fun invoke(x: Int): Int = x + amount
}

/** Each call returns a counter with its OWN captured variable. */
fun makeCounter(): () -> Int {
    var count = 0
    return { ++count }
}

/**
 * 10b. LAMBDAS: syntax, function types, references, closures
 *      Run: mvn -q compile exec:java -Dmain.class=review.functions.LambdasKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("LAMBDAS  -  syntax, function types, references, closures")

    section("Lambda syntax")
    syntax(
        "{ x: Int -> x * 2 }" to "parameters, arrow, body: the LAST expression is the result",
        "val f: (Int) -> Int = { x -> x * 2 }" to "the declared type lets you leave the parameter type out",
        "{ it * 2 }" to "exactly one parameter: the implicit name it",
        "{ _, b -> b }" to "_ for a parameter you do not use",
    )
    val twice = { x: Int -> x * 2 };   show { twice(21) }                      // the type (Int) -> Int is inferred
    val triple: (Int) -> Int = { it * 3 };   show { triple(4) }                // the declared type is where 'it' gets its type
    show { twice.invoke(5) }                                                   // f(x) is shorthand for f.invoke(x)
    val add: (Int, Int) -> Int = { a, b -> a + b };   show { add(2, 3) }
    val second: (Int, Int) -> Int = { _, b -> b };   show { second(1, 9) }
    val describe = { n: Int ->
        val sq = n * n
        "square of $n is $sq"                                                  // several statements: the last expression is the result
    }
    show { describe(3) }
    wontCompile("val f = { it * 2 }", "no expected type, so there is no implicit 'it': write { x: Int -> x * 2 }")
    wontCompile("val f: (Int) -> Int = { a, b -> a + b }", "a lambda for (Int) -> Int takes exactly one parameter")

    section("Trailing lambda")
    syntax(
        "f(a, b) { lambda }" to "the last parameter is a function: the lambda goes OUTSIDE the parentheses",
        "f { lambda }" to "...and the parentheses vanish when the lambda is the only argument",
    )
    show { listOf(1, 2, 3).map { it * 10 } }
    show { listOf(1, 2, 3).fold(0) { acc, x -> acc + x } }                     // fold(initial) { lambda }
    show { (1..3).joinToString(separator = "-") { "n$it" } }                   // named arguments first, lambda last
    note("Put the function parameter LAST in your own functions, so callers can use the trailing form")

    section("Function types")
    syntax(
        "(Int) -> Int" to "takes an Int, returns an Int",
        "(Int, Int) -> Boolean" to "two parameters",
        "() -> Unit" to "no parameters, no useful result",
        "((Int) -> Int)?" to "a NULLABLE function: the parentheses let the ? apply to the whole type",
        "(Int) -> (Int) -> Int" to "returns a function (see HigherOrder)",
    )
    val ops: Map<String, (Int, Int) -> Int> = mapOf("+" to { a: Int, b: Int -> a + b }, "*" to { a: Int, b: Int -> a * b })
    show { ops.getValue("+")(6, 3) }                                           // look a function up in a Map and call it
    show { ops["*"]?.invoke(6, 3) }                                            // Map.get returns null for a missing key, so use ?.invoke
    show { ops["^"]?.invoke(6, 3) }                                            // missing key: nothing is called
    show { callOrMinusOne({ it + 1 }, 5) }
    show { callOrMinusOne(null, 5) }                                           // a null function: ?.invoke gives null, the elvis supplies -1
    val shout = mutableListOf<String>()
    val hello: () -> Unit = { shout.add("hi") }                                // ends in a Boolean, but for () -> Unit that value is dropped
    hello()
    show { shout }
    wontCompile("val cb: ((Int) -> Unit)? = null; cb(1)", "a nullable function must be called as cb?.invoke(1)")

    section("Function references")
    syntax(
        "::name" to "a top-level function used as a value",
        "Type::member" to "a member as a function: its FIRST parameter is the receiver",
        "value::member" to "a bound reference: the receiver is already fixed",
        "::ClassName" to "a constructor used as a function",
    )
    show { listOf(1, 2, 3, 4).filter(::isOdd) }
    show { listOf("a", "bcd", "ef").map(String::length) }                      // (String) -> Int
    show { listOf("1", "x").map(String::toIntOrNull) }                         // [1, null]
    show { listOf("an", "xy").map("banana"::contains) }                        // bound: "banana" is fixed, the list items are the argument
    show { listOf("a", "b").map(::Tag) }                                       // constructor reference

    section("Closures: a lambda captures VARIABLES, not values")
    syntax("var n = 0; val inc = { n++ }" to "the lambda reads AND writes the outer var (Java lambdas cannot)")
    var count = 0
    val inc = { count++ }
    inc()
    inc()
    show { count }                                                             // 2: the lambda changed the outer variable
    var x = 1
    val read = { x }
    x = 2
    show { read() }                                                            // 2, not 1: it sees the variable as it is NOW
    val a = makeCounter()
    val b = makeCounter()
    a()
    a()
    show { a() }                                                               // 3: counter a keeps its own count
    show { b() }                                                               // 1: counter b has another one
    val perRound = mutableListOf<() -> Int>()
    for (i in 1..3) perRound.add { i }
    show { perRound.map { it() } }                                             // [1, 2, 3]: a for loop makes a NEW i each round
    var j = 0
    val shared = mutableListOf<() -> Int>()
    while (j < 3) { shared.add { j }; j++ }
    show { shared.map { it() } }                                               // [3, 3, 3]: all three closures share ONE var j

    section("Anonymous functions and invoke")
    syntax("fun(x: Int): Int { return x * 2 }" to "anonymous function: explicit types, and return leaves IT, not the caller")
    val sq = fun(x: Int): Int { return x * x };   show { sq(6) }
    show { listOf(1, 2, 3, 4).filter(fun(n) = n % 2 == 0) }                    // the parameter type is inferred from filter
    val add5 = Adder(5);   show { add5(10) }                                   // operator fun invoke: an object called like a function
    note("A bare return in a lambda leaves the ENCLOSING function (inline lambdas only): see HigherOrder")
}
