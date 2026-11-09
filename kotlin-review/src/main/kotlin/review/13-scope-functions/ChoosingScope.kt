package review.scope

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** A class with a label, used to show which `this` a nested scope function sees. */
class Box(var label: String)

/**
 * 13b. CHOOSING A SCOPE FUNCTION, takeIf, and the traps
 *      Run: mvn -q compile exec:java -Dmain.class=review.scope.ChoosingScopeKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CHOOSING A SCOPE FUNCTION  -  the usual jobs and the traps")

    section("The usual jobs")
    syntax(
        "x?.let { ... }" to "run the block only when x is not null",
        "Type().apply { prop = ... }" to "configure an object, get it back (builder style)",
        "x.also { log(it) }" to "a side effect (logging, printing) in the middle of a chain",
        "with(x) { a(); b() }" to "group several calls on one object",
        "run { val t = ...; result }" to "a block that computes a value, temp variables stay inside",
    )
    val maybe: String? = "abc"
    val none: String? = null
    show { maybe?.let { it.length } }                              // not null: the block runs
    show { none?.let { it.length } }                               // null: the block is skipped, the result is null
    val box = Box("start").apply { label = "set" }
    show { box.label }                                             // apply: set the property, keep using the object
    show { with(box) { label.uppercase() + label.length } }        // with: several calls on box, no `box.` repeated
    val n = run { val a = 3; val b = 4; a * b };  show { n }       // a and b live only inside the run block

    section("also for side effects in a chain")
    val log = mutableListOf<String>()
    val sorted = listOf(3, 1, 2).also { log += "before=$it" }.sorted().also { log += "after=$it" }
    show { sorted }                                                // the chain still returns the list
    show { log }                                                   // also() saw the list at each step
    val nums = listOf(3, 1, 2)
    show { nums.also { log += "seen" }.let { it.max() } }          // .also { }.let { }: a side effect, then a transform

    section("takeIf and takeUnless: the object or null")
    syntax(
        "x.takeIf { predicate }" to "x when the predicate is true, else null",
        "x.takeUnless { predicate }" to "x when the predicate is false, else null",
    )
    show { 8.takeIf { it > 5 } }                                   // true: 8 comes back
    show { 3.takeIf { it > 5 } }                                   // false: null
    show { 3.takeUnless { it > 5 } }                               // the opposite filter
    show { "42".toIntOrNull()?.takeIf { it > 0 } }                 // parse, then keep only positive numbers
    show { "   ".takeIf { it.isNotBlank() } ?: "default" }         // blank text becomes the default
    note("takeIf works like filter for ONE value: it turns an if/else into a chain")

    section("Trap: nested scopes, `it` and `this`")
    val ys = listOf(1, 2)
    show { "ab".let { s -> ys.map { n -> s + n } } }               // name the parameters when you nest let
    val a = Box("a")
    val b = Box("b")
    a.apply { b.apply { label = "changed" } }                      // inside, `label` means the INNERMOST this
    show { a.label }                                               // a was not touched
    show { b.label }                                               // b was changed
    a.apply outer@{ b.apply { label = this@outer.label + "!" } }   // a label (outer@) reaches the outer this
    show { b.label }                                               // b.label = a.label + "!"
    note("Nesting two scope functions makes `it` / `this` hard to read: name the lambda parameter, or use a plain val")
    note("A good rule: do not nest more than two, and do not chain scope functions just because you can")

    section("Trap: the lambda result is not always the object")
    show { Point(1, 2).let { it.x = 5 } }                          // the lambda ends in an assignment: the result is Unit
    show { mutableListOf(1).let { it.add(2) } }                    // add() returns Boolean: you get true, not the list
    show { mutableListOf(1).apply { add(2) } }                     // apply returns the list: this is what you wanted
    wontCompile("val p: Point = Point().let { it.x = 5 }", "let returns Unit here, not a Point")
    note("When the next call needs the object back, use apply or also, not let or run")
}
