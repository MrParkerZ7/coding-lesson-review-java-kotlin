package review.advanced

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** inline: the body AND the lambda are pasted into the caller, so no Function object is created. */
inline fun <T> withLog(events: MutableList<String>, name: String, block: () -> T): T {
    events.add("start $name")
    try {
        return block()
    } finally {
        events.add("end $name")
    }
}

/** A hand-made forEach: because it is inline, a `return` inside its lambda leaves the CALLER. */
inline fun eachItem(items: List<Int>, action: (Int) -> Unit) {
    for (item in items) action(item)
}

fun firstEven(items: List<Int>): Int? {
    eachItem(items) { if (it % 2 == 0) return it }         // non-local return: leaves firstEven, not just the lambda
    return null
}

/** noinline: this one lambda may be stored or passed on as a value. */
inline fun keepAndRun(noinline saved: () -> String, store: MutableList<() -> String>, plain: () -> String): String {
    store.add(saved)                                       // allowed only because `saved` is noinline
    return plain()
}

/** crossinline: the lambda runs later, in another context, so it may not `return` from the caller. */
inline fun later(crossinline action: () -> String): () -> String = { action() }

/** An inline property: only the accessors are inlined (the property has no backing field). */
inline val List<Int>.secondOrZero: Int get() = getOrNull(1) ?: 0

/**
 * 20a. INLINE, NOINLINE, CROSSINLINE
 *      Run: mvn -q compile exec:java -Dmain.class=review.advanced.InlineFunctionsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("INLINE FUNCTIONS  -  inline, noinline, crossinline")

    section("inline: paste the body and the lambda into the caller")
    syntax(
        "inline fun f(block: () -> T)" to "no Function object, no call: the code is copied to each call site",
        "noinline block: () -> T" to "keep ONE lambda a real object (store it, pass it on)",
        "crossinline block: () -> T" to "lambda runs in another context: non-local return is banned",
    )
    val events = mutableListOf<String>()
    show { withLog(events, "job") { 6 * 7 } }              // the lambda's result comes back through the inline function
    show { events }                                        // finally ran after the block
    note("Why: a normal lambda is an object, and a capturing one is allocated on every call. Inlined, there is none")
    note("The standard library does this for map, filter, forEach, let, run, apply, also, use, repeat, measureTimeMillis")

    section("Non-local return")
    show { firstEven(listOf(1, 3, 4, 6)) }                 // return it jumped out of firstEven from inside the lambda
    show { firstEven(listOf(1, 3)) }
    val trace = mutableListOf<String>()
    fun early(): Int { withLog(trace, "x") { return 5 }; return 0 }
    show { early() }                                       // returns 5 from early(), not 0
    show { trace }                                         // and finally still ran: "end x" is there
    wontCompile("val g = { return 1 }", "a lambda that is a plain object cannot return from the function around it")
    note("A normal (non-inline) lambda can only return@label to itself; forEach, map, let, run ... are inline, so a plain return works there")

    section("noinline and crossinline")
    val store = mutableListOf<() -> String>()
    show { keepAndRun({ "kept" }, store, { "plain" }) }    // runs `plain` inline
    show { store.size }                                    // `saved` was kept as an object
    show { store[0]() }                                    // and can be called later
    val delayed = later { "ran later" }
    show { delayed() }                                     // a crossinline lambda used inside another lambda
    wontCompile("inline fun bad(block: () -> Unit) { store.add(block) }", "an inline lambda is not an object: mark it noinline to store it")
    wontCompile("later { return \"x\" }", "crossinline forbids a non-local return: use return@later")
    val labelled = later { return@later "labelled" }
    show { labelled() }                                    // return@later is fine: it returns from the lambda only

    section("inline property accessors and when NOT to inline")
    syntax("inline val Type.name: R get() = ...  |  inline var" to "inline the getter (and setter); no backing field allowed")
    show { listOf(10, 20, 30).secondOrZero }
    show { listOf(10).secondOrZero }
    note("Do NOT inline a big function: its body is copied to every call site and the bytecode grows")
    note("Inline only functions that TAKE A LAMBDA: without one the compiler warns \"performance impact from inlining is insignificant\"")
    note("An inline function cannot be recursive, and a public one cannot touch private members (@PublishedApi is the escape)")
    note("reified type parameters need inline: see lesson 19 ErasureAndReified")
}
