package review.delegation

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

interface Printer {
    val name: String
    fun print(text: String): String
}

class PlainPrinter : Printer {
    override val name = "plain"
    override fun print(text: String) = "[$text]"
}

/** Decorator: reuses every member of `inner`, replaces only print. */
class LoudPrinter(private val inner: Printer) : Printer by inner {
    override fun print(text: String) = inner.print(text.uppercase()) + "!"
}

interface Walker { fun walk(): String }
interface Swimmer { fun swim(): String }
class LegWalker : Walker { override fun walk() = "walking" }
class FinSwimmer : Swimmer { override fun swim() = "swimming" }

/** Two interfaces, two delegates: a poor man's multiple inheritance. */
class Duck(w: Walker, s: Swimmer) : Walker by w, Swimmer by s

interface Greeter {
    fun who(): String
    fun hello(): String
}

class BaseGreeter : Greeter {
    override fun who() = "base"
    override fun hello() = "hello from ${who()}"           // calls who() on the DELEGATE, never on the wrapper
}

class ShoutGreeter(g: Greeter) : Greeter by g {
    override fun who() = "shout"
}

/** Delegation: wraps a list and counts what is added. */
class CountingList<T>(private val inner: MutableList<T> = mutableListOf()) : MutableList<T> by inner {
    var adds = 0
    override fun add(element: T): Boolean { adds++; return inner.add(element) }
    override fun addAll(elements: Collection<T>): Boolean { adds += elements.size; return inner.addAll(elements) }
}

/** Inheritance: the same counting idea on top of a real HashSet. */
class CountingSet<E> : HashSet<E>() {
    var adds = 0
    override fun add(element: E): Boolean { adds++; return super.add(element) }
    override fun addAll(elements: Collection<E>): Boolean { adds += elements.size; return super.addAll(elements) }
}

/**
 * 18d. CLASS DELEGATION: `class A(b: B) : B by b`
 *      Run: mvn -q compile exec:java -Dmain.class=review.delegation.ClassDelegationKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("CLASS DELEGATION  -  by, decorator, two interfaces")

    section("Forward everything, override only what changes")
    syntax(
        "class A(b: B) : B by b" to "the compiler writes every B member as a call to b",
        "class A : B by BImpl()" to "the delegate can be created right there",
        "override fun f() = ..." to "wins over the forwarded one",
    )
    val loud = LoudPrinter(PlainPrinter())
    show { loud.name }                                     // forwarded to PlainPrinter, no code written
    show { loud.print("hi") }                              // overridden: uppercases, then asks the inner printer
    show { PlainPrinter().print("hi") }                    // the original is unchanged

    section("Delegating two interfaces")
    syntax("class C(a: A, b: B) : A by a, B by b" to "one class gets the members of both")
    val duck = Duck(LegWalker(), FinSwimmer())
    show { duck.walk() }
    show { duck.swim() }
    wontCompile("class C(a: P, b: Q) : P by a, Q by b", "if P and Q both declare f(), C must override f() itself")

    section("TRAP: the delegate never sees the wrapper")
    val shout = ShoutGreeter(BaseGreeter())
    show { shout.who() }                                   // overridden in the wrapper
    show { shout.hello() }                                 // hello() runs inside BaseGreeter, so who() is "base": no virtual dispatch to the wrapper
    note("With inheritance, hello() would call the overridden who(). With delegation it cannot: the delegate knows nothing about the wrapper")

    section("Delegation vs inheritance")
    val counting = CountingList<Int>()
    counting.addAll(listOf(1, 2, 3));  show { counting.adds }   // delegate.addAll does not call our add(): counted once per element
    counting.add(4);  show { counting.adds }
    val set = CountingSet<Int>()
    set.addAll(listOf(1, 2, 3));  show { set.adds }        // HashSet.addAll calls add() per element, which we also count: double
    show { set.size }
    wontCompile("class Fancy : PlainPrinter()", "PlainPrinter is final (classes are final unless open)")
    note("A final class cannot be inherited, but an interface it implements can always be wrapped with `by`")
    note("A class has ONE superclass but can delegate any number of interfaces")
    note("Prefer delegation to inheritance when you only reuse behavior (Effective Java: favor composition)")
}
