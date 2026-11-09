/**
 * 2c. ITERATORS AND GENERATORS: the protocol behind for...of, spread and destructuring, and lazy sequences
 *     Run: npx tsx src/2-loops/iterators-and-generators.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

/** An iterable written by hand: [Symbol.iterator]() returns an object with next(). */
class Countdown implements Iterable<number> {
  private readonly start: number;
  constructor(start: number) {
    this.start = start;
  }
  [Symbol.iterator](): Iterator<number> {
    let current = this.start;
    return {
      next: (): IteratorResult<number> => (current > 0 ? { value: current--, done: false } : { value: undefined, done: true }),
    };
  }
}

const log: string[] = [];

/** A generator: calling it runs NOTHING, each next() runs the body up to the next yield. */
function* noisy(): Generator<number, string> {
  log.push("start");
  yield 1;
  log.push("middle");
  yield 2;
  log.push("end");
  return "done";
}

/** An infinite generator: fine, because the consumer decides how much to pull. */
function* naturals(): Generator<number> {
  let n = 1;
  while (true) yield n++;
}

/** yield* delegates to another iterable and continues afterwards. */
function* both(): Generator<number> {
  yield 0;
  yield* [1, 2];
}

function* fibonacci(): Generator<number> {
  let [a, b] = [0, 1];
  while (true) {
    yield a;
    [a, b] = [b, a + b];
  }
}

/** take by hand: stops after count items without pulling one more (the source's finally blocks run on return). */
function* take<T>(source: Iterable<T>, count: number): Generator<T> {
  if (count <= 0) return;
  let taken = 0;
  for (const item of source) {
    yield item;
    if (++taken >= count) return;
  }
}

/** A generator with a finally block: it shows when the loop closes the iterator. */
function* withCleanup(): Generator<number> {
  try {
    yield 1;
    yield 2;
    yield 3;
  } finally {
    log.push("cleanup");
  }
}

title("ITERATORS AND GENERATORS  -  how for...of really works");

section("The iterable protocol");
syntax(
  ["obj[Symbol.iterator]()", "returns an ITERATOR: an object with next()"],
  ["it.next()", "{ value, done }: done is true after the last value"],
  ["for (x of it) | [...it] | const [a, b] = it | Array.from(it)", "all of them call the protocol until done"],
);
const stepper = [10, 20][Symbol.iterator]();
show(() => stepper.next());                              // { value: 10, done: false }
show(() => stepper.next());
show(() => stepper.next());                              // { value: undefined, done: true }: finished
show(() => [...new Countdown(3)]);                       // a hand-written iterable works with spread
show(() => Math.max(...new Countdown(5)));               // ... and as call arguments
const [firstOne, , thirdOne] = new Countdown(4);
show(() => [firstOne, thirdOne]);                        // ... and with destructuring: [ 4, 2 ]
show(() => [...new Map([["a", 1]])]);                    // Map: [key, value] pairs
show(() => [...new Set([3, 3, 4])]);                     // Set: unique values in insertion order
show(() => Array.from("abc"));                           // String: characters (by code point)
const used = [1, 2].values();
show(() => [...used]);
show(() => [...used]);                                   // TRAP: [], an iterator is single-use: the first spread consumed it
wontCompile("[...{ a: 1 }]", "TS2488: a plain object has no [Symbol.iterator]() (Object.entries(o) gives an array)");
note("Arrays, strings, Map, Set, typed arrays and generators are iterable; plain objects are not");

section("Generators: function* and yield");
syntax(
  ["function* name() { yield value; }", "calling it returns a generator object; the body runs only as it is pulled"],
  ["yield* other", "yield everything another iterable produces"],
  ["return value", "ends the generator: the value shows up in the last next(), never in for...of or spread"],
);
const gen = noisy();
show(() => log);                                         // []: creating the generator ran nothing
show(() => gen.next());                                  // runs up to the first yield
show(() => log);                                         // [ 'start' ]
show(() => gen.next());
show(() => gen.next());                                  // the return value arrives with done: true
show(() => log);                                         // [ 'start', 'middle', 'end' ]
show(() => [...noisy()]);                                // [ 1, 2 ]: for...of and spread drop the return value "done"
log.length = 0;
for (const x of withCleanup()) {
  if (x === 2) break;
}
show(() => log);                                         // [ 'cleanup' ]: break closes the generator, its finally block runs
show(() => [...both()]);                                 // [ 0, 1, 2 ]: yield* handed over to an array in the middle

section("Infinite sequences with take");
show(() => [...take(naturals(), 5)]);                    // [ 1, 2, 3, 4, 5 ]: only 5 values were ever computed
show(() => [...take(fibonacci(), 8)]);                   // the Fibonacci numbers without a limit in the generator
show(() => [...take(naturals(), 0)]);                    // []
show(() => naturals().take(3).toArray());                // ES2025 iterator helpers do the same (lesson 4)
note("Never spread or Array.from an infinite generator: [...naturals()] runs until memory is gone");
note("Async generators (async function*) and for await...of: lesson 16");

section("Cost");
bigO(
  ["for...of | spread | Array.from over n items", "O(n) time"],
  ["[...iterable]", "O(n) memory: the whole sequence becomes an array"],
  ["a generator consumed lazily", "O(1) memory: one value at a time"],
  ["take(infinite, k)", "O(k): only k values are computed"],
);
