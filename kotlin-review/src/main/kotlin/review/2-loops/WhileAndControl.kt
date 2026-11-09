package review.loops

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 2b. WHILE, DO-WHILE, BREAK, CONTINUE, LABELS
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.WhileAndControlKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("WHILE LOOPS AND LOOP CONTROL")

    section("while and do-while")
    syntax(
        "while (condition) { }" to "checks BEFORE each round: may run zero times",
        "do { } while (condition)" to "runs the body first: at least once",
        "while (true) { ... break }" to "loop until something inside says stop",
    )
    show { buildList { var n = 3; while (n > 0) add(n--) } }                   // n-- uses the value, THEN decrements
    show { buildList { var tries = 0; do { tries++; add("try$tries") } while (tries < 2) } }   // the body ran once before the check
    show { buildList { var i = 0; while (true) { if (i == 3) break; add(i++) } } }
    show { buildList { val queue = ArrayDeque(listOf(1, 2, 3)); while (queue.isNotEmpty()) add(queue.removeFirst()) } }   // the shape of BFS
    wontCompile("while (1) { }", "the condition must be a Boolean")

    section("break and continue")
    syntax(
        "break" to "leave the loop now",
        "continue" to "skip to the next round",
        "label@ for (...) { break@label }" to "leave (or continue) an OUTER loop",
    )
    show { buildList { for (i in 1..6) { if (i % 2 == 1) continue; add(i) } } }   // skip the odd numbers
    show { buildList { for (i in 1..9) { if (i == 4) break; add(i) } } }          // stop at 4
    val pairs = mutableListOf<String>()
    outer@ for (i in 1..3) {
        for (j in 1..3) {
            if (j == 2) continue@outer                                         // jump to the NEXT i
            if (i == 3) break@outer                                            // leave BOTH loops
            pairs.add("($i,$j)")
        }
    }
    show { pairs }                                                             // labels reach the outer loop

    section("break and continue inside forEach { }")
    syntax(
        "xs.forEach { if (...) return@forEach }" to "acts like continue",
        "run { xs.forEach { if (...) return@run } }" to "acts like break",
    )
    wontCompile("listOf(1, 2).forEach { if (it == 1) break }", "break and continue only work in real loops, not in a lambda")
    show { buildList { listOf(1, 2, 3, 4).forEach { if (it == 2) return@forEach; add(it) } } }          // like continue
    show { buildList { run { listOf(1, 2, 3, 4).forEach { if (it == 3) return@run; add(it) } } } }       // like break
    note("A bare return inside forEach leaves the WHOLE function, not just the lambda")
    note("When you need break or continue, a plain for loop is the simplest and clearest choice")
}
