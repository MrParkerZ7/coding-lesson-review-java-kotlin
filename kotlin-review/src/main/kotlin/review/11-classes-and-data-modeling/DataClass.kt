package review.classes

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

data class Point(val x: Int, val y: Int)

class PlainPoint(val x: Int, val y: Int)

data class Customer(val id: Int, val name: String) {
    var lastLogin: String = "never"                    // declared in the body: NOT part of equals, toString or copy
}

data class Team(val name: String, val members: MutableList<String>)

data class Cell(var x: Int)                            // a mutable key: see the HashSet trap

/**
 * 11c. DATA CLASS: equals, hashCode, toString, copy, componentN
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.DataClassKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("DATA CLASS  -  value equality, copy, destructuring")

    section("What a data class generates")
    syntax(
        "data class Name(val a: T, val b: U)" to "needs at least one val or var in the primary constructor",
        "equals() | hashCode() | toString()" to "generated from the primary-constructor properties",
        "copy(a = x)" to "a new object with some properties changed",
        "val (a, b) = obj" to "component1(), component2() ... in declaration order",
    )
    val p = Point(1, 2);   show { p }                                          // toString: Point(x=1, y=2)
    show { p == Point(1, 2) }                                                  // true: == calls the generated equals
    show { p === Point(1, 2) }                                                 // false: === is identity, these are two objects
    show { p.hashCode() == Point(1, 2).hashCode() }                            // true: equal objects must have equal hash codes
    show { p.copy(y = 9) }                                                     // p itself is not changed
    show { p.component2() }                                                    // destructuring calls these
    val (px, py) = p;   show { "$px,$py" }
    show { PlainPoint(1, 2) == PlainPoint(1, 2) }                              // false: a plain class compares by identity
    wontCompile("data class Empty()", "needs at least one primary-constructor parameter")
    wontCompile("data class Bad(x: Int)", "every primary-constructor parameter must be a val or var")
    wontCompile("open data class Loose(val a: Int)", "a data class cannot be open")

    section("Only primary-constructor properties count")
    val a = Customer(1, "Ann")
    val b = Customer(1, "Ann")
    b.lastLogin = "today"
    show { a == b }                                                            // true: lastLogin is in the body, equals ignores it
    show { b }                                                                 // toString ignores it too
    show { b.copy().lastLogin }                                                // "never": copy builds a fresh object, the body property restarts

    section("As a Set element or a Map key")
    show { setOf(Point(1, 2), Point(1, 2)).size }                              // 1: equal data classes are the same element
    show { setOf(PlainPoint(1, 2), PlainPoint(1, 2)).size }                    // 2: plain objects are all different
    show { mapOf(Point(0, 0) to "origin")[Point(0, 0)] }                       // found by VALUE: the way to key a grid cell
    val cell = Cell(1)
    val seen = hashSetOf(cell)
    cell.x = 2
    show { seen.contains(cell) }                                               // false: the hash changed while the cell sat in the set
    show { seen.size }                                                         // 1: it is still stored, but can no longer be found
    note("Keys and set elements should be immutable: use val in data classes that go into a HashSet or HashMap")

    section("copy is shallow")
    val team = Team("A", mutableListOf("x"))
    val twin = team.copy(name = "B")
    twin.members.add("y")
    show { team.members }                                                      // the original team changed too
    show { team.members === twin.members }                                     // true: copy shares the same list object
    note("Keep collections read-only (List) and build changes with copy(members = members + \"y\")")
    note("Java records are the nearest relative; a data class also has copy() and componentN (lesson 16)")
}
