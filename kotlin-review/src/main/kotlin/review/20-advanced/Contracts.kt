package review.advanced

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private fun errorOf(block: () -> Unit): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName

fun isTextPlain(x: Any?): Boolean = x is String            // the compiler cannot see what the Boolean MEANS

@OptIn(ExperimentalContracts::class)
fun isText(x: Any?): Boolean {
    contract { returns(true) implies (x is String) }       // "if I return true, x is a String": the caller may smart cast
    return x is String
}

@OptIn(ExperimentalContracts::class)
fun isTextLiar(x: Any?): Boolean {
    contract { returns(true) implies (x is String) }       // a LIE: the compiler trusts the contract and does not check the body
    return true
}

@OptIn(ExperimentalContracts::class)
inline fun once(block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }   // "block runs exactly once, right here"
    block()
}

inline fun maybeTwice(block: () -> Unit) { block() }       // no contract: the compiler assumes nothing

fun lengthChecked(s: String?): Int {
    require(s != null)                                     // stdlib contract: returns normally only if the condition is true
    return s.length                                        // so s is smart cast to String
}

fun firstChar(s: String?): Char? {
    if (s.isNullOrEmpty()) return null                     // stdlib contract: false means s is not null
    return s[0]                                            // smart cast to String
}

/**
 * 20f. CONTRACTS: teach the compiler what a function guarantees
 *      Run: mvn -q compile exec:java -Dmain.class=review.advanced.ContractsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CONTRACTS  -  smart casts and initialization across calls")

    section("Contracts you already use")
    syntax(
        "contract { returns(true) implies (x is T) }" to "a Boolean function that proves a type or non-null",
        "contract { callsInPlace(block, EXACTLY_ONCE) }" to "a lambda parameter that runs once, here and now",
    )
    show { lengthChecked("abc") }                          // require(s != null) smart casts s to String
    show { errorOf { lengthChecked(null) } }               // and require throws when it is false
    show { firstChar("kotlin") }                           // isNullOrEmpty() smart casts in the code after it
    show { firstChar("") }
    note("Also contracts: requireNotNull, check, checkNotNull, isNullOrBlank, let, run, apply, also, with, use, repeat")

    section("A contract of our own (experimental, needs @OptIn)")
    val v: Any = "hello"
    wontCompile("if (isTextPlain(v)) v.length", "v is still Any: the compiler cannot see why the Boolean is true")
    show { if (isText(v)) v.length else -1 }               // smart cast to String after isText(v) thanks to the contract
    show { if (isText(42)) "text" else "not text" }

    section("callsInPlace: initialize a val inside a lambda")
    val n: Int
    once { n = 5 }                                         // allowed: the contract promises exactly one run
    show { n }                                             // a val assigned inside a lambda
    val m: Int
    run { m = 7 }                                          // run has the same contract in the standard library
    show { m }                                             // the same, with the standard run
    wontCompile("val k: Int; maybeTwice { k = 1 }", "without a contract the lambda might run twice: val reassignment")

    section("TRAP: the compiler trusts a contract without checking it")
    val liar: Any = 123
    show { errorOf { if (isTextLiar(liar)) liar.length } }   // liar is an Int, but the contract smart casts it to String
    note("A wrong contract does not fail to compile: it fails at run time (ClassCastException). Custom contracts are rare for this reason")
    note("Custom contracts are still experimental (@OptIn(ExperimentalContracts::class)); the stdlib ones are stable and everywhere")
}
