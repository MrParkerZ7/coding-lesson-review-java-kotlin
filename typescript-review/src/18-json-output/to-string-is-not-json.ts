/**
 * 18a. String(obj) IS NOT JSON
 *      Run: npx tsx src/18-json-output/to-string-is-not-json.ts        (add --plain for ASCII output)
 */
import { inspect } from "node:util";
import { note, section, show, syntax, thrown, title } from "../output";
import { sampleUser } from "./sample-data";

/** A class with its own toString: nicer for logs, still not JSON. */
class Point {
  constructor(
    public x: number,
    public y: number,
  ) {}
  toString(): string {
    return `Point(${this.x},${this.y})`;
  }
}

title("TO JSON  -  console.log(obj) and String(obj) are not JSON");

section("What String, templates and console.log give you");
const user = sampleUser();
show(() => String(user));                               // the default toString of any plain object
show(() => `${user}`);                                  // a template literal calls the same toString
show(() => "user: " + user);                            // so does + with a string: the classic accident in a log line
show(() => String([1, [2, 3]]));                        // an array joins its items with commas: no brackets, nested arrays flattened
show(() => String(["a", "b"]));                         // the quotes around the strings are gone as well
show(() => String(new Map([["a", 1]])));                // a Map does not even list its entries
show(() => String(null));                               // "null" the text, which only LOOKS like JSON null
show(() => String(undefined));                          // "undefined": not valid JSON at all
const compact = inspect(user, { breakLength: Infinity });
show(() => compact);                                    // what console.log(user) prints (util.inspect): single quotes, bare keys
show(() => compact.includes('"'));                      // no double quote anywhere: invalid JSON
show(() => thrown(() => JSON.parse(compact)));          // proof: a JSON parser rejects it
const point = new Point(1, 2);
show(() => String(point));                              // your own toString decides what String() gives
show(() => JSON.stringify(point));                      // JSON.stringify never calls toString: it reads the fields (toJSON is its hook)

section("What JSON needs");
syntax(
  ['{ "key": value, ... }', "object: every key is a double-quoted string, then a colon"],
  ["[ value, value ]", "array: commas BETWEEN items, never after the last"],
  ['"text"', "string: double quotes only; escape the quote, the backslash and newlines"],
  ["123 | 1.5 | true | false | null", "number, boolean, null: lowercase; no NaN, no Infinity, no undefined"],
);
note("Interview trap: console.log(user) shows { id: 1, name: 'Ann' }: that is JavaScript inspect format, not JSON. Say it before you print.");
note("The same is true of String(obj), Array.prototype.toString and every collection: they are for humans and logs");

section("Three ways to get real JSON (the next files)");
syntax(
  ["JSON.stringify(value, replacer?, space?)", "built in, no import, on any judge: json-stringify"],
  ["JSON.parse(text, reviver?)", "the way back, with its own traps: json-parse"],
  ["toJson(value: unknown): string", "hand-written: escape + one recursive function, when the interviewer asks you to build it: hand-written-json"],
);
note("Unlike Java and Kotlin, JavaScript needs no library: JSON is a global object. Learn what JSON.stringify drops, then the hand-written one");
