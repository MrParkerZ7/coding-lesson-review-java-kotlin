/**
 * 4d. LAZY ITERATORS: arrays are eager, iterators and generators do the work on demand (ES2025 helpers)
 *     Run: npx tsx src/4-array-methods/lazy-iterators.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title } from "../output";

/** An endless generator: 1, 2, 3 ... It only ever computes the next value when someone asks for it. */
function* naturals(): Generator<number> {
  let n = 1;
  while (true) yield n++;
}

/** The same helpers written by hand as generators (before ES2025 this was the way). */
function* mapG<T, U>(items: Iterable<T>, f: (x: T) => U): Generator<U> {
  for (const x of items) yield f(x);
}
function* takeG<T>(items: Iterable<T>, n: number): Generator<T> {
  if (n <= 0) return;
  let seen = 0;
  for (const x of items) {
    yield x;
    if (++seen >= n) return;                            // stop BEFORE asking the source for another element
  }
}

title("LAZY ITERATORS  -  one element at a time, only when asked");

section("Eager arrays vs lazy iterators: the evaluation order");
syntax(
  ["xs.map(f).filter(g)", "EAGER: map runs on every element, builds an array, then filter runs on all of it"],
  ["Iterator.from(xs).map(f).filter(g).toArray()", "LAZY: each element goes through the whole chain before the next one starts"],
);
const eagerLog: string[] = [];
const eager = [1, 2, 3, 4]
  .map((x) => { eagerLog.push("map " + x); return x * 10; })
  .filter((x) => { eagerLog.push("filter " + x); return x > 10; })
  .slice(0, 1);
show(() => eager);                                      // [20]
show(() => eagerLog);                                   // all four maps first, then all four filters: 8 calls
const lazyLog: string[] = [];
const lazy = Iterator.from([1, 2, 3, 4])
  .map((x) => { lazyLog.push("map " + x); return x * 10; })
  .filter((x) => { lazyLog.push("filter " + x); return x > 10; })
  .take(1)
  .toArray();
show(() => lazy);                                       // the same result [20] ...
show(() => lazyLog);                                    // ... with 4 calls: it stopped as soon as take(1) was satisfied
note("Both are O(n) at worst; the lazy chain wins when it can stop early and when the intermediate arrays are big");

section("Nothing runs until something asks (generators)");
const events: string[] = [];
function* noisy(): Generator<number> {
  events.push("start");
  yield 1;
  events.push("after 1");
  yield 2;
  events.push("done");
}
const g = noisy();
show(() => events);                                     // []: calling a generator function runs NO code yet
g.next();
show(() => events);                                     // ["start"]: it ran up to the first yield and paused
g.next();
show(() => events);                                     // resumed after the first yield
show(() => [...g]);                                     // [] : it resumes after yield 2, runs the rest, has nothing more to yield
show(() => events);                                     // "done" was added when it resumed

section("Iterator helpers (ES2025, Node 22+)");
syntax(
  ["Iterator.from(iterable)", "wrap any iterable (array, string, Map, Set, generator) as an iterator with helpers"],
  ["it.map | filter | take | drop | flatMap", "LAZY: return another iterator, nothing computed yet"],
  ["it.toArray | reduce | forEach | some | every | find", "EAGER: pull values and give a result"],
);
show(() => naturals().filter((x) => x % 2 === 0).map((x) => x * x).take(3).toArray());   // an infinite source is fine: take stops it
show(() => naturals().drop(5).take(3).toArray());       // skip 5, then take 3: 6, 7, 8
show(() => naturals().find((x) => x * x > 50));         // 8: find stops at the first match
show(() => naturals().take(5).reduce((a, b) => a + b)); // 15
show(() => naturals().some((x) => x > 3));              // true: stops at 4
show(() => new Map([["a", 1], ["b", 2]]).values().map((v) => v * 2).toArray());   // Map, Set and array iterators have the helpers too
show(() => new Set([3, 1, 3]).values().toArray());      // a Set to an array: duplicates gone
show(() => Iterator.from("abc").map((c) => c.toUpperCase()).toArray());
show(() => Array.isArray(Iterator.from([1, 2])));       // false: an iterator has no length, no index, no sort
show(() => thrown(() => Iterator.from([] as number[]).reduce((a, b) => a + b)));   // TypeError: reduce of an empty iterator, like arrays
show(() => thrown(() => Iterator.from([1]).take(-1)));  // RangeError: take needs a count of 0 or more
show(() => thrown(() => Iterator.from([1]).flatMap(() => "ab").toArray()));         // TRAP: flatMap rejects strings: return [...s] instead
note("Do not call .toArray() or spread on an infinite iterator: it never finishes. Put take(n) or find first");

section("An iterator is used up after one pass");
const once = [1, 2, 3].values();
show(() => [...once]);                                  // [1, 2, 3]
show(() => [...once]);                                  // TRAP: [] , the second spread finds the iterator exhausted
const reusable = [1, 2, 3];
show(() => [...reusable].length + [...reusable].length);   // an array can be walked again and again

section("Generator pipelines by hand, and Array.from with a function");
const pipeLog: string[] = [];
const squares = mapG([1, 2, 3, 4], (x) => { pipeLog.push("sq " + x); return x * x; });
show(() => [...takeG(squares, 2)]);                     // [1, 4]
show(() => pipeLog);                                    // only 2 squares were computed
syntax(
  ["Array.from(iterable, (x, i) => y)", "convert and transform in ONE pass (no intermediate array)"],
  ["Array.from({ length: n }, (_, i) => y)", "build an array from the index"],
);
show(() => Array.from("abc", (c) => c.toUpperCase()));
show(() => Array.from(new Set([1, 2, 2, 3]), (x) => x * 10));
show(() => Array.from({ length: 4 }, (_, i) => i * i));
show(() => Array.from(new Map([["a", 1]]).entries()));  // a Map to an array of [key, value]
show(() => Array.from(naturals().take(3)));             // any iterable works

section("Cost");
bigO(
  ["array chain of k steps", "O(n * k) time, k intermediate arrays of up to n elements"],
  ["iterator chain of k steps", "O(n * k) work in ONE pass, O(1) extra space, stops early on take / find / some"],
  ["generator or iterator step", "O(1) per element plus a function call per step: measure before preferring it on small arrays"],
  ["Array.from(iterable, f)", "O(n): one pass, one array"],
);
