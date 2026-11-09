/**
 * 2a. FOR LOOPS: classic for, for...of, for...in, forEach, ranges, labels, changing an array while looping
 *     Run: npx tsx src/2-loops/for-loops.ts        (add --plain for ASCII output)
 *     Each demo collects what the loop visits with collect((add) => { loop calling add(x) }), so the result is printed.
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

/** Runs a loop body that calls add(x) for everything it visits, and returns what was added (TypeScript has no buildList). */
function collect(body: (add: (x: unknown) => void) => void): unknown[] {
  const out: unknown[] = [];
  body((x) => out.push(x));
  return out;
}

title("FOR LOOPS  -  classic for, for...of, for...in, forEach");

section("The classic for loop and ranges");
syntax(
  ["for (let i = a; i < b; i++)", "b EXCLUDED: for (let i = 0; i < n; i++) is the index loop"],
  ["for (let i = a; i <= b; i++)", "b included"],
  ["for (let i = a; i > b; i -= step)", "counting down, or any step"],
  ["Array.from({ length: n }, (_, i) => i)", "a range as an array: JavaScript has no range literal"],
);
show(() => collect((add) => { for (let i = 1; i <= 5; i++) add(i); }));       // 1..5 with <=
show(() => collect((add) => { for (let i = 0; i < 5; i++) add(i); }));        // 0..4 with <
show(() => collect((add) => { for (let i = 5; i > 0; i--) add(i); }));        // counting down
show(() => collect((add) => { for (let i = 0; i <= 10; i += 3) add(i); }));   // a step of 3
show(() => collect((add) => { for (let i = 5; i < 3; i++) add(i); }));        // TRAP: empty, the condition is false at once (no error)
show(() => Array.from({ length: 5 }, (_, i) => i + 1));  // [1..5]: the length option plus a map function
show(() => [...Array(5).keys()]);                        // 0..4: Array(5) is empty slots, keys() still counts them
show(() => collect((add) => { for (let x = 0; x < 1; x += 0.1) add(x); }).length);   // TRAP: 11, not 10: 0.1 added ten times is 0.9999999999999999
note("Never count with a decimal: use an integer counter and divide inside the loop (lesson 0 numbers)");

section("for...of walks VALUES, for...in walks KEYS");
syntax(
  ["for (const x of iterable)", "each VALUE of an array, string, Map, Set, generator (anything iterable)"],
  ["for (const [i, x] of xs.entries())", "index and value together (the Kotlin withIndex)"],
  ["for (const key in object)", "each enumerable KEY as a string, inherited ones included"],
);
const fruits = ["apple", "banana", "cherry"];
const ages = new Map([["ann", 30], ["bob", 25]]);
show(() => collect((add) => { for (const f of fruits) add(f); }));
show(() => collect((add) => { for (const [i, f] of fruits.entries()) add(`${i}=${f}`); }));   // destructuring: the interview favourite
show(() => collect((add) => { for (const [k, v] of ages) add(`${k}=${v}`); }));           // a Map yields [key, value] pairs
show(() => collect((add) => { for (const c of "hey") add(c); }));                         // a string yields characters (by code point)
show(() => collect((add) => { for (let i = fruits.length - 1; i >= 0; i--) add(fruits[i]); }));   // backwards by index
show(() => collect((add) => { for (const i in fruits) add(i); }));                        // TRAP: the indexes as STRINGS "0", "1", "2"
show(() => collect((add) => { for (const i in fruits) add(i + 1); }));                    // TRAP: "01", not 1: i is text
const base = { inherited: 1 };
const child: Record<string, number> = Object.create(base);
child.own = 2;
show(() => collect((add) => { for (const k in child) add(k); }));                         // TRAP: also walks the prototype chain
show(() => Object.keys(child));                          // own keys only: the safe replacement (Object.entries for pairs)
wontCompile("for (const x of { a: 1 }) { }", "TS2488: must have a '[Symbol.iterator]()' method (a plain object is not iterable)");
note("Arrays and strings: for...of. Objects: Object.keys / Object.entries with for...of. for...in only to see inherited keys");

section("break, continue, labels, and forEach");
syntax(
  ["break | continue", "leave the loop | skip to the next round"],
  ["label: for (...) { break label; }", "leave (or continue) an OUTER loop"],
  ["xs.forEach((x) => { ... })", "cannot break; a return only skips to the next element; it does NOT wait for async work"],
);
const pairs: string[] = [];
outer: for (let i = 1; i <= 3; i++) {
  for (let j = 1; j <= 3; j++) {
    if (j === 2) continue outer;                         // jump to the NEXT i
    if (i === 3) break outer;                            // leave BOTH loops
    pairs.push(`(${i},${j})`);
  }
}
show(() => pairs);                                       // labels reach the outer loop
show(() => collect((add) => { for (const x of [1, 2, 3, 4]) { if (x === 2) continue; if (x === 4) break; add(x); } }));
show(() => collect((add) => { [1, 2, 3].forEach((x) => { if (x === 2) return; add(x); }); }));   // return acts like continue
wontCompile("[1, 2].forEach((x) => { if (x === 2) break; });", "TS1107: Jump target cannot cross function boundary");
const order: string[] = [];
[1, 2].forEach(async (n) => { await null; order.push(`task${n}`); });   // forEach ignores the returned promises
order.push("after forEach");
await new Promise((resolve) => setTimeout(resolve, 40));
show(() => order);                                       // the loop ended before either task finished
const order2: string[] = [];
for (const n of [1, 2]) { await null; order2.push(`task${n}`); }
order2.push("after for-of");
show(() => order2);                                      // for...of with await runs the tasks one by one (lesson 16)

section("Changing an array while looping over it");
const forward = [1, 2, 2, 3];
for (let i = 0; i < forward.length; i++) if (forward[i] === 2) forward.splice(i, 1);
show(() => forward);                                     // TRAP: [1, 2, 3]: removing shifted the next 2 into the slot just visited
const backward = [1, 2, 2, 3];
for (let i = backward.length - 1; i >= 0; i--) if (backward[i] === 2) backward.splice(i, 1);
show(() => backward);                                    // going backwards is safe: removals only shift what is already visited
show(() => [1, 2, 2, 3].filter((x) => x !== 2));         // best: build a new array (no exception, unlike Java's ConcurrentModificationException)
const grows = [1, 2];
const visitedOf = collect((add) => { for (const x of grows) { add(x); if (x < 10) grows.push(x + 10); } });
show(() => visitedOf);                                   // for...of sees elements added during the loop
const growsForEach = [1, 2];
const visitedForEach = collect((add) => { growsForEach.forEach((x) => { add(x); if (x < 10) growsForEach.push(x + 10); }); });
show(() => visitedForEach);                              // forEach visits only the original length
note("A loop that pushes while iterating can run forever: collect changes in a second array and merge after the loop");

section("Cost");
bigO(
  ["one loop over n elements", "O(n)"],
  ["two nested loops over n", "O(n^2)"],
  ["for (i = 0; i < n; i++) { for (j = i + 1; j < n; j++) }", "still O(n^2): about n*n/2 pairs"],
  ["xs.includes(x) | xs.indexOf(x) inside a loop", "O(n) each, so O(n^2) overall: use a Set (lesson 5)"],
  ["xs.splice(i, 1) inside a loop", "O(n) each: filter once instead"],
  ["a loop that halves the range each round", "O(log n)"],
);
