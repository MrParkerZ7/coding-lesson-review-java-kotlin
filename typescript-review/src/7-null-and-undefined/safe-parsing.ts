/**
 * 7d. SAFE PARSING: NaN, "or undefined" results, and lookups that may find nothing
 *     Run: npx tsx src/7-null-and-undefined/safe-parsing.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** A whole text as a base-10 integer, or undefined. No trimming, no 12px, no 1e3, and the result must be exact (a safe integer). */
function tryParseInt(s: string): number | undefined {
  if (!/^[+-]?\d+$/.test(s)) return undefined;
  const n = Number(s);
  return Number.isSafeInteger(n) ? n : undefined;
}

/** A number with a fallback for NaN. Note: "" and blank text are NOT NaN, they convert to 0. */
function parseOr(s: string, fallback: number): number {
  const n = Number(s);
  return Number.isNaN(n) ? fallback : n;
}

/** JSON text to a value, or undefined when the text is not valid JSON (undefined is never a JSON value, so it is unambiguous). */
function tryJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}

/** Exactly "true" or "false", or undefined. Boolean("false") would be true: any non-empty text is truthy. */
function tryParseBool(s: string): boolean | undefined {
  if (s === "true") return true;
  if (s === "false") return false;
  return undefined;
}

title("SAFE PARSING  -  bad input becomes NaN, undefined, or an exception");

section("Number, parseInt, parseFloat: three different answers");
syntax(
  ["Number(s)", "the WHOLE text must be a number, else NaN; blank text gives 0"],
  ["parseInt(s, 10) | parseFloat(s)", "reads as far as it can and ignores the rest; NaN only when nothing was read"],
  ["Number.isNaN(x)", "the only reliable NaN test (lesson 0 numbers)"],
);
show(() => Number("42"));
show(() => Number("12px"));                             // NaN: the whole text must be a number
show(() => parseInt("12px", 10));                       // 12: reads the digits and stops
show(() => parseFloat("3.14abc"));                      // 3.14
show(() => Number(""));                                 // TRAP: 0, blank text is 0, not NaN
show(() => Number("  "));                               // 0 as well
show(() => parseInt("", 10));                           // NaN: parseInt found nothing to read
show(() => Number(" 42 "));                             // 42: Number ignores whitespace around the text
show(() => Number("1e3"));                              // 1000
show(() => parseInt("1e3", 10));                        // 1: stops at the e
show(() => parseInt("0x1f"));                           // 31: without a radix a 0x prefix means hexadecimal
show(() => parseInt("0x1f", 10));                       // 0: always pass the radix
show(() => parseInt("0", 10) ? "valid" : "invalid");    // TRAP: "invalid", 0 is falsy; test Number.isNaN, not truthiness
show(() => Number.isNaN(Number("abc")));
show(() => parseOr("abc", -1));
show(() => parseOr("7", -1));
show(() => parseOr("", -1));                            // 0, not -1: blank text is not NaN

section("tryParseInt: number | undefined instead of NaN or an exception");
syntax(["function tryParseInt(s: string): number | undefined", "the TypeScript form of Kotlin's toIntOrNull() and Java's Optional<Integer>"]);
show(() => tryParseInt("42"));
show(() => tryParseInt("4x2"));                         // undefined: not a number
show(() => tryParseInt(" 42"));                         // undefined: no trimming, a space makes it invalid
show(() => tryParseInt("+5"));                          // a leading plus is accepted
show(() => tryParseInt(""));                            // undefined: Number("") would have said 0
show(() => tryParseInt("2147483648"));                  // fits: a JavaScript number is not limited to 32 bits
show(() => tryParseInt("9007199254740993"));            // undefined: above 2^53 the value would be inexact (use bigint, lesson 0)
show(() => tryParseInt("abc") ?? 0);                    // the idiom: parse with a default
show(() => tryParseBool("true"));
show(() => tryParseBool("yes"));                        // undefined: only "true" and "false" count
show(() => Boolean("false"));                           // TRAP: true, any non-empty text is truthy

section("JSON.parse throws; wrap it when the input is not yours");
show(() => thrown(() => JSON.parse("{")));              // SyntaxError
show(() => tryJson("{\"a\":1}"));
show(() => tryJson("{"));                               // undefined: invalid text
show(() => tryJson("null"));                            // null: a valid JSON value is different from undefined
show(() => tryJson(""));                                // undefined: empty text is not JSON either
note("JSON.parse returns any: the result still has to be checked (type guard, lesson 7c), the parse only proves the text is JSON");

section("Lookups that may find nothing: T | undefined");
syntax(["T | undefined", "the Optional of TypeScript: map.get(k), arr.find(f), arr.pop(), arr.at(i), arr[i]"]);
const stock = new Map<string, number | undefined>([["apple", 3], ["gap", undefined]]);
const nums: number[] = [4, 8, 1];
const empty: number[] = [];
show(() => stock.get("pear"));                          // undefined: no such key
show(() => stock.get("pear") ?? 0);                     // the default form
show(() => stock.get("gap"));                           // undefined too, but the key EXISTS ...
show(() => stock.has("gap"));                           // ... so ask has() when a value may legitimately be undefined
show(() => nums.find((n) => n > 5));
show(() => nums.find((n) => n > 50));                   // undefined
show(() => nums.findIndex((n) => n > 50));              // -1: findIndex and indexOf use a number sentinel instead
show(() => nums.at(10));
show(() => empty.pop());                                // undefined: no exception on an empty array
show(() => empty.at(-1));
show(() => Math.max(...empty));                         // TRAP: -Infinity, not undefined and not an error
show(() => nums.find((n) => n > 5)?.toFixed(1));        // chain on the result with ?.

section("arr[i] is typed as a plain number (without a flag)");
const first: number = empty[0];                         // compiles: tsc assumes every index is in range
show(() => first);                                      // TRAP: undefined at run time, although the type says number
wontCompile("const first: number = empty[0];  (flag on)", "with --noUncheckedIndexedAccess: TS2322 Type number | undefined is not assignable to type number");
note("noUncheckedIndexedAccess makes arr[i] and record[key] 'T | undefined' (tsconfig, lesson 20); the project here runs without it");
