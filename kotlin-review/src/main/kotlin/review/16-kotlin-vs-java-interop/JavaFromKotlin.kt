package review.interop

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile
import java.time.LocalDate
import java.util.function.IntUnaryOperator

/**
 * 16c. CALLING JAVA FROM KOTLIN: statics, SAM, java.time, Class objects
 *      Run: mvn -q compile exec:java -Dmain.class=review.interop.JavaFromKotlinKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CALLING THE JDK FROM KOTLIN  -  no Java source needed")

    section("Java static members: call them on the class")
    syntax("ClassName.member(...)" to "static methods and fields look the same as in Java")
    show { Math.max(3, 9) }                                           // java.lang.Math, imported automatically
    show { Integer.MAX_VALUE }                                        // Kotlin's own Int.MAX_VALUE is preferred
    show { Character.isDigit('7') }                                   // Kotlin: '7'.isDigit()
    show { Integer.toBinaryString(10) }
    show { Integer.compare(3, 5) }                                    // -1: negative when the first is smaller
    show { Math.floorMod(-7, 3) }                                     // the result takes the sign of the divisor: 0..2 ...
    show { -7 % 3 }                                                   // ... but Kotlin's % keeps the sign of the left side
    note("Prefer the Kotlin API when there is one (Int.MAX_VALUE, c.isDigit(), list.sorted()); use the Java one when there is not")

    section("SAM conversion: a lambda where Java wants a one-method interface")
    syntax(
        "InterfaceName { params -> body }" to "builds an object of a Java functional interface",
        "javaFunction { params -> body }" to "or pass the lambda straight to a function that takes one",
    )
    val log = mutableListOf<String>()
    val task = Runnable { log += "ran" }
    task.run()
    show { log }                                                      // Runnable { } built from a lambda
    val byLength = Comparator<String> { a, b -> a.length - b.length }
    show { listOf("ccc", "a", "bb").sortedWith(byLength) }            // a Java Comparator from a lambda
    show { listOf("ccc", "a", "bb").sortedWith { a, b -> b.length - a.length } }   // or pass the lambda directly
    val twice = IntUnaryOperator { it * 2 }
    show { twice.applyAsInt(21) }                                     // java.util.function.* interfaces work the same way

    section("java.time: a fixed date keeps the output stable")
    val date = LocalDate.of(2024, 2, 28)
    show { date }                                                     // a Java object printed by its toString()
    show { date.plusDays(2) }                                         // 2024 is a leap year: Feb 29 exists
    show { date.year }                                                // getYear() seen as a property
    show { date.dayOfWeek }
    show { date.isLeapYear }                                          // isLeapYear() seen as a property
    show { exceptionName { LocalDate.of(2023, 2, 29) } }              // Java exceptions are plain unchecked ones in Kotlin

    section("Class objects: ::class vs ::class.java")
    syntax(
        "String::class" to "a Kotlin KClass: works on every Kotlin type",
        "String::class.java" to "the java.lang.Class: what Java APIs and reflection want",
        "value.javaClass" to "the same as value::class.java",
    )
    show { String::class.qualifiedName }                              // the Kotlin type ...
    show { String::class.java.name }                                  // ... and the Java class behind it
    show { "text".javaClass.simpleName }
    show { "text"::class.java == String::class.java }                 // both ways give the same Class object

    section("Kotlin's String is java.lang.String: the Java names are not all there")
    show { "Hello".equals("hello", ignoreCase = true) }               // Kotlin's name for equalsIgnoreCase
    wontCompile("\"abc\".equalsIgnoreCase(\"ABC\")", "no such method on kotlin.String: use equals(other, ignoreCase = true)")
    wontCompile("\"abc\".charAt(0)", "no such method on kotlin.String: use s[0]")
    wontCompile("\"abc\".toUpperCase()", "deprecated at ERROR level: use uppercase()")
    show { "abc"[0] }
    show { "abc".uppercase() }
}
