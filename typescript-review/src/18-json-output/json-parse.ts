/**
 * 18c. JSON.parse(text, reviver): text to value, SyntaxError, any vs unknown, reviver, precision, JSON as a deep clone
 *      Run: npx tsx src/18-json-output/json-parse.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";
import { type User, sampleUser } from "./sample-data";

const isObject = (x: unknown): x is Record<string, unknown> => typeof x === "object" && x !== null && !Array.isArray(x);

/** A hand-written type guard: the only honest way to turn `unknown` into a User. */
function isUser(x: unknown): x is User {
  if (!isObject(x)) return false;
  const address = x.address;
  const addressOk = address === null || (isObject(address) && typeof address.city === "string" && (address.zip === null || typeof address.zip === "string"));
  return (
    typeof x.id === "number" &&
    typeof x.name === "string" &&
    typeof x.active === "boolean" &&
    typeof x.score === "number" &&
    Array.isArray(x.tags) &&
    x.tags.every((tag) => typeof tag === "string") &&
    addressOk
  );
}

class Dog {
  name = "Rex";
  bark(): string {
    return "woof";
  }
}

title("JSON.parse  -  text to value, and the traps on the way back");

section("Text to value");
syntax(
  ["JSON.parse(text)", "returns any: the TYPE of the result is whatever the text said"],
  ["JSON.parse(text, (key, value) => ...)", "reviver: called for every value, children before parents, last call has the key ''"],
);
show(() => JSON.parse("42"));                           // any JSON value is valid at the top, not only objects
show(() => JSON.parse('"text"'));
show(() => JSON.parse("null"));
show(() => JSON.parse("[1, 2, 3]"));
show(() => JSON.parse('{"a": [1, {"b": true}]}'));
show(() => JSON.parse(null as unknown as string));      // TRAP: null is converted to the text "null", which is valid: no error, result null
show(() => thrown(() => JSON.parse(undefined as unknown as string)));   // but undefined becomes "undefined": SyntaxError

section("Invalid JSON is a SyntaxError");
show(() => thrown(() => JSON.parse('{"a":1,}')));       // trailing comma
show(() => thrown(() => JSON.parse("{'a':1}")));        // single quotes
show(() => thrown(() => JSON.parse("{a:1}")));          // unquoted key
show(() => thrown(() => JSON.parse("NaN")));            // JavaScript values that JSON does not have: NaN, undefined, Infinity
show(() => thrown(() => JSON.parse("")));               // empty text
show(() => thrown(() => JSON.parse('{"a":1} x')));      // anything after the value
show(() => thrown(() => JSON.parse("01")));             // leading zero
note("Valid JavaScript is not valid JSON: no comments, no trailing commas, no single quotes, no unquoted keys");

section("parse returns any: validate, do not trust");
const text = '{"id":"7","name":"Ann"}';
const trusted = JSON.parse(text) as User;               // compiles, checks nothing: any converts to every type
show(() => typeof trusted.id);                          // string, although User says number: the type lies
const data: unknown = JSON.parse(text);                 // declare it unknown instead: now the compiler makes you check
show(() => isUser(data));                               // false: id is a string and several fields are missing
show(() => isUser(JSON.parse(JSON.stringify(sampleUser()))));   // true: a real User passes every check
wontCompile("const d: unknown = JSON.parse(text); d.id;", "TS18046: 'd' is of type 'unknown'");
note("Schema libraries (zod, valibot) generate these guards; the idea is the same: parse, then check, then use");

section("Reviver: turn text back into richer values");
const order: string[] = [];
JSON.parse('{"a":{"b":1},"c":[2,3]}', (key, value) => {
  order.push(key);
  return value;
});
show(() => order);                                      // b before a, array indexes as strings, and "" for the whole value LAST
const dated = JSON.parse('{"createdAt":"2026-11-09T00:00:00.000Z","name":"x"}', (key, value) => (key.endsWith("At") && typeof value === "string" ? new Date(value) : value));
show(() => dated.createdAt instanceof Date);            // without the reviver it would stay a string
show(() => dated.createdAt.getUTCFullYear());
show(() => JSON.parse('{"a":1,"secret":2}', (key, value) => (key === "secret" ? undefined : value)));   // returning undefined deletes the key
const exact = JSON.parse('{"id":12345678901234567890}', (key, value, context?: { source?: string }) => (key === "id" && context?.source !== undefined ? BigInt(context.source) : value));
show(() => exact.id);                                   // the 3rd argument carries the raw source text: the way to keep a big integer exact (Node 24)
note("A date goes out as an ISO string and comes back as a string: nothing in JSON says it was a date. Revive by key name or by schema");

section("Numbers, keys and prototypes");
show(() => JSON.parse("9007199254740993"));             // TRAP: 2^53 + 1 is not a double: the last digit is lost
show(() => JSON.parse('{"id":12345678901234567890}').id);   // ids above 2^53 arrive wrong: send them as strings
show(() => JSON.parse('{"a":1,"a":2}'));                // duplicate keys are not an error: the last one wins
const sneaky = JSON.parse('{"__proto__":{"admin":true}}');
show(() => Object.keys(sneaky));                        // __proto__ becomes an ordinary own key
show(() => sneaky.admin);                               // the prototype was NOT changed: parse is safe, a later merge or Object.assign is not
note("Never merge parsed input into an object with a recursive merge you wrote yourself: that is how prototype pollution happens");

section("JSON round trip as a deep clone, and the better way");
const original = { d: new Date(Date.UTC(2026, 10, 9)), u: undefined, n: NaN, m: new Map([["k", 1]]), s: new Set([1]) };
const viaJson = JSON.parse(JSON.stringify(original));
const viaClone = structuredClone(original);
show(() => viaJson);                                    // the date is a string, u is gone, NaN is null, Map and Set are {}
show(() => viaClone);                                   // structuredClone keeps Date, undefined, NaN, Map and Set
show(() => viaJson.d instanceof Date);
show(() => viaClone.d instanceof Date);
show(() => thrown(() => structuredClone({ f: () => 1 })));   // functions cannot be cloned: a DOMException
show(() => structuredClone(new Dog()) instanceof Dog);   // class instances come back as plain objects: only the data survives
note("JSON round trip is fine for plain data (numbers, strings, booleans, null, arrays, plain objects); otherwise use structuredClone");
