package review.classes

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

val initEvents = mutableListOf<String>()                   // records when LazyDemo is created

object IdGenerator {
    private var next = 1
    fun nextId(): Int = next++
}

object LazyDemo {
    init { initEvents.add("LazyDemo created") }
    fun touch(): String = "touched"
}

interface Describer { fun describe(): String }

object English : Describer {                               // an object can implement interfaces
    override fun describe(): String = "hello"
}

class Pizza private constructor(val name: String, val price: Int) {
    companion object {
        const val BASE_PRICE = 8
        fun margherita(): Pizza = Pizza("margherita", BASE_PRICE)
        fun fromName(name: String): Pizza = Pizza(name, BASE_PRICE + 2)
    }
}

class Shop {
    val sign = "open"

    companion object Factory {                             // a named companion
        fun create(): Shop = Shop()
    }
}

fun hasMethod(type: Class<*>, name: String): Boolean = type.methods.any { it.name == name }

/**
 * 11d. OBJECT, COMPANION OBJECT, OBJECT EXPRESSIONS
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.ObjectAndCompanionKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("OBJECT AND COMPANION  -  singletons, Kotlin's static, anonymous objects")

    section("object: a singleton")
    syntax(
        "object Name { ... }" to "a class with exactly ONE instance, created on first use",
        "Name.member" to "no constructor call, no new",
        "object Name : Interface { ... }" to "an object can implement interfaces or extend a class",
    )
    show { IdGenerator.nextId() }                                              // 1
    show { IdGenerator.nextId() }                                              // 2: the one instance keeps its state
    initEvents.add("before")
    LazyDemo.touch()
    initEvents.add("after")
    show { initEvents }                                                        // LazyDemo was created at its first use, not at program start
    show { English.describe() }
    wontCompile("IdGenerator()", "an object has no constructor: its name already IS the instance")

    section("companion object: Kotlin's replacement for static")
    syntax(
        "class A { companion object { fun make() = A() } }" to "call it as A.make(): a member of the class, not of an instance",
        "companion object { const val X = 1 }" to "a compile-time constant (only primitives and String)",
        "class A private constructor(...)" to "callers must go through the factory",
        "companion object Factory { ... }" to "a named companion: A.Factory.make()",
    )
    show { Pizza.margherita().name }
    show { Pizza.BASE_PRICE }
    show { Pizza.fromName("hawaii").price }                                    // BASE_PRICE + 2
    show { Pizza.Companion.fromName("x").price }                               // Pizza.Companion is a real object: the default name
    show { Shop.create().sign }
    show { Shop.Factory.create().sign }                                        // the same, spelled with the companion's name
    wontCompile("Pizza(\"x\", 1)", "the constructor is private")
    show { hasMethod(Pizza::class.java, "margherita") }                        // false: not a method of Pizza itself
    show { hasMethod(Pizza.Companion::class.java, "margherita") }              // true: it lives in the companion object
    note("So Java must call Pizza.Companion.margherita() unless you add @JvmStatic (lesson 16)")
    note("A helper that needs no class state is a top-level function in the file, not a companion member")

    section("object expressions: anonymous objects")
    syntax(
        "object : Type { ... }" to "an instance of an anonymous implementation, made on the spot",
        "object { val x = 1 }" to "an ad-hoc object that just holds a few values",
    )
    val byLength = object : Comparator<String> {
        override fun compare(a: String, b: String): Int = a.length.compareTo(b.length)
    }
    show { listOf("ccc", "a", "bb").sortedWith(byLength) }
    val anon = object : Describer {
        override fun describe(): String = "anonymous"
    }
    show { anon.describe() }
    var clicks = 0
    val listener = object : Runnable { override fun run() { clicks++ } }
    listener.run()
    listener.run()
    show { clicks }                                                            // 2: like a lambda, it can change an outer var
    val origin = object { val x = 0; val y = 0 }
    show { origin.x + origin.y }                                               // fine as a local; a public function returning it hides x (type Any)
    note("One abstract method: a lambda or SAM conversion is shorter. Several methods or its own state: object expression")
    note("object Name is ONE named instance for the whole program; object : Type { } makes a new anonymous instance each time it runs")
}
