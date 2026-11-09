package review.delegation

import review.note
import review.section
import review.show
import review.syntax
import review.title
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private fun errorOf(block: () -> Unit): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName

/** A delegate: any object with getValue (and setValue for a var) can stand behind a property. */
class Trimmed(private var stored: String = "") : ReadWriteProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String = stored
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        stored = value.trim()                              // every write goes through here
    }
}

class Form {
    var name by Trimmed()                                  // `by` hands getValue / setValue to the delegate
}

class Slider {
    val log = mutableListOf<String>()
    var position: Int by Delegates.observable(0) { prop, old, new -> log.add("$old>$new") }
    var volume: Int by Delegates.vetoable(5) { _, _, new -> new in 0..10 }   // return false to reject the write
}

class User(map: Map<String, Any?>) {
    val name: String by map                                // the key is the property name
    val age: Int by map
}

class Late {
    var port: Int by Delegates.notNull()                   // like lateinit, but works for Int too
}

class Legacy {
    var oldName = "a"
    var newName by this::oldName                           // delegate to another property
}

/**
 * 18c. PROPERTY DELEGATES: observable, vetoable, notNull, by map, custom
 *      Run: mvn -q compile exec:java -Dmain.class=review.delegation.PropertyDelegatesKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("PROPERTY DELEGATES  -  by observable, vetoable, map, custom")

    section("The shape: val/var name by delegate")
    syntax(
        "var name: Type by delegate" to "reads call delegate.getValue, writes call delegate.setValue",
        "operator fun getValue(thisRef: Any?, p: KProperty<*>): T" to "required for val and var",
        "operator fun setValue(thisRef: Any?, p: KProperty<*>, v: T)" to "also required for var",
    )
    val form = Form()
    form.name = "  Ann  ";  show { form.name }            // the delegate trimmed on write
    show { form.name.length }

    section("Delegates.observable and vetoable")
    syntax(
        "Delegates.observable(init) { prop, old, new -> }" to "runs AFTER every write, cannot refuse it",
        "Delegates.vetoable(init) { prop, old, new -> Boolean }" to "runs BEFORE the write: false rejects it",
    )
    val slider = Slider()
    slider.position = 3; slider.position = 3; slider.position = 9
    show { slider.log }                                    // observable fires even when the value is equal
    slider.volume = 8;  show { slider.volume }             // accepted
    slider.volume = 99;  show { slider.volume }            // vetoed: the old value stays
    slider.volume = -1;  show { slider.volume }

    section("Delegates.notNull and by map")
    syntax(
        "var x: Int by Delegates.notNull()" to "late init for primitives; reading early throws IllegalStateException",
        "val name: String by map" to "a Map<String, Any?> is the storage",
    )
    val late = Late()
    show { errorOf { late.port } }                         // lateinit cannot do Int, this can
    late.port = 8080;  show { late.port }
    val user = User(mapOf("name" to "Kim", "age" to 30))
    show { user.name }
    show { user.age }
    val bad = User(mapOf("name" to "Kim"))
    show { errorOf { bad.age } }                           // missing key: fails at the read, not at construction
    val mutable = mutableMapOf<String, Any?>("count" to 1)
    var count: Any? by mutable                             // a var by MutableMap writes back into the map
    count = 2
    show { mutable }                                       // the write went into the map

    section("Local delegated property and delegating to another property")
    val seen = mutableListOf<Int>()
    var level by Delegates.observable(1) { _, _, new -> seen.add(new) }   // a local variable can be delegated too
    level = 5; level = 7
    show { seen }                                          // the observer ran on each write
    val legacy = Legacy()
    legacy.newName = "b";  show { legacy.oldName }         // `by this::oldName` forwards both read and write
    note("A delegate class can implement ReadWriteProperty / ReadOnlyProperty, or just have the operator functions")
    note("provideDelegate is a third operator: it runs once when the property is created (validation, registration)")
}
