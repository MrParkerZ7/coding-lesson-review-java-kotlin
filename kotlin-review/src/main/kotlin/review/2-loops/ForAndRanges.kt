package review.loops

import review.bigO
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 2a. FOR LOOPS AND RANGES
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.ForAndRangesKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 *     Each demo collects what the loop visits with buildList { ... add(x) }, so the result is printed.
 */
fun main() {
    title("FOR LOOPS AND RANGES")

    section("Ranges")
    syntax(
        "for (i in a..b)" to "from a up to b, b INCLUDED",
        "for (i in a until b) | for (i in a..<b)" to "b EXCLUDED (0 until n is the index loop)",
        "for (i in a downTo b)" to "counting down, b included",
        "for (i in range step n)" to "every n-th value",
    )
    show { buildList { for (i in 1..5) add(i) } }                              // 1..5 includes the end
    show { buildList { for (i in 1 until 5) add(i) } }                         // until excludes the end
    show { buildList { for (i in 1..<5) add(i) } }                             // 1..<5 is the newer spelling of until
    show { buildList { for (i in 5 downTo 1) add(i) } }
    show { buildList { for (i in 0..10 step 3) add(i) } }
    show { buildList { for (i in 10 downTo 0 step 5) add(i) } }                // step goes AFTER downTo
    show { buildList { for (c in 'a'..'e') add(c) } }                          // Char ranges work too
    show { buildList { for (i in 1..0) add(i) } }                              // TRAP: EMPTY, ranges only count up: use downTo
    show { buildList { repeat(3) { add(it) } } }                               // repeat(n) { }: the index starts at 0
    wontCompile("for (i in 1..3) { i = 5 }", "the loop variable is a val")
    wontCompile("for (x in 1.0..3.0) { }", "a decimal range has no next value to step to")

    section("A range is also a value")
    val r = 1..10
    show { r.sum() }
    show { r.count() }
    show { r.first }
    show { r.last }
    show { 5 in r }                                                            // fast range check, great in conditions
    show { r.reversed().take(3) }
    show { (r step 4).toList() }

    section("Loops over collections")
    syntax(
        "for (x in xs)" to "each element",
        "for (i in xs.indices)" to "each index 0..lastIndex",
        "for ((i, x) in xs.withIndex())" to "index and element together",
        "for ((k, v) in map)" to "each key and value of a Map",
    )
    val fruits = listOf("apple", "banana", "cherry")
    val ages = mapOf("ann" to 30, "bob" to 25)
    show { buildList { for (f in fruits) add(f) } }
    show { buildList { for (i in fruits.indices) add("$i=${fruits[i]}") } }
    show { buildList { for ((i, f) in fruits.withIndex()) add("$i=$f") } }     // destructuring: the interview favourite
    show { buildList { fruits.forEachIndexed { i, f -> add("$i:$f") } } }      // the lambda form
    show { buildList { for (c in "hey") add(c) } }                             // a String is a sequence of Char
    show { buildList { for ((k, v) in ages) add("$k=$v") } }
    show { buildList { for (i in fruits.lastIndex downTo 0) add(fruits[i]) } } // backwards by index
    show { buildList { for (f in fruits.reversed()) add(f) } }                 // backwards, a reversed copy
    show { buildList { for (i in fruits.indices step 2) add(fruits[i]) } }     // every 2nd element
    show { buildList { for (i in 0 until 3) for (j in i + 1 until 3) add("$i$j") } }   // all pairs i < j: the nested loop of brute force

    section("Cost")
    bigO(
        "one loop over n elements" to "O(n)",
        "two nested loops over n" to "O(n^2)",
        "for (i in 0 until n) { for (j in i + 1 until n) }" to "still O(n^2): about n*n/2 pairs",
        "a loop that halves the range each round" to "O(log n)",
    )
}
