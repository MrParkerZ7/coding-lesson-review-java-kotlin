package review.advanced

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

@JvmInline
value class UserId(val value: Int)

@JvmInline
value class OrderId(val value: Int)

fun findUser(id: UserId): String = "user#${id.value}"

@JvmInline
value class Cents(val amount: Long) : Comparable<Cents> {
    init { require(amount >= 0) { "negative amount" } }    // validation in init is allowed
    val dollars: Long get() = amount / 100                 // a computed property: no backing field in a value class
    operator fun plus(other: Cents) = Cents(amount + other.amount)
    override fun compareTo(other: Cents) = amount.compareTo(other.amount)
}

typealias Meters = Double                                  // only another NAME for Double
fun lengthOf(m: Meters) = m

/**
 * 20b. VALUE CLASS and typealias: type safety at no cost
 *      Run: mvn -q compile exec:java -Dmain.class=review.advanced.ValueClassKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("VALUE CLASS  -  a new type that is the old type at run time")

    section("The problem: two Ints that must not be mixed")
    syntax(
        "@JvmInline value class Name(val v: T)" to "ONE val in the primary constructor; wraps a T",
        "Name(5)" to "a new type for the compiler, a plain T in the bytecode (mostly)",
    )
    val user = UserId(7)
    val order = OrderId(7)
    show { findUser(user) }
    show { user == UserId(7) }                             // equals compares the wrapped value
    show { user }                                          // toString of a value class looks like a data class
    wontCompile("findUser(order)", "an OrderId is not a UserId, although both wrap 7")
    wontCompile("findUser(7)", "a bare Int is not a UserId either")
    note("A plain `fun findUser(id: Int)` would accept an order id or any number: the compiler could not help")

    section("What happens at run time")
    val fields = UserId::class.java.declaredFields
    show { fields.map { it.type.name } }                   // one field, a primitive int: the wrapper holds nothing else
    val compiled = Class.forName("review.advanced.ValueClassKt").declaredMethods.first { it.name.startsWith("findUser") }
    show { compiled.parameterTypes.map { it.name } }       // findUser takes a raw int: no UserId object is created
    show { compiled.name.startsWith("findUser-") }         // the name is mangled so findUser(Int) can still coexist
    note("It is boxed (a real UserId object) when used as Any, as T in a generic, or as UserId?: List<UserId> boxes each id")

    section("What a value class may and may not do")
    val price = Cents(1250)
    show { price + Cents(50) }                             // methods and operators are fine
    show { price.dollars }                                 // a computed property: allowed, it stores nothing
    show { Cents(5) < Cents(9) }                           // interfaces are fine, here Comparable
    show { runCatching { Cents(-1) }.exceptionOrNull()?.javaClass?.simpleName }   // init { require } ran
    wontCompile("value class Id(val v: Int)", "on the JVM it needs @JvmInline")
    wontCompile("@JvmInline value class P(val a: Int, val b: Int)", "exactly one property in the constructor")
    wontCompile("@JvmInline value class V(var v: Int)", "the property must be a val")
    wontCompile("@JvmInline value class C(val v: Int) : Number()", "no superclass; only interfaces")
    wontCompile("@JvmInline value class W(val v: Int) { val extra: Int = 5 }", "no second stored field: a body property may only have a getter")

    section("typealias is a name, not a type")
    syntax("typealias Name = ExistingType" to "a shorter or clearer name; the compiler treats both as the SAME type")
    val feet: Double = 3.0
    show { lengthOf(feet) }                                // compiles: Meters and Double are one type, no protection
    note("typealias helps readability (typealias Handler = (Event) -> Unit); only a value class adds safety")
    note("Interview: value class vs data class? data class = a full object (many fields, copy, componentN); value class = ONE field, unboxed")
}
