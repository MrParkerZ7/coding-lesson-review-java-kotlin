/**
 * 13b. INHERITANCE AND ABSTRACT: extends, super, override, abstract, implements, composition
 *      Run: npx tsx src/13-classes-and-objects/inheritance-and-abstract.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("INHERITANCE AND ABSTRACT  -  extends, super, override, abstract, implements");

section("extends, super, override");
syntax(
  ["class B extends A { }", "B inherits A's fields and methods (B.prototype links to A.prototype)"],
  ["constructor() { super(args); ... }", "a derived constructor MUST call super() before it touches this"],
  ["override m() { super.m(); }", "marks an intentional override (error if A has no m); super.m() runs A's version"],
);
class Vehicle {
  constructor(public wheels: number) {}
  honk(): string { return "beep"; }
  describe(): string { return `${this.wheels} wheels`; }
}
class Car extends Vehicle {
  constructor(public brand: string) { super(4); }       // super first, then this
  override honk(): string { return `${super.honk()} beep`; }
}
const car = new Car("Volvo");
show(() => car.honk());                                 // the parent's answer, extended
show(() => car.describe());                             // inherited unchanged
const asVehicle: Vehicle = car;
show(() => asVehicle.honk());                           // the run-time class decides: Car's honk runs through a Vehicle variable
show(() => car instanceof Vehicle);
show(() => Object.getPrototypeOf(Car) === Vehicle);     // statics inherit too: the Car function itself links to Vehicle
wontCompile("constructor() { this.n = 1; super(1); }", "TS17009: 'super' must be called before accessing 'this' in a derived constructor");
wontCompile("class D extends Base { constructor() { } }", "TS2377: Constructors for derived classes must contain a 'super' call");
wontCompile("class S2 extends S { override nope(): void {} }", "TS4113: an 'override' member must exist in the base class");
note("noImplicitOverride (not part of strict) makes the keyword mandatory: a silent override is then TS4114");

section("abstract classes");
syntax(["abstract class A { abstract m(): T; }", "cannot be instantiated; abstract members have no body and every concrete subclass must supply them"]);
abstract class Shape {
  abstract area(): number;                              // no body
  describe(): string { return `${this.constructor.name} with area ${this.area()}`; }   // a concrete method may use the abstract one
}
class Rect extends Shape {
  constructor(private w: number, private h: number) { super(); }
  area(): number { return this.w * this.h; }
}
show(() => new Rect(3, 4).describe());
wontCompile("new Shape()", "TS2511: Cannot create an instance of an abstract class");
wontCompile("class Blob extends Shape {}", "TS2515: Non-abstract class 'Blob' does not implement inherited abstract member area");
const LooseShape = Shape as unknown as new () => Shape; // abstract is compile-time only: a cast gets past it
const loose = new LooseShape();
show(() => thrown(() => loose.describe()));             // TypeError: this.area is not a function

section("implements is only a compile-time check");
interface Printable { print(): string }
class Memo implements Printable { print(): string { return "memo"; } }
show(() => new Memo().print());
const protoOf = (o: object): unknown => Object.getPrototypeOf(o);
show(() => protoOf(Memo.prototype) === Object.prototype);   // implements added nothing to the prototype chain
wontCompile("class NoPrint implements Printable {}", "TS2420: Class 'NoPrint' incorrectly implements interface 'Printable'");
note("An interface does not exist at run time, so 'x instanceof Printable' cannot compile (lesson 12, type-compatibility)");

section("Trap: an overridden method called from the parent constructor");
class Parent {
  constructor(log: unknown[]) { log.push(this.label()); }   // runs before the child's fields exist
  label(): string { return "parent"; }
}
class Child extends Parent {
  private tag = "child";                                // assigned only AFTER super() has returned
  override label(): string { return this.tag; }
}
const seen: unknown[] = [];
new Child(seen);
show(() => seen);                                       // TRAP: [undefined], although label() is typed to return a string
note("Never call an overridable method from a constructor: the subclass is not initialised yet (Kotlin and Java have the same trap)");

section("Composition over inheritance");
syntax(["class Truck { constructor(private engine: Engine) {} }", "has-a: hold a part and delegate, instead of is-a: inherit its whole surface"]);
class Engine { start(): string { return "vroom"; } }
class Truck {
  constructor(private engine: Engine = new Engine()) {}
  go(): string { return this.engine.start(); }
}
show(() => new Truck().go());
show(() => new Truck({ start: () => "fake" }).go());    // any object with start() fits (structural typing): easy to test
note("Inherit for a real is-a relation that you control; otherwise compose. Deep hierarchies make a child depend on its parent's internals");
