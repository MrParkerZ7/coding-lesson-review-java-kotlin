package review.interop

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile
import java.lang.reflect.Modifier

/** The class the annotations are applied to. Each effect below is checked through Java reflection. */
class Tally {
    var total = 0                           // plain property: private field + getTotal() + setTotal()
    var isReady = false                     // name starts with `is`: getter isReady(), setter setReady()
    var finished = false                    // other names: getFinished(), setFinished()

    @JvmField
    var raw = 1                             // a public field: no getter, no setter

    @JvmOverloads
    fun greet(name: String = "you", times: Int = 1, mark: String = "!"): String = (name + mark).repeat(times)

    fun plain(a: Int = 0, b: Int = 1): Int = a + b      // the same shape, without the annotation

    @JvmName("totalValue")
    fun computeTotal(): Int = total

    @Throws(java.io.IOException::class)
    fun load(): String = "loaded"

    fun loadQuietly(): String = "ok"

    companion object {
        fun helper() = "helper"                         // a method of the Companion object
        @JvmStatic fun create() = Tally()               // also a static method of Tally
        const val LIMIT = 10                            // a static final field of Tally
        @JvmField val SHARED = "shared"                 // a static field of Tally, with no getter
    }
}

/** Two extensions with the same name and an erased List parameter would clash on the JVM: @JvmName separates them. */
@JvmName("totalOfInts")
fun List<Int>.totalOf(): Int = sum()

@JvmName("totalOfStrings")
fun List<String>.totalOf(): Int = sumOf { it.length }

fun methodNames(cls: Class<*>): List<String> = cls.declaredMethods.map { it.name }

fun methodCount(cls: Class<*>, name: String): Int = cls.declaredMethods.count { it.name == name }

fun hasMethod(cls: Class<*>, name: String): Boolean = cls.methods.any { it.name == name }

fun isStaticMethod(cls: Class<*>, name: String): Boolean = Modifier.isStatic(cls.getMethod(name).modifiers)

/**
 * 16d. @JvmStatic, @JvmOverloads, @JvmField, @JvmName, @Throws: proved by reflection
 *      Run: mvn -q compile exec:java -Dmain.class=review.interop.JvmAnnotationsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("@Jvm ANNOTATIONS  -  shaping the Java view of Kotlin code")
    val tally = Tally::class.java
    val companion = Tally.Companion::class.java

    section("Properties: what Java sees")
    syntax(
        "var total = 0" to "private field total + getTotal() + setTotal(int)",
        "var isReady = false" to "isReady() + setReady(boolean)   (the `is` prefix is kept for the getter)",
        "@JvmField var raw = 1" to "a public field raw; NO getter and NO setter",
    )
    show { methodNames(tally).filter { it.endsWith("Total") }.sorted() }
    show { methodNames(tally).filter { it.endsWith("Ready") }.sorted() }   // trap: isReady / setReady, not getReady
    show { methodNames(tally).filter { it.endsWith("Finished") }.sorted() }
    show { Modifier.isPrivate(tally.getDeclaredField("total").modifiers) }   // the field behind a normal property is private
    show { Modifier.isPublic(tally.getField("raw").modifiers) }       // @JvmField makes the field itself public
    show { hasMethod(tally, "getRaw") }                               // and removes the accessors

    section("@JvmStatic and companion members")
    syntax(
        "companion object { fun f() }" to "Java calls Tally.Companion.f()",
        "companion object { @JvmStatic fun f() }" to "Java calls Tally.f(), also a static method",
        "companion object { const val X = 1 }" to "a static final field Tally.X",
        "companion object { @JvmField val X = ... }" to "a static field Tally.X, no getter",
    )
    show { hasMethod(tally, "helper") }                               // not a method of Tally at all ...
    show { hasMethod(companion, "helper") }                           // ... it lives on the Companion object
    show { isStaticMethod(tally, "create") }                          // @JvmStatic: a real static method on Tally
    show { tally.getField("LIMIT").get(null) }                        // const val: a static field, read with no instance
    show { Modifier.isStatic(tally.getField("SHARED").modifiers) }

    section("@JvmOverloads: the Java overloads a default argument needs")
    syntax("@JvmOverloads fun f(a: Int = 0, b: Int = 1)" to "Java gets f(), f(int), f(int, int)")
    show { methodCount(tally, "greet") }                              // 3 defaults: the full method + 3 shorter ones
    show { methodCount(tally, "plain") }                              // no annotation: Java sees only the full method
    show { Tally().greet() }                                          // Kotlin callers never needed the overloads
    show { Tally().greet("Ann", 2) }

    section("@JvmName and @Throws")
    syntax(
        "@JvmName(\"newName\") fun f()" to "Java sees newName(); Kotlin still calls f()",
        "@Throws(IOException::class) fun f()" to "adds `throws IOException`, so Java can catch it",
    )
    show { hasMethod(tally, "totalValue") }
    show { hasMethod(tally, "computeTotal") }                         // the Kotlin name no longer exists for Java
    show { Tally().computeTotal() }                                   // Kotlin code still uses the Kotlin name
    show { tally.getMethod("load").exceptionTypes.map { it.simpleName } }
    show { tally.getMethod("loadQuietly").exceptionTypes.map { it.simpleName } }   // Kotlin declares none by default
    show { listOf(1, 2, 3).totalOf() }                                // both totalOf extensions work in Kotlin ...
    show { listOf("ab", "c").totalOf() }
    show { hasMethod(Class.forName("review.interop.JvmAnnotationsKt"), "totalOfInts") }   // ... and have different JVM names
    wontCompile("fun List<Int>.sizeOf() = 1  and  fun List<String>.sizeOf() = 2", "same JVM signature after erasure: add @JvmName to one")
    note("Without @Throws a Java caller cannot write catch (IOException e): javac says it is never thrown")
    note("File-level @file:JvmName and the ...Kt class names: next file, FileFacade")
}
