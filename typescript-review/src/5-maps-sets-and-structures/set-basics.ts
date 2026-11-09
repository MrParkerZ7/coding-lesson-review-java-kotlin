/**
 * 5a. SETS: unique values and fast membership tests
 *     Run: npx tsx src/5-maps-sets-and-structures/set-basics.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

title("SETS  -  no duplicates, O(1) has");

section("Creating and changing");
syntax(
  ["new Set<T>(iterable?)", "a Set; the duplicates of the iterable are dropped"],
  ["s.add(x) | s.delete(x) | s.clear()", "add (returns the SET, so calls chain) | remove (returns true if it was there) | empty"],
  ["s.has(x) | s.size", "membership test, O(1) | number of values (a property, not size())"],
);
const ids = new Set([3, 1, 3, 2]);
show(() => ids);                                        // duplicates dropped, insertion order kept
show(() => ids.size);
show(() => ids.add(5).add(1));                          // add returns the Set itself: 1 was already there, nothing changes
show(() => ids.delete(3));                              // true: it was there
show(() => ids.delete(3));                              // false: not there any more
show(() => ids.has(2));
show(() => [...ids]);                                   // a Set is iterable: spread it into an array
show(() => [...new Set([3, 3, 1, 3, 2, 1])]);           // THE de-duplicate idiom
show(() => new Set("hello").size);                      // a string is iterable: 4 distinct characters
wontCompile("ids.add('x')", "TS2345: argument of type 'string' is not assignable to parameter of type 'number'");

section("Which values count as equal? (SameValueZero)");
show(() => new Set([NaN, NaN]).size);                   // 1: unlike ===, a Set treats NaN as equal to NaN
show(() => new Set([0, -0]).size);                      // 1: 0 and -0 are the same value
show(() => new Set([1, "1"]).size);                     // 2: no conversion, 1 and "1" are different
show(() => new Set([{ a: 1 }, { a: 1 }]).size);         // TRAP: 2, objects (and arrays) are compared by REFERENCE
const shared = { a: 1 };
show(() => new Set([shared, shared]).size);             // 1: the very same object
note("To de-duplicate objects by content, key them: a Map keyed by JSON.stringify(x) or by an id field (counting-and-grouping.ts)");

section("Order");
const order = new Set([1, 2, 3]);
order.add(2);
show(() => [...order]);                                 // adding an existing value does NOT move it
order.delete(2);
order.add(2);
show(() => [...order]);                                 // delete then add puts it at the END: insertion order
show(() => [...new Set([5, 1, 3])].toSorted((a, b) => a - b));   // a Set never sorts: sort a copy when you need order
show(() => [...new Set([1, 2]).entries()]);             // entries() gives [value, value] pairs: a Set is a Map whose key is its value
note("Iteration is in insertion order, never sorted; the standard library has no sorted set (sort a copy, or keep a sorted array)");

section("Set algebra: ES2025 methods (Node 22+)");
syntax(
  ["a.union(b) | a.intersection(b)", "everything | only what both have"],
  ["a.difference(b) | a.symmetricDifference(b)", "in a but not b | in exactly one of them"],
  ["a.isSubsetOf(b) | a.isSupersetOf(b) | a.isDisjointFrom(b)", "all in b | contains all of b | nothing in common"],
);
const a = new Set([1, 2, 3]);
const b = new Set([3, 4]);
show(() => a.union(b));
show(() => a.intersection(b));
show(() => a.difference(b));
show(() => a.symmetricDifference(b));
show(() => a.isSubsetOf(new Set([1, 2, 3, 4])));
show(() => a.isDisjointFrom(new Set([9])));
show(() => new Set([...a, ...b]));                      // the manual union: spread both into one Set
show(() => new Set([...a].filter((x) => b.has(x))));    // the manual intersection: filter with has (the older way)
show(() => new Set([...a].filter((x) => !b.has(x))));   // the manual difference
show(() => a.size === b.size && a.isSubsetOf(b));       // Set equality by hand: there is no equals, and a === b is identity

section("Two classic uses");
show(() => [1, 2, 3, 1].length !== new Set([1, 2, 3, 1]).size);   // contains a duplicate? compare the sizes
const seen = new Set<number>();
let firstDup: number | undefined;
for (const x of [4, 2, 7, 2, 4]) {
  if (seen.has(x)) { firstDup = x; break; }
  seen.add(x);
}
show(() => firstDup);                                   // 2: the first element that was already seen
const letters = new Set("hello");
show(() => letters.has("l"));                           // O(1), where array.includes is O(n)
note("JS add() returns the Set, not a Boolean: to know if a value was new, call has first, or compare size before and after");

section("Cost");
bigO(
  ["Set add | has | delete", "O(1) on average"],
  ["new Set(xs) | [...s] | forEach", "O(n)"],
  ["union | intersection | difference", "O(n + m)"],
  ["array.includes(x)", "O(n): a Set is the fix for repeated lookups"],
  ["Set of objects", "compared by reference: the same shape is NOT the same value"],
);
