/**
 * 5f. DEQUE AND BFS: a double-ended queue by hand, sliding-window maximum, breadth-first search
 *     Run: npx tsx src/5-maps-sets-and-structures/deque-and-bfs.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

type ListNode<T> = { value: T; prev: ListNode<T> | undefined; next: ListNode<T> | undefined };

/** Double-ended queue as a doubly linked list: all end operations are O(1). */
class Deque<T> {
  #first: ListNode<T> | undefined;
  #last: ListNode<T> | undefined;
  #size = 0;
  get size(): number {
    return this.#size;
  }
  peekFirst(): T | undefined {
    return this.#first?.value;
  }
  peekLast(): T | undefined {
    return this.#last?.value;
  }
  addFirst(value: T): void {
    const node: ListNode<T> = { value, prev: undefined, next: this.#first };
    if (this.#first) this.#first.prev = node;
    else this.#last = node;
    this.#first = node;
    this.#size++;
  }
  addLast(value: T): void {
    const node: ListNode<T> = { value, prev: this.#last, next: undefined };
    if (this.#last) this.#last.next = node;
    else this.#first = node;
    this.#last = node;
    this.#size++;
  }
  removeFirst(): T | undefined {
    const node = this.#first;
    if (!node) return undefined;
    this.#first = node.next;
    if (this.#first) this.#first.prev = undefined;
    else this.#last = undefined;
    this.#size--;
    return node.value;
  }
  removeLast(): T | undefined {
    const node = this.#last;
    if (!node) return undefined;
    this.#last = node.prev;
    if (this.#last) this.#last.next = undefined;
    else this.#first = undefined;
    this.#size--;
    return node.value;
  }
}

/** Sliding-window maximum with a monotonic deque of INDEXES (values decreasing). O(n): each index enters and leaves once. */
function slidingMax(nums: number[], k: number): number[] {
  if (k < 1) return [];
  const window = new Deque<number>();
  const out: number[] = [];
  for (let i = 0; i < nums.length; i++) {
    while (window.size > 0 && nums[window.peekLast()!]! <= nums[i]!) window.removeLast();   // smaller values can never win again
    window.addLast(i);
    if (window.peekFirst()! <= i - k) window.removeFirst();                                // the front slid out of the window
    if (i >= k - 1) out.push(nums[window.peekFirst()!]!);
  }
  return out;
}

type Graph = Map<string, string[]>;

/** Breadth-first search: the visit order from `start`, nearest first. O(V + E). */
function bfsOrder(graph: Graph, start: string): string[] {
  const order: string[] = [];
  const seen = new Set([start]);                        // mark when ENQUEUED, so no node enters the queue twice
  const queue = new Deque<string>();
  queue.addLast(start);
  while (queue.size > 0) {
    const node = queue.removeFirst()!;
    order.push(node);
    for (const next of graph.get(node) ?? []) {
      if (!seen.has(next)) {
        seen.add(next);
        queue.addLast(next);
      }
    }
  }
  return order;
}

/** Fewest edges from `from` to `to`, or -1: BFS finds the shortest path in an unweighted graph. O(V + E). */
function shortestSteps(graph: Graph, from: string, to: string): number {
  const dist = new Map([[from, 0]]);
  const queue = new Deque<string>();
  queue.addLast(from);
  while (queue.size > 0) {
    const node = queue.removeFirst()!;
    const d = dist.get(node)!;
    if (node === to) return d;
    for (const next of graph.get(node) ?? []) {
      if (!dist.has(next)) {
        dist.set(next, d + 1);
        queue.addLast(next);
      }
    }
  }
  return -1;
}

title("DEQUE AND BFS  -  both ends, and nearest-first search");

section("Deque: add and remove at either end in O(1)");
syntax(
  ["addFirst(x) | addLast(x)", "add at the front | at the back"],
  ["removeFirst() | removeLast()", "remove and return the front | the back (undefined when empty)"],
  ["peekFirst() | peekLast() | size", "look without removing | how many"],
);
const deque = new Deque<number>();
deque.addLast(2);
deque.addLast(3);
deque.addFirst(1);
deque.addLast(4);
show(() => deque.size);
show(() => deque.removeFirst());                        // 1: the front
show(() => deque.removeLast());                         // 4: the back
show(() => [deque.peekFirst(), deque.peekLast()]);      // [2, 3]: the two that are left, still there
show(() => [deque.removeFirst(), deque.removeFirst(), deque.removeFirst()]);   // 2, 3, then undefined: it is empty
note("A deque is a stack AND a queue at once. An Array has no O(1) removeFirst, so a deque is a small class or a linked list");

section("Sliding-window maximum: the monotonic deque");
show(() => slidingMax([1, 3, -1, -3, 5, 3, 6, 7], 3));   // the maximum of every 3 in a row
show(() => slidingMax([4, 2, 12, 3], 1));               // window 1: the array itself
show(() => slidingMax([5, 4, 3], 3));                   // one window only
show(() => slidingMax([1, 2], 5));                      // a window longer than the array: no windows
show(() => slidingMax([], 3));                          // empty input
show(() => slidingMax([-5, -2, -9], 2));                // negatives work: nothing assumes positive values

section("BFS: level by level");
const graph: Graph = new Map([["A", ["B", "C"]], ["B", ["D"]], ["C", ["D", "E"]], ["D", ["F"]], ["E", ["F"]], ["F", []]]);
show(() => bfsOrder(graph, "A"));                       // A | B C | D E | F
show(() => bfsOrder(graph, "F"));                       // no neighbours: only itself
show(() => bfsOrder(graph, "Z"));                       // an unknown start: only itself (graph.get gives undefined, ?? [])
show(() => shortestSteps(graph, "A", "F"));             // 3 edges: A, B, D, F
show(() => shortestSteps(graph, "A", "A"));             // 0: already there
show(() => shortestSteps(graph, "F", "A"));             // -1: the edges only go one way
note("Mark a node as seen when you ENQUEUE it, not when you dequeue it, or it can enter the queue many times");

section("Cost");
bigO(
  ["Deque (linked list) add | remove at either end", "O(1)"],
  ["slidingMax", "O(n) time, O(k) space (brute force: O(n * k))"],
  ["BFS | shortestSteps", "O(V + E) time, O(V) space for the queue and the seen set"],
  ["Array shift as the queue of a BFS", "O(n) per removal in the model: use a Deque or Queue (stack-and-queue.ts)"],
);
