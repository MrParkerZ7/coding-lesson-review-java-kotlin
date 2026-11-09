/**
 * 18d. JSON BY HAND (no JSON.stringify): escape + one recursive function with type narrowing
 *      Run: npx tsx src/18-json-output/hand-written-json.ts        (add --plain for ASCII output)
 */
import { bigO, block, note, section, show, syntax, thrown, title } from "../output";
import { sampleUser } from "./sample-data";

interface Options {
  indent?: number;                                      // spaces per level; omit for compact output
  sortKeys?: boolean;                                   // alphabetical keys: stable text for tests and hashes
}

/** Escape the inside of a JSON string: the quote, the backslash and control characters. O(n). */
function escapeJson(s: string): string {
  const parts: string[] = [];
  for (let i = 0; i < s.length; i++) {
    const c = s[i];
    if (c === '"') parts.push('\\"');
    else if (c === "\\") parts.push("\\\\");
    else if (c === "\n") parts.push("\\n");
    else if (c === "\r") parts.push("\\r");
    else if (c === "\t") parts.push("\\t");
    else if (s.charCodeAt(i) < 0x20) parts.push("\\u" + s.charCodeAt(i).toString(16).padStart(4, "0"));   // any other control character
    else parts.push(c);
  }
  return parts.join("");
}

const quote = (s: string): string => `"${escapeJson(s)}"`;

/** One level of brackets: compact, or one item per line with the indent. */
function wrap(open: string, items: string[], close: string, options: Options, depth: number): string {
  if (items.length === 0) return open + close;
  const indent = options.indent ?? 0;
  if (indent === 0) return open + items.join(",") + close;
  const pad = " ".repeat(indent * (depth + 1));
  return `${open}\n${items.map((item) => pad + item).join(",\n")}\n${" ".repeat(indent * depth)}${close}`;
}

/** The recursive core. Returns undefined for values JSON cannot hold, so the caller can drop (object) or null (array) them. */
function serialize(value: unknown, options: Options, depth: number, path: Set<object>): string | undefined {
  if (value === null) return "null";
  if (typeof value === "boolean") return String(value);
  if (typeof value === "number") return Number.isFinite(value) ? String(value) : "null";   // JSON has no NaN or Infinity
  if (typeof value === "bigint") return value.toString();                                  // digits only: readers may round them
  if (typeof value === "string") return quote(value);
  if (typeof value !== "object") return undefined;                                         // undefined, function, symbol
  if (path.has(value)) throw new TypeError("circular structure");                          // an object that contains itself
  const hook = value as { toJSON?: () => unknown };
  if (typeof hook.toJSON === "function") return serialize(hook.toJSON(), options, depth, path);   // Date and your own classes
  path.add(value);
  try {
    const next = (v: unknown): string | undefined => serialize(v, options, depth + 1, path);
    if (Array.isArray(value) || value instanceof Set) {
      const items = [...value].map((item) => next(item) ?? "null");                        // keep positions: undefined becomes null
      return wrap("[", items, "]", options, depth);
    }
    const entries: Array<[string, unknown]> =
      value instanceof Map ? [...value].map(([k, v]) => [String(k), v]) : Object.entries(value);
    if (options.sortKeys) entries.sort(([a], [b]) => (a < b ? -1 : a > b ? 1 : 0));
    const colon = options.indent ? ": " : ":";
    const items: string[] = [];
    for (const [key, item] of entries) {
      const text = next(item);
      if (text !== undefined) items.push(quote(key) + colon + text);                       // an undefined value drops the key
    }
    return wrap("{", items, "}", options, depth);
  } finally {
    path.delete(value);                                 // leaving the object: only ANCESTORS count as a cycle, shared children are fine
  }
}

/** Any value to JSON text. (JSON.stringify returns undefined for an undefined input; this one returns "null".) */
const toJson = (value: unknown, options: Options = {}): string => serialize(value, options, 0, new Set()) ?? "null";

title("JSON BY HAND  -  escape + one recursive function");

