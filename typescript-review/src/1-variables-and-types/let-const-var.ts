/**
 * 1a. LET, CONST AND VAR: scope, hoisting, the temporal dead zone, what const does NOT freeze
 *     Run: npx tsx src/1-variables-and-types/let-const-var.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** var is hoisted: the name exists from the top of the function, with the value undefined, until the line runs. */
function varHoisting(): [string, number | undefined] {
  const before = String(h);
  var h: number | undefined = 1;
  return [before, h];
}

/** var inside an if is still visible after it: its scope is the whole function. */
function varIf(flag: boolean): number | undefined {
  if (flag) {
    var inner: number | undefined = 1;
  }
  return inner;
}

/** var allows the same name twice in one scope (the second declaration is just an assignment). */
function varTwice(): number {
  var a = 1;
  var a = 2;
  return a;
}

/** An inner let hides the outer one only inside its own block. */
function shadowing(): number[] {
  let x = 1;
  const seen: number[] = [];
  {
    let x = 2;
    seen.push(x);
  }
  seen.push(x);
  return seen;
}

title("LET, CONST AND VAR  -  scope, hoisting and what const really fixes");

section("The three declarations");
syntax(
  ["const name = value;", "block scoped, cannot be reassigned: the default choice"],
  ["let name = value;", "block scoped, can be reassigned: for counters and accumulators"],
  ["var name = value;", "function scoped, hoisted: the pre-2015 form, do not write it"],
);
const price = 10;  show(() => price);                    // type inferred: number
let count = 0;  show(() => count);
count += 5;  show(() => count);                          // let may change its value
note("Default to const; use let only when the variable really changes; never use var in new code");

section("Scope: block (let, const) vs function (var)");
{
  var leaked = "var ignores the block";
  let hidden = "let stays in the block";
  show(() => hidden);                                    // visible inside the block
}
show(() => leaked);                                      // still visible: var belongs to the whole function (or module)
wontCompile("{ let hidden = 1; } hidden;", "TS2304: Cannot find name 'hidden' (the block ended)");
show(() => varIf(true));
show(() => varIf(false));                                // undefined: the declaration was hoisted, the assignment never ran
show(() => shadowing());                                 // [ 2, 1 ]: the inner x hid the outer one only inside its block
show(() => varTwice());                                  // 2: var accepts a redeclaration
wontCompile("let a = 1; let a = 2;", "TS2451: Cannot redeclare block-scoped variable 'a'");

section("Hoisting and the temporal dead zone (TDZ)");
syntax(["use before the declaration line", "var: undefined | let and const: ReferenceError | function declaration: works"]);
show(() => varHoisting());                               // [ 'undefined', 1 ]: h existed before its line, without a value
const peek = () => lateValue;                            // tsc allows this: the read happens later, inside a function
show(() => thrown(peek));                                // ReferenceError: lateValue is in its dead zone until its line runs
const lateValue = 5;
show(() => peek());                                      // 5: after the declaration the same call works
show(() => declaredBelow());                             // 7: a function DECLARATION is hoisted with its body (lesson 10)
function declaredBelow(): number {
  return 7;
}
wontCompile("console.log(early); let early = 1;", "TS2448: Block-scoped variable 'early' used before its declaration");
note("tsc catches the direct read; only a read hidden inside a function reaches the run-time ReferenceError");

section("const fixes the binding, not the value");
syntax(["const obj = { ... }", "the variable always points at the same object; the object itself may still change"]);
wontCompile("const n = 1; n = 2;", "TS2588: Cannot assign to 'n' because it is a constant");
wontCompile("const x;", "TS1155: 'const' declarations must be initialized");
const user = { name: "Ann", tags: ["a"] };
user.name = "Bob";  show(() => user.name);               // allowed: only the variable user is fixed
user.tags.push("b");  show(() => user.tags);             // allowed: the array behind it is mutable
const frozen = Object.freeze({ id: 1, inner: { n: 1 } });
wontCompile("frozen.id = 2;", "TS2540: Cannot assign to 'id' because it is a read-only property");
const writable = frozen as { id: number; inner: { n: number } };   // the cast removes readonly for tsc only
show(() => thrown(() => { writable.id = 2; }));          // TypeError: a frozen object refuses writes in strict mode (modules are strict)
show(() => frozen.id);                                   // 1: unchanged
frozen.inner.n = 2;  show(() => frozen.inner.n);         // TRAP: freeze is SHALLOW, the nested object is still writable
show(() => Object.isFrozen(frozen.inner));               // false
note("Deep immutability needs a recursive freeze or a copy-on-change style (lesson 13); in sloppy mode a frozen write is silently ignored");

section("The var-in-a-loop closure trap");
syntax(
  ["for (var i = 0; ...) { fns.push(() => i) }", "ONE i shared by every closure"],
  ["for (let i = 0; ...) { fns.push(() => i) }", "a NEW i per round: each closure keeps its own"],
);
const fromVar: Array<() => number> = [];
for (var k = 0; k < 3; k++) fromVar.push(() => k);
const fromLet: Array<() => number> = [];
for (let j = 0; j < 3; j++) fromLet.push(() => j);
show(() => fromVar.map((f) => f()));                     // TRAP: [ 3, 3, 3 ]: all three read the same k, which ended at 3
show(() => fromLet.map((f) => f()));                     // [ 0, 1, 2 ]: the reason let was added
show(() => k);                                           // 3: the var loop counter is still alive after the loop
note("Closures in depth: lesson 10");
