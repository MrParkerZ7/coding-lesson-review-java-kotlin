package review.advanced

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

data class Vec2(val x: Int, val y: Int) {
    operator fun plus(o: Vec2) = Vec2(x + o.x, y + o.y)
    operator fun minus(o: Vec2) = Vec2(x - o.x, y - o.y)
    operator fun times(k: Int) = Vec2(x * k, y * k)
    operator fun unaryMinus() = Vec2(-x, -y)
    operator fun get(i: Int) = if (i == 0) x else y
    infix fun dot(o: Vec2) = x * o.x + y * o.y             // infix: a dot b
}

operator fun Int.times(v: Vec2) = v * this                 // an extension operator: 3 * v

class Board {
    private val cells = IntArray(4)
    operator fun get(row: Int, col: Int) = cells[row * 2 + col]
    operator fun set(row: Int, col: Int, value: Int) { cells[row * 2 + col] = value }
}

class Box(private val lo: Vec2, private val hi: Vec2) {
    operator fun contains(p: Vec2) = p.x in lo.x..hi.x && p.y in lo.y..hi.y
}

class Multiplier(private val k: Int) {
    operator fun invoke(n: Int) = n * k
}

class Bag {
    val items = mutableListOf<Int>()
    operator fun plusAssign(n: Int) { items.add(n) }       // bag += 1 changes the bag in place
}

data class Version(val major: Int, val minor: Int) : Comparable<Version> {
    override fun compareTo(other: Version) = compareValuesBy(this, other, { it.major }, { it.minor })
}

class Range2(private val a: Int, private val b: Int) {
    operator fun component1() = a                          // destructuring needs componentN
    operator fun component2() = b
}

/**
 * 20c. OPERATOR OVERLOADING and infix
 *      Run: mvn -q compile exec:java -Dmain.class=review.advanced.OperatorOverloadingKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("OPERATOR OVERLOADING  -  a + b is a.plus(b)")

    section("Arithmetic and unary operators")
    syntax(
        "a + b | a - b | a * b | a / b | a % b" to "plus | minus | times | div | rem",
        "-a | +a | !a | a++ | a--" to "unaryMinus | unaryPlus | not | inc | dec",
        "operator fun Type.plus(o: Type): Type" to "the shape; `operator` is required on the declaration",
    )
    val a = Vec2(1, 2)
    val b = Vec2(3, 4)
    show { a + b }                                         // a.plus(b)
    show { b - a }
    show { a * 3 }                                         // a.times(3)
    show { 3 * a }                                         // Int.times(Vec2): the extension operator above
    show { -a }                                            // a.unaryMinus()
    show { a dot b }                                       // infix call: 1*3 + 2*4
    show { 1 shl 2 + 1 }                                   // TRAP: infix has LOWER precedence than +, so this is 1 shl 3
    wontCompile("operator fun Vec2.pow(n: Int) = this", "only the fixed operator names (plus, get, invoke ...) can be operators")

    section("Index, call, in, destructuring")
    syntax(
        "a[i] | a[i, j]  and  a[i] = v" to "get(i) | get(i, j)  and  set(i, v)",
        "a(x)" to "invoke(x): an object you can call like a function",
        "x in a  |  x !in a" to "a.contains(x): note the reversed order",
        "val (p, q) = a" to "component1(), component2()",
    )
    show { a[1] }                                          // a.get(1)
    val board = Board()
    board[0, 1] = 7                                        // board.set(0, 1, 7)
    board[0, 1] += 5                                       // get, add, then set
    show { board[0, 1] }                                   // 7 was set, then += 5 read it and wrote 12 back
    val triple = Multiplier(3)
    show { triple(5) }                                     // triple.invoke(5)
    val box = Box(Vec2(0, 0), Vec2(10, 10))
    show { Vec2(5, 5) in box }                             // box.contains(Vec2(5, 5))
    show { Vec2(11, 5) in box }
    val (lo, hi) = Range2(3, 9)
    show { lo + hi }                                       // 3 + 9, from component1() and component2()

    section("Comparison, equality, ranges")
    syntax(
        "a < b | a > b | a <= b | a >= b" to "a.compareTo(b) < 0 ... (implement Comparable<T>)",
        "a == b | a != b" to "a?.equals(b) ?: (b === null): null safe, you override equals",
        "a..b  |  a..<b" to "a.rangeTo(b) | a.rangeUntil(b): free for every Comparable",
    )
    show { Version(1, 2) < Version(1, 10) }                // compared by major, then minor
    show { Version(1, 5) in Version(1, 2)..Version(1, 9) } // the range comes from Comparable.rangeTo
    show { Version(1, 9) in Version(1, 2)..<Version(1, 9) }   // ..< leaves the end out
    show { Version(2, 0) == Version(2, 0) }                // data class equals

    section("Compound assignment")
    syntax("a += b" to "a.plusAssign(b) if it exists, else a = a + b (then a must be a var)")
    val bag = Bag()
    bag += 1
    bag += 2
    show { bag.items }                                     // plusAssign: the same bag was changed, `val` is fine
    wontCompile("val v = Vec2(1, 2); v += Vec2(1, 1)", "no plusAssign, so it means v = v + ...: a val cannot be reassigned")
    note("If a var has BOTH plus and plusAssign, a += b is ambiguous and does not compile: define one of them")
    note("Operators are just functions: a + b and a.plus(b) are the same call. You cannot invent new symbols, only use the fixed ones")
}
