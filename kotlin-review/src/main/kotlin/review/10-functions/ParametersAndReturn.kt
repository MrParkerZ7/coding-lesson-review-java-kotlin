package review.functions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

fun greet(name: String, greeting: String = "Hello", punctuation: String = "!"): String = "$greeting, $name$punctuation"

/** A default value may use the parameters before it. */
fun box(width: Int, height: Int = width): String = "${width}x$height"

/** The default list is built again on EVERY call (unlike Python's shared default). */
fun addTo(x: Int, into: MutableList<Int> = mutableListOf()): List<Int> {
    into.add(x)
    return into
}

fun square(x: Int) = x * x
fun isEven(n: Int): Boolean = n % 2 == 0
fun record(sink: MutableList<String>, msg: String) { sink.add(msg) }   // block body, no declared type: returns Unit
fun addOne(list: MutableList<Int>) { list.add(1) }

fun total(vararg nums: Int): Int = nums.sum()
fun kindOf(vararg nums: Int): String = nums::class.simpleName ?: "?"
fun join(vararg parts: String, sep: String): String = parts.joinToString(sep)

fun zeroFirst(vararg nums: Int): IntArray {
    nums[0] = 0
    return nums
}

fun sumOfSquares(xs: List<Int>): Int {
    fun sq(x: Int) = x * x                  // local function: only visible inside sumOfSquares
    return xs.sumOf { sq(it) }
}

fun countAbove(xs: List<Int>, limit: Int): Int {
    fun above(x: Int) = x > limit           // a local function can use the outer parameters
    return xs.count(::above)
}

fun minMax(xs: List<Int>): Pair<Int, Int> = xs.min() to xs.max()
fun stats(xs: List<Int>): Triple<Int, Int, Double> = Triple(xs.min(), xs.max(), xs.average())

fun pick(x: Int): String = "Int"
fun pick(x: Int, y: Int = 0): String = "Int + default"
fun pick(x: Any): String = "Any"

/**
 * 10a. FUNCTIONS: parameters, defaults, vararg, return values
 *      Run: mvn -q compile exec:java -Dmain.class=review.functions.ParametersAndReturnKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("FUNCTIONS  -  parameters, defaults, vararg, return values")

    section("Default and named arguments")
    syntax(
        "fun f(p: Type = default)" to "a default value: the caller may leave the argument out",
        "f(p = value)" to "named argument: any order, and the call documents itself",
    )
    show { greet("Ann") }                                                      // both defaults used
    show { greet("Ann", "Hi") }                                                // positional: the 2nd parameter is greeting
    show { greet("Ann", punctuation = "?") }                                   // skip greeting by naming the one you want
    show { greet(punctuation = ".", name = "Bob") }                            // named arguments may come in any order
    show { greet(name = "Cy", "Yo") }                                          // positional after named: only when the named one is in its own place
    show { box(3) }                                                            // a default may use an earlier parameter
    show { addTo(1) }
    show { addTo(2) }                                                          // [2], not [1, 2]: a fresh default list on every call
    wontCompile("greet(greeting = \"Hi\")", "no value for name, which has no default")
    wontCompile("greet(punctuation = \"?\", \"Ann\")", "a positional argument after a named one needs the named one in its own place")

    section("Return types: single-expression, block body, Unit")
    syntax(
        "fun f(x: Int) = expr" to "single-expression body: the return type is inferred",
        "fun f(x: Int): Int { return expr }" to "block body: write the return type",
        "fun f(x: Int) { ... }" to "block body without a type returns Unit (Java: void)",
    )
    show { square(7) }
    show { isEven(10) }
    val log = mutableListOf<String>()
    show { record(log, "saved") }                                              // Unit is a real object, printed as kotlin.Unit
    show { log }
    wontCompile("fun f(x: Int) { return x }", "a block body with no declared type returns Unit")
    wontCompile("fun fact(n: Int) = if (n <= 1) 1 else n * fact(n - 1)", "a recursive function needs its return type written")

    section("Parameters are read-only, the object is shared")
    wontCompile("fun inc(x: Int) { x = x + 1 }", "a parameter is a val: it cannot be reassigned")
    val nums = mutableListOf(0)
    addOne(nums);   show { nums }                                              // the function got the SAME list, not a copy
    note("References are passed by value: a function cannot rebind your variable, but it can change the object you share")

    section("vararg and the spread operator")
    syntax(
        "fun f(vararg xs: Int)" to "xs is an IntArray inside the function",
        "f(1, 2, 3)  |  f()" to "any number of arguments, also none",
        "f(*arr)  |  f(1, *arr, 9)" to "spread *: pass an array as separate arguments",
    )
    show { total(1, 2, 3) }
    show { total() }                                                           // zero arguments is fine
    val arr = intArrayOf(4, 5)
    show { total(*arr) }
    show { total(1, *arr, 9) }                                                 // spread mixes with plain arguments
    show { kindOf(1, 2) }                                                      // vararg Int is an IntArray (vararg String: Array<out String>)
    val original = intArrayOf(1, 2)
    zeroFirst(*original);   show { original }                                  // [1, 2]: the spread passed a COPY (Java passes the array itself)
    show { join("a", "b", "c", sep = "-") }                                    // a parameter after a vararg must be named
    show { listOf(*arrayOf(1, 2)) }                                            // an Array<Int> spreads into listOf's vararg
    wontCompile("listOf(*intArrayOf(1, 2))", "an IntArray spreads only into 'vararg Int', not into a generic vararg T")

    section("Local functions")
    syntax("fun outer() { fun helper(x: Int) = ...; ... }" to "a function inside a function: sees the outer parameters")
    show { sumOfSquares(listOf(1, 2, 3)) }
    show { countAbove(listOf(1, 5, 9), 4) }                                    // above() reads the outer parameter limit
    note("Typical use: a recursive helper (DFS) that sees the state instead of passing it down every call")

    section("Returning several values")
    syntax(
        "fun f(): Pair<A, B> = a to b" to "two results",
        "fun f(): Triple<A, B, C>" to "three results; more than that: a data class (lesson 11)",
        "val (x, y) = f()" to "destructuring calls component1(), component2() ...",
    )
    show { minMax(listOf(3, 1, 2)) }
    val (lo, hi) = minMax(listOf(3, 1, 2));   show { "$lo..$hi" }
    val (_, _, mean) = stats(listOf(1, 2, 6));   show { mean }                 // _ skips a component
    show { stats(listOf(1, 2, 6)) }

    section("Overloads and defaults")
    syntax("fun pick(x: Int)  |  fun pick(x: Any)" to "same name, different parameters: the most specific candidate wins")
    show { pick(1) }                                                           // pick(x: Int) beats pick(x: Int, y = 0): no default needed
    show { pick(1, 2) }
    show { pick(1L) }                                                          // a Long is never narrowed to Int: only pick(Any) fits
    show { pick("s") }
    note("Prefer ONE function with defaults to a pile of overloads; Java callers need @JvmOverloads (lesson 16)")
}
