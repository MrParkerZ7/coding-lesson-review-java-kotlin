package review.classes

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

open class Vehicle(val wheels: Int) {
    open fun honk(): String = "beep"
    fun describe(): String = "$wheels wheels"                  // not open: subclasses cannot override it
}

class Car : Vehicle(4) {
    override fun honk(): String = super.honk() + " beep"       // super.f() calls the parent's version
}

abstract class Figure {
    abstract fun area(): Int
    fun summary(): String = "area=${area()}"                   // an abstract class may have concrete members
}

class Rect(private val w: Int, private val h: Int) : Figure() {
    override fun area(): Int = w * h
}

interface Greeter {
    val name: String                                           // an abstract property: no storage in an interface
    fun greet(): String = "Hello, $name"                       // a method with a default body
}

class Friend(override val name: String) : Greeter

interface Walker { fun move(): String = "walk" }
interface Swimmer { fun move(): String = "swim" }

class Duck : Walker, Swimmer {
    override fun move(): String = super<Walker>.move() + "+" + super<Swimmer>.move()   // super<Type> picks one
}

/** TRAP: the base constructor calls an open function that the subclass overrides. */
open class Base(log: MutableList<String>) {
    init { log.add("Base sees: ${label()}") }
    open fun label(): String = "base"
}

class Derived(log: MutableList<String>) : Base(log) {
    private val tag = "derived"                               // assigned AFTER the Base constructor has run
    override fun label(): String = tag
}

/**
 * 11b. INHERITANCE: final by default, open/override, abstract, interface, super
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.InheritanceAndInterfacesKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("INHERITANCE  -  open, override, abstract, interface")

    section("Final by default: open and override")
    syntax(
        "open class A / open fun f()" to "classes and members are FINAL unless marked open (Java: the opposite)",
        "class B : A(args)" to "extends A: the parentheses call A's constructor",
        "override fun f()" to "mandatory keyword; an override is itself open unless marked final",
        "super.f()" to "call the parent's version",
    )
    val car = Car()
    show { car.honk() }                                                        // parent "beep" plus the child's own
    show { car.describe() }
    val asVehicle: Vehicle = Car()
    show { asVehicle.honk() }                                                  // the run-time class decides: Car's override runs
    wontCompile("class Van : Car()", "Car is final: it was not declared open")
    wontCompile("class Truck : Vehicle(6) { override fun describe() = \"x\" }", "describe() is final in Vehicle")

    section("abstract class and interface")
    syntax(
        "abstract class A { abstract fun f(): T }" to "cannot be instantiated; abstract members have no body",
        "interface I { val p: T; fun f() = ... }" to "no state, but methods MAY have a default body",
        "class C : A(), I1, I2" to "one superclass (with parentheses), any number of interfaces",
    )
    show { Rect(3, 4).summary() }
    show { Friend("Ann").greet() }                                             // the default method calls the abstract property
    wontCompile("Figure()", "an abstract class cannot be instantiated")

    section("Two interfaces with the same default method")
    show { Duck().move() }                                                     // super<Walker> and super<Swimmer> pick each default
    wontCompile("class Mute : Walker, Swimmer", "both define move(): the class must override it")

    section("Trap: a base constructor calls an open function")
    val seen = mutableListOf<String>()
    Derived(seen)
    show { seen }                                                              // null: Derived.tag is not assigned yet
    note("Never call an open function from a constructor or init block: the subclass is not initialized yet")
    note("Prefer composition over inheritance; for 'is-a with a few kinds' use sealed (EnumAndSealed)")
}
