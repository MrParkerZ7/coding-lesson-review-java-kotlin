/**
 * 3d. SEARCH AND SORT: indexOf, includes, find, comparators, stability, binary search
 *     Run: npx tsx src/3-arrays/search-and-sort.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title, wontCompile } from "../output";

type Person = { name: string; age: number };

/** Binary search on a SORTED array: the index of target, or -1. O(log n). */
function binarySearch(sorted: number[], target: number): number {
  let lo = 0;
  let hi = sorted.length - 1;
  while (lo <= hi) {
    const mid = lo + ((hi - lo) >> 1);                  // not (lo + hi) / 2: that is a fraction in JS
    const value = sorted[mid]!;                         // ! only matters under noUncheckedIndexedAccess: mid is inside [lo, hi]
    if (value === target) return mid;
    if (value < target) lo = mid + 1;
    else hi = mid - 1;
  }
  return -1;
}

/** Lower bound: the first index whose value is >= target (the insert position); sorted.length when all are smaller. O(log n). */
function lowerBound(sorted: number[], target: number): number {
  let lo = 0;
  let hi = sorted.length;
  while (lo < hi) {
    const mid = lo + ((hi - lo) >> 1);
    if (sorted[mid]! < target) lo = mid + 1;
    else hi = mid;
  }
  return lo;
}

title("SEARCH AND SORT  -  find an element, put them in order");

section("Searching by value");
syntax(
  ["xs.indexOf(x) | xs.lastIndexOf(x)", "index of the first | last match (===), or -1"],
  ["xs.includes(x)", "true or false (SameValueZero: finds NaN)"],
  ["xs.find(f) | xs.findIndex(f)", "first element | its index; undefined | -1 when none matches"],
  ["xs.findLast(f) | xs.findLastIndex(f)", "the same from the end (ES2023)"],
);
const data = [5, 3, 8, 3, 9];
show(() => data.indexOf(3));
show(() => data.lastIndexOf(3));
show(() => data.indexOf(42));                           // -1 when missing: never test with `if (xs.indexOf(x))` (0 is falsy)
show(() => data.includes(8));
show(() => [NaN].indexOf(NaN));                         // TRAP: -1, indexOf uses === and NaN !== NaN
show(() => [NaN].includes(NaN));                        // true: includes uses SameValueZero
show(() => [{ id: 1 }].includes({ id: 1 }));            // false: objects are compared by reference
show(() => data.find((x) => x > 5));
show(() => data.findIndex((x) => x > 5));
show(() => data.findLast((x) => x < 5));                // 3: the LAST one below 5
show(() => data.findLastIndex((x) => x < 5));
show(() => data.find((x) => x > 100));                  // undefined when nothing matches
const zeroFound = [0, 1].find((x) => x === 0);  show(() => zeroFound);   // TRAP: it found 0, which is falsy
note("find returns the ELEMENT: test `found !== undefined`, not `if (found)` (0, \"\" and false are falsy)");

section("Min and max");
show(() => Math.max(...data));                          // Math.max takes numbers, not an array: spread it
show(() => Math.min(...data));
show(() => Math.max());                                 // TRAP: -Infinity for no arguments, not an error
const big = new Array<number>(1_000_000).fill(1);
show(() => thrown(() => Math.max(...big)));             // TRAP: RangeError, too many arguments (the limit depends on the engine)
const biggest = (best: number, x: number): number => (x > best ? x : best);
show(() => big.reduce(biggest, -Infinity));             // reduce has no such limit (lesson 4)

section("Sorting numbers: the comparator");
syntax(
  ["xs.sort((a, b) => a - b)", "ascending: negative = a first, 0 = tie, positive = b first"],
  ["xs.sort((a, b) => b - a)", "descending"],
  ["xs.toSorted(cmp)", "the same, on a copy (ES2023)"],
);
show(() => data.toSorted((a, b) => a - b));
show(() => data.toSorted((a, b) => b - a));
show(() => data.toSorted());                            // no comparator = text order: right here only because every number has one digit
show(() => [10, 9, 1].toSorted());                      // TRAP: text order, "1" < "10" < "9"
wontCompile("data.sort((a, b) => a > b)", "TS2345: the comparator must return a number, not a boolean");
note("A boolean comparator is a classic bug in plain JS (true is 1, false is 0, never negative): TS rejects it");

section("Sorting strings and objects");
const letters = ["b", "a", "C", "B"];
show(() => letters.toSorted());                         // code unit order: all upper case letters first, then lower case
const byText = (x: string, y: string): number => x.localeCompare(y, "en");   // always pass the locale: same result on every machine
show(() => letters.toSorted(byText));                   // dictionary order: a b B C (lower case before its upper case)
const byNumber = (x: string, y: string): number => x.localeCompare(y, "en", { numeric: true });
show(() => ["10", "9", "1"].toSorted(byNumber));        // numeric collation: "1" < "9" < "10"
const people: Person[] = [{ name: "Cid", age: 30 }, { name: "Bob", age: 25 }, { name: "Ann", age: 30 }];
const nameOf = (p: Person): string => p.name;
const byAge = (p: Person, q: Person): number => p.age - q.age;
const byAgeDesc = (p: Person, q: Person): number => q.age - p.age;
const byAgeDescThenName = (p: Person, q: Person): number => q.age - p.age || p.name.localeCompare(q.name, "en");
show(() => people.toSorted(byAge).map(nameOf));         // stable: Cid stays before Ann (both are 30)
show(() => people.toSorted(byAgeDesc).map(nameOf));     // descending: ties keep the input order
show(() => people.toSorted(byAgeDescThenName).map(nameOf));   // age descending, then name: `||` tries the next key on a tie
const byLength = (x: string, y: string): number => x.length - y.length;
show(() => ["pear", "fig", "apple"].toSorted(byLength));   // by a computed key
note("sort is STABLE since ES2019: elements that compare equal keep their input order");

section("Binary search by hand (JavaScript has none built in)");
const sorted = [1, 3, 5, 7, 9, 11];
show(() => binarySearch(sorted, 7));
show(() => binarySearch(sorted, 1));                    // first element
show(() => binarySearch(sorted, 11));                   // last element
show(() => binarySearch(sorted, 4));                    // not there: -1
show(() => binarySearch([], 4));                        // empty array: -1, the loop never runs
show(() => lowerBound([1, 2, 2, 2, 3], 2));            // 1: the FIRST of the equal values
show(() => lowerBound(sorted, 6));                      // 3: where 6 would be inserted
show(() => lowerBound(sorted, 100));                    // 6 = length: bigger than everything
note("Sort the array first, or the result is garbage. Compute mid as lo + ((hi - lo) >> 1): (lo + hi) / 2 can be a fraction");

section("Cost");
bigO(
  ["indexOf | includes | find | findIndex", "O(n): a linear scan (a Set makes membership O(1))"],
  ["sort | toSorted", "O(n log n); needs a copy for toSorted"],
  ["binary search on a sorted array", "O(log n)"],
  ["sort once + many binary searches", "O(n log n + q log n) for q queries (q linear scans: O(q n))"],
);
