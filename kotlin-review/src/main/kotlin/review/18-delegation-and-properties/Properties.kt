package review.delegation

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

const val MAX_USERS = 100                                  // top-level const val: a compile-time constant

private fun errorOf(block: () -> Unit): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName

private var idCalls = 0
private fun nextId(): Int = ++idCalls                      // a function with a visible side effect

class Rect(val width: Int, val height: Int) {
    val area: Int get() = width * height                   // computed: no backing field, nothing stored
    val isSquare get() = width == height                   // the type is inferred from the getter
}

class Tank {
    var liters: Int = 0
        set(value) {
            require(value >= 0) { "liters must be >= 0" }  // a setter can validate
            field = value                                  // `field` is the backing field: assigning `liters` here would loop forever
        }
    var label: String = "tank"
        get() = field.uppercase()                          // a getter can transform on the way out
}

class Counter {
    var count = 0
        private set                                        // readable everywhere, writable only inside the class
    fun inc() { count++ }
}

class Ids {
    val once = nextId()                                    // initializer: runs ONCE, when the object is built
    val everyTime get() = nextId()                         // getter: runs on EVERY read
}

interface Named {
    val name: String                                       // an interface can declare a property
    val greeting: String get() = "Hello, $name"            // and give it a default getter (no field in an interface)
}

class Dog(override val name: String) : Named

class Settings {
    companion object {
        const val LIMIT = 10                               // const val inside a companion or object
    }
}

/**
 * 18a. PROPERTIES: field, getters and setters, computed values
 *      Run: mvn -q compile exec:java -Dmain.class=review.delegation.PropertiesKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("PROPERTIES  -  field, get, set, private set, const")

    section("A property is a field plus a getter plus a setter")
    syntax(
        "var name: Type = init" to "getter AND setter, generated for you",
        "val name: Type = init" to "getter only",
        "    get() { ... }  |  set(value) { field = value }" to "custom accessors; `field` is the backing field",
    )
    val tank = Tank()
    tank.liters = 40;  show { tank.liters }                // the setter ran, then field = value
    show { errorOf { tank.liters = -1 } }                  // require() failed inside the setter
    show { tank.liters }                                   // the rejected write left the old value
    show { tank.label }                                    // the getter uppercases the stored "tank"

    section("Computed property: a getter with no field")
    syntax("val area: Int get() = width * height" to "no storage: recomputed on every read")
    val r = Rect(3, 4)
    show { r.area }                                        // 12, computed now
    show { r.isSquare }
    show { Rect(5, 5).isSquare }
    show { Rect::class.java.declaredFields.map { it.name } }   // only width and height: area has NO field

    section("Initializer runs once, getter runs every time")
    val ids = Ids()
    show { ids.once }                                      // fixed when the object was built
    show { ids.once }                                      // still the same
    show { ids.everyTime }                                 // a new call each read
    show { ids.everyTime }                                 // next number: val does not mean constant

    section("private set and const val")
    syntax(
        "var x = 0; private set" to "outside code reads it but cannot assign it",
        "const val NAME = value" to "compile-time constant: top level, object or companion; only Int, String, ...",
    )
    val counter = Counter()
    repeat(2) { counter.inc() }
    show { counter.count }                                 // changed only through inc()
    wontCompile("counter.count = 5", "the setter is private")
    show { MAX_USERS }                                     // const val at top level
    show { Settings.LIMIT }                                // const val in a companion object
    wontCompile("const val list = listOf(1)", "const needs a compile-time constant of a primitive or String type")
    wontCompile("class A { const val X = 1 }", "const val is allowed only at top level, in an object or in a companion")

    section("Property in an interface")
    syntax("interface I { val name: String }" to "implementers override it, often as a constructor val")
    val dog = Dog("Rex")
    show { dog.name }                                      // overridden in the constructor
    show { dog.greeting }                                  // the interface's default getter used `name`
    wontCompile("interface I { val x: Int = 5 }", "an interface property has no backing field, so no initializer")
    note("TRAP: writing `liters = value` inside the setter of `liters` calls the setter again: StackOverflowError. Use `field`")
}
