package review.sets;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 5f. STACK, QUEUE, DEQUE: ArrayDeque does all three
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.StackQueueDeque
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class StackQueueDeque {

    /** Balanced brackets: a stack remembers the open ones. O(n). */
    static boolean isBalanced(String s) {
        Deque<Character> open = new ArrayDeque<>();                              // used as a stack
        Map<Character, Character> pairs = Map.of(')', '(', ']', '[', '}', '{');
        for (char c : s.toCharArray()) {
            if (pairs.containsValue(c)) {
                open.push(c);
            } else if (pairs.containsKey(c)) {
                if (open.isEmpty()) return false;                                // a closer with nothing open
                char top = open.pop();                                           // char, not Character: != then compares the VALUES
                if (top != pairs.get(c)) return false;
            }
        }
        return open.isEmpty();                                                   // anything still open is unbalanced
    }

    public static void main(String[] args) {
        title("STACK, QUEUE, DEQUE");

        section("Stack (LIFO): last in, first out");
        syntax(
                "Deque<T> stack = new ArrayDeque<>();", "the stack to use (java.util.Stack is legacy)",
                "push(x) | pop() | peek()", "put on top | remove the top, THROWS if empty | look at the top, null if empty"
        );
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);  stack.push(2);  stack.push(3);   show(() -> stack);      // TRAP: [3, 2, 1] the TOP is printed first (push = addFirst)
        show(() -> stack.pop());                                                 // 3 comes out first
        show(() -> stack.peek());                                                // 2: look at the top without removing it
        show(() -> stack);
        show(() -> new ArrayDeque<Integer>().peek());                            // null on an empty stack
        show(() -> thrown(() -> new ArrayDeque<Integer>().pop()));               // pop() on an empty stack throws
        show(() -> thrown(() -> new ArrayDeque<Integer>().offer(null)));         // ArrayDeque rejects null (lesson 7)
        Stack<Integer> legacy = new Stack<>();
        legacy.push(1);  legacy.push(2);  legacy.push(3);   show(() -> legacy);   // the legacy Stack prints the bottom first
        note("Stack extends Vector: every call is synchronized, and get(i) / add(i, x) break the LIFO rule. Use ArrayDeque");

        section("Queue (FIFO): first in, first out");
        syntax(
                "Queue<T> queue = new ArrayDeque<>();", "or new LinkedList<>() (allows null, slower)",
                "offer(x) | poll() | peek()", "enqueue | dequeue | look at the front: null when empty",
                "add(x) | remove() | element()", "the same three, but they THROW when it fails or is empty"
        );
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("a");  queue.offer("b");  queue.offer("c");   show(() -> queue);   // [a, b, c]: the front is printed first
        show(() -> queue.poll());                                                // "a" arrived first, leaves first
        show(() -> queue.peek());                                                // look at the front
        show(() -> queue.size());
        show(() -> new ArrayDeque<String>().poll());                             // null on an empty queue
        show(() -> thrown(() -> new ArrayDeque<String>().remove()));             // remove() on an empty queue throws

        section("Deque: both ends");
        syntax(
                "addFirst | addLast | pollFirst | pollLast", "put or take at either end, all O(1)",
                "peekFirst | peekLast", "look at either end, null when empty"
        );
        Deque<Integer> deque = new ArrayDeque<>(List.of(2, 3));
        deque.addFirst(1);  deque.addLast(4);   show(() -> deque);
        show(() -> deque.pollFirst());
        show(() -> deque.pollLast());
        show(() -> deque.peekFirst());
        note("push = addFirst, pop = removeFirst, offer = addLast, poll = pollFirst: stack calls work at the FRONT, queue calls add at the BACK");

        section("Brackets with a stack");
        show(() -> isBalanced("([]{})"));
        show(() -> isBalanced("([)]"));                                          // closes in the wrong order
        show(() -> isBalanced("(("));                                            // never closed
        show(() -> isBalanced("())"));                                           // closed once too often: the empty-stack check matters
        show(() -> isBalanced(""));                                              // nothing to balance

        section("Cost");
        bigO(
                "ArrayDeque addFirst | addLast | pollFirst | pollLast", "O(1)",
                "peek | peekFirst | peekLast | size", "O(1)",
                "contains | remove(Object) on any of them", "O(n)",
                "LinkedList as a queue", "O(1) too, but a node object per element: slower and bigger than ArrayDeque"
        );
    }
}
