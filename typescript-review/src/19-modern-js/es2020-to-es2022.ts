/**
 * 19b. ES2020 TO ES2022: optional chaining, ??, BigInt, class fields, top-level await
 *      Run: npx tsx src/19-modern-js/es2020-to-es2022.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title } from "../output";

title("ES2020 TO ES2022  -  the editions that changed everyday code");

section("The editions");
syntax(
  ["ES2020", "?. optional chaining, ?? nullish coalescing, BigInt, globalThis, Promise.allSettled, matchAll, dynamic import(), import.meta"],
  ["ES2021", "replaceAll, Promise.any (and AggregateError), ||= &&= ??=, numeric separators 1_000, WeakRef"],
  ["ES2022", "class fields, #private (and #x in obj), static blocks, top-level await, .at(), Object.hasOwn, Error cause, regex d flag"],
);

section("ES2020");
const user: { address?: { city?: string } } = {};
show(() => user.address?.city);                         // ?. stops at null or undefined: no TypeError (lesson 7)
const zero = 0;
show(() => zero ?? 5);                                  // ?? replaces only null or undefined ...
show(() => zero || 5);                                  // ... || replaces every falsy value
show(() => 2n ** 64n);                                  // BigInt: integers of any size (lesson 0)
show(() => typeof globalThis);                          // one name for the global object in Node, browsers and workers
const settled = await Promise.allSettled([Promise.resolve(1), Promise.reject(new Error("no"))]);
show(() => settled.map((r) => r.status));               // waits for ALL of them and never rejects
const digits = /\d+/g;
const runs = [..."a1b22".matchAll(digits)].map((m) => m[0]);
show(() => runs);                                       // matchAll: an iterator of every match, with its groups
const path = await import("node:path");                 // dynamic import(): load a module on demand, returns a promise
show(() => typeof path.join);
note("Also ES2020: import.meta (the module's own url) and export * as ns from '...' (lesson 20)");

section("ES2021");
show(() => "a-b-c".replaceAll("-", "+"));               // every match, no regex with the g flag needed
const firstOk = await Promise.any([Promise.reject(new Error("a")), Promise.resolve("b")]);
show(() => firstOk);                                    // the first FULFILLED one wins (race: the first to settle)
const allFail = await Promise.any([Promise.reject(new Error("a"))]).catch((e: unknown) => (e as Error).constructor.name);
show(() => allFail);                                    // every promise rejected: an AggregateError holding all reasons
const cfg: { port?: number; name: string } = { name: "" };
cfg.port ??= 3000;                                      // assign only if null or undefined
cfg.name ||= "anon";                                    // assign only if falsy
show(() => cfg);
let counter = 5;
counter &&= counter + 1;                                // assign only if truthy
show(() => counter);
show(() => 1_000_000);                                  // numeric separators: readability only

section("ES2022");
syntax(
  ["class C { field = v; #priv = v; static s = v; static { ... } }", "fields in the class body, truly private #names, static initialisation blocks"],
  ["#priv in obj", "a brand check: does this object have the private field?"],
);
class Counter {
  static created = 0;
  step = 1;                                             // public field
  #count = 0;                                           // private field: invisible outside the class body
  static { Counter.created = 100; }                     // runs once, when the class is defined
  inc(): number { this.#count += this.step; return this.#count; }
  static isCounter(o: object): boolean { return #count in o; }
}
const counter1 = new Counter();
show(() => counter1.inc());
show(() => Counter.created);
show(() => Counter.isCounter(counter1));
show(() => Counter.isCounter({}));
const awaitedAtTop = await Promise.resolve("top-level await");   // await outside a function: only in ES modules (this file is one)
show(() => awaitedAtTop);
show(() => [1, 2, 3].at(-1));                           // .at(): negative indexes count from the end (also on strings)
show(() => "abc".at(-1));
show(() => Object.hasOwn({ a: 1 }, "a"));               // the safe replacement for obj.hasOwnProperty("a")
const wrapped = new Error("could not save", { cause: new RangeError("disk full") });
show(() => wrapped.cause instanceof RangeError);        // Error cause: keep the original error when you wrap it (lesson 17)
const indexRe = /b(c)/d;
show(() => indexRe.exec("abcd")?.indices?.[1]);         // the d flag: [start, end] of every group
const original = { nested: { v: 1 } };
const copy = structuredClone(original);
show(() => copy.nested !== original.nested);            // a deep copy: Node 17 and every current browser (a web API, not ECMAScript)
note("Top-level await needs an ES module (here package.json says type: module) and a module setting of es2022 or later");
