package review.interop

import review.note
import review.section
import review.show
import review.syntax
import review.title

/** Java needs private fields, a constructor, getters and setters. Kotlin needs this line. */
class Person(var name: String, val age: Int)

/** Java: a record, or a class with Lombok's @Data. Kotlin: a data class. */
data class Coord(val x: Int, val y: Int)

/** Java's switch needs break (or it falls through) and was a statement. Kotlin's when is an expression. */
fun dayKind(day: String): String = when (day) {
    "SAT", "SUN" -> "weekend"
    else -> "weekday"
}

/** Java: instanceof, then a cast. Kotlin: after `is`, the variable already has the new type. */
fun describe(x: Any): String = when (x) {
    is String -> "text of length ${x.length}"
    is Int -> "int plus one is ${x + 1}"
    else -> "something else"
}

/** One function with a default value instead of two overloads. */
fun greet(name: String, greeting: String = "Hello"): String = "$greeting, $name"

/** Kotlin has no `static`: constants and factories live in a companion object. */
class Config private constructor(val level: Int) {
    companion object {
        const val MAX_LEVEL = 10
        fun create(level: Int) = Config(level.coerceIn(0, MAX_LEVEL))
    }
}

/** A singleton is one keyword: `object`. */
object Registry {
    private val names = mutableListOf<String>()
    fun add(name: String): Int { names += name; return names.size }
}

fun noop() {}

/**
 * 16a. WHY KOTLIN OVER JAVA: the same ideas, side by side
 *      Run: mvn -q compile exec:java -Dmain.class=review.interop.KotlinVsJavaKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("KOTLIN vs JAVA  -  left is Java, right is Kotlin")

    section("Declarations and syntax")
    syntax(
        "final int x = 1;" to "val x = 1   (type inferred, no ;)",
        "int count = 0;" to "var count = 0",
        "Person p = new Person(\"Ann\", 30);" to "val p = Person(\"Ann\", 30)   (no new)",
        "\"Hi \" + name + \"!\"" to "\"Hi \$name!\"   (string template)",
        "a.equals(b)    |    a == b" to "a == b   |   a === b   (== is equals, === is identity)",
        "cond ? a : b" to "if (cond) a else b   (if is an expression)",
        "void f() { }" to "fun f() { }   (returns Unit)",
        "Object   |   int   |   int[]" to "Any   |   Int   |   IntArray",
    )
    val p = Person("Ann", 30)
    p.name = "Bea";  show { p.name }                                  // p.name calls getName() / setName() for you
    show { p.age }                                                    // val: only a getter exists
    show { "a" == "a" }                                               // == compares content, like Java's equals
    show { noop() }                                                   // Unit is a real value; Java's void has none

    section("Boilerplate: data class vs record / Lombok")
    syntax("record Coord(int x, int y) { }   |   @Data class" to "data class Coord(val x: Int, val y: Int)")
    show { Coord(1, 2) }                                              // toString written for you
    show { Coord(1, 2) == Coord(1, 2) }                               // equals / hashCode written for you
    show { Coord(1, 2).copy(y = 9) }                                  // copy with one field changed

    section("switch vs when, instanceof + cast vs smart cast")
    syntax(
        "switch (d) { case \"SAT\": ...; break; default: ... }" to "when (d) { \"SAT\", \"SUN\" -> ...; else -> ... }",
        "if (x instanceof String) { ((String) x).length() }" to "if (x is String) x.length      (smart cast)",
    )
    show { dayKind("SUN") }                                           // no break, no fall-through, and it returns a value
    show { dayKind("MON") }
    show { describe("kotlin") }                                       // x is already a String inside the branch
    show { describe(41) }

    section("Overloads vs default arguments, static vs companion")
    syntax(
        "String greet(String n) { return greet(n, \"Hello\"); }" to "fun greet(name: String, greeting: String = \"Hello\")",
        "static final int MAX = 10;   static Config create()" to "companion object { const val MAX = 10; fun create() }",
        "enum Singleton { INSTANCE }   |   static holder class" to "object Registry",
    )
    show { greet("Ann") }                                             // default used
    show { greet("Ann", "Hi") }                                       // default replaced
    show { greet(greeting = "Yo", name = "Bo") }                      // named arguments, any order
    show { Config.MAX_LEVEL }                                         // looks like a static field
    show { Config.create(99).level }                                  // looks like a static method; 99 is capped at 10
    show { Registry.add("a") }                                        // one instance, created on first use
    show { Registry.add("b") }

    section("Null safety, checked exceptions, extension functions")
    syntax(
        "String s = null; s.length();" to "val s: String? = null; s?.length    (checked by the compiler)",
        "try { read(); } catch (IOException e) { }  (forced)" to "no checked exceptions: catching is optional",
        "StringUtils.capitalize(s)   (static helper class)" to "s.replaceFirstChar { it.uppercase() }   (extension)",
    )
    val s: String? = null
    show { s?.length }                                                // Java would throw NullPointerException here
    show { "kotlin".replaceFirstChar { it.uppercase() } }             // an extension function added to String

    section("The same loop, Java style and Kotlin style")
    syntax(
        "for (int i = 0; i < n; i++) { }" to "for (i in 0 until n) { }",
        "for (String s : list) { }" to "for (s in list) { }",
        "list.stream().filter(x -> x % 2 == 0).mapToInt(x -> x).sum()" to "list.filter { it % 2 == 0 }.sum()",
    )
    val nums = listOf(1, 2, 3, 4, 5, 6)
    var javaStyle = 0
    for (i in 0 until nums.size) {
        if (nums[i] % 2 == 0) javaStyle += nums[i]
    }
    show { javaStyle }                                                // the loop a Java programmer writes
    show { nums.filter { it % 2 == 0 }.sum() }                        // the same result in one line
    show { javaStyle == nums.filter { it % 2 == 0 }.sum() }

    section("Interview answer: why Kotlin over Java?")
    note("Null safety in the type system: NullPointerException becomes a compile-time error, not a crash")
    note("Far less boilerplate: data class, properties, default arguments, type inference, string templates")
    note("Expressions: if, when and try return values; smart casts; extension functions; no checked exceptions")
    note("Coroutines for async code, 100% interoperable with Java: same JVM, same libraries, can be mixed file by file")
    note("Honest trade-offs: slower full builds, a bigger language to learn, and Java-facing APIs need @Jvm* annotations")
}
