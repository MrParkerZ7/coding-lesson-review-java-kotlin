/**
 * 5e. STACK AND QUEUE: an Array is a stack; a queue needs a little class (the deque and BFS: deque-and-bfs.ts)
 *     Run: npx tsx src/5-maps-sets-and-structures/stack-and-queue.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

/** FIFO queue: enqueue at the back, dequeue at the front in O(1) amortized (an array plus a head index, no shift). */
class Queue<T> {
  #items: T[] = [];
  #head = 0;                                            // index of the front element
  get size(): number {
    return this.#items.length - this.#head;
  }
  enqueue(x: T): void {
    this.#items.push(x);
  }
  dequeue(): T | undefined {
    if (this.size === 0) return undefined;
    const x = this.#items[this.#head];
    this.#head++;
    if (this.#head * 2 >= this.#items.length) {         // half of the array is dead slots: copy the live part once
      this.#items = this.#items.slice(this.#head);
      this.#head = 0;
    }
    return x;
  }
  peek(): T | undefined {
    return this.#items[this.#head];
  }
}

/** Balanced brackets: a stack remembers the open ones. O(n). */
function isBalanced(s: string): boolean {
  const closing = new Map([[")", "("], ["]", "["], ["}", "{"]]);   // a Map: no prototype keys to trip over
  const open: string[] = [];
  for (const c of s) {
    if ("([{".includes(c)) open.push(c);
    else if (closing.has(c) && open.pop() !== closing.get(c)) return false;   // wrong or missing opener
  }
  return open.length === 0;                             // anything left open is unbalanced
}

title("STACK AND QUEUE  -  an Array is a stack; a queue needs a little more");

section("Stack (LIFO): push and pop on the end of an Array");
syntax(
  ["stack.push(x) | stack.pop() | stack.at(-1)", "push | pop (undefined when empty) | peek the top without removing it"],
  ["stack.length === 0", "isEmpty: there is no stack.isEmpty()"],
);
const stack: number[] = [];
stack.push(1);
stack.push(2);
stack.push(3);
show(() => stack);
show(() => stack.pop());                                // 3: the last one in comes out first
show(() => stack.at(-1));                               // peek: 2 stays on top
show(() => [].pop());                                   // TRAP: undefined, no exception (you cannot tell it from a stored undefined)
show(() => isBalanced("([]{})"));
show(() => isBalanced("([)]"));                         // false: the ] meets an open (
show(() => isBalanced("(("));                           // false: openers left over
show(() => isBalanced("]"));                            // false: a closer with nothing open
show(() => isBalanced(""));                             // true: nothing to match

section("Queue (FIFO): the Array version is simple, but shift is O(n)");
const naive = ["a", "b", "c"];                          // push = enqueue
naive.push("d");
show(() => naive.shift());                              // "a": dequeue from the front
show(() => naive);
note("shift moves every remaining element one slot (O(n) in the language model; V8 has shortcuts for small arrays, do not rely on them for big queues)");

section("A real queue: head index, O(1) amortized");
const queue = new Queue<string>();
queue.enqueue("a");
queue.enqueue("b");
queue.enqueue("c");
show(() => queue.size);
show(() => queue.dequeue());                            // "a": it arrived first, it leaves first
show(() => queue.peek());                               // "b": the front, not removed
show(() => queue.size);
show(() => [queue.dequeue(), queue.dequeue(), queue.dequeue()]);   // the third call finds it empty: undefined
const big = new Queue<number>();
for (let i = 0; i < 1000; i++) big.enqueue(i);
let total = 0;
while (big.size > 0) total += big.dequeue()!;
show(() => total);                                      // 499500: all 1000 values came out, in order, no shift
note("A Queue<T | undefined> cannot tell 'empty' from a stored undefined: check size first");

section("Cost");
bigO(
  ["Array push | pop | at(-1)", "O(1) (push amortized)"],
  ["Array shift | unshift", "O(n) in the model: use a Queue class when a queue has many elements"],
  ["Queue (head index) enqueue | dequeue | peek", "O(1) amortized"],
  ["isBalanced", "O(n): each character is pushed and popped at most once"],
);
