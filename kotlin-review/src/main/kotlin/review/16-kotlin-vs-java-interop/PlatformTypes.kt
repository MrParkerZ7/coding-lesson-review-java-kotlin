package review.interop

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** Trusting a Java value as non-null: it compiles, and fails at THIS line when the value is null. */
fun unsafeEnvLength(): Int {
    val value: String = System.getenv("DOES_NOT_EXIST_XYZ")
    return value.length
}

/** Runs the block and reports the simple name of the exception it threw, or "no exception". */
fun exceptionName(block: () -> Any?): String =
    try {
        block()
        "no exception"
    } catch (e: Throwable) {
        e::class.simpleName ?: "?"
    }

/**
 * 16b. PLATFORM TYPES: Java values whose nullability Kotlin cannot know
 *      Run: mvn -q compile exec:java -Dmain.class=review.interop.PlatformTypesKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("PLATFORM TYPES  -  String! and the crash that compiles")

    section("A Java value has the type T!  (T or T?, nobody checked)")
    syntax(
        "val s = javaCall()" to "type String!: use it as String or String?, the compiler does not stop you",
        "val s: String? = javaCall()" to "SAFE: you declared it nullable, so you must handle null",
        "val s: String = javaCall()" to "TRUSTING: a null fails right here, at the assignment",
    )
    val env = System.getenv("DOES_NOT_EXIST_XYZ")
    show { env }                                                      // the variable does not exist: Java returns null
    show { exceptionName { env.length } }                             // compiles without ? or !!, crashes when run
    show { exceptionName { unsafeEnvLength() } }                      // declared String, so the null is caught at the assignment
    show { exceptionName { System.getProperty("no.such.key").length } }   // another Java API that returns null

    section("How to declare it safely")
    val safe: String? = System.getenv("DOES_NOT_EXIST_XYZ")
    show { safe?.length }                                             // forced to handle null, so no crash
    show { safe ?: "default" }
    show { System.getenv("DOES_NOT_EXIST_XYZ").orEmpty() }            // orEmpty() accepts a nullable receiver
    show { System.getProperty("no.such.key") ?: "none" }
    wontCompile("val len: Int = safe.length", "safe is String?: use ?. or ?: first")
    note("Rule: write the nullable type when you take a value from Java. Annotations @Nullable / @NotNull in the Java code turn String! into String? / String")
    note("Lesson 7 has ?. ?: !! in full; lesson 18 has lateinit")

    section("Mapped types: Java classes seen as Kotlin types")
    syntax(
        "java.lang.Object" to "Any",
        "int   |   Integer" to "Int   |   Int?   (boxed only when nullable or inside a generic)",
        "void" to "Unit",
        "int[]   |   Integer[]" to "IntArray   |   Array<Int>",
        "java.util.List<T>" to "List<T> (read-only view)  or  MutableList<T>",
    )
    show { Any::class.java.name }                                     // the class on the JVM
    show { Int::class.java.name }                                     // the primitive int ...
    show { Int::class.javaObjectType.name }                           // ... and the box Integer
    show { List::class.java.name }
    show { MutableList::class.java.name }                             // trap: List and MutableList are the SAME Java type
    show { String::class.java.name }
    val map = java.util.HashMap<String, Int>()
    map.put("a", 1)
    show { map.get("zz") }                                            // Kotlin maps Map.get to V?, so this one is NOT a platform type

    section("Using a Java class from Kotlin")
    val names = java.util.ArrayList<String>()
    names.add("Ann"); names.add("Bob");  show { names }               // add() is the Java method, unchanged
    show { names.size }                                               // Java's size() is seen as a property
    show { names[0] }                                                 // and get(0) as an index
    show { names.javaClass.name }                                     // kotlin.collections.ArrayList is this same class
    show { ArrayList<String>().javaClass.name }
    note("Java getters and setters are seen as properties: getName() -> name, setName(x) -> name = x, isOpen() -> isOpen")
}
