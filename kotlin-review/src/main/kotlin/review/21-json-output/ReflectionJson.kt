package review.json

import java.lang.reflect.Modifier
import review.block
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** A class with a property that is computed, not stored: reflection over FIELDS cannot see it. */
class Rect(val w: Int, val h: Int) {
    val area: Int get() = w * h
}

/** A companion object is a STATIC field of the class: reflection sees it. */
class WithCompanion(val a: Int) {
    companion object {
        const val LIMIT = 10
        val NAME = "x"
    }
}

/** A lazy property is stored as a hidden Lazy field. */
class Cached {
    val big: Int by lazy { 5 }
}

/** A node that points back at itself: a cycle. */
class Node(val name: String) {
    var next: Node? = null
}

/** Any value to a tree of Maps, Lists and plain values (what toJson understands). Plain objects are read field by field. */
fun toTree(value: Any?): Any? = when (value) {
    null, is Boolean, is Number, is String, is Char -> value
    is Enum<*> -> value.name
    is Map<*, *> -> value.entries.associate { (k, v) -> k.toString() to toTree(v) }
    is Iterable<*> -> value.map { toTree(it) }
    is Array<*> -> value.map { toTree(it) }
    is IntArray -> value.toList()
    else -> objectToMap(value)
}

/** Read every non-static field of a plain object with Java reflection. */
fun objectToMap(obj: Any): Map<String, Any?> {
    val result = LinkedHashMap<String, Any?>()
    for (field in obj.javaClass.declaredFields) {
        if (Modifier.isStatic(field.modifiers) || field.isSynthetic) continue   // skip Companion and compiler fields
        field.isAccessible = true                                               // the fields are private
        result[field.name] = toTree(field.get(obj))
    }
    return result
}

/**
 * 21c. JSON FROM ANY OBJECT BY REFLECTION  (no library)
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.ReflectionJsonKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("JSON BY REFLECTION  -  one function for every class")

    section("Idea: object -> Map -> toJson")
    syntax(
        "obj.javaClass.declaredFields" to "every field the class declares, private ones too",
        "field.isAccessible = true" to "needed to read a private field",
        "field.get(obj)" to "the value; recurse into it when it is another object",
    )
    val user = sampleUser()
    show { objectToMap(user).keys.toList() }                                   // the property names, in declaration order
    show { toJson(toTree(user)) }                                              // the same text as the hand-written User.asMap version
    show { toJson(toTree(listOf(user.address, null))) }                        // lists of objects work, null stays null
    block(toPrettyJson(toTree(user)))

    section("Traps")
    show { toJson(toTree(Rect(3, 4))) }                                        // area is missing: it has a getter but no field
    show { Rect(3, 4).area }                                                   // ... although the property exists
    show { runCatching { toTree(java.time.LocalDate.of(2026, 11, 9)) }.exceptionOrNull()?.javaClass?.simpleName }   // JDK classes are sealed off since Java 17
    val loop = Node("a")
    loop.next = loop
    show { runCatching { toTree(loop) }.exceptionOrNull()?.javaClass?.simpleName }   // a cycle recurses forever: an Error, not an Exception
    note("Field order comes from the JVM: in practice it is declaration order, but the language does not promise it")
    show { toJson(toTree(WithCompanion(1))) }                                  // the static Companion field was skipped on purpose
    show { Cached::class.java.declaredFields.map { it.name } }                 // by lazy stores a hidden Lazy field, named after the property
    show { runCatching { toTree(Cached()) }.exceptionOrNull()?.javaClass?.simpleName }   // the Lazy object holds a reference to itself: endless recursion
    note("Statics are skipped, but a by lazy property leaks its delegate field and breaks the walk")
    note("Reflection is a trick for the interview board: in real code use a library (LibraryJson)")
}
