/**
 * 19a. ES2015 TO ES2019: the language you write every day
 *      Run: npx tsx src/19-modern-js/es2015-to-es2019.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title } from "../output";

title("ES2015 TO ES2019  -  what each edition added, with a real run of each feature");

section("The editions");
syntax(
  ["ES2015 (ES6)", "let/const, arrow, class, template literal, destructuring, default/rest/spread, modules, Promise, generators, Map/Set/Symbol"],
  ["ES2016", "the ** operator, Array.prototype.includes"],
  ["ES2017", "async/await, Object.values/entries, padStart/padEnd, trailing commas in parameter lists"],
  ["ES2018", "object rest/spread, for await...of, Promise.finally, regex named groups, lookbehind and the s flag"],
  ["ES2019", "flat/flatMap, Object.fromEntries, optional catch binding, trimStart/trimEnd, a guaranteed stable sort"],
);
note("The standard has shipped once a year since ES2015: the name is the year (ES2015 is the old 'ES6'). tsc's target and lib options choose what it emits and types");

section("ES2015: the big one");
const [first, second] = [10, 20];
show(() => [first, second]);                            // array destructuring
const { x, ...others } = { x: 1, y: 2, z: 3 };
show(() => [x, others]);                                // object destructuring with rest
show(() => Math.max(...[3, 9, 4]));                     // spread arguments
const twice = (n: number): number => n * 2;             // arrow function: short syntax, no own this
show(() => [1, 2, 3].map(twice));
show(() => `sum is ${first + second}`);                 // template literal with an expression
const greet = (who = "you"): string => `hi ${who}`;     // default parameter
show(() => greet());
class Animal { speak(): string { return "..."; } }
show(() => typeof Animal);                              // class: sugar over a constructor function (lesson 11)
function* count(): Generator<number> { yield 1; yield 2; yield 3; }
show(() => [...count()]);                               // generator: a function that can pause (lesson 2)
show(() => new Map([["a", 1]]).get("a"));               // Map and Set (lesson 5)
show(() => new Set([1, 1, 2]).size);
show(() => Symbol("id").description);                   // Symbol: a unique key (the description getter is ES2019)
show(() => [1, 2, 3].find((n) => n > 1));         // new library methods: Array.find, String.includes, Number.isInteger ...
show(() => "abc".includes("bc"));
note("Also ES2015: import/export modules (lesson 20), Promise, for...of, Proxy and Reflect, typed arrays, tail-call rules (V8 never implemented them)");

section("ES2016 and ES2017");
show(() => 2 ** 10);                                    // the exponent operator (ES2016)
const withNaN = [1, NaN];
show(() => withNaN.includes(NaN));                      // includes finds NaN (ES2016) ...
show(() => withNaN.indexOf(NaN));                       // ... indexOf compares with === and does not
const stock = { apples: 3, pears: 0 };
show(() => Object.values(stock));                       // ES2017
show(() => Object.entries(stock));
show(() => "7".padStart(3, "0"));                       // padStart / padEnd (ES2017)
async function load(): Promise<string> { return "loaded"; }
const awaited = await load();                           // async/await (ES2017); top-level await needs ES2022 and a module
show(() => awaited);

section("ES2018");
const base = { a: 1, b: 2 };
show(() => ({ ...base, b: 3, c: 4 }));                  // object spread (rest was shown above)
async function* ticks(): AsyncGenerator<number> { yield 1; yield 2; }
const got: number[] = [];
for await (const t of ticks()) got.push(t);             // for await...of: iterate an async iterable
show(() => got);
const order: string[] = [];
await Promise.resolve("ok").finally(() => { order.push("finally ran"); });   // Promise.prototype.finally
show(() => order);
const dateRe = /(?<year>\d{4})-(?<month>\d{2})/;
show(() => dateRe.exec("2026-11")?.groups?.year);       // named capture groups
const priceRe = /(?<=\$)\d+/;
show(() => priceRe.exec("cost $42")?.[0]);              // lookbehind: digits that follow a dollar sign
const dotAll = /a.b/s;
show(() => dotAll.test("a\nb"));                        // the s flag lets . match a newline

section("ES2019");
show(() => [1, [2, [3, [4]]]].flat(Infinity));          // flat: flatten nested arrays
show(() => ["a b", "c"].flatMap((s) => s.split(" ")));  // map, then flatten one level
show(() => Object.fromEntries([["a", 1], ["b", 2]]));   // the inverse of Object.entries
let caught = false;
try { JSON.parse("{"); } catch { caught = true; }       // optional catch binding: no (e) when you do not need it
show(() => caught);
show(() => "  hi  ".trimStart() + "|");                 // trimStart / trimEnd
const rows = [{ k: 1, n: "a" }, { k: 0, n: "b" }, { k: 1, n: "c" }];
show(() => rows.sort((p, q) => p.k - q.k).map((r) => r.n));   // sort is stable since ES2019: equal keys keep their order
