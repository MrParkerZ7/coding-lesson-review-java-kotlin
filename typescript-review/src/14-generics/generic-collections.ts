/**
 * 14c. GENERIC COLLECTIONS: Stack<T>, Queue<T>, LRUCache<K, V>
 *      Run: npx tsx src/14-generics/generic-collections.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

/** LIFO: pop gives T | undefined because an empty stack has nothing to give. */
class Stack<T> {
  private items: T[] = [];
  push(item: T): void {
    this.items.push(item);
  }
  pop(): T | undefined {
    return this.items.pop();
  }
  peek(): T | undefined {
    return this.items.at(-1);
  }
  get size(): number {
    return this.items.length;
  }
}

/** FIFO with a head index: dequeue does not shift the array, so it is O(1) (amortized). */
class Queue<T> {
  private items: T[] = [];
  private head = 0;
  enqueue(item: T): void {
    this.items.push(item);
  }
  dequeue(): T | undefined {
    if (this.head >= this.items.length) return undefined;
    const item = this.items[this.head++];
    if (this.head * 2 >= this.items.length) {        // compact once half of the array is dead space
      this.items = this.items.slice(this.head);
      this.head = 0;
    }
    return item;
  }
  get size(): number {
    return this.items.length - this.head;
  }
}

/** A Map keeps insertion order, so the FIRST key is the least recently used one. */
class LRUCache<K, V> {
  private map = new Map<K, V>();
  constructor(private readonly capacity: number) {}
  get(key: K): V | undefined {
    if (!this.map.has(key)) return undefined;
    const value = this.map.get(key) as V;            // has() proved it is there; V itself may include undefined
    this.map.delete(key);
    this.map.set(key, value);                        // re-insert: now the most recently used
    return value;
  }
  put(key: K, value: V): void {
    this.map.delete(key);
    this.map.set(key, value);
    if (this.map.size > this.capacity) {
      const oldest = this.map.keys().next();
      if (!oldest.done) this.map.delete(oldest.value);
    }
  }
  keys(): K[] {
    return [...this.map.keys()];
  }
}

title("GENERIC COLLECTIONS  -  Stack, Queue, LRUCache");

section("Stack<T> and Queue<T>");
syntax(["class Stack<T> { push(item: T): void; pop(): T | undefined }", "the element type is fixed per instance"]);
const stack = new Stack<number>();
stack.push(1);
stack.push(2);
show(() => stack.pop());                                // LIFO: the last pushed comes first
show(() => stack.peek());                               // peek looks without removing
show(() => new Stack<string>().pop());                  // empty: undefined, no exception
wontCompile("stack.push('a');", "TS2345: Argument of type 'string' is not assignable to parameter of type 'number'");
const queue = new Queue<string>();
queue.enqueue("a");
queue.enqueue("b");
queue.enqueue("c");
show(() => queue.dequeue());                            // FIFO: the first added comes first
show(() => queue.size);                                 // 2 left; the array is only compacted now and then
note("Array.shift() moves every element left (O(n) as specified; engines optimise it): a head index makes dequeue O(1) by design");

section("LRUCache<K, V> on a Map");
syntax(["class LRUCache<K, V> { get(key: K): V | undefined; put(key: K, value: V): void }", "two type parameters: key and value"]);
const cache = new LRUCache<string, number>(2);
cache.put("a", 1);
cache.put("b", 2);
cache.get("a");                                         // touching "a" makes "b" the least recently used
cache.put("c", 3);                                      // over capacity: "b" is evicted
show(() => cache.keys());                               // oldest first: a, c
show(() => cache.get("b"));                             // evicted: undefined
note("Map keeps insertion order: delete + set moves a key to the end, keys().next() is the oldest");

section("Cost");
bigO(
  ["Stack push / pop / peek", "O(1)"],
  ["Queue enqueue / dequeue (head index)", "O(1) amortized"],
  ["LRUCache get / put (Map)", "O(1) average"],
);
