/**
 * 19c. ES2023 TO ES2025 AND TYPESCRIPT 5.x: the newest features, each run on Node 24
 *      Run: npx tsx src/19-modern-js/es2023-to-es2025.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

title("ES2023 TO ES2025 AND TYPESCRIPT 5.x  -  what is new, and how to prove it runs");

section("The editions");
syntax(
  ["ES2023", "findLast/findLastIndex, toSorted/toReversed/toSpliced/with (copying array methods), hashbang comments"],
  ["ES2024", "Object.groupBy / Map.groupBy, Promise.withResolvers, isWellFormed, resizable and transferable ArrayBuffer, the v regex flag"],
  ["ES2025", "iterator helpers, Set methods (union ...), Promise.try, RegExp.escape, import attributes (JSON modules)"],
  ["Array.fromAsync", "the newest: it runs on Node 24, but this project's tsc lib has no types for it (see below)"],
);
note("Every line below ran on Node 24: a feature in the spec is not always in your runtime, so check the engine (node.green, MDN) before using one");

section("ES2023: the copying array methods and findLast");
const nums = [5, 1, 4];
show(() => nums.findLast((n) => n > 3));                // search from the END: 4
show(() => nums.findLastIndex((n) => n > 3));
show(() => nums.toSorted((p, q) => p - q));             // a sorted COPY: sort() mutates, toSorted() does not
show(() => nums.toReversed());
show(() => nums.toSpliced(1, 1, 9, 9));                 // splice without the mutation
show(() => nums.with(1, 0));                            // replace one element in a copy
show(() => nums);                                       // untouched by all of the above

section("ES2024: groupBy and withResolvers");
const people = [{ name: "Ann", age: 30 }, { name: "Bob", age: 25 }, { name: "Cy", age: 30 }];
const byAge = Object.groupBy(people, (p) => String(p.age));
show(() => Object.keys(byAge));                         // a null-prototype object; integer-like keys come out ascending (lesson 11)
show(() => byAge["30"]?.map((p) => p.name));
const byAgeMap = Map.groupBy(people, (p) => p.age);     // Map keeps any key type and the insertion order
show(() => [...byAgeMap.keys()]);
const { promise, resolve } = Promise.withResolvers<string>();   // a promise and its resolve function, no executor callback
resolve("settled later");
const resolved = await promise;
show(() => resolved);
const lone = "ab" + String.fromCharCode(0xd800);        // a lone surrogate: broken UTF-16
show(() => lone.isWellFormed());                        // ES2024 check for text that JSON or URLs would choke on
show(() => "ab".isWellFormed());

section("ES2025: Set methods and iterator helpers");
const left = new Set([1, 2, 3]);
const right = new Set([2, 3, 4]);
show(() => left.union(right));
show(() => left.intersection(right));
show(() => left.difference(right));
show(() => left.symmetricDifference(right));
show(() => left.isSubsetOf(new Set([1, 2, 3, 4])));
function* naturals(): Generator<number> { let n = 1; while (true) yield n++; }   // infinite: only a lazy pipeline can use it
const evenSquares = naturals().filter((n) => n % 2 === 0).map((n) => n * n).take(3).toArray();
show(() => evenSquares);                                // helpers work on any iterator and stay lazy until toArray
const middle = Iterator.from([1, 2, 3, 4]).drop(1).take(2).toArray();
show(() => middle);
note("Iterator helpers: lesson 4 (lazy-iterators) compares them with arrays and records the evaluation order");

section("Newest: no types in this project's lib, so a cast");
const ArrayNext = Array as unknown as { fromAsync<T>(source: AsyncIterable<T>): Promise<T[]> };
async function* numbers(): AsyncGenerator<number> { yield 1; yield 2; }
const collected = await ArrayNext.fromAsync(numbers());   // Array.from for async iterables
show(() => collected);
const PromiseNext = Promise as unknown as { try(fn: () => unknown): Promise<unknown> };
const outcome = await PromiseNext.try(() => JSON.parse("{")).catch((e: unknown) => (e as Error).constructor.name);
show(() => outcome);                                    // Promise.try (ES2025): a throw inside becomes a rejection
wontCompile("Array.fromAsync(gen())", "TS2550: Property 'fromAsync' does not exist (add lib ESNext.Array; passes with --lib ES2024,ESNext.Array)");
wontCompile("Promise.try(() => 1)", "TS2339: Property 'try' does not exist on type 'PromiseConstructor' (add lib ESNext.Promise)");

section("TypeScript 5.x: what the compiler added");
syntax(
  ["4.9  satisfies", "check a value against a type but keep its narrow inferred type (lesson 12)"],
  ["5.0  const type parameters", "function f<const T>(x: T): infer literals and tuples instead of widening"],
  ["5.0  standard decorators, union enums, verbatimModuleSyntax", "ES decorators; every enum is a union of its members; explicit type-only imports"],
  ["5.2  using / await using", "explicit resource management: Symbol.dispose runs when the scope ends"],
  ["5.4  NoInfer<T>", "keep a parameter out of inference"],
  ["5.5  inferred type predicates", "arr.filter((x) => x !== undefined) is typed T[] with no hand-written guard"],
  ["5.8  --erasableSyntaxOnly", "reject syntax that needs code generation (enum, namespace, parameter properties)"],
);
function tuple<const T extends readonly unknown[]>(items: T): T { return items; }
function loose<T extends readonly unknown[]>(items: T): T { return items; }
const exact = tuple(["a", 1]);                          // typed readonly ["a", 1]
const widened = loose(["a", 1]);                        // typed (string | number)[]
show(() => widened);                                    // the same value at run time: only the TYPE differs, and types are erased
const pinned: readonly ["a", 1] = exact;
show(() => pinned);
wontCompile("const bad: readonly ['a', 1] = loose(['a', 1])", "TS2322: Type '(string | number)[]' is not assignable to type 'readonly [\"a\", 1]'");
const light = <C extends string>(colors: C[], start?: NoInfer<C>): C | undefined => start ?? colors[0];
show(() => light(["red", "green"], "green"));
wontCompile("light(['red', 'green'], 'blue')", "TS2345: Argument of type '\"blue\"' is not assignable to '\"red\" | \"green\" | undefined'");
const maybe: Array<number | undefined> = [1, undefined, 3];
const defined: number[] = maybe.filter((n) => n !== undefined);   // an error before TS 5.5: you had to write (n): n is number
show(() => defined);
const events: string[] = [];
function useResource(): void {
  using res = { [Symbol.dispose]() { events.push("disposed"); } };   // disposed when the block ends, even on throw
  events.push("working");
  void res;
}
useResource();
show(() => events);
wontCompile("enum Color { Red }  // with --erasableSyntaxOnly", "TS1294: This syntax is not allowed when 'erasableSyntaxOnly' is enabled");
note("The flag exists for Node's type stripping: node file.ts (Node 24) just erases the types, but an enum needs generated code and stops with ERR_UNSUPPORTED_TYPESCRIPT_SYNTAX");
