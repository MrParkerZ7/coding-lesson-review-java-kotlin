package review.classes

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

class Person(val name: String, var age: Int = 18)

/** Property initializers and init blocks run in SOURCE order; a secondary constructor body runs last. */
class InitOrder(log: MutableList<String>) {
    val first = "A".also { log.add("property first") }
    init { log.add("init block 1") }
    val second = "B".also { log.add("property second") }
    init { log.add("init block 2") }

    constructor(log: MutableList<String>, extra: Int) : this(log) {
        log.add("secondary constructor body $extra")
    }
}

/** TRAP: the init block runs before `size` is assigned, so describe() reads the default 0. */
class EarlyRead(log: MutableList<String>) {
    init { log.add("init sees ${describe()}") }
    val size: Int = 5
    fun describe(): String = "size=$size"
}

class Temperature {
    var celsius: Double = 0.0
        set(value) { field = value.coerceAtLeast(-273.15) }   // field = the hidden storage behind the property

    val fahrenheit: Double
        get() = celsius * 9 / 5 + 32                           // computed on every read: no field
}

class Counter {
    var count = 0
        private set                                            // readable outside, assignable only here

    fun increment() { count++ }
}

/** The setter calls itself: assigning the property inside its own setter never ends. */
class Endless {
    var value: Int = 0
        set(v) { value = v }
}

class Vault {
    private val secret = 42
    internal val level = 1
    val label = "vault"

    fun reveal(): Int = secret                                 // a class can use its own private members
}

/**
 * 11a. CLASS BASICS: constructors, init order, properties, visibility
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.ClassBasicsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CLASS BASICS  -  constructors, init, properties, visibility")

    section("Primary constructor")
    syntax(
        "class Name(val a: T, var b: U = d)" to "parameters that are ALSO properties; defaults allowed",
        "class Name(a: T)" to "a plain parameter: usable in init blocks and property initializers only",
        "Name(1, 2)" to "no new keyword",
    )
    val p = Person("Ann")
    show { p.name }
    show { p.age }                                                             // the default value
    p.age = 19;   show { p.age }                                               // var: has a setter
    wontCompile("p.name = \"Bob\"", "name is a val: it has no setter")
    note("Without toString a class prints as ClassName@hash: a data class gives a readable one (DataClass)")

    section("init blocks and the order of initialization")
    syntax(
        "init { ... }" to "part of the primary constructor; runs in SOURCE order with the property initializers",
        "constructor(...) : this(...) { }" to "secondary constructor: must call the primary one first",
    )
    val order = mutableListOf<String>()
    InitOrder(order, 7)
    show { order }                                                             // top to bottom, then the secondary body
    val early = mutableListOf<String>()
    EarlyRead(early)
    show { early }                                                             // TRAP: size is still 0 when the init block runs
    wontCompile("class A { init { println(size) }; val size = 5 }", "a direct read of a property before its declaration")

    section("Properties: get, set, field, private set")
    syntax(
        "val x: T  get() = expr" to "computed on every read: no backing field",
        "var x: T = v  set(value) { field = ... }" to "field is the hidden storage behind the property",
        "var x: T = v  private set" to "readable everywhere, assignable only inside the class",
    )
    val t = Temperature()
    t.celsius = 100.0;   show { t.fahrenheit }                                 // celsius * 9 / 5 + 32
    t.celsius = -500.0;   show { t.celsius }                                   // the setter clamped it to absolute zero
    val c = Counter()
    c.increment();   show { c.count }
    wontCompile("c.count = 5", "the setter is private")
    show { errorOf { Endless().value = 1 } }                                   // set(v) { value = v } calls the setter again: write to field
    note("Writing to the property name inside its own getter or setter recurses: use field")

    section("Visibility")
    syntax(
        "public (default)" to "visible everywhere (Java's default is package-private)",
        "internal" to "visible inside the same module (one Maven or Gradle project)",
        "protected" to "the class and its subclasses (NOT the package, unlike Java)",
        "private" to "the class only; at the top level of a file: that file only",
    )
    val vault = Vault()
    show { vault.reveal() }
    show { vault.label }
    wontCompile("Vault().secret", "private: visible only inside the class")
    note("Constructors can be private too: the way to force callers through a factory (ObjectAndCompanion)")
}

/** The simple name of the exception a block throws, or null when it does not throw. */
fun errorOf(block: () -> Any?): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName
