package review.sets

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title
import java.util.PriorityQueue

/** Balanced brackets: a stack remembers the open ones. O(n). */
fun isBalanced(s: String): Boolean {
    val open = ArrayDeque<Char>()                  // used as a stack
    val pairs = mapOf(')' to '(', ']' to '[', '}' to '{')
    for (c in s) {
        when (c) {
            in pairs.values -> open.addLast(c)
            in pairs -> if (open.removeLastOrNull() != pairs[c]) return false
        }
    }
    return open.isEmpty()
}

/**
 * 5d. STACK, QUEUE, DEQUE and HEAP: ArrayDeque and PriorityQueue
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.StackQueueDequeKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("STACK, QUEUE, DEQUE, HEAP")

    section("Stack (LIFO): last in, first out")
    syntax(
        "val stack = ArrayDeque<T>()" to "Kotlin's built-in deque: a stack AND a queue",
        "stack.addLast(x) | removeLast() | last()" to "push | pop | peek (top)",
        "removeLastOrNull() | lastOrNull()" to "pop and peek on an empty stack without an exception",
    )
    val stack = ArrayDeque<Int>()
    stack.addLast(1); stack.addLast(2); stack.addLast(3);   show { stack }
    show { stack.removeLast() }                                                // pop: 3 comes out first
    show { stack.last() }                                                      // peek: look at the top without removing it
    show { stack }
    show { ArrayDeque<Int>().removeLastOrNull() }                              // null on an empty stack
    show { runCatching { ArrayDeque<Int>().removeLast() }.exceptionOrNull()?.javaClass?.simpleName }   // removeLast() on empty throws
    note("java.util.Stack is legacy (synchronized, slow): use ArrayDeque")

    section("Queue (FIFO): first in, first out")
    syntax("queue.addLast(x) | removeFirst() | first()" to "enqueue | dequeue | peek (front)")
    val queue = ArrayDeque<String>()
    queue.addLast("a"); queue.addLast("b"); queue.addLast("c");   show { queue }
    show { queue.removeFirst() }                                               // dequeue: "a" arrived first, leaves first
    show { queue.first() }                                                     // peek at the front
    show { queue.size }

    section("Deque: both ends")
    syntax("addFirst(x) | addLast(x) | removeFirst() | removeLast()" to "all O(1)")
    val deque = ArrayDeque(listOf(2, 3))
    deque.addFirst(1); deque.addLast(4);   show { deque }

    section("Heap: PriorityQueue always gives the smallest first")
    syntax(
        "PriorityQueue<T>()" to "min-heap",
        "PriorityQueue<T>(compareByDescending { it })" to "max-heap",
        "add(x) | poll() | peek()" to "insert | remove the top | look at the top",
    )
    val minHeap = PriorityQueue(listOf(5, 1, 4, 2, 3))
    show { minHeap.peek() }                                                    // 1: the smallest
    show { minHeap.toList() }                                                  // TRAP: iteration order is the heap array, NOT sorted
    show { generateSequence { minHeap.poll() }.toList() }                      // poll() until empty gives sorted order
    val maxHeap = PriorityQueue<Int>(compareByDescending<Int> { it })
    maxHeap.addAll(listOf(5, 1, 4))
    show { maxHeap.poll() }                                                    // 5: the largest comes first

    section("Brackets with a stack")
    show { isBalanced("([]{})") }
    show { isBalanced("([)]") }
    show { isBalanced("((") }

    section("Cost")
    bigO(
        "ArrayDeque addFirst | addLast | removeFirst | removeLast" to "O(1)",
        "PriorityQueue add | poll" to "O(log n)",
        "PriorityQueue peek" to "O(1)",
        "contains on any of them" to "O(n)",
    )
}
