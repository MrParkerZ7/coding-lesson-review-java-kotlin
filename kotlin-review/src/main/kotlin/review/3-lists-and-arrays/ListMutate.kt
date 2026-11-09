package review.lists

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 3d. LISTS: adding, removing, changing, and removing safely while looping
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ListMutateKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("LISTS  -  add, remove, change")

    section("Adding, removing, changing (MutableList only)")
    syntax(
        "m.add(x) | m.add(i, x) | m += x | m.addAll(xs)" to "append | insert at index | append | append many",
        "m.remove(value) | m.removeAt(i) | m.removeAll { }" to "by VALUE | by INDEX | by condition",
        "m[i] = x | m.clear()" to "replace at index | remove everything",
    )
    val m = mutableListOf(1, 2, 3)
    m.add(4);                              show { m }
    m.add(0, 99);                          show { m }                          // insert at index 0: the rest shifts right
    m += 5;                                show { m }                          // plusAssign is add
    m.addAll(listOf(6, 7));                show { m }
    m.remove(99);                          show { m }                          // by VALUE
    show { m.removeAt(0) }                                                     // by INDEX: returns the removed element
    m[0] = 100;                            show { m }                          // replace at an index
    m.removeAll { it % 2 == 0 };           show { m }                          // remove everything matching
    m.clear();                             show { m }

    section("Trap: remove(value) or removeAt(index)?")
    val ints = mutableListOf(10, 20, 30)
    show { ints.remove(1) }                                                    // false: there is no VALUE 1 in the list
    show { ints.removeAt(1) }                                                  // 20: index 1
    show { ints }

    section("Trap: val or var with +=")
    var readOnlyVar = listOf(1, 2)
    readOnlyVar += 3;                      show { readOnlyVar }                // var + List: += builds a NEW list and reassigns
    wontCompile("val xs = listOf(1, 2); xs += 3", "a val cannot be reassigned: use a MutableList")
    note("MutableList (val or var): += changes the list in place.  var + List: += makes a new list and reassigns")

    section("Removing while looping")
    val broken = mutableListOf(1, 2, 3, 4)
    show { runCatching { for (x in broken) if (x == 2) broken.remove(x) }.exceptionOrNull()?.javaClass?.simpleName }   // for-in + remove = ConcurrentModificationException
    val safe = mutableListOf(1, 2, 3, 4, 5, 6)
    safe.removeAll { it % 2 == 0 };        show { safe }                       // the simplest safe way
    show { (1..6).filter { it % 2 == 1 } }                                     // or build a new list with filter
    val backwards = mutableListOf(1, 2, 3, 4, 5, 6)
    for (i in backwards.indices.reversed()) if (backwards[i] % 2 == 0) backwards.removeAt(i);   show { backwards }   // go from the end: indexes stay valid
    val withIterator = mutableListOf(1, 2, 3, 4, 5, 6)
    val iter = withIterator.iterator()
    while (iter.hasNext()) if (iter.next() % 2 == 0) iter.remove();   show { withIterator }   // iterator.remove(): the loop-safe way by hand
    note("Rule: never add or remove on a list that a for-in loop is walking, except through the iterator")
}
