/**
 * 11d. ARRAY AND OBJECT ODDITIES: holes, length, key order, dates, Math.max
 *      Run: npx tsx src/11-js-quirks/array-and-object-oddities.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title } from "../output";

title("ARRAY AND OBJECT ODDITIES  -  holes, length, key order, months from 0");

section("Holes: a missing slot is not undefined");
syntax(
  ["[1, , 3]", "an empty slot (a hole): the index does not exist at all"],
  ["Array(n)  |  arr[10] = x  |  delete arr[i]  |  arr.length = bigger", "the other ways to make holes"],
);
const holey = [1, , 3];
show(() => holey);                                      // util.inspect prints the hole as <1 empty item>
show(() => holey.length);                               // the hole still counts
show(() => 1 in holey);                                 // false: index 1 does not exist
show(() => holey[1]);                                   // reading a missing slot gives undefined (like a missing property)
show(() => Object.keys(holey));                         // the keys are strings, and the hole has none
show(() => holey.map((x) => (x ?? 0) * 2));             // TRAP: map and filter skip holes: the callback never runs for them
show(() => holey.filter(() => true).length);            // 2, not 3: only the real slots are visited
show(() => Array.from(holey));                          // Array.from and spread turn holes into real undefined values
const del = [1, 2, 3];
delete del[1];
show(() => del);                                        // delete removes the VALUE but not the slot: a hole, and length stays 3
show(() => del.length);
note("To remove an element for real use splice or filter (array-mutation)");

section("length is writable");
syntax(["arr.length = n", "shorter: cuts the array (the values are gone); longer: adds holes"]);
const arr = [1, 2, 3, 4, 5];
arr.length = 2;
show(() => arr);                                        // truncated for good
arr.length = 4;
show(() => arr);                                        // the old 3, 4, 5 do not come back: two holes
arr[6] = 7;
show(() => arr.length);                                 // assigning past the end grows length to index + 1
show(() => thrown(() => { arr.length = -1; }));         // a length must be an integer 0 .. 2^32 - 1
show(() => arr["1"]);                                   // index 1 is really the key "1": property names are strings
const pair = [5, 6];
// @ts-expect-error
show(() => pair["01"]);                                 // [TS refuses] undefined: only the exact text "1" matches, "01" is another key

section("Math.max, parseInt and friends");
show(() => Math.max());                                 // the neutral element of max (Math.min() is Infinity)
show(() => Math.max(...[]));                            // TRAP: max of an empty array is -Infinity, not undefined and not 0
// @ts-expect-error
show(() => Math.max([1, 5, 3]));                        // [TS refuses] forgot the spread: the array became the text "1,5,3"
// @ts-expect-error
show(() => parseInt(null));                             // [TS refuses] null became the text "null"
// @ts-expect-error
show(() => parseInt(null, 36));                         // [TS refuses] "null" is a valid base-36 number (n=23 u=30 l=21)
show(() => ["1", "2", "3"].map(parseInt));              // map passes the index as the radix (higher-order-functions)
show(() => [10, 9, 1].sort());                          // TRAP: the default sort compares TEXT, "10" < "9" (array-mutation)
show(() => Number.MAX_SAFE_INTEGER);                    // 2^53 - 1, the biggest integer a double holds exactly
show(() => Number.MAX_SAFE_INTEGER + 2);                // TRAP: the same as +1, a double cannot hold ...993 (lesson 0)

section("Key order of objects");
syntax(["Object.keys(obj)", "integer-like keys first in ASCENDING order, then the other string keys in insertion order"]);
const keys = { b: 1, 2: 1, a: 1, 1: 1 };
show(() => Object.keys(keys));                          // TRAP: not the order you wrote
const odd = { b: 1, "-1": 1, 10: 1, "01": 1, 2: 1 };
show(() => Object.keys(odd));                           // only canonical non-negative integers jump the queue: "-1" and "01" do not
show(() => JSON.stringify({ b: 1, 2: 1, a: 1 }));       // JSON follows the same order (lesson 18)
note("A Map keeps pure insertion order for every key type: use it when order matters (lesson 5)");

section("Dates: months start at 0, days at 1, weekdays at 0 = Sunday");
syntax(["new Date(Date.UTC(y, m, d))", "m is 0..11 (January = 0); d is 1..31. Always UTC in these lessons: local time depends on the machine"]);
const d = new Date(Date.UTC(2026, 10, 9));              // month 10 is NOVEMBER
show(() => d.toISOString());
show(() => d.getUTCMonth());                            // 10, not 11
show(() => d.getUTCDate());                             // the day of the month starts at 1
show(() => d.getUTCDay());                              // 1 = Monday (0 = Sunday)
const iso = (ms: number): string => new Date(ms).toISOString();
show(() => iso(Date.UTC(2026, 0, 32)));                 // day 32 of January rolls over into February: no error
show(() => iso(Date.UTC(2026, 12, 1)));                 // month 12 rolls over into the next year
show(() => iso(Date.UTC(2026, 1, 30)));                 // 30 February becomes 2 March (2026 is not a leap year)
const fromText = new Date("2026-11-09");
show(() => fromText.getTime() === d.getTime());         // a date-only ISO text is read as UTC midnight
