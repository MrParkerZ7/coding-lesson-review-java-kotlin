package review.variables

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

const val MAX_USERS = 100                          // top-level const val: a compile-time constant

object Limits {
    const val TIMEOUT_SECONDS = 30                 // const val is also allowed inside an object
}

/** A tiny mutable holder: the property is a var, so the object can change even when the reference is a val. */
class Counter(var hits: Int)

/**
 * 1a. VAL AND VAR: read-only vs changeable, type inference, const val
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.ValAndVarKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("VAL AND VAR  -  read-only vs changeable, and type inference")

    section("val and var")
    syntax(
        "val name: Type = value" to "read-only: assigned once (like final in Java)",
        "var name: Type = value" to "changeable: can be assigned again",
        "val name = value" to "the type is inferred from the value",
    )
    val i = 42;  show { i }                                   // type inferred: Int
    val big = 3_000_000_000;  show { big }                    // too big for an Int, so the literal is inferred as Long
    val ratio = 1.5;  show { ratio }                          // decimals are Double by default
    val letter = 'k';  show { letter }                        // single quotes: Char
    val exact: Long = 42;  show { exact }                     // explicit type: the literal 42 becomes a Long
    var score = 10;  show { score }
    score += 5;  show { score }                               // a var can change its value
    wontCompile("i = 43", "val cannot be reassigned")
    wontCompile("score = \"ten\"", "a var keeps its type: an Int variable cannot hold a String")
    note("Prefer val; use var only when the value really changes (loops, accumulators)")

    section("val is NOT immutability")
    syntax("val list = mutableListOf(...)" to "val fixes the REFERENCE; the object behind it may still change")
    val list = mutableListOf(1)
    list.add(2);  show { list }                               // the val still points at the same list, which grew
    val counter = Counter(0)
    counter.hits++;  show { counter.hits }                    // a var property inside a val object can change
    wontCompile("list = mutableListOf(9)", "the reference is a val: it cannot point to another list")
    note("For a list that cannot change, use a read-only List type (lesson 12), not just val")

    section("const val")
    syntax("const val NAME = value" to "compile-time constant: top level or in an object; only primitives and String")
    show { MAX_USERS }                                        // inlined by the compiler at every use
    show { Limits.TIMEOUT_SECONDS }
    wontCompile("fun f() { const val x = 5 }", "const val is not allowed on a local variable")
    wontCompile("const val ITEMS = listOf(1)", "only primitives and String can be const")
    wontCompile("const val NOW = System.currentTimeMillis()", "a const needs a value known at compile time")

    section("Deferred initialization: assign exactly once")
    syntax("val name: Type" to "declare now, assign later; the type is required")
    val label: String
    if (i > 40) label = "big" else label = "small"
    show { label }                                            // every path assigns it exactly once
    wontCompile("val n: Int; n = 1; n = 2", "a val can be assigned only once")
    wontCompile("val n: Int; val m = n + 1", "reading it before assigning is an error")
    wontCompile("val n", "no type and no value: the compiler cannot infer anything")

    section("The declared type: Any and Int?")
    var anything: Any = 42;  show { anything }                // Any can hold a value of every type
    anything = "text";  show { anything }                     // still an Any variable; the VALUE's type changed
    val maybe: Int? = null;  show { maybe }                   // Int? may hold null; null tools are lesson 7
    wontCompile("val n: Int = null", "Int cannot hold null: declare Int?")

    section("Shadowing: an inner name hides an outer one")
    val level = 1
    show { val level = 2; level }                             // legal: the inner level hides the outer one
    show { level }                                            // the outer val was never touched
    wontCompile("val a = 1; val a = 2", "the same name twice in one scope is an error")
    note("Shadowing compiles without an error (IDEs flag it as name shadowed): avoid it, it hides bugs in long functions")

    section("Destructuring declarations")
    syntax("val (a, b) = pair" to "takes component1(), component2(), ...; use _ to skip one")
    val pair = Pair("age", 30)
    val (key, value) = pair;  show { key }                    // a Pair gives first and second
    show { value }
    val (q, r) = 17 / 5 to 17 % 5;  show { q }                 // quotient and remainder in one go
    show { r }
    val (_, _, third) = listOf(1, 2, 3);  show { third }      // a List gives component1..component5; _ skips one
    var a = 1;  var b = 2
    a = b.also { b = a };  show { listOf(a, b) }              // swap without a temp: also returns b, then sets b to the old a

    section("Naming")
    syntax(
        "val userName | fun loadUser()" to "camelCase for variables, parameters, functions, properties",
        "const val MAX_USERS" to "UPPER_SNAKE_CASE for constants",
        "class UserAccount" to "PascalCase for classes, objects, interfaces",
    )
    note("No new, no semicolons; the type comes after the name: val count: Int")
    note("Int is a primitive on the JVM when it cannot be null, and a boxed Integer when it is Int? or in a List")
}
