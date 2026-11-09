package review.scope

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** A tiny mutable class to run the scope functions on. */
class Point(var x: Int = 0, var y: Int = 0) {
    override fun toString() = "Point($x, $y)"
}

/**
 * 13a. THE FIVE SCOPE FUNCTIONS: let, run, with, apply, also
 *      Run: mvn -q compile exec:java -Dmain.class=review.scope.FiveFunctionsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("SCOPE FUNCTIONS  -  let, run, with, apply, also")

    section("The table: two questions, two answers each")
    syntax(
        "x.let { it ... }" to "object is `it`;   returns the LAMBDA RESULT",
        "x.run { this ... }" to "object is `this`; returns the LAMBDA RESULT",
        "with(x) { this ... }" to "object is `this`; returns the LAMBDA RESULT (not an extension)",
        "x.apply { this ... }" to "object is `this`; returns the OBJECT itself",
        "x.also { it ... }" to "object is `it`;   returns the OBJECT itself",
    )
    note("Question 1: how do I reach the object?  it (let, also)  or  this (run, with, apply)")
    note("Question 2: what comes out?  the lambda's last value (let, run, with)  or  the object (apply, also)")

    section("let: it, returns the lambda result")
    show { "kotlin".let { it.length } }                              // result = last expression of the lambda
    show { "kotlin".let { it.uppercase() } }
    show { "kotlin".let { word -> word.length * 2 } }                // you may rename `it`

    section("run and with: this, return the lambda result")
    show { "kotlin".run { length } }                                 // `length` means this.length
    show { with("kotlin") { length + 1 } }                           // with(obj) { }: obj is passed as an argument
    show { run { 6 * 7 } }                                           // run without a receiver: a block that is an expression
    note("run and with do the same job; with(x) { } is a plain function, x.run { } is an extension (works with ?.run)")

    section("apply and also: return the object itself")
    val p = Point().apply { x = 3; y = 4 };  show { p }              // apply: set properties, get the object back
    val xs = mutableListOf(1).also { it.add(2) };  show { xs }   // also: a side effect, get the object back
    show { "kotlin".also { it.length } }                             // the lambda result (6) is thrown away: the String comes out
    show { "kotlin".let { it.length } }                              // same body in let: the Int comes out

    section("The same lambda body, five different results")
    show { Point(1, 2).let { it.x + it.y } }                         // let: lambda result
    show { Point(1, 2).run { x + y } }                               // run: lambda result
    show { with(Point(1, 2)) { x + y } }                             // with: lambda result
    show { Point(1, 2).apply { x += y } }                            // apply: the object, now changed
    show { Point(1, 2).also { it.x += it.y } }                       // also: the object, now changed

    section("Which name goes with which receiver")
    wontCompile("\"kotlin\".let { length }", "inside let the object is `it`, not `this`")
    wontCompile("\"kotlin\".run { it.length }", "inside run the object is `this`, there is no `it`")
    note("Interview answer: let/also give the object as `it`; run/with/apply give it as `this`.")
    note("apply and also return the receiver (good for chains and builders); let, run, with return the lambda result.")
    note("All five are inline functions, so they cost nothing extra at run time (lesson 20).")
}
