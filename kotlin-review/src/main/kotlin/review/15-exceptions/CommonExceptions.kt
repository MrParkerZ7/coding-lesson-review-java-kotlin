package review.exceptions

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** Always "text", but typed Any: casts of it cannot be judged at compile time. */
fun mystery(): Any = "text"

/** Typed Throwable, so the compiler lets us ask whether an Error is an Exception. */
fun anError(): Throwable = StackOverflowError()

/** Recursion with no end: every call waits for the next one, so the stack runs out. */
fun endless(n: Int): Int = endless(n + 1) + 1

/**
 * 15b. THE EXCEPTIONS YOU WILL MEET, WITH THE CODE THAT TRIGGERS EACH
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.CommonExceptionsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("COMMON EXCEPTIONS  -  the trigger and the class name")

    section("The family tree")
    syntax(
        "Throwable" to "root: everything you can throw",
        "  Exception" to "what you normally catch (RuntimeException and the rest)",
        "  Error" to "serious trouble: StackOverflowError, OutOfMemoryError, NotImplementedError (TODO)",
    )
    show { NumberFormatException::class.java.superclass.simpleName }   // a subclass: catching the parent catches it
    show { IllegalArgumentException::class.java.superclass.simpleName }
    show { ArrayIndexOutOfBoundsException::class.java.superclass.simpleName }
    show { anError() is Exception }                                    // false: catch (e: Exception) does NOT stop it
    show { anError() is Error }
    wontCompile("StackOverflowError() is Exception", "the compiler knows an Error can never be an Exception")

    section("Arithmetic: Int divides by zero with an exception, Double does not")
    show { thrown { 10 / zero() } }                                    // Int / 0 throws
    show { thrown { 10 % zero() } }                                    // so does Int % 0
    show { 10.0 / zero() }                                             // Double / 0 is Infinity
    show { -10.0 / zero() }
    show { 0.0 / zero() }                                              // 0.0 / 0 is NaN, still no exception
    show { thrown { Math.addExact(Int.MAX_VALUE, 1) } }                // overflow normally wraps silently; addExact throws

    section("NumberFormatException: toInt() on bad text")
    show { thrown { "abc".toInt() } }
    show { thrown { "".toInt() } }                                     // empty text is bad too
    show { thrown { " 7".toInt() } }                                   // a space is bad: toInt does not trim
    show { thrown { "2147483648".toInt() } }                           // one past Int.MAX_VALUE
    show { "abc".toIntOrNull() }                                       // the safe form returns null (lesson 7)

    section("IndexOutOfBoundsException and its subclasses")
    show { thrown { mutableListOf(1)[2] } }
    show { thrown { intArrayOf(1)[3] } }                               // arrays throw the subclass ArrayIndexOutOfBoundsException
    show { thrown { "abc"[5] } }                                       // strings throw StringIndexOutOfBoundsException
    show { thrown { "abc".substring(2, 9) } }
    show { mutableListOf(1).getOrNull(2) }                             // the safe form (lesson 3)

    section("NoSuchElementException and its look-alike")
    show { thrown { emptyList<Int>().first() } }
    show { thrown { emptyList<Int>().max() } }                         // max() on empty throws; maxOrNull() returns null
    show { thrown { mapOf("a" to 1).getValue("b") } }                  // map[key] is null, getValue(key) throws
    show { thrown { ArrayDeque<Int>().removeFirst() } }
    show { thrown { emptyList<Int>().single() } }                      // none: NoSuchElementException
    show { thrown { listOf(1, 2).single() } }                          // trap: two elements is IllegalArgumentException

    section("ClassCastException, NullPointerException, StackOverflowError")
    show { thrown { mystery() as Int } }                               // a String is not an Int
    show { mystery() as? Int }                                         // as? gives null instead of throwing
    show { thrown { nothing()!! } }                                    // !! on null: NullPointerException
    show { thrown { endless(0) } }                                     // an Error, not an Exception
    note("ConcurrentModificationException (remove while looping) is shown in lesson 3, ListMutate")
    note("Catch the narrowest type you can handle; catching Throwable also swallows Errors")
}
