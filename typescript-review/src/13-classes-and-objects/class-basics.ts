/**
 * 13a. CLASS BASICS: constructor, parameter properties, private vs #private, accessors, static
 *      Run: npx tsx src/13-classes-and-objects/class-basics.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("CLASS BASICS  -  constructor, fields, access modifiers, accessors, static");

section("Constructor, fields, parameter properties");
syntax(
  ["class C { a: T; b = v; constructor(x: T) { this.a = x; } }", "declare fields; give them an initializer or assign them in the constructor"],
  ["constructor(private x: T, public y = d)", "parameter properties: a modifier on a parameter declares AND assigns the field"],
  ["readonly a: T", "may be assigned only in the constructor (checked at compile time)"],
);
class Account {
  readonly id: string;
  balance = 0;
  constructor(id: string, private owner: string, public currency = "USD") { this.id = id; }
  deposit(amount: number): this { this.balance += amount; return this; }   // returning this allows chaining
  describe(): string { return `${this.owner}: ${this.balance} ${this.currency}`; }
}
const acct = new Account("a1", "Ann").deposit(50).deposit(25);
show(() => acct);                                       // every field, the private one included: types hide nothing
show(() => acct.describe());
wontCompile("acct.id = 'b2'", "TS2540: Cannot assign to 'id' because it is a read-only property");
wontCompile("Account('a1', 'Bob')", "TS2348: Value of type 'typeof Account' is not callable. Did you mean to include 'new'?");
const callWithoutNew = Account as unknown as (id: string) => unknown;   // a cast to get past tsc
show(() => thrown(() => callWithoutNew("a1")));         // TypeError: a class constructor cannot be called without new

section("private vs #private: only one is real");
syntax(
  ["public | protected | private", "TypeScript only: checked by tsc, then erased; nothing is protected at run time"],
  ["#name", "JavaScript private: enforced by the engine; only code inside the class body can touch it"],
);
class Vault {
  private hint = "tsprivate";                           // erased: an ordinary property in the output
  #secret = 42;                                         // a real private slot
  peek(): number { return this.#secret; }
  static isVault(o: object): boolean { return #secret in o; }   // brand check: does o carry the private slot?
}
const vault = new Vault();
wontCompile("vault.hint", "TS2341: Property 'hint' is private and only accessible within class 'Vault'");
wontCompile("vault.#secret", "TS18013: Property '#secret' is not accessible outside class 'Vault' because it has a private identifier");
show(() => vault["hint"]);                              // TRAP: bracket access passes tsc, and at run time nothing is hidden
const leaked = vault as unknown as { hint: string };    // a cast does the same
show(() => leaked.hint);
show(() => Object.keys(vault));                         // hint is an own property; #secret is not listed anywhere
show(() => JSON.stringify(vault));                      // the TS-private field leaks into JSON (lesson 18); #secret never does
show(() => vault.peek());
show(() => Vault.isVault(vault));
show(() => Vault.isVault({}));
show(() => thrown(() => Vault.prototype.peek.call({})));   // a real #private cannot be forged: the slot is missing on {}

section("Getters and setters");
syntax(
  ["get name(): T { ... }", "read like a property; computed on every read"],
  ["set name(v: T) { ... }", "assign like a property; the place for validation"],
);
class Thermo {
  #celsius = 0;
  get celsius(): number { return this.#celsius; }
  set celsius(value: number) {
    if (value < -273.15) throw new RangeError("below absolute zero");
    this.#celsius = value;
  }
  get fahrenheit(): number { return (this.#celsius * 9) / 5 + 32; }   // a getter alone: read-only
}
const temp = new Thermo();
temp.celsius = 100;
show(() => temp.fahrenheit);
show(() => thrown(() => { temp.celsius = -300; }));     // the setter rejects it
wontCompile("temp.fahrenheit = 50", "TS2540: Cannot assign to 'fahrenheit' because it is a read-only property");
show(() => Object.hasOwn(Thermo.prototype, "celsius"));   // accessors live on the prototype, not on each instance
show(() => JSON.stringify(temp));                       // {}: no own enumerable property, so JSON sees nothing

section("static members and static blocks");
syntax(
  ["static x = v  |  static m() { }", "belongs to the class itself: reached as C.x, never through an instance"],
  ["static { ... }", "runs ONCE, when the class is defined: the place for non-trivial static setup (ES2022)"],
);
class Registry {
  static count = 0;
  static readonly names: string[] = [];
  static { Registry.names.push("boot"); }               // already ran before any line below
  constructor() { Registry.count++; }
}
show(() => Registry.names);
new Registry();
new Registry();
show(() => Registry.count);                             // one counter for the whole class
wontCompile("new Registry().count", "TS2576: Property 'count' does not exist on type 'Registry'. Did you mean to access the static member 'Registry.count'?");
show(() => "count" in new Registry());                  // false: statics are on the constructor function

section("Prototype methods vs instance arrows (this-and-binding, prototypes-under-the-hood)");
class Greeter {
  hi(): string { return "hi"; }                         // one function on Greeter.prototype, shared
  hey = (): string => "hey";                            // a new function in EVERY instance
}
const greeter = new Greeter();
show(() => Object.keys(greeter));                       // only the own property
show(() => Object.hasOwn(Greeter.prototype, "hi"));
note("Rule of thumb: private for team discipline and tooling, #private when the data must really be unreachable");
