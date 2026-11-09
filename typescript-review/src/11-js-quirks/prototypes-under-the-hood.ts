/**
 * 11c. PROTOTYPES UNDER THE HOOD: the chain, class as sugar, what new does
 *      Run: npx tsx src/11-js-quirks/prototypes-under-the-hood.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title } from "../output";

title("PROTOTYPES UNDER THE HOOD  -  objects inherit from objects, a class is sugar");

section("The prototype chain");
syntax(
  ["Object.getPrototypeOf(obj)", "the object that obj falls back to when a property is missing"],
  ["Object.create(proto)", "a new object whose prototype is proto"],
  ["obj.x", "own property first, then each prototype up the chain, then undefined"],
);
const protoOf = (o: object): object | null => Object.getPrototypeOf(o);   // a short alias used below
const animal = { eats: true, describe(): string { return `eats=${this.eats}`; } };
const dog = Object.create(animal) as { eats: boolean; barks?: boolean; describe(): string };
dog.barks = true;
show(() => dog.eats);                                   // not on dog itself: found one step up, on animal
show(() => protoOf(dog) === animal);
show(() => Object.hasOwn(dog, "eats"));                 // own properties only, no chain
show(() => "eats" in dog);                              // the in operator walks the chain
show(() => Object.keys(dog));                           // own enumerable keys only
const walked: string[] = [];
for (const key in dog) walked.push(key);
show(() => walked);                                     // TRAP: for...in also lists INHERITED enumerable keys, methods of a literal included
show(() => dog.describe());                             // this is dog, the object before the dot
show(() => protoOf(animal) === Object.prototype);       // animal -> Object.prototype -> null
show(() => protoOf(Object.prototype));                  // the chain ends in null
dog.eats = false;                                       // writing CREATES an own property: it shadows, never edits the prototype
show(() => animal.eats);
show(() => Object.hasOwn(dog, "eats"));
note("obj.__proto__ is an old accessor for the same link; in code use Object.getPrototypeOf / Object.setPrototypeOf");

section("A class is sugar over a constructor function and a prototype");
syntax(
  ["class C { m() {} }", "C is a function; m is placed ONCE on C.prototype and shared by every instance"],
  ["new C()", "an object whose prototype is C.prototype"],
);
class Point {
  constructor(public x: number, public y: number) {}
  len(): number { return Math.hypot(this.x, this.y); }
}
const p = new Point(3, 4);
show(() => typeof Point);                               // the class IS its constructor function
show(() => protoOf(p) === Point.prototype);
show(() => Object.hasOwn(p, "x"));                      // fields are own: one copy per instance
show(() => Object.hasOwn(p, "len"));                    // the method lives on the prototype, once
show(() => Object.hasOwn(Point.prototype, "len"));
show(() => p.constructor === Point);                    // found through the prototype
show(() => Object.keys(Point.prototype));               // class methods are not enumerable, so for...in skips them
show(() => Object.getOwnPropertyNames(Point.prototype));
class Button {
  label = "ok";
  method(): string { return this.label; }               // prototype: one function shared by all
  arrow = (): string => this.label;                     // class field: a new function per instance (this-and-binding)
}
const [b1, b2] = [new Button(), new Button()];
show(() => b1.method === b2.method);
show(() => b1.arrow === b2.arrow);
show(() => Object.keys(b1));                            // only the own ones: the arrow field, not the method

section("instanceof asks about the chain, nothing else");
syntax(["x instanceof C", "is C.prototype somewhere on x's prototype chain?  (same as C.prototype.isPrototypeOf(x))"]);
show(() => p instanceof Point);
show(() => Point.prototype.isPrototypeOf(p));           // the question, asked by hand
const fake = Object.create(Point.prototype) as Point;   // linked to the prototype, but the constructor never ran
show(() => fake instanceof Point);                      // TRAP: instanceof only checks the chain, not that a constructor ran
show(() => fake.x);                                     // no constructor ran, so no fields
class Point3 extends Point {}
show(() => protoOf(Point3.prototype) === Point.prototype);   // extends = one more link in the chain
show(() => new Point3(0, 0) instanceof Point);

section("What new does, written by hand");
syntax(["new F(args)", "1. create an object linked to F.prototype  2. call F with this = that object  3. an object RETURNED by F replaces it"]);
function Legacy(this: { n: number }, n: number): void { this.n = n; }
Legacy.prototype.double = function (this: { n: number }): number { return this.n * 2; };
function fakeNew(ctor: Function, ...args: unknown[]): object {
  const obj = Object.create(ctor.prototype);            // step 1
  const result = ctor.apply(obj, args);                 // step 2
  return typeof result === "object" && result !== null ? result : obj;   // step 3
}
const made = fakeNew(Legacy, 4) as { n: number; double(): number };
show(() => made.n);
show(() => made.double());                              // found through the prototype link
show(() => made instanceof Legacy);
class Hijack { constructor() { return { hijacked: true } as unknown as Hijack; } }
show(() => new Hijack());                               // step 3: the returned object replaces the new one
show(() => new Hijack() instanceof Hijack);

section("Telling objects apart: Object.prototype.toString");
syntax(["Object.prototype.toString.call(v)", "'[object Tag]' with the built-in type of v: the old reliable check (typeof says 'object' for null and arrays)"]);
const tag = (v: unknown): string => Object.prototype.toString.call(v);
show(() => tag([]));
show(() => tag(null));
show(() => tag(new Date(0)));
show(() => tag(() => 0));
class Tagged { get [Symbol.toStringTag](): string { return "Tagged"; } }
show(() => tag(new Tagged()));                          // a class can choose its own tag
const bare = Object.create(null) as Record<string, number>;
show(() => "toString" in {});                           // every plain object inherits it from Object.prototype
show(() => "toString" in bare);                         // Object.create(null) has NO prototype (a clean dictionary, lesson 5)
