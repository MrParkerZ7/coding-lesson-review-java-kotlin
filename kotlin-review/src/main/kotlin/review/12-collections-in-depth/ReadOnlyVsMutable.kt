package review.collectionsdepth

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile
import java.util.Collections

/** Hands out its list two ways: as a live read-only view and as a snapshot copy. */
class Inbox {
    private val items = mutableListOf<String>()
    val view: List<String> get() = items                 // read-only TYPE, same list object
    val snapshot: List<String> get() = items.toList()    // a copy made at this moment

    fun add(text: String) { items.add(text) }
}

fun className(o: Any): String = o.javaClass.name

/** The simple name of the exception a block throws, or null when it does not throw. */
fun errorOf(block: () -> Any?): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName

/**
 * 12a. READ-ONLY vs MUTABLE collections: a List is a VIEW, not a frozen list
 *      Run: mvn -q compile exec:java -Dmain.class=review.collectionsdepth.ReadOnlyVsMutableKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("READ-ONLY vs MUTABLE  -  a List is a view, not immutability")

    section("List and MutableList")
    syntax(
        "List<T>" to "read-only interface: get, size, contains, iterate",
        "MutableList<T>" to "adds add, remove, set, clear ...",
        "val view: List<T> = mutableList" to "a read-only VIEW of the same object, not a copy",
    )
    val owner = mutableListOf(1, 2, 3)
    val view: List<Int> = owner
    owner.add(4);   show { view }                                              // the view sees the change: it is the same object
    show { view === owner }                                                    // true: no copy was made
    show { view is MutableList<*> }                                            // true: List is a promise made by the compiler only
    (view as MutableList<Int>).add(5);   show { owner }                        // a cast breaks the promise
    show { errorOf { (listOf(1, 2) as MutableList<Int>).add(3) } }             // listOf's own list refuses at run time
    note("Read-only means 'this reference cannot change it', not 'nobody can': the owner and a cast still can")

    section("toList() copies, = shares")
    syntax(
        "xs.toList() | xs.toMutableList()" to "a NEW list with the same elements: a snapshot",
        "val b = a" to "no copy: two names for one list",
    )
    val snap = owner.toList()
    owner.add(6)
    show { snap }                                                              // later changes do not reach the copy
    show { owner }
    show { snap === owner }
    val inbox = Inbox()
    inbox.add("first")
    inbox.add("second")
    val live = inbox.view
    val frozen = inbox.snapshot
    inbox.add("mail")
    show { live }                                                              // the view followed the change
    show { frozen }                                                            // the snapshot did not
    note("Expose a List when callers may watch live; expose toList() when they must not see later changes")

    section("List<out E> is covariant, MutableList<E> is invariant")
    syntax(
        "List<out E>" to "a List<Int> IS a List<Number>: you can only read from it",
        "MutableList<E>" to "a MutableList<Int> is NOT a MutableList<Number>: you could add a Double",
    )
    val ints: List<Int> = listOf(1, 2)
    val numbers: List<Number> = ints;   show { numbers }
    wontCompile("val m: MutableList<Number> = mutableListOf<Int>()", "MutableList is invariant: Int is not Number here")
    note("Same for Set<out E> and Map<K, out V>. The full story is lesson 19 (Variance)")

    section("Creating: listOf, mutableListOf, arrayListOf, buildList, emptyList")
    syntax(
        "listOf(...)" to "read-only; behind it a fixed-size Java list",
        "mutableListOf(...) | arrayListOf(...)" to "a java.util.ArrayList behind both",
        "buildList { add(x) }" to "fill a mutable list inside the block, get a read-only List out",
        "emptyList<T>()" to "the shared empty list",
    )
    show { className(listOf(1, 2)) }                                           // a fixed-size Java list: add and remove throw
    show { className(mutableListOf(1, 2)) }
    show { className(arrayListOf(1, 2)) }                                      // the same class: arrayListOf only promises the ArrayList type
    show { buildList { add(1); addAll(listOf(2, 3)) } }
    show { emptyList<Int>() }
    show { errorOf { Collections.unmodifiableList(owner).add(0) } }            // Java's wrapper: refuses at RUN time, Kotlin refuses at compile time

    section("+ and - build NEW lists")
    syntax(
        "xs + x  |  xs + ys" to "a new list with the element(s) added at the end",
        "xs - x" to "a new list WITHOUT the first occurrence of x",
        "xs - ys" to "a new list without every element that appears in ys",
    )
    val base = listOf(1, 2, 3, 2)
    show { base + 9 }
    show { base + listOf(8, 9) }
    show { base - 2 }                                                          // TRAP: only the FIRST 2 goes
    show { base - listOf(2) }                                                  // this form removes every 2
    show { base }                                                              // unchanged: + and - never touch the original

    section("+= : plusAssign or plus?")
    syntax(
        "val xs: MutableList<T>;  xs += x" to "plusAssign: adds IN PLACE",
        "var xs: List<T>;  xs += x" to "xs = xs + x: a NEW list, and xs points to it",
        "var xs: MutableList<T>;  xs += x" to "also plusAssign (in place): a List would not fit the var",
    )
    val shared = mutableListOf(1)
    val alias = shared
    shared += 2
    show { alias }                                                             // [1, 2]: changed in place, the alias sees it
    var grown = listOf(1, 2)
    val before = grown
    grown += 3
    show { grown }
    show { before }                                                            // [1, 2]: the old list is untouched, grown points to a new one
    var both = mutableListOf(1)
    val bothAlias = both
    both += 2
    show { bothAlias }                                                         // [1, 2]: still in place
    wontCompile("val xs = listOf(1); xs += 2", "xs would have to point to a new list, but a val cannot be reassigned")

    section("Cost")
    bigO(
        "val v: List<T> = mutableList" to "O(1): a view, nothing is copied",
        "xs.toList() | xs.toMutableList()" to "O(n): copies every element",
        "xs + x | xs - x" to "O(n): builds a whole new list",
        "var xs: List<T>; loop { xs += x }" to "O(n^2) in total: each += copies the list",
        "mutable.add(x) at the end" to "O(1) amortized",
        "Collections.unmodifiableList(xs)" to "O(1): a wrapper, not a copy",
    )
}
