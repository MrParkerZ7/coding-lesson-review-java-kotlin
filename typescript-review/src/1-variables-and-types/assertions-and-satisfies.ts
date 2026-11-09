/**
 * 1c. TYPEOF QUERY, SATISFIES, AS AND !: telling tsc about types (none of them checks anything at run time)
 *     Run: npx tsx src/1-variables-and-types/assertions-and-satisfies.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

const config = { host: "localhost", port: 8080 };
type Config = typeof config;                 // typeof in a TYPE position: { host: string; port: number }
type ConfigKey = keyof Config;               // "host" | "port"

type Palette = Record<"red" | "green", string | number[]>;

/** A class field that is assigned later, not in the constructor: the ! tells tsc to trust that. */
class Session {
  token!: string;
  login(): void {
    this.token = "abc";
  }
}

function connect(c: Config): string {
  return `${c.host}:${c.port}`;
}

title("TYPEOF QUERY, SATISFIES, AS AND !  -  instructions to the compiler");

section("typeof in a type position");
syntax(
  ["type T = typeof value;", "the type of an existing VALUE (not the typeof operator that returns a string)"],
  ["keyof T | T[K] | ReturnType<typeof fn>", "the keys | one property type | a function's result type"],
);
show(() => connect({ host: "db", port: 5432 }));         // the parameter type came from the config value
const keys: ConfigKey[] = ["host", "port"];
show(() => keys.map((k) => String(config[k])));          // keyof typeof: only real keys fit, so config[k] is safe
wontCompile("const c: Config = { host: 'db', port: '1' };", "TS2322: Type 'string' is not assignable to type 'number'");

section("satisfies: check the shape, keep the precise type");
syntax(
  ["const x = value satisfies T;", "tsc checks value against T but x keeps its own narrower type"],
  ["const x: T = value;", "x gets the wide type T: the precise knowledge is lost"],
);
const p = { red: [255, 0, 0], green: "#0f0" } satisfies Palette;
show(() => p.red.map((c) => c / 2));                     // red is still number[], so map works
show(() => p.green.toUpperCase());                       // green is still a string
wontCompile("const q: Palette = { ... }; q.red.map(f)", "TS2339: Property 'map' does not exist on type 'string | number[]'");
wontCompile("const b = { red: [1], gren: 'x' } satisfies Palette;", "TS2561: 'gren' does not exist in type 'Palette' (a typo is caught)");
note("satisfies emits nothing: at run time the object is just the object");

section("as: a claim, not a check");
syntax(["value as T", "tell tsc to treat value as T: no check, no conversion, nothing at run time"]);
const data = JSON.parse('{"a":"text"}') as { a: number };   // JSON.parse returns any; as only relabels it
show(() => data.a);                                      // "text": typed number, actually a string
show(() => typeof data.a);                               // the lie is invisible until something uses it
const five = 5 as unknown as string;                     // as unknown as T: the escape hatch for unrelated types
show(() => thrown(() => five.toUpperCase()));            // TypeError: toUpperCase is not a function on a number
wontCompile("const s = 'abc' as number;", "TS2352: Conversion of type 'string' to type 'number' may be a mistake");
note("as may only go to a related type (wider or narrower). Check the value (typeof, a type guard) rather than assert it: lesson 7");

section("! : the non-null assertion");
syntax(["expr!", "tell tsc the value is not null or undefined; no check, so a wrong guess crashes later"]);
const stock = new Map<string, number>([["apple", 3]]);
const known = stock.get("apple")!;
const absent = stock.get("pear")!;                       // typed number, but the key is missing
show(() => known);
show(() => absent);                                      // undefined: the ! hid it from the type
show(() => thrown(() => absent.toFixed(1)));             // TypeError: the crash happens later, far from the cause
wontCompile("stock.get('apple').toFixed(1)", "TS2532: Object is possibly 'undefined' (Map.get returns number | undefined)");
show(() => stock.get("pear") ?? 0);                      // the honest way: give a default, or test for undefined
const stock2 = stock.get("pear");
show(() => (stock2 === undefined ? "missing" : stock2.toFixed(1)));   // narrowing instead of asserting

section("! : definite assignment");
syntax(
  ["field!: Type;", "the field is assigned after the constructor (init method, framework): do not report TS2564"],
  ["let x!: Type;", "the same for a variable assigned later"],
);
const session = new Session();
show(() => session.token);                               // undefined: typed string, empty until login() runs
session.login();
show(() => session.token);                               // "abc": after login() the claim has become true
wontCompile("class Box { value: number; }", "TS2564: Property 'value' has no initializer and is not definitely assigned in the constructor");
note("Better: assign in the constructor or give the field a default, so the type tells the truth");
