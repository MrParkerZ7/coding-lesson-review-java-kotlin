/**
 * 13d. EQUALITY AND IMMUTABILITY: value objects, equals, immutable updates, deep freeze
 *      Run: npx tsx src/13-classes-and-objects/equality-and-immutability.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("EQUALITY AND IMMUTABILITY  -  identity vs value, and updating without mutating");

section("Objects are compared by identity: write equals yourself");
syntax(
  ["a === b", "for objects: the SAME object in memory (identity), never the same contents"],
  ["a.equals(b)", "your own method: compare the fields that make up the value"],
);
class Money {
  constructor(readonly amount: number, readonly currency: string) {}
  equals(other: Money): boolean { return this.amount === other.amount && this.currency === other.currency; }
  plus(other: Money): Money { return new Money(this.amount + other.amount, this.currency); }   // a NEW value, this is untouched
}
const m1 = new Money(5, "USD");
const m2 = new Money(5, "USD");
show(() => m1 === m2);                                  // TRAP: false, two objects with identical contents
show(() => m1.equals(m2));
show(() => Object.is(m1, m1));
show(() => new Set([m1, m2]).size);                     // Set and Map compare keys by identity too: both stay
const keyOf = (m: Money): string => `${m.amount} ${m.currency}`;
show(() => new Set([m1, m2].map(keyOf)).size);          // fix: use a string KEY as the identity of a value
const textA = JSON.stringify({ x: 1, y: 2 });
const textB = JSON.stringify({ y: 2, x: 1 });
show(() => textA === textB);                            // the JSON trick depends on key order: fragile
show(() => m1.plus(m2).amount);
note("JavaScript has no built-in value-equal objects (no records, no data classes): equals or a string key is the answer");

section("Immutable updates: copy the path, share the rest");
syntax(
  ["{ ...obj, k: v }", "a new object with k replaced; one level only"],
  ["arr.with(i, v) | toSorted() | toSpliced(...) | toReversed()", "ES2023: the copying twins of the mutating array methods"],
  ["[...arr, x]  |  arr.filter(...)  |  arr.map(...)", "append | remove | change: all return a new array"],
);
const user = { name: "Ann", address: { city: "Oslo", zip: "0150" }, tags: ["a", "b"] };
const renamed = { ...user, name: "Bea" };
show(() => user.name);                                  // untouched
show(() => renamed.address === user.address);           // shared: spread is shallow, which is fine when nobody mutates
const moved = { ...user, address: { ...user.address, city: "Bergen" } };   // copy every level along the path you change
show(() => [user.address.city, moved.address.city]);
const nums = [3, 1, 2];
show(() => nums.with(0, 99));
show(() => nums.toSorted());
show(() => nums.toSpliced(1, 1));
show(() => [...nums, 4]);
show(() => nums.filter((n) => n !== 1));
show(() => nums);                                       // nothing above changed it

section("Freezing by hand: deepFreeze and DeepReadonly");
syntax(["Object.freeze is shallow", "to freeze a whole tree, freeze each object and recurse into its children"]);
type DeepReadonly<T> = { readonly [K in keyof T]: DeepReadonly<T[K]> };   // lesson 15 builds this step by step
function deepFreeze<T extends object>(value: T): DeepReadonly<T> {
  Object.freeze(value);                                 // freeze first: an already frozen object ends a cycle
  for (const key of Reflect.ownKeys(value)) {
    const child = (value as Record<PropertyKey, unknown>)[key];
    if (typeof child === "object" && child !== null && !Object.isFrozen(child)) deepFreeze(child);
  }
  return value as DeepReadonly<T>;
}
const shallowOnly = Object.freeze({ db: { host: "h", ports: [1, 2] } });
const config = deepFreeze({ db: { host: "h", ports: [1, 2] } });
show(() => Object.isFrozen(shallowOnly.db));            // false: Object.freeze stopped at the first level
show(() => Object.isFrozen(config.db.ports));           // true: every level
wontCompile("config.db.host = 'x'", "TS2540: Cannot assign to 'host' because it is a read-only property");
wontCompile("config.db.ports.push(3)", "TS2339: Property 'push' does not exist on type 'readonly number[]'");
const sneaky = config.db.ports as number[];             // the cast again: tsc is silenced, the engine is not
show(() => thrown(() => sneaky.push(3)));               // TypeError: a frozen array cannot grow

section("readonly arrays and as const are promises to tsc only");
syntax(
  ["readonly T[]  |  ReadonlyArray<T>", "no push, pop, sort ...: only the non-mutating methods exist on the type"],
  ["value as const", "deep readonly with literal types: tuples, not arrays; still nothing frozen"],
);
const list: readonly number[] = [1, 2, 3];
show(() => list.map((n) => n * 2));
wontCompile("list.push(4)", "TS2339: Property 'push' does not exist on type 'readonly number[]'");
const writable = list as number[];
writable.push(4);
show(() => list);                                       // TRAP: the "readonly" array grew; the type only forbids YOU, not the data
const settings = { mode: "dark", sizes: [1, 2] } as const;
show(() => Object.isFrozen(settings));                  // false: as const freezes nothing
wontCompile("settings.mode = 'light'", "TS2540: Cannot assign to 'mode' because it is a read-only property");
wontCompile("settings.sizes.push(3)", "TS2339: Property 'push' does not exist on type 'readonly [1, 2]'");

section("Cost");
bigO(
  ["{ ...obj, k: v }", "O(number of keys): a shallow copy"],
  ["[...arr, x] | arr.with(i, v) | toSpliced(...)", "O(n): a full copy for every update (push is O(1) amortised)"],
  ["arr.toSorted()", "O(n log n): copy, then sort"],
  ["deepFreeze(tree)", "O(size of the whole tree), once"],
);
note("Many updates to a big structure? Libraries with structural sharing (Immer, persistent collections) avoid the repeated full copies");
