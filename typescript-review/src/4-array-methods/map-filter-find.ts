/**
 * 4a. MAP, FILTER, FLATMAP, FIND, SOME, EVERY: the everyday array methods
 *     Run: npx tsx src/4-array-methods/map-filter-find.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

title("MAP, FILTER, FIND  -  transform, select, test, find");

const nums = [1, 2, 3, 4, 5, 6];
const words = ["kiwi", "fig", "apple", "fig"];

section("Transform and select: map, filter");
syntax(
  ["xs.map((x, i, arr) => y)", "a NEW array of the same length; the callback gets value, index, array"],
  ["xs.filter((x, i, arr) => boolean)", "a NEW array with the elements whose callback is truthy"],
);
show(() => nums.map((x) => x * x));
show(() => words.map((w) => w.length));
show(() => nums.map((x, i) => x * i));                  // the second argument is the INDEX
show(() => nums.filter((x) => x % 2 === 0));
show(() => nums.filter((_, i) => i % 2 === 0));         // every second element, chosen by index
show(() => [0, 1, "", "a", null, undefined, NaN].filter(Boolean));   // drops every falsy value
const mixed = [1, null, 3];
const clean: number[] = mixed.filter((x) => x !== null);   // TS 5.5+ infers the type guard, so this is number[]
show(() => clean);
wontCompile("const b: number[] = [1, null].filter(Boolean)", "TS2322: filter(Boolean) does not narrow: still (number | null)[]");

section("TRAP: map(parseInt)");
show(() => ["1", "2", "3"].map(parseInt));              // [1, NaN, NaN]: map passes (value, INDEX), so it is parseInt("2", 1)
show(() => ["10", "10", "10"].map(parseInt));           // [10, NaN, 2]: radix 0 = default, 1 = invalid, 2 = binary
show(() => ["1", "2", "3"].map(Number));                // Number takes one argument: safe
show(() => ["1", "2", "3"].map((s) => parseInt(s, 10)));   // or pass the radix yourself
note("TS accepts map(parseInt): the extra arguments fit its optional radix. Write the arrow function when the callee has optional parameters");

section("Flatten: flatMap, flat");
syntax(
  ["xs.flatMap((x) => [...])", "map, then flatten ONE level: map + flat(1) in one pass"],
  ["xs.flat(depth)", "flatten nested arrays; flat(Infinity) goes all the way"],
);
show(() => [[1, 2], [3], []].flat());                   // the empty array adds nothing
show(() => ["ab", "cd"].flatMap((s) => [...s]));        // a string spreads into characters
show(() => [1, 2, 3].flatMap((a) => [10, 20].map((b) => a + b)));   // two nested loops in one line
show(() => nums.flatMap((x) => (x % 2 === 0 ? [x * 10] : [])));    // [] drops an element: filter + map in one step
show(() => [[1, [2]], [3]].flatMap((x) => x));          // flatMap goes ONE level only: [1, [2], 3]
show(() => [1, , 3].flat());                            // flat also removes holes

section("Test: some, every, includes");
show(() => nums.some((x) => x > 5));
show(() => nums.every((x) => x > 0));
show(() => [].every((x) => x > 0));                     // TRAP: true, nothing breaks the rule (vacuous truth)
show(() => [].some((x) => x > 0));                      // false: nothing can match
const calls: number[] = [];
nums.some((x) => { calls.push(x); return x === 2; });
show(() => calls);                                      // [1, 2]: some stops at the first true, every at the first false
show(() => words.includes("fig"));                      // includes takes a value, some takes a test (lesson 3)

section("Find: find, findIndex, findLast");
show(() => nums.find((x) => x > 3));
show(() => nums.find((x) => x > 99));                   // undefined, no exception
show(() => nums.findIndex((x) => x > 3));
show(() => nums.findIndex((x) => x > 99));              // -1
show(() => nums.findLast((x) => x < 3));                // searches from the end
show(() => [].at(0));                                   // the first of an empty array: undefined (no exception)
note("find returns the element itself: a found 0 is falsy, so test `!== undefined` (lesson 3)");

section("entries, keys, values: iterators, not arrays");
const abc = ["a", "b", "c"];
show(() => [...abc.entries()]);                         // [index, value] pairs
show(() => [...abc.keys()]);                            // the indexes
show(() => [...abc.values()]);                          // the values (what for...of walks)
show(() => Array.isArray(abc.entries()));               // false: an iterator, usable once (see lazy-iterators.ts)
const pairs: string[] = [];
for (const [i, v] of abc.entries()) pairs.push(i + v);
show(() => pairs);                                      // the idiom when a for...of needs the index
show(() => Object.fromEntries(abc.entries()));          // an object keyed by index (keys become strings)

section("forEach and chains");
const seen: number[] = [];
nums.forEach((x) => { if (x === 2) return; seen.push(x); });
show(() => seen);                                       // return skips one element, like continue; it can never stop the loop
show(() => [1, 2].forEach(() => 0));                    // forEach returns undefined: no chaining
const isOdd = (x: number): boolean => x % 2 === 1;
const square = (x: number): number => x * x;
show(() => nums.filter(isOdd).map(square).map(String));   // a chain: three passes, three new arrays
note("forEach cannot break and does not wait for async callbacks (lessons 2 and 16): use for...of or some");

section("Cost");
bigO(
  ["map | filter | flatMap | flat", "O(n) time and a new array each (a chain of k steps: k passes, k arrays)"],
  ["some | every | find | findIndex", "O(n) worst case; they stop at the first decision"],
  ["forEach | entries | keys | values", "O(n) / O(1) to create the iterator, nothing copied"],
  ["filter(...).length > 0", "O(n) always: use some, which can stop early"],
);
