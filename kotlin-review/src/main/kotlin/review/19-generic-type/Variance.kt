package review.generics

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

open class Animal(val name: String) {
    override fun toString() = "Animal($name)"
}

class Dog(name: String) : Animal(name) {
    override fun toString() = "Dog($name)"
}

/** out T: this class only PRODUCES T (T appears in return positions only) -> covariant. */
class Producer<out T>(private val item: T) {
    fun get(): T = item
}

/** in T: this class only CONSUMES T (T appears in parameter positions only) -> contravariant. */
class Consumer<in T> {
    fun accept(item: T): String = "accepted $item"
}

/** Use-site variance: this one function promises to only WRITE Dogs into the list. */
fun fillWithDogs(target: MutableList<in Dog>) {
    target.add(Dog("Fido"))
}

/** Use-site variance: this one function promises to only READ Animals from the list. */
fun firstName(source: MutableList<out Animal>): String = source[0].name

/**
 * 19b. VARIANCE: invariant, out (covariant), in (contravariant)
 *      Run: mvn -q compile exec:java -Dmain.class=review.generics.VarianceKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("VARIANCE  -  invariant, out (producer), in (consumer)")

    section("Invariant by default")
    syntax("class Box<T>" to "invariant: Box<Dog> is NOT a Box<Animal>")
    wontCompile("val a: MutableList<Animal> = mutableListOf<Dog>()", "it would let you add a Cat to a list of Dogs")
    note("Kotlin's Array<T> is invariant too (Java arrays are not)")

    section("out: producers are covariant, Producer<Dog> is a Producer<Animal>")
    syntax("class Producer<out T>" to "T only comes OUT (return types): Producer<Dog> is a Producer<Animal>")
    val dogs: List<Dog> = listOf(Dog("Rex"), Dog("Max"))
    val animals: List<Animal> = dogs;   show { animals }      // List<out E> is read-only, so this is safe
    val animalProducer: Producer<Animal> = Producer(Dog("Rex"))
    show { animalProducer.get() }                             // reading an Animal out of a Producer<Dog> is safe

    section("in: consumers are contravariant, Consumer<Animal> is a Consumer<Dog>")
    syntax("class Consumer<in T>" to "T only goes IN (parameter types): Consumer<Animal> is a Consumer<Dog>")
    val dogConsumer: Consumer<Dog> = Consumer<Animal>()
    show { dogConsumer.accept(Dog("Rex")) }                   // something that accepts any Animal can accept a Dog
    val byName: Comparator<Animal> = compareBy<Animal> { it.name }
    show { listOf(Dog("Max"), Dog("Ann")).sortedWith(byName) }   // Comparator<in T> is contravariant: an Animal comparator sorts Dogs

    section("Use-site variance: out / in on one parameter")
    syntax(
        "fun f(list: MutableList<out Animal>)" to "only READ Animals from it",
        "fun f(list: MutableList<in Dog>)" to "only WRITE Dogs into it",
    )
    val animalList = mutableListOf<Animal>()
    fillWithDogs(animalList);   show { animalList }           // MutableList<Animal> is a MutableList<in Dog>
    show { firstName(mutableListOf(Dog("Rex"))) }             // MutableList<Dog> is a MutableList<out Animal>

    section("Rule of thumb")
    note("out = the type only comes OUT (you read it)   -> Java: ? extends")
    note("in  = the type only goes IN  (you write it)   -> Java: ? super")
    note("Producer-out, Consumer-in. Mutable and read+write types stay invariant.")
}
