package review.lists

import review.bigO
import review.section
import review.show
import review.syntax
import review.title

/**
 * 3c. LISTS: creating, reading, copy vs alias, 2D lists
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ListCreateAccessKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("LISTS  -  create and read")

    section("Creating")
    syntax(
        "listOf(1, 2, 3)" to "read-only List",
        "mutableListOf(1, 2, 3)" to "MutableList (ArrayList)",
        "List(size) { index -> value }" to "built by a lambda",
        "emptyList<Type>()" to "the type is required: nothing to infer it from",
    )
    val readOnly = listOf(1, 2, 3);        show { readOnly }
    val mutable = mutableListOf(1, 2, 3);  show { mutable }
    val squares = List(5) { it * it };     show { squares }                    // index -> value
    val zeros = MutableList(3) { 0 };      show { zeros }                      // the mutable version
    show { (1..5).toList() }
    show { emptyList<String>() }
    show { listOf(1, null, 3) }                                                // a List<Int?>: elements may be null

    section("Reading")
    syntax(
        "xs[i] | xs.get(i)" to "element at index i (from 0)",
        "xs.first() | xs.last() | xs.getOrNull(i)" to "ends, and a safe access",
        "xs.size | xs.lastIndex | xs.indices" to "length, last valid index, 0..lastIndex",
    )
    val xs = listOf(10, 20, 30, 40, 50)
    show { xs[0] }
    show { xs.first() }
    show { xs.last() }
    show { xs.size }
    show { xs.lastIndex }
    show { xs.indices }
    show { xs.getOrNull(99) }                                                  // null instead of an exception
    show { xs.getOrElse(99) { -1 } }                                           // or a default
    show { xs.firstOrNull { it > 25 } }
    show { 30 in xs }                                                          // contains
    show { xs.isEmpty() }
    val (first, second) = xs;              show { first + second }             // destructuring takes the first components
    show { runCatching { xs[99] }.exceptionOrNull()?.javaClass?.simpleName }   // a bad index throws
    show { runCatching { emptyList<Int>().first() }.exceptionOrNull()?.javaClass?.simpleName }   // first() on empty throws: use firstOrNull()

    section("Copy vs alias, equality")
    val original = mutableListOf(1, 2, 3)
    val alias = original
    val copy = original.toMutableList()
    original.add(4);                       show { alias }                      // alias is the SAME list
    show { copy }                                                              // the copy is independent (a shallow copy)
    show { listOf(1, 2) == listOf(1, 2) }                                      // == compares the contents
    show { alias === original }                                                // === asks: the same object?

    section("2D lists")
    syntax("List(rows) { MutableList(cols) { value } }" to "every row is its own list")
    val grid = List(3) { r -> MutableList(3) { c -> r * 3 + c } };   show { grid }
    grid[1][2] = 50;                       show { grid[1] }
    show { grid.map { it.sum() } }                                             // sum of each row
    show { grid.flatten() }
    show { List(3) { c -> List(3) { r -> grid[r][c] } } }                      // transpose: rows become columns

    section("Cost")
    bigO(
        "xs[i]" to "O(1)",
        "contains | indexOf" to "O(n)",
        "add(x) at the end" to "O(1) amortized",
        "add(i, x) | removeAt(i)" to "O(n): the rest shifts",
    )
}
