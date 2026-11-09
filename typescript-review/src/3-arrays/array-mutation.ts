/**
 * 3b. ARRAY MUTATION: what changes the array in place, what returns a new one
 *     Run: npx tsx src/3-arrays/array-mutation.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title } from "../output";

title("ARRAY MUTATION  -  push, pop, splice, sort ... and their copies");

section("The ends: push, pop, shift, unshift");
syntax(
  ["a.push(x, ...) | a.pop()", "add at the end, returns the NEW LENGTH | remove the last, returns it"],
  ["a.unshift(x, ...) | a.shift()", "add at the start, returns the NEW LENGTH | remove the first, returns it"],
);
const a = [1, 2, 3];
show(() => a.push(4, 5));                               // returns the new length, not the array
show(() => a);
show(() => a.pop());                                    // returns the removed element
show(() => a.shift());
show(() => a.unshift(0, 1));                            // the new length again: [0, 1, 2, 3, 4]
show(() => a);
show(() => [].pop());                                   // undefined on an empty array, no exception
note("push returning the LENGTH (not the array) is a classic trap: a.push(x).push(y) does not chain");

section("splice: remove, insert and replace in one call");
syntax(
  ["a.splice(start, deleteCount, ...items)", "mutates a; returns an ARRAY of the removed elements"],
  ["a.slice(start, end)", "a COPY of a part (end exclusive); a is untouched"],
);
const s = [0, 1, 2, 3, 4, 5];
show(() => s.splice(1, 2));                             // removes 1 and 2, returns [1, 2]
show(() => s);
show(() => s.splice(1, 0, 10, 11));                     // deleteCount 0: pure insert, returns []
show(() => s);
show(() => s.splice(1, 1, 99));                         // remove one and insert one: a replace
show(() => s.splice(-2));                               // no deleteCount: everything from index -2 (the last 2)
show(() => s);
const t = [0, 1, 2, 3, 4, 5];
show(() => t.slice(1, 3));                              // [1, 2]: the end index is exclusive
show(() => t.slice(-2));                                // the last two
show(() => t);                                          // slice did not change t
note("slice COPIES, splice CHANGES the array: one letter apart, opposite effects");

section("Clearing and filling");
const c = [1, 2, 3];
c.length = 0;  show(() => c);                           // the fastest way to empty an array in place
show(() => [1, 2, 3, 4, 5].fill(0, 1, 3));              // fill(value, start, end): [1, 0, 0, 4, 5]
show(() => [1, 2, 3].fill(7));                          // fill changes the array and returns the SAME array

section("sort and reverse mutate AND return the same array");
const r = [3, 1, 2];
const result = r.sort((x, y) => x - y);  show(() => result === r);   // true: not a copy, the very same array
show(() => r);
const rev = [1, 2, 3];
rev.reverse();  show(() => rev);                        // in place too
show(() => [10, 9, 1].sort());                          // TRAP: the default sort compares STRINGS, so "1" < "10" < "9"
show(() => [10, 9, 1].sort((x, y) => x - y));           // the numeric comparator fixes it
show(() => ["b", "a", "C"].sort());                     // upper case letters sort before lower case (code units)
show(() => [undefined, 3, 1].sort());                   // undefined always goes last, the comparator never sees it
note("Always pass a comparator for numbers. The default order is text order: 1, 10, 9");

section("delete leaves a hole");
const d = [1, 2, 3];
delete d[1];  show(() => d);                            // TRAP: length stays 3, the slot is EMPTY (not undefined)
show(() => d.length);
note("To remove an element use splice(i, 1) (mutates) or filter (new array); never delete");

section("Non-mutating versions (ES2023): return a NEW array");
syntax(
  ["a.toSorted(cmp) | a.toReversed()", "sorted | reversed copy"],
  ["a.toSpliced(i, n, ...items) | a.with(i, x)", "splice copy | copy with one element replaced"],
);
const orig = [3, 1, 2];
show(() => orig.toSorted((x, y) => x - y));
show(() => orig.toReversed());
show(() => orig.toSpliced(1, 1));                       // [3, 2]: remove index 1
show(() => orig.with(0, 99));                           // replace index 0
show(() => orig);                                       // the original never changed
show(() => thrown(() => orig.with(5, 0)));              // with() is strict: a bad index is a RangeError
note("Before ES2023: [...a].sort(cmp), [...a].reverse(), a.slice(), a.map((v, i) => i === 0 ? 99 : v)");

section("Cost");
bigO(
  ["a[i] | a.length | push | pop", "O(1) (push is amortized O(1))"],
  ["shift | unshift | splice | insert/remove in the middle", "O(n): the elements behind move (V8 shortcuts small cases; do not rely on it)"],
  ["slice | spread copy | toSorted", "O(n) for the copy (toSorted: O(n log n))"],
  ["sort | toSorted", "O(n log n), stable"],
);
