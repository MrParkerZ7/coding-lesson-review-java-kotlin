/**
 * 11b. HOISTING AND SCOPE: var, function, let/const (TDZ), strict mode, this
 *      Run: npx tsx src/11-js-quirks/hoisting-and-scope.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("HOISTING AND SCOPE  -  what exists before its line runs");

section("var: the declaration is hoisted, the assignment is not");
syntax(
  ["var x = 5;", "function-scoped; the NAME exists from the start of the function, with the value undefined until the line runs"],
  ["function f() { ... }", "declaration: name AND body are hoisted (function-forms)"],
  ["let / const / class", "hoisted too, but untouchable until their line: the temporal dead zone (TDZ)"],
);
function hoistedVar(): unknown[] {
  // @ts-expect-error
  const before = [typeof v, v];                         // v already exists, holding undefined (tsc says TS2454 here)
  var v = 5;
  return [...before, v];
}
show(() => hoistedVar());                               // no error at run time: v existed, holding undefined, until its line ran
wontCompile("use(v);  var v = 5;", "TS2454: Variable 'v' is used before being assigned (tsc sees what JS hoists)");
show(() => thrown(() => viaVar()));                     // viaVar is hoisted as undefined, so it is not a function yet
var viaVar = function (): number { return 1; };
show(() => viaVar());                                   // once the line ran it works
note("The var surprises are why modern code uses const and let only (block scope, lesson 1)");

section("let, const, class: the temporal dead zone");
const probe = (): unknown => tdz;                       // the function only READS tdz when it is called
show(() => thrown(probe));                              // the const exists but is still dead
const tdz = 1;
show(() => probe());                                    // after its line ran: 1
const probeType = (): string => typeof laterConst;
show(() => thrown(probeType));                          // TRAP: typeof does NOT save you inside the TDZ
const laterConst = 2;
show(() => thrown(() => new LaterClass()));             // a class declaration is not hoisted like a function
class LaterClass {}
show(() => new LaterClass() instanceof LaterClass);

section("typeof on a name that was never declared is safe");
syntax(["typeof name", "'undefined' for a name that does not exist; reading the bare name would throw ReferenceError"]);
declare const ghost: unknown;                           // tells tsc the name exists; nothing is emitted, so at run time it does not
show(() => typeof ghost);                               // no error
show(() => thrown(() => ghost));                        // reading the bare name is a real read of a variable that is not there
note("The classic use: feature checks such as typeof window !== 'undefined' (is this a browser?)");

section("Strict mode: ES modules and classes are always strict");
declare let neverDeclared: number;
show(() => thrown(() => { neverDeclared = 1; }));       // in sloppy mode this silently created a global variable
show(() => this);                                       // the top-level this of an ES module (a CommonJS file gives module.exports)
function plainCall(this: unknown): unknown { return this; }
show(() => plainCall.call(undefined));                  // a plain call has no this in strict code (sloppy: the global object)
note("A TypeScript file with import/export is an ES module, so it is strict: that is why these throw");

section("Scope: shadowing and per-iteration let");
syntax(["{ const x = ...; }", "a block makes a new scope; an inner name SHADOWS the outer one with the same name inside the block"]);
const label = "outer";
const seen: string[] = [label];
{
  const label = "inner";                                // legal: a different variable in a different scope
  seen.push(label);
}
seen.push(label);
show(() => seen);                                       // the outer one was never touched
const caps: Array<() => number> = [];
for (let n = 0; n < 6; n += 2) { caps.push(() => n); n++; }   // the body bumps its own copy of n
const gotCaps = caps.map((fn) => fn());
show(() => gotCaps);                                    // each iteration has its own n, and the next one starts from a copy of the last value
note("var in a for loop has ONE shared variable for the whole loop: the closures trap in lesson 10 (closures)");

export {};                                              // makes tsx treat this file as an ES module, so the top-level this is undefined
