/**
 * 1e. TEXT AND FORMATTING: template literals, tagged templates, String.raw, toFixed, Intl, padding
 *     Run: npx tsx src/1-variables-and-types/text-and-formatting.ts        (add --plain for ASCII output)
 *     (the string type itself: lesson 0 strings-basics; string operations for problems: lesson 6)
 */
import { block, note, section, show, syntax, title } from "../output";

/** A tag function: receives the fixed text parts and the values apart, and returns anything. */
function bracket(strings: TemplateStringsArray, ...values: number[]): string {
  return strings.reduce((out, text, i) => out + text + (i < values.length ? `[${values[i]}]` : ""), "");
}

/** Shows how a tag sees a template: how many text parts, how many values, and the raw first part. */
function inspectTemplate(strings: TemplateStringsArray, ...values: unknown[]): [number, number, string | undefined] {
  return [strings.length, values.length, strings.raw[0]];
}

title("TEXT AND FORMATTING  -  build text, format numbers, align columns");

section("Template literals");
syntax(
  ["`text ${expression} text`", "backticks: any expression inside ${ }, may span several lines"],
  ["`outer ${ `inner ${x}` }`", "templates nest"],
);
const name = "Ann";
const n: number = 3;
show(() => `${name} has ${n} ${n === 1 ? "item" : "items"}`);   // any expression, a ternary included
show(() => `${n} x 2 = ${n * 2}`);                       // calculations
show(() => `a${`b${"c"}`}`);                             // nested: abc
show(() => `${[1, 2]} | ${null} | ${undefined}`);        // an array joins with commas; null and undefined print as words
show(() => `${{ a: 1 }}`);                               // TRAP: [object Object], the default text of an object (lesson 18)
const poem = `roses
are red`;
show(() => poem.split("\n"));                            // the line break typed in the source IS part of the text
show(() => "tab\there".length);                          // 8: an escape like \t is ONE character

section("Tagged templates and String.raw");
syntax(
  ["tag`text ${a} text`", "calls tag(strings, a): the text parts and the values arrive separately"],
  ["String.raw`C:\\temp`", "the built-in tag that keeps backslashes as typed (no escape processing)"],
);
show(() => bracket`x=${1} y=${2}`);                      // the tag decides how to join the parts
show(() => inspectTemplate`a${1}b`);                     // [ 2, 1, 'a' ]: two text parts around one value
show(() => "C:\temp\new".length);                        // 9: the escapes \t and \n became a tab and a line feed
show(() => String.raw`C:\temp\new`.length);              // 11: every backslash is kept
note("Tagged templates power SQL and HTML escaping libraries: the tag can escape the values but never the fixed text");

section("Formatting numbers");
syntax(
  ["x.toFixed(d) | x.toPrecision(p)", "TEXT with d decimals | p significant digits"],
  ["new Intl.NumberFormat(\"en-US\", options).format(x)", "grouping, currency, percent: ALWAYS pass the locale or the output follows the machine"],
  ["x.toLocaleString(\"en-US\", options)", "the same in one call"],
);
show(() => (1234.5678).toFixed(2));                      // rounds to 2 decimals, returns a STRING
show(() => (1.005).toFixed(2));                          // TRAP: "1.00", 1.005 is really 1.00499999999999989...
show(() => (-1.5).toFixed(0));                           // "-2": toFixed rounds half away from zero ...
show(() => Math.round(-1.5));                            // -1: ... Math.round rounds half up. They disagree
show(() => (1234.5678).toPrecision(6));                  // "1234.57": 6 significant digits
const big = 1234567.891;
show(() => new Intl.NumberFormat("en-US").format(big));  // thousands separators
show(() => big.toLocaleString("en-US", { maximumFractionDigits: 1 }));   // the same, with at most 1 decimal
show(() => (1234.5).toLocaleString("en-US", { minimumFractionDigits: 2 }));
const usd = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
show(() => usd.format(1234.5));                          // a formatter object can be built once and reused
show(() => (0.256).toLocaleString("en-US", { style: "percent" }));   // 26%: the value is multiplied by 100
show(() => big.toLocaleString("de-DE"));                 // another locale: the separators swap
const day = new Date(Date.UTC(2026, 10, 9));             // a fixed date; months count from 0, so 10 is November
const dateStyle = { timeZone: "UTC", year: "numeric", month: "long", day: "numeric" } as const;
show(() => day.toLocaleDateString("en-US", dateStyle));  // pass a time zone too, or the day follows the machine
note("Money: format with Intl at the very end and compute in integer cents (lesson 0 numbers)");

section("Padding and aligned columns");
syntax(["s.padStart(n, fill) | s.padEnd(n, fill)", "grow a string to n characters: numbers right-aligned, names left-aligned"]);
show(() => "7".padStart(3, "0"));                        // "007"
show(() => "ab".padEnd(5, "-") + "|");                   // "ab---|"
show(() => "toolong".padStart(3));                       // "toolong": padding never truncates
const items: [string, number][] = [["apple", 1.5], ["kiwi", 12], ["melon", 100.25]];
block(items.map(([item, price]) => item.padEnd(8, ".") + price.toFixed(2).padStart(8)).join("\n"));
show(() => JSON.stringify("say \"hi\"").length);         // 12: JSON adds the outer quotes and escapes the inner ones (lesson 18)
