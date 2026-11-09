/**
 * 10b. THIS AND BINDING: this depends on HOW a function is called
 *      Run: npx tsx src/10-functions-and-closures/this-and-binding.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("THIS AND BINDING  -  who is this? It is decided at the call, not where the function is written");

section("The four ways a function is called");
syntax(
  ["obj.method(args)", "this = obj, the thing before the dot"],
  ["fn(args)", "this = undefined (ESM and class bodies are strict; a sloppy script would give the global object)"],
  ["fn.call(t, a) | fn.apply(t, [a]) | fn.bind(t, a)", "this = t: call now | call now with an array | return a NEW function with this fixed"],
  ["new Fn(args)", "this = the fresh object being built (lesson 11, prototypes-under-the-hood)"],
);
class Counter {
  n = 0;
  inc(): number { return ++this.n; }
}
const counter = new Counter();
show(() => counter.inc());                              // this is counter: 1
const detached = counter.inc;                           // only the function is copied, the object is left behind
show(() => thrown(() => detached()));                   // TRAP: this is undefined, so this.n is a TypeError
show(() => thrown(() => [1, 2].map(counter.inc)));      // same loss when a method is passed as a callback
show(() => [1, 2].map(() => counter.inc()));            // fix 1: wrap it in an arrow that calls it on the object
const boundInc = counter.inc.bind(counter);
show(() => [1, 2].map(boundInc));                       // fix 2: bind returns a copy whose this is fixed (see call, apply, bind)
note("TS does not warn about the detached call: a method has no declared this unless you add one (last section)");

section("Arrow functions have no this of their own");
syntax(["() => this", "uses the this of the code AROUND it, fixed when the arrow is created; call/apply/bind cannot change it"]);
class Clock {
  ticks = 0;
  tick(): number { return ++this.ticks; }
  runAll(list: number[]): number[] { return list.map(() => this.tick()); }   // the arrow keeps runAll's this
  tickField = (): number => ++this.ticks;                                   // a class-field arrow: this is the instance
}
const clock = new Clock();
show(() => clock.runAll([7, 7, 7]));                    // the callback arrow sees the Clock: [1, 2, 3]
const { tickField, tick } = clock;                      // pull both out of the object
show(() => tickField());                                // the arrow field still knows its instance: 4
show(() => thrown(() => tick()));                       // the prototype method lost it: TypeError
show(() => Object.hasOwn(clock, "tickField"));          // true: an arrow field is a copy on EVERY instance
show(() => Object.hasOwn(clock, "tick"));              // false: a method lives once on the prototype
const [c1, c2] = [new Clock(), new Clock()];
show(() => c1.tick === c2.tick);                        // true: one shared function
show(() => c1.tickField === c2.tickField);              // false: one function object per instance
const seen: unknown[] = [];
[1].forEach(function (this: unknown) { seen.push(this); });
[1].forEach(function (this: unknown) { seen.push(this); }, "ctx");
show(() => seen);                                       // a plain function callback: undefined; forEach's 2nd argument sets this
wontCompile("list.forEach(function () { this.ticks++; })", "TS2683: 'this' implicitly has type 'any' (noImplicitThis)");
const objectWithArrow = { name: "Ann", arrow: () => typeof this, method() { return typeof this; } };
show(() => objectWithArrow.arrow());                    // TRAP: an arrow property does NOT get the object: this is the module's, undefined
show(() => objectWithArrow.method());                   // a method shorthand does: "object"
note("Rule of thumb: arrows for callbacks that need the outer this, methods for things callers call on an object");

section("call, apply, bind");
syntax(
  ["fn.call(thisArg, a, b)", "call now; arguments one by one"],
  ["fn.apply(thisArg, [a, b])", "call now; arguments as an array (spread made it rare)"],
  ["fn.bind(thisArg, a)", "a new function with this (and leading arguments) fixed; the original is untouched"],
);
function describe(this: { name: string }, greeting: string, mark: string): string {
  return `${greeting}, ${this.name}${mark}`;
}
const ann = { name: "Ann" };
const bob = { name: "Bob" };
show(() => describe.call(ann, "Hi", "!"));
show(() => describe.apply(bob, ["Yo", "?"]));
const hiAnn = describe.bind(ann, "Hi");                 // partial application: this and the first argument are fixed
show(() => hiAnn("."));
show(() => hiAnn.call(bob, "~"));                       // TRAP: a bound function ignores a later call/bind: still Ann
show(() => hiAnn.name);                                 // bound functions are named "bound " + the original name
show(() => hiAnn.length);                               // 1: the unfixed parameters left (the this parameter is not counted)

section("The this parameter (TypeScript only)");
syntax(["function f(this: T, a: A)", "a fake first parameter: it types this and is ERASED, so it is not in fn.length and not passed"]);
function whoAmI(this: { name: string }): string { return this.name; }
show(() => whoAmI.call(ann));
show(() => whoAmI.length);                              // 0: the this parameter is not a real parameter
wontCompile("whoAmI()", "TS2684: The 'this' context of type 'void' is not assignable to 'this' of type '{ name: string; }'");
const greeter = { name: "Zed", greet(this: { name: string }): string { return `hi ${this.name}`; } };
show(() => greeter.greet());                            // called on the object: fine
wontCompile("const lost = greeter.greet; lost()", "TS2684: a detached method is now a compile error, not a run-time TypeError");
note("Lesson 13 shows the same loss for class methods; lesson 16 shows it for timers and callbacks");

export {};                                             // makes tsx treat the file as an ES module, where a top-level this is undefined
