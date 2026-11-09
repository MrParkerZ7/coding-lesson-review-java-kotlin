/**
 * 7b. OPTIONAL CHAINING ?. AND NULLISH COALESCING ??
 *     Run: npx tsx src/7-null-and-undefined/optional-chaining-and-nullish.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

type Address = { street: { name: string }; city?: string };
type User = { name: string; address?: Address; tags?: string[]; greet?: () => string };

const ann: User = { name: "Ann", address: { street: { name: "Main St" }, city: "Oslo" }, tags: ["admin", "dev"], greet: () => "hi Ann" };
const bob: User = { name: "Bob" };
const cy: User = { name: "Cy", address: { street: { name: "Elm St" } } };

title("?. AND ??  -  stop at null or undefined, or replace it");

section("Optional chaining ?.");
syntax(
  ["a?.b", "a.b when a is not null or undefined, otherwise undefined (the result type gets | undefined)"],
  ["a?.[i] | a?.f()", "the same for an index and for a call"],
  ["f?.()", "call f only when it is not null or undefined"],
);
show(() => ann.address?.city);                          // every link exists, so the chain gives the city
show(() => bob.address?.city);                          // undefined: address is missing, .city never runs
show(() => cy.address?.city);                           // undefined: the city is missing
show(() => ann.address?.city?.length);                  // 4: the length of "Oslo"
show(() => bob.address?.street.name);                   // undefined: ONE ?. skips the whole rest of the chain, no ?. needed after it
show(() => ann.tags?.[0]);                              // index form
show(() => bob.tags?.[0]);
show(() => ann.greet?.());                              // call form: the method exists
show(() => bob.greet?.());                              // undefined: the method is missing, so it is not called
const nobody = null as User | null;
show(() => nobody?.name);                               // TRAP: undefined, not null: the result of a skipped chain is always undefined
show(() => bob.tags?.length ?? 0);                      // a missing array is not an empty array: say what you mean with ??
show(() => thrown(() => (ann.name as unknown as () => void)?.()));   // TypeError: ?.() only guards null and undefined, not "is not a function"

section("Short-circuit: the rest of the chain is not evaluated");
const calls: string[] = [];
const trace = (label: string): string => { calls.push(label); return label; };
bob.address?.city?.concat(trace("never"));
show(() => calls);                                      // []: the argument was not evaluated, trace never ran
ann.address?.city?.concat(trace("runs"));
show(() => calls);                                      // [ 'runs' ]: the chain went all the way
function parenthesized(u: User | null): string | undefined {
  // @ts-expect-error TS2532 Object is possibly undefined: the parentheses END the short circuit, so .city runs on undefined
  return (u?.address).city;
}
show(() => thrown(() => parenthesized(null)));          // TRAP: TypeError; tsc catches it (the line above is expected to fail to compile)
note("(a?.b).c is not a?.b.c: do not wrap an optional chain in parentheses and keep going");

section("?? versus ||");
syntax(
  ["a ?? b", "b only when a is null or undefined"],
  ["a || b", "b when a is ANY falsy value: 0, \"\", false, NaN, null, undefined (lesson 0)"],
);
const settings: { retries?: number; label?: string; verbose?: boolean; ratio?: number } = { retries: 0, label: "", verbose: false, ratio: NaN };
show(() => settings.retries || 3);                      // TRAP: 3, the valid value 0 is replaced
show(() => settings.retries ?? 3);                      // 0: only a missing value gets the default
show(() => settings.label || "n/a");                    // "n/a": an empty string is replaced
show(() => settings.label ?? "n/a");                    // "": kept
show(() => settings.verbose || true);                   // true: a deliberate false is lost
show(() => settings.verbose ?? true);                   // false: kept
show(() => settings.ratio || 1);                        // 1: NaN is falsy too
show(() => settings.ratio ?? 1);                        // NaN: NaN is not nullish, so ?? keeps it (test it with Number.isNaN, lesson 7d)
wontCompile("const mixed = a || b ?? c;", "TS5076: '||' and '??' operations cannot be mixed without parentheses");
const a = 0, b = 0, c = 5;
show(() => (a || b) ?? c);                              // 0: with parentheses it is allowed, and ?? sees 0 (not nullish)

section("Assignment forms ??=  ||=  &&=");
syntax(
  ["a ??= b", "a = b only when a is null or undefined (b is not even evaluated otherwise)"],
  ["a ||= b", "a = b only when a is falsy"],
  ["a &&= b", "a = b only when a is truthy"],
);
const opts: { port?: number | null; name?: string; admin?: boolean; count: number } = { port: null, name: "", admin: true, count: 0 };
opts.port ??= 8080;  show(() => opts.port);             // null was replaced
opts.name ??= "anon";  show(() => opts.name);           // "": not nullish, so NOT replaced
opts.name ||= "anon";  show(() => opts.name);           // "anon": "" is falsy, so replaced
opts.count &&= 99;  show(() => opts.count);             // 0: falsy, so &&= does nothing
opts.admin &&= false;  show(() => opts.admin);          // truthy, so it was assigned
const lazyCalls: string[] = [];
const expensive = (): number => { lazyCalls.push("expensive"); return 1; };
opts.port ??= expensive();
show(() => lazyCalls);                                  // []: port is set, so expensive() never ran (that is the short-circuit)

section("Deep access with a default; delete with ?.");
show(() => ann.address?.city ?? "unknown");
show(() => bob.address?.city ?? "unknown");             // the everyday combination: chain + default
show(() => cy.address?.street.name);
const dan: User = { name: "Dan", address: { street: { name: "Via Roma" }, city: "Rome" } };
show(() => delete dan.address?.city);                   // true: the property was deleted (only an optional property may be deleted)
show(() => dan.address?.city);                          // undefined now
show(() => delete bob.address?.city);                   // true as well: a skipped chain deletes nothing and still answers true
note("Interview answer: ?. for optional access, ?? for a default that keeps 0, \"\" and false, || only when every falsy value should be replaced");
