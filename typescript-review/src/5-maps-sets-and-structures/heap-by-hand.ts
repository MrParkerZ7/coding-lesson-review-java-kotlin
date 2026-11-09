/**
 * 5g. HEAP BY HAND: JavaScript has NO PriorityQueue, so write the binary heap (the top is the smallest by a comparator)
 *     Run: npx tsx src/5-maps-sets-and-structures/heap-by-hand.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

type Compare<T> = (a: T, b: T) => number;               // negative: a comes out first; zero: tie; positive: b first

/** Binary heap in an array: parent (i - 1) >> 1, children 2i + 1 and 2i + 2. The element with the SMALLEST comparator rank is on top. */
class MinHeap<T> {
  #items: T[];
  readonly #compare: Compare<T>;

  constructor(compare: Compare<T>, items: Iterable<T> = []) {
    this.#compare = compare;
    this.#items = [...items];
    for (let i = (this.#items.length >> 1) - 1; i >= 0; i--) this.#siftDown(i);   // heapify in O(n): from the last parent up
  }
  get size(): number {
    return this.#items.length;
  }
  peek(): T | undefined {
    return this.#items[0];                              // O(1): the top is always index 0
  }
  push(x: T): void {
    this.#items.push(x);
    this.#siftUp(this.#items.length - 1);               // the new leaf climbs while it beats its parent
  }
  pop(): T | undefined {
    if (this.#items.length === 0) return undefined;
    const top = this.#items[0]!;
    const last = this.#items.pop()!;                    // the last leaf takes the root's place and sinks
    if (this.#items.length > 0) {
      this.#items[0] = last;
      this.#siftDown(0);
    }
    return top;
  }
  toArray(): T[] {
    return [...this.#items];                            // the heap ORDER (a copy), not sorted order: for the demo only
  }
  #siftUp(start: number): void {
    let i = start;
    while (i > 0) {
      const parent = (i - 1) >> 1;
      if (this.#compare(this.#items[i]!, this.#items[parent]!) >= 0) break;
      [this.#items[i], this.#items[parent]] = [this.#items[parent]!, this.#items[i]!];
      i = parent;
    }
  }
  #siftDown(start: number): void {
    let i = start;
    const n = this.#items.length;
    while (true) {
      const left = 2 * i + 1;
      const right = 2 * i + 2;
      let best = i;
      if (left < n && this.#compare(this.#items[left]!, this.#items[best]!) < 0) best = left;
      if (right < n && this.#compare(this.#items[right]!, this.#items[best]!) < 0) best = right;
      if (best === i) return;
      [this.#items[i], this.#items[best]] = [this.#items[best]!, this.#items[i]!];
      i = best;
    }
  }
}

const ascending: Compare<number> = (a, b) => a - b;
const descending: Compare<number> = (a, b) => b - a;

/** Empty the heap: the values come out in comparator order. O(n log n). */
function drain<T>(heap: MinHeap<T>): T[] {
  const out: T[] = [];
  while (heap.size > 0) out.push(heap.pop()!);
  return out;
}

/** The k largest values, biggest first: keep a MIN-heap of size k, the smallest of the k is the one to throw out. O(n log k). */
function kLargest(nums: number[], k: number): number[] {
  const heap = new MinHeap(ascending);
  for (const x of nums) {
    heap.push(x);
    if (heap.size > k) heap.pop();                      // drop the smallest of the k + 1
  }
  return drain(heap).reverse();                         // ascending out of the heap, so reverse
}

/** The k-th largest value (k = 1 is the maximum), or undefined when there are fewer than k values. O(n log k). */
function kthLargest(nums: number[], k: number): number | undefined {
  if (k < 1 || k > nums.length) return undefined;
  return kLargest(nums, k).at(-1);                      // the smallest of the k largest
}

title("HEAP BY HAND  -  priority queue with a comparator");

section("The structure");
syntax(
  ["new MinHeap<T>(compare)", "compare(a, b) < 0 means a comes out first: (a, b) => a - b is a MIN-heap, b - a a MAX-heap"],
  ["push(x) | pop() | peek() | size", "insert O(log n) | remove the top O(log n) | look at the top O(1) | count"],
  ["parent (i - 1) >> 1  |  children 2i + 1, 2i + 2", "a tree stored in an array: no node objects, no pointers"],
);
const heap = new MinHeap(ascending);
for (const x of [5, 1, 4, 2, 3]) heap.push(x);
show(() => heap.peek());                                // 1: the smallest
show(() => heap.toArray());                             // TRAP: [1, 2, 4, 5, 3]: only the top is guaranteed, the rest is NOT sorted
show(() => heap.size);
show(() => drain(heap));                                // pop until empty: sorted order, that is heap sort
show(() => heap.size);                                  // the heap is empty now
show(() => heap.pop());                                 // undefined on an empty heap, no exception
show(() => heap.peek());
const maxHeap = new MinHeap(descending, [5, 1, 4]);     // the comparator decides: the largest comes out first
show(() => maxHeap.pop());                              // 5
show(() => maxHeap.toArray());

section("Heap of objects: order by one field");
type Job = { id: string; priority: number };
const jobs = new MinHeap<Job>((a, b) => a.priority - b.priority);
jobs.push({ id: "write", priority: 3 });
jobs.push({ id: "deploy", priority: 1 });
jobs.push({ id: "test", priority: 2 });
show(() => jobs.pop()?.id);                             // "deploy": the smallest priority number
show(() => jobs.pop()?.id);
const ties = new MinHeap<Job>((a, b) => a.priority - b.priority);
for (const id of ["a", "b", "c", "d", "e"]) ties.push({ id, priority: 1 });
show(() => drain(ties).map((j) => j.id).join(""));     // TRAP: all equal, yet NOT "abcde": a heap is not stable
note("Equal priorities come out in an arbitrary order. For FIFO among equals, compare a counter too: (a.p - b.p) || (a.seq - b.seq)");
note("The heap never re-sorts when a field of a stored element changes: pop it and push it again");

section("Interview patterns: top-K, k-th largest, heap sort");
show(() => kLargest([5, 1, 9, 3, 7], 2));               // [9, 7]
show(() => kLargest([4, 4, 2], 2));                     // duplicates count separately: [4, 4]
show(() => kLargest([3, 1], 5));                        // k larger than the array: everything, biggest first
show(() => kLargest([], 3));                            // empty input
show(() => kthLargest([3, 2, 1, 5, 6, 4], 2));          // 5
show(() => kthLargest([3, 2, 3, 1, 2, 4, 5, 5, 6], 4)); // 4: duplicates count
show(() => kthLargest([1], 2));                         // undefined: fewer than k values
show(() => drain(new MinHeap(ascending, [9, -3, 7, 0, -3])));   // heap sort with negatives and a duplicate
show(() => drain(new MinHeap(ascending)));              // empty
note("Sorting everything is O(n log n); a heap of size k is O(n log k), and it works on a stream you cannot hold in memory");

section("Cost");
bigO(
  ["push | pop", "O(log n): the element climbs or sinks one level at a time"],
  ["peek | size", "O(1)"],
  ["new MinHeap(compare, items) (heapify)", "O(n), faster than n pushes (O(n log n))"],
  ["heap sort | draining the heap", "O(n log n)"],
  ["contains | remove an arbitrary element", "O(n): a heap is not searchable"],
  ["Array.sort for the k largest", "O(n log n) against the heap's O(n log k)"],
);
