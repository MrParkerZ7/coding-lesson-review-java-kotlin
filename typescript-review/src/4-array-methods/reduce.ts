/**
 * 4b. REDUCE: collapse an array into one value (a number, a string, an object, a Map, another array)
 *     Run: npx tsx src/4-array-methods/reduce.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("REDUCE  -  fold many elements into one value");

const nums = [1, 2, 3, 4];
const words = ["kiwi", "fig", "apple", "fig"];

section("The shape: reduce((acc, x, i, arr) => next, initial)");
syntax(
  ["xs.reduce((acc, x) => next, initial)", "start from initial; the result has the type of initial"],
  ["xs.reduce((acc, x) => next)", "no initial: the FIRST element is the start and the loop begins at index 1"],
  ["xs.reduceRight((acc, x) => next, initial)", "the same, walking from the last element to the first"],
);
show(() => nums.reduce((acc, x) => acc + x, 0));        // sum
show(() => nums.reduce((acc, x) => acc * x, 1));        // product: the start is 1, not 0
show(() => nums.reduce((acc, x) => acc + x));           // no initial: starts with 1, adds 2, 3, 4
show(() => nums.reduce((acc, x) => acc + x, ""));       // the initial value sets the type: a STRING "1234"
show(() => nums.reduce((best, x) => Math.max(best, x), -Infinity));   // a hand-made max; -Infinity is its neutral start
show(() => words.reduce((n, w) => n + w.length, 0));    // total letters: the element type (string) differs from the result (number)
show(() => nums.reduce((acc, x, i) => acc + x * i, 0)); // the third argument is the index: 0*1 + 1*2 + 2*3 + 3*4

section("TRAP: reduce on an empty array");
const empty: number[] = [];
const add = (a: number, b: number): number => a + b;
show(() => empty.reduce(add, 0));                       // with an initial value: just returns it
show(() => thrown(() => empty.reduce(add)));            // TypeError: reduce of empty array with no initial value
show(() => [7].reduce((acc, x) => acc + x * 100));      // one element and no initial: the callback never runs, 7 comes back
show(() => empty.reduce(add, 0) / empty.length);        // 0 / 0 is NaN: an average of nothing needs its own guard
note("Always give an initial value unless the array is known to be non-empty");

section("Order matters: reduce vs reduceRight");
show(() => nums.reduce((acc, x) => acc - x));           // ((1 - 2) - 3) - 4 = -8
show(() => nums.reduceRight((acc, x) => acc - x));      // ((4 - 3) - 2) - 1 = -2
show(() => nums.reduce((acc, x) => "(" + acc + x + ")", ""));        // the oldest result is the innermost
show(() => nums.reduceRight((acc, x) => acc + x, ""));  // "4321": a string reversed (reduce would give "1234")
show(() => [..."abc"].reduce((acc, ch) => ch + acc, ""));   // reverse a string by prepending

section("Building objects, Maps and arrays");
const counts = words.reduce((acc, w) => acc.set(w, (acc.get(w) ?? 0) + 1), new Map<string, number>());
show(() => counts);                                     // word frequencies in a Map (insertion order)
const byLength = words.reduce<Record<number, string[]>>((acc, w) => {
  (acc[w.length] ??= []).push(w);                       // create the list on first use, then push
  return acc;
}, {});
show(() => byLength);                                   // group by length into an object (next file: grouping-and-counting.ts)
const rows = [[1, 2], [3], [4, 5]];
show(() => rows.reduce((acc, row) => acc.concat(row), []));   // flatten one level (flat() does it today)
const addUnique = (acc: number[], x: number): number[] => (acc.includes(x) ? acc : [...acc, x]);
show(() => [3, 1, 3, 2, 1].reduce(addUnique, []));      // unique values, first-seen order kept
const pipeline = [(x: number) => x + 1, (x: number) => x * 2, (x: number) => x - 3];
show(() => pipeline.reduce((value, fn) => fn(value), 5));   // function pipeline: ((5 + 1) * 2) - 3 = 9
wontCompile("[1, 2].reduce((acc, x) => { acc[x] = x; return acc; }, {})", "TS7053: no index signature on type '{}': the initial value fixes the accumulator type");
note("The accumulator type comes from the initial value. For an object, give the type: reduce<Record<string, number>>(f, {}) or {} as Record<...>");

section("Running totals: an accumulator that is an array");
const prefix = nums.reduce<number[]>((acc, x) => {
  acc.push((acc.at(-1) ?? 0) + x);                      // the last total plus x
  return acc;
}, []);
show(() => prefix);                                     // prefix sums: [1, 3, 6, 10]
const bestSoFar = (acc: number[], x: number): number[] => {
  acc.push(Math.max(x, acc.at(-1) ?? -Infinity));
  return acc;
};
show(() => [3, 1, 4, 1, 5].reduce(bestSoFar, []));      // the best value seen so far, at every position
note("[...acc, x] copies the whole accumulator on EVERY step: O(n^2). Push onto acc and return it: O(n)");

section("Cost");
bigO(
  ["reduce | reduceRight", "O(n) time, one pass; O(1) extra space for a number accumulator"],
  ["reduce into a Map or object", "O(n) time, O(distinct keys) space"],
  ["reduce with [...acc, x] or acc.concat(x)", "O(n^2): a new array every step"],
  ["reduce vs a for...of loop", "the same cost; the loop is easier to read once the callback needs braces and a return"],
);
