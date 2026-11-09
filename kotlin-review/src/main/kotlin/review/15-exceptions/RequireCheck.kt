package review.exceptions

import review.note
import review.section
import review.show
import review.syntax
import review.title

/** Only here to ask the JVM whether assertions are on for the program's classes. */
class AssertionProbe

/** require: the CALLER gave a bad argument, so IllegalArgumentException. */
fun setAge(age: Int): Int {
    require(age >= 0) { "age must be >= 0, was $age" }
    return age
}

/** check: THIS object is in the wrong state, so IllegalStateException. */
class Door {
    var open = false

    fun enter(): String {
        check(open) { "door is closed" }
        return "in"
    }
}

/** requireNotNull returns its argument as non-null, and the compiler remembers that afterwards. */
fun lengthOf(text: String?): Int {
    requireNotNull(text) { "text is required" }
    return text.length
}

/** Compiles because TODO() has type Nothing, which fits any return type. */
fun notWrittenYet(): Int = TODO("later")

/**
 * 15c. require, check, assert, error, TODO
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.RequireCheckKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("REQUIRE / CHECK  -  validate arguments and state")

    section("require for arguments, check for state")
    syntax(
        "require(condition) { message }" to "bad ARGUMENT from the caller: IllegalArgumentException",
        "check(condition) { message }" to "bad STATE of this object: IllegalStateException",
        "error(message)" to "always throws IllegalStateException; its type is Nothing",
        "requireNotNull(x) { msg }  |  checkNotNull(x) { msg }" to "like require / check, and return x as non-null",
        "assert(condition)" to "AssertionError, but only when the JVM runs with -ea",
        "TODO(\"reason\")" to "NotImplementedError: a placeholder that compiles",
    )
    show { setAge(30) }
    show { thrown { setAge(-1) } }                                    // the argument was wrong
    show { runCatching { setAge(-1) }.exceptionOrNull()?.message }    // the lazy message built from the argument
    show { runCatching { require(zero() > 0) }.exceptionOrNull()?.message }   // the default message of require
    val door = Door()
    show { thrown { door.enter() } }                                  // the object was in the wrong state
    door.open = true
    show { door.enter() }                                             // same call, valid state: it works
    show { runCatching { check(zero() > 0) }.exceptionOrNull()?.message }     // the default message of check

    section("The message is a lambda: built only when the check fails")
    var built = 0
    require(zero() == 0) { built++; "never built" }
    show { built }                                                    // 0: the check passed, so the message was never computed
    show { thrown { error("always fails") } }                         // error() is a throw with a message
    show { runCatching { error("always fails") }.exceptionOrNull()?.message }

    section("requireNotNull and checkNotNull")
    show { lengthOf("abc") }                                          // after requireNotNull, text is smart-cast to String
    show { thrown { lengthOf(null) } }
    show { runCatching { lengthOf(null) }.exceptionOrNull()?.message }
    show { thrown { checkNotNull(nothing()) } }                       // check flavor: IllegalStateException
    show { runCatching { requireNotNull(nothing()) }.exceptionOrNull()?.message }   // default message

    section("assert is off by default, TODO() is an Error")
    show { AssertionProbe::class.java.desiredAssertionStatus() }      // false unless the JVM was started with -ea
    show { thrown { assert(zero() > 0) } }                            // so by default assert does nothing at all
    show { thrown { notWrittenYet() } }
    show { runCatching { TODO("later") }.exceptionOrNull()?.message }
    show { runCatching { TODO() }.exceptionOrNull() is Exception }   // false: try / catch (e: Exception) does NOT catch TODO()
    note("assert is for the developer's own checks; require and check run always, so use them for real validation")
    note("Which one? require = the argument is wrong. check = the state is wrong. error = this cannot happen.")
    note("requireNotNull / checkNotNull when a null comes from outside and you want a clear message")
}
