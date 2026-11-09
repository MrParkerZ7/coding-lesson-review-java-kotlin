package review.advanced

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/** Tail call: the recursive call is the LAST thing done, so the compiler turns it into a loop. */
tailrec fun sumTo(n: Long, acc: Long = 0): Long = if (n == 0L) acc else sumTo(n - 1, acc + n)

/** The same sum, but `n + ...` is still waiting after the call returns: every level keeps a stack frame. */
fun sumToPlain(n: Long): Long = if (n == 0L) 0 else n + sumToPlain(n - 1)

tailrec fun gcdOf(a: Int, b: Int): Int = if (b == 0) a else gcdOf(b, a % b)

tailrec fun fibFrom(n: Int, a: Long = 0, b: Long = 1): Long = if (n == 0) a else fibFrom(n - 1, b, a + b)

/** Report the stack size at the bottom of the recursion. */
tailrec fun stackAtTail(n: Int): Int = if (n == 0) Thread.currentThread().stackTrace.size else stackAtTail(n - 1)
fun stackAtPlain(n: Int): Int = if (n == 0) Thread.currentThread().stackTrace.size else stackAtPlain(n - 1)

/**
 * 20d. tailrec: recursion without a stack
 *      Run: mvn -q compile exec:java -Dmain.class=review.advanced.TailrecKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("tailrec  -  a tail call becomes a loop")

    section("tailrec turns recursion into a loop")
    syntax(
        "tailrec fun f(n: T, acc: R = init): R" to "the recursive call must be the LAST operation",
        "the accumulator parameter" to "carries the partial result forward, so nothing waits after the call",
    )
    show { sumTo(1_000_000) }                              // a million levels, no stack problem
    show { gcdOf(48, 18) }                                 // Euclid: the classic tail-recursive function
    show { fibFrom(50) }                                   // linear time, O(1) stack: a and b are the accumulators
    show { sumTo(0) }

    section("TRAP: the same recursion without tailrec")
    val overflow = runCatching { sumToPlain(1_000_000) }.exceptionOrNull()
    show { overflow?.javaClass?.simpleName }               // the call stack ran out long before a million frames
    show { overflow is Exception }                         // it is an Error, so catch (e: Exception) would NOT catch it
    show { sumToPlain(1000) }                              // a shallow depth is fine
    show { stackAtTail(1000) == stackAtTail(10) }          // proof: tailrec uses the same stack depth for 10 and 1000 levels
    show { stackAtPlain(1000) > stackAtPlain(10) }         // without tailrec the depth grows with n

    section("When tailrec does not apply")
    wontCompile("open class A { open tailrec fun f(n: Int): Int = if (n == 0) 0 else f(n - 1) }", "tailrec cannot be open: an override could change the call")
    note("fact(n) = n * fact(n - 1) is NOT a tail call: the multiplication happens after the call. Add an accumulator")
    note("The compiler only WARNS \"no tail calls are found\" when tailrec is wrong; it still compiles, and the stack still grows")
    note("A call inside try/catch/finally is not optimized, and neither is mutual recursion (a calls b, b calls a): only a function calling itself")
    note("Interview: tail recursion is just a loop written as a function; a plain for/while loop does the same without the question")

    section("Cost")
    bigO(
        "sumTo(n) with tailrec: time | stack" to "O(n) | O(1)",
        "sumToPlain(n) without tailrec: time | stack" to "O(n) | O(n)  (StackOverflowError for large n)",
        "fibFrom(n) with accumulators" to "O(n) time, O(1) stack",
    )
}
