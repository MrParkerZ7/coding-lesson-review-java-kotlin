/**
 * 10c. CLOSURES: a function that remembers the variables around it
 *      Run: npx tsx src/10-functions-and-closures/closures.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title } from "../output";

title("CLOSURES  -  private state, loops, memoize, once, currying");

section("A closure keeps a live link to the variables around it");
syntax(
  ["function outer() { let x = 0; return () => ++x; }", "the inner function keeps x alive after outer has returned"],
  ["const f = outer();  const g = outer();", "every call makes its OWN x: f and g do not share"],
);
function makeCounter(start = 0): () => number {
  let count = start;                                    // lives as long as the returned function is reachable
  return () => ++count;
}
const a = makeCounter();
const b = makeCounter(100);
show(() => a());                                        // first call
show(() => a());                                        // the same count, remembered between calls
show(() => b());                                        // b has its own count, started at 100
show(() => a());                                        // a was not touched by b
note("A closure captures the VARIABLE, not a copy of its value (see 'stale closure' below)");

section("Private state without classes");
function makeAccount(initial: number) {
  let balance = initial;                                // no way to reach it except through the methods
  return {
    deposit(amount: number): number { balance += amount; return balance; },
    getBalance(): number { return balance; },
  };
}
const account = makeAccount(10);
show(() => account.deposit(5));
show(() => account.getBalance());
show(() => "balance" in account);                       // false: it is not a property at all
show(() => Object.keys(account));                       // only the methods are visible
note("Classes do the same with #private fields (lesson 13); closures were the only way before ES2022");

section("The classic loop trap: var shares one variable, let makes one per iteration");
syntax(
  ["for (var i = 0; ...) fns.push(() => i)", "ONE i for the whole loop: every closure sees its final value"],
  ["for (let i = 0; ...) fns.push(() => i)", "a fresh i per iteration: every closure keeps its own"],
);
const withVar: Array<() => number> = [];
for (var v = 0; v < 3; v++) withVar.push(() => v);
const gotVar = withVar.map((fn) => fn());
show(() => gotVar);                                     // TRAP: all three closures read the one v after the loop ended
const withLet: Array<() => number> = [];
for (let l = 0; l < 3; l++) withLet.push(() => l);
const gotLet = withLet.map((fn) => fn());
show(() => gotLet);                                     // one l per iteration
const withIife: Array<() => number> = [];
for (var w = 0; w < 3; w++) ((copy: number) => { withIife.push(() => copy); })(w);
const gotIife = withIife.map((fn) => fn());
show(() => gotIife);                                    // the pre-ES2015 fix: an IIFE gives each closure its own copy
note("Timers in a loop (setTimeout inside for-var) are the same trap: lesson 16");

section("memoize: a cache hidden in a closure");
syntax(["const fast = memoize(slowFn)", "same results, but each distinct argument list is computed once"]);
function memoize<A extends unknown[], R>(fn: (...args: A) => R): (...args: A) => R {
  const cache = new Map<string, R>();                   // private to this memoized function
  return (...args: A): R => {
    const key = JSON.stringify(args);                   // arguments become a text key
    if (cache.has(key)) return cache.get(key) as R;
    const result = fn(...args);
    cache.set(key, result);
    return result;
  };
}
let squareCalls = 0;
const square = memoize((n: number): number => { squareCalls++; return n * n; });
show(() => square(9));
show(() => square(9));                                  // answered from the cache
show(() => squareCalls);                                // the body ran once
let slowCalls = 0;
const fibSlow = (n: number): number => { slowCalls++; return n < 2 ? n : fibSlow(n - 1) + fibSlow(n - 2); };
let fastCalls = 0;
const fibFast = memoize((n: number): number => { fastCalls++; return n < 2 ? n : fibFast(n - 1) + fibFast(n - 2); });
show(() => fibSlow(20));
show(() => slowCalls);                                  // exponential: the same n is recomputed again and again
show(() => fibFast(20));
show(() => fastCalls);                                  // linear: n = 0..20 computed once each
show(() => JSON.stringify([undefined]));                // TRAP: "[null]", the key of f(undefined) equals the key of f(null)

section("once: run a function only the first time");
function once<A extends unknown[], R>(fn: (...args: A) => R): (...args: A) => R {
  let done = false;
  let result: R | undefined;
  return (...args: A): R => {
    if (!done) { result = fn(...args); done = true; }
    return result as R;
  };
}
let initCalls = 0;
const init = once((name: string) => { initCalls++; return `ready ${name}`; });
show(() => init("db"));
show(() => init("cache"));                              // ignored: the first result is returned again
show(() => initCalls);                                  // the function body ran once

section("Currying and partial application");
syntax(
  ["const f = (a) => (b) => a + b", "currying: one argument at a time, each step returns a function"],
  ["partial(fn, a)", "fix some arguments now, supply the rest later"],
);
const curry2 = <A, B, R>(fn: (x: A, y: B) => R) => (x: A) => (y: B): R => fn(x, y);
const add = (x: number, y: number): number => x + y;
show(() => curry2(add)(1)(2));                          // one argument at a time
const add5 = curry2(add)(5);                            // a function waiting for the second number
show(() => add5(10));
const partial = <A, Rest extends unknown[], R>(fn: (x: A, ...rest: Rest) => R, x: A) => (...rest: Rest): R => fn(x, ...rest);
const add7 = partial(add, 7);
show(() => add7(3));
show(() => [1, 2, 3].map(add7));                        // map also passes index and array: add ignores the extra arguments
note("fn.bind(null, 7) does the same partial application (this-and-binding)");

section("Stale closure: a captured value does not follow the variable");
let price = 10;
const readPrice = (): number => price;                  // reads the variable on every call
price = 20;
show(() => readPrice());                                // the closure sees the later change
const state = { count: 0 };
const snapshot = state.count;                           // a COPY of the value, taken now
const readStale = (): number => snapshot;
const readLive = (): number => state.count;
state.count = 5;
show(() => readStale());                                // stale: it kept the old value
show(() => readLive());                                 // live: it reads state.count on every call
note("A React callback created in an earlier render keeps THAT render's values: the same 'stale closure' bug");
note("Cost: a closure keeps everything it captured alive until the closure itself is unreachable (watch big arrays and DOM nodes)");