section("1. Escape the text");
syntax(['" -> \\"   \\ -> \\\\   newline -> \\n   tab -> \\t   other control char -> \\u00XX', "what a JSON string may NOT contain raw"]);
show(() => escapeJson('say "hi"'));                     // the quotes get a backslash
show(() => escapeJson("C:\\temp"));                     // a backslash becomes two
show(() => escapeJson("line1\nline2"));                 // a real newline becomes the two characters \ and n
show(() => escapeJson(String.fromCharCode(1)));         // control character 1 becomes a six-character escape
const samples = ['say "hi"', "C:\\temp", "line1\nline2", String.fromCharCode(1), "tab\there", "plain"];
show(() => samples.every((s) => quote(s) === JSON.stringify(s)));   // the same text JSON.stringify writes for every sample
const name = 'O"Brien';
show(() => thrown(() => JSON.parse(`{"name":"${name}"}`)));      // TRAP: without escape the quote ends the string early: SyntaxError
show(() => JSON.parse(`{"name":"${escapeJson(name)}"}`).name);   // with escape it reads back intact
note("Forgetting to escape is THE bug: a name like O\"Brien breaks the whole document");

section("2. One recursive function for every value");
syntax(["function toJson(value: unknown): string", "narrow with typeof, instanceof and Array.isArray; objects and arrays call toJson again"]);
show(() => toJson(null));                               // the literal null, no quotes
show(() => toJson(true));
show(() => toJson(42));
show(() => toJson("text"));                             // strings get quotes
show(() => toJson([1, "two", null, undefined]));        // undefined in an array becomes null, like JSON.stringify
show(() => toJson({ a: 1, b: ["x", "y"] }));            // objects and arrays nest freely
show(() => toJson({ a: undefined, f: () => 1, c: 1 })); // an object drops undefined and functions
show(() => toJson([]));                                 // [] : an empty list is safe
show(() => toJson(0.1 + 0.2));                          // String(number): the shortest text that reads back the same
show(() => toJson(NaN));                                // JSON cannot hold NaN: this version writes null
show(() => toJson(-0));                                 // String(-0) is "0", like JSON.stringify
show(() => toJson(new Date(Date.UTC(2026, 10, 9))));    // Date has toJSON: an ISO text in quotes
show(() => toJson(new Set([1, 2])));                    // deliberate difference: JSON.stringify gives {}, this gives an array
show(() => toJson(new Map([["a", 1]])));                // and a Map becomes an object
const unicode = "caf\u00e9 \ud83d\ude00";
show(() => toJson(unicode) === JSON.stringify(unicode)); // non-ASCII text passes through untouched, emoji included

section("3. Your own data, and cycles");
const user = sampleUser();
show(() => toJson(user));                               // the whole User on one line
show(() => toJson(user) === JSON.stringify(user));      // identical to the built-in for plain data
const shared = { x: 1 };
show(() => toJson({ a: shared, b: shared }));           // the same object twice is NOT a cycle: path.delete in finally keeps it legal
const loop: { self?: unknown } = {};
loop.self = loop;
show(() => thrown(() => toJson(loop)));                 // an ancestor met again IS a cycle: TypeError instead of endless recursion

section("4. Pretty print and sorted keys");
syntax(["wrap(open, items, close): '[\\n' + items joined by ',\\n' + '\\n]'", "one item per line, indent grows by `indent` spaces per level"]);
block(toJson(user, { indent: 2 }));
show(() => toJson(user, { indent: 2 }) === JSON.stringify(user, null, 2));   // same text as the built-in pretty printer
show(() => toJson({ a: [], b: {} }, { indent: 2 }) === JSON.stringify({ a: [], b: {} }, null, 2));   // empty containers stay on one line
show(() => toJson({ b: 2, a: 1 }));                     // insertion order
show(() => toJson({ b: 2, a: 1 }, { sortKeys: true })); // alphabetical: the same data always gives the same text

section("5. Traps");
show(() => toJson(toJson({ a: 1 })));                   // TRAP: JSON text is a string, so it was quoted AGAIN
show(() => toJson({ a: 1 }));                           // the right call: pass the VALUE, not text
show(() => JSON.parse(toJson(9007199254740993n)));      // bigint digits are exact in the text, but JavaScript readers round them
note("Interview order: escape first, then the typeof chain, then arrays and objects, then cycles, then pretty print if asked");

section("Cost");
bigO(
  ["toJson over n values", "O(n) time: every value is visited once (sortKeys adds O(k log k) per object)"],
  ["parts.join('') / items.join(',')", "O(n) total"],
  ["s += piece in a loop", "fine in V8 (it builds a rope), O(n^2) on engines that copy: join is the portable idiom"],
  ["recursion depth", "= nesting depth: a 100 000-deep structure overflows the stack (RangeError), like JSON.stringify"],
);
