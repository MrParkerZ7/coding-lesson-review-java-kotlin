/**
 * 18b. JSON.stringify(value, replacer, space): the built-in way to JSON, and everything it silently drops
 *      Run: npx tsx src/18-json-output/json-stringify.ts        (add --plain for ASCII output)
 */
import { block, note, section, show, syntax, thrown, title } from "../output";
import { sampleUser } from "./sample-data";

/** Only OWN ENUMERABLE data properties reach the JSON: the private field, the getter and the method do not. */
class Account {
  owner = "Ann";
  #pin = 1234;
  get label(): string {
    return `account of ${this.owner}`;
  }
  describe(): string {
    return `${this.owner}:${this.#pin}`;
  }
}

/** toJSON is the hook: JSON.stringify calls it and serializes what it returns. */
class Temperature {
  constructor(readonly celsius: number) {}
  toJSON(): { c: number } {
    return { c: this.celsius };
  }
}

const circular: { name: string; self?: unknown } = { name: "loop" };
circular.self = circular;

title("JSON.stringify  -  the built-in way, and what it drops");

section("The three arguments");
syntax(
  ["JSON.stringify(value)", "compact text, no spaces"],
  ["JSON.stringify(value, null, 2)", "pretty: 2 spaces per level (a number up to 10, or a string such as '\\t')"],
  ["JSON.stringify(value, ['k1', 'k2'])", "replacer ARRAY: keep only these keys, at every level"],
  ["JSON.stringify(value, (key, value) => ...)", "replacer FUNCTION: called for every key, return the value to write (undefined drops the key)"],
);
const user = sampleUser();
show(() => JSON.stringify(user));                       // one line: the format to send over a network
block(JSON.stringify(user, null, 2));
show(() => JSON.stringify({ a: 1 }, null, "--"));      // a string can be the indent too
show(() => JSON.stringify(user, ["id", "name"]));       // an allowlist of keys
show(() => JSON.stringify(user, ["id", "address", "city"]));   // TRAP: it applies at EVERY level, so the nested keys must be listed too

section("Replacer function");
const visited: string[] = [];
JSON.stringify({ a: { b: 1 } }, (key, value) => {
  visited.push(key);
  return value;
});
show(() => visited);                                    // the first call has the empty key and the whole value; parents before children
show(() => JSON.stringify({ id: 1, password: "secret" }, (key, value) => (key === "password" ? undefined : value)));   // redact a field
show(() => JSON.stringify({ price: 9.99, qty: 2 }, (_key, value) => (typeof value === "number" ? Math.round(value) : value)));
show(() => JSON.stringify({ big: 10n }, (_key, value) => (typeof value === "bigint" ? value.toString() : value)));   // the standard bigint fix: write it as a string
note("A function replacer is also the place to turn a Map into an object or a Set into an array before they are lost");

section("Silently dropped or changed (no error, no warning)");
show(() => JSON.stringify({ a: undefined, b: 1 }));     // undefined in an object: the key disappears
show(() => JSON.stringify([undefined, () => 1, Symbol("s")]));   // in an ARRAY the same values become null, to keep the positions
show(() => JSON.stringify({ f: () => 1, s: Symbol("s") }));   // function and symbol values vanish from objects
show(() => JSON.stringify({ n: NaN, i: Infinity, m: -Infinity }));   // JSON has no NaN or Infinity: all become null
show(() => JSON.stringify(-0));                         // the sign of zero is lost
show(() => JSON.stringify({ d: new Date(Date.UTC(2026, 10, 9)) }));   // Date has toJSON: an ISO text, which parses back as a STRING
show(() => JSON.stringify(new Date(NaN)));              // an invalid Date has toJSON returning null
show(() => JSON.stringify(new Map([["a", 1]])));        // TRAP: a Map has no own enumerable properties
show(() => JSON.stringify(new Set([1, 2])));            // and neither does a Set
show(() => JSON.stringify(Object.fromEntries(new Map([["a", 1]]))));   // the fixes: Object.fromEntries(map), [...set]
show(() => JSON.stringify(new Uint8Array([1, 2])));     // a typed array becomes an object with index keys, not an array
show(() => JSON.stringify({ b: 1, 2: "x", a: 2, 1: "y" }));   // integer-like keys come first in ascending order, then the rest as inserted
show(() => JSON.stringify({ [Symbol("k")]: 1, a: 1 })); // symbol KEYS are skipped too
show(() => JSON.stringify(Object.create({ inherited: 1 })));   // inherited properties are not own
show(() => JSON.stringify(Object.defineProperty({ a: 1 }, "hidden", { value: 2 })));   // non-enumerable: skipped

section("Class instances: own enumerable fields only");
show(() => JSON.stringify(new Account()));              // owner only: #pin is private, label is a prototype getter, describe is a method
show(() => JSON.stringify(new Temperature(100)));       // toJSON decides the output
show(() => JSON.stringify({ get full() { return "computed"; } }));   // an own getter of an object literal IS enumerable: it runs and is written
note("A class with private state or computed values needs a toJSON, or a function that builds the plain object first");

section("Top-level values and the two ways to throw");
show(() => JSON.stringify("text"));                     // a string is JSON too: with its quotes (and the quotes inside get escaped)
show(() => JSON.stringify(null));
show(() => JSON.stringify(undefined));                  // TRAP: not the text "undefined" but the value undefined, although the TypeScript type says string
show(() => JSON.stringify(() => 1));                    // same for a function
show(() => thrown(() => JSON.stringify({ n: 10n })));   // a bigint cannot be serialized: TypeError (fix: replacer or toString)
show(() => thrown(() => JSON.stringify(circular)));     // a cycle is a TypeError ("Converting circular structure to JSON")
note("JSON.stringify(x).length on an undefined x crashes later with a confusing TypeError: check the result when x may be undefined");
note("Error objects serialize to {} (lesson 17); the round trip back is the next file (json-parse)");
