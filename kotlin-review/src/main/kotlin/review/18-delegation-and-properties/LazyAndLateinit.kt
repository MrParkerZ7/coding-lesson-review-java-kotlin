package review.delegation

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

private fun errorOf(block: () -> Unit): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName

class Report {
    var builds = 0                                         // counts how many times the lazy block ran
    val summary: String by lazy { builds++; "report#$builds" }
}

class Flaky {
    var tries = 0
    val value: Int by lazy { tries++; check(tries >= 2) { "not yet" }; 42 }   // fails on the first try
}

class Service {
    lateinit var client: String                            // set later (injection, setUp), not in the constructor
    fun connect() { client = "connected" }
    fun ready(): Boolean = this::client.isInitialized      // allowed only for a lateinit declared in a reachable scope
}

/**
 * 18b. lazy AND lateinit: two ways to initialize late
 *      Run: mvn -q compile exec:java -Dmain.class=review.delegation.LazyAndLateinitKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("lazy AND lateinit  -  initialize late")

    section("by lazy: the block runs once, on first access")
    syntax(
        "val name: Type by lazy { compute }" to "val only; computed on first read, then cached",
        "lazy(LazyThreadSafetyMode.X) { ... }" to "SYNCHRONIZED (default) | PUBLICATION | NONE",
    )
    val report = Report()
    show { report.builds }                                 // 0: declaring it did not run the block
    show { report.summary }                                // first read runs the block
    show { report.summary }                                // cached: same value, no second run
    show { report.builds }                                 // still 1: the block ran exactly once

    section("Thread-safety modes and isInitialized")
    syntax(
        "SYNCHRONIZED" to "default: one thread computes, others wait; the value is computed once",
        "PUBLICATION" to "several threads may compute; the first result wins",
        "NONE" to "no locking: fastest, only safe in a single thread",
    )
    val box = lazy(LazyThreadSafetyMode.NONE) { 6 * 7 }
    show { box.isInitialized() }                           // Lazy<T> itself can tell: not computed yet
    show { box.value }                                     // compute now
    show { box.isInitialized() }
    val flaky = Flaky()
    show { errorOf { flaky.value } }                       // the block threw
    show { flaky.value }                                   // TRAP: a failed lazy is NOT cached: the block runs again
    show { flaky.tries }                                   // 2 runs: the first failed, the second succeeded
    wontCompile("var x: Int by lazy { 1 }", "lazy is read-only: it has getValue but no setValue")

    section("lateinit var: promise to set it before the first read")
    syntax(
        "lateinit var name: Type" to "var only, non-null, not Int/Long/Boolean/..., no initializer",
        "this::name.isInitialized" to "ask whether it has been set",
    )
    val service = Service()
    show { service.ready() }                               // not set yet
    show { errorOf { service.client } }                    // read too early
    service.connect()
    show { service.client }                                // set by connect()
    show { service.ready() }
    lateinit var local: String                             // lateinit also works for a local variable
    local = "hi"
    show { local }                                         // a local lateinit var works too
    wontCompile("service::client.isInitialized", "the backing field must be accessible: ask from inside the class")
    wontCompile("lateinit val s: String", "lateinit needs a var")
    wontCompile("lateinit var n: Int", "not allowed on primitive types: use Delegates.notNull")
    wontCompile("lateinit var s: String?", "not allowed on nullable types: null is already 'not set'")

    section("lateinit vs lazy")
    syntax(
        "lateinit var x: T" to "a var that SOMEONE ELSE sets, any time; read before that: UninitializedPropertyAccessException",
        "val x: T by lazy { ... }" to "a val that COMPUTES ITSELF on first read and caches it; can never be read too early",
        "var x: Int by Delegates.notNull()" to "the lateinit for Int and other primitives (IllegalStateException when unset)",
    )
    note("Q: lateinit vs lazy?  lateinit = a var that someone ELSE fills (DI, setUp); lazy = a val that computes ITSELF once on demand")
    note("lateinit has no cost per read beyond a null check; lazy keeps a Lazy object and (by default) a lock")
}
