/**
 * 3c. COPY VS ALIAS: arrays are objects, so = copies the reference, not the elements
 *     Run: npx tsx src/3-arrays/copy-vs-alias.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("COPY VS ALIAS  -  who else sees my change?");

section("Alias: = copies the reference");
syntax(
  ["const b = a;", "b and a are TWO names for the SAME array"],
  ["a === b", "identity: are they the same array? (never compares contents)"],
);
const a = [1, 2, 3];
const b = a;
a.push(4);
show(() => b);                                          // b sees the change: one array, two names
show(() => a === b);                                    // true: the same object
const p = [1, 2];
const q = [1, 2];
show(() => p === q);                                    // TRAP: false, two different arrays with equal contents
show(() => p == q);                                     // false as well: == on objects is identity too
wontCompile("[1, 2] === [1, 2]", "TS2839: this condition will always return 'false' (compares objects by reference)");
note("There is no built-in equals for arrays: compare lengths, then every element (below)");
const sameContents = (x: number[], y: number[]): boolean => x.length === y.length && x.every((v, i) => v === y[i]);
show(() => sameContents([1, 2], [1, 2]));
show(() => sameContents([1, 2], [1, 2, 3]));
show(() => JSON.stringify(p) === JSON.stringify(q));    // a shortcut for small plain data (lesson 18)

section("Shallow copies: a new outer array");
syntax(
  ["[...a] | a.slice() | Array.from(a) | a.concat()", "four ways to copy the slots; the ELEMENTS are not copied"],
  ["structuredClone(a)", "a DEEP copy: nested arrays and objects are copied too"],
);
const orig = [1, 2, 3];
const spread = [...orig];
const sliced = orig.slice();
const viaFrom = Array.from(orig);
orig.push(4);
show(() => orig);                                       // the original grew
show(() => spread);                                     // independent: unchanged
show(() => sliced);
show(() => viaFrom);
show(() => spread === orig);                            // false: a different array

section("TRAP: a shallow copy shares the inner objects");
const grid = [[1, 2], [3, 4]];
const shallow = [...grid];                              // a new outer array, the SAME two inner arrays
shallow[0].push(99);
show(() => grid);                                       // grid changed too: the row is shared
show(() => shallow[0] === grid[0]);                     // true: one row object, two references
const people = [{ name: "Ann" }, { name: "Bob" }];
const copyPeople = [...people];
copyPeople[0].name = "ZED";
show(() => people);                                     // changed too: the same object sits inside both arrays
copyPeople.push({ name: "Cid" });
show(() => people.length);                              // but adding a slot to the copy does not touch the original

section("Deep copy: structuredClone");
const deepGrid = [[1, 2], [3, 4]];
const deep = structuredClone(deepGrid);
deep[0].push(99);
show(() => deepGrid);                                   // untouched: the rows were copied too
show(() => deep);
show(() => deep[0] === deepGrid[0]);                    // false
show(() => thrown(() => structuredClone([() => 1])));   // functions cannot be cloned: DOMException (DataCloneError)
const mixed = structuredClone([new Date(Date.UTC(2026, 10, 9)), new Set([1, 2])]);
show(() => mixed[0] instanceof Date);                   // Date, Map, Set, nested arrays survive (JSON would not keep them)
show(() => mixed[1] instanceof Set);
note("structuredClone is global in Node 17+ and browsers. JSON.parse(JSON.stringify(x)) also deep-copies but loses Date, Map, Set, undefined (lesson 18)");

section("Arrays passed to functions");
function addOne(xs: number[]): void {
  xs.push(1);                                           // changes the caller's array
}
function reassign(xs: number[]): void {
  xs = [];                                              // only rebinds the local name
  xs.push(7);
}
const data = [5];
addOne(data);
show(() => data);                                       // mutated: the function got the REFERENCE (passed by value)
reassign(data);
show(() => data);                                       // unchanged: the caller's variable still points at its array
note("JavaScript passes the reference by value. To protect the caller, copy inside: ([...xs]) or declare readonly number[]");
const frozen = Object.freeze([1, 2, 3]);
show(() => thrown(() => (frozen as number[]).push(4)));   // freeze makes push throw TypeError (cast: the type is readonly)
show(() => Object.isFrozen(frozen));
note("const only fixes the variable (lesson 1): a const array can still be pushed to. freeze is shallow");
