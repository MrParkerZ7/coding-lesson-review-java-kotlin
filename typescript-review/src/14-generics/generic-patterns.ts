/**
 * 14d. GENERIC PATTERNS: Result<T, E>, groupBy<T, K>, memoize, Comparator<T>
 *      Run: npx tsx src/14-generics/generic-patterns.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

/** Success or failure as a value: the discriminant `ok` narrows the union (lesson 17 builds on it). */
type Result<T, E = string> = { ok: true; value: T } | { ok: false; error: E };
const ok = <T>(value: T): Result<T, never> => ({ ok: true, value });
const err = <E>(error: E): Result<never, E> => ({ ok: false, error });

function parseAge(text: string): Result<number> {
  const n = Number(text);
  return Number.isInteger(n) && n >= 0 ? ok(n) : err(`not an age: ${text}`);
}

/** Group items by a computed key: any key type works because the buckets live in a Map. */
function groupBy<T, K>(items: readonly T[], keyOf: (item: T) => K): Map<K, T[]> {
  const groups = new Map<K, T[]>();
  for (const item of items) {
    const key = keyOf(item);
    const bucket = groups.get(key);
    if (bucket) bucket.push(item);
    else groups.set(key, [item]);
  }
  return groups;
}

/** A rest parameter typed with a type parameter keeps the argument list of fn: A is a tuple. */
function memoize<A extends unknown[], R>(fn: (...args: A) => R): (...args: A) => R {
  const cache = new Map<string, R>();
  return (...args: A): R => {
    const key = JSON.stringify(args);
    if (cache.has(key)) return cache.get(key) as R;
    const result = fn(...args);
    cache.set(key, result);
    return result;
  };
}

type Comparator<T> = (a: T, b: T) => number;
const by = <T, K extends string | number>(key: (item: T) => K): Comparator<T> => (a, b) => {
  const ka = key(a);
  const kb = key(b);
  return ka < kb ? -1 : ka > kb ? 1 : 0;
};
const reversed = <T>(cmp: Comparator<T>): Comparator<T> => (a, b) => cmp(b, a);
const thenBy = <T>(first: Comparator<T>, second: Comparator<T>): Comparator<T> => (a, b) => first(a, b) || second(a, b);

title("GENERIC PATTERNS  -  functions you write in an interview");

section("Result<T, E>: failure as a value");
syntax(["type Result<T, E = string> = { ok: true; value: T } | { ok: false; error: E }", "a discriminated union with two type parameters"]);
const good = parseAge("42");
const bad = parseAge("-5");
show(() => (good.ok ? good.value : good.error));        // narrowed by the ok field: value is a number here
show(() => (bad.ok ? bad.value : bad.error));           // and error is a string here
wontCompile("console.log(good.value);", "TS2339: Property 'value' does not exist on type 'Result<number, string>'");
note("Check `ok` first: TS only lets you read value after it knows the case");

section("groupBy<T, K>, memoize and Comparator<T>");
const words = ["fig", "kiwi", "plum", "apple", "pear"];
show(() => groupBy(words, (w) => w.length));           // K = number: Map(3) of length to words
show(() => groupBy(words, (w) => w.length > 3));        // K = boolean: any key type works in a Map
const calls: number[] = [];
const square = memoize((n: number) => {
  calls.push(n);
  return n * n;
});
square(4);
square(4);
square(5);
show(() => calls);                                      // the function body ran once per distinct argument
wontCompile("square('x');", "TS2345: Argument of type 'string' is not assignable to parameter of type 'number'");
const people = [{ name: "Bo", age: 30 }, { name: "Al", age: 30 }, { name: "Cy", age: 25 }];
const byAgeDescThenName = thenBy(reversed(by((p: (typeof people)[number]) => p.age)), by((p) => p.name));
show(() => people.toSorted(byAgeDescThenName).map((p) => p.name));   // oldest first, ties by name
note("Comparator<T> = (a: T, b: T) => number; sort() and toSorted() take exactly this type");
note("Object.groupBy / Map.groupBy exist (lesson 4): the hand-written version is what interviews ask for");

section("Cost");
bigO(
  ["groupBy over n items", "O(n)"],
  ["memoize hit", "O(1) plus the cost of JSON.stringify(args)"],
);
