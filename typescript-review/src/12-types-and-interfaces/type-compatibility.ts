/**
 * 12d. TYPE COMPATIBILITY: structural typing, covariance holes, {} vs object, any, brands, erasure
 *      Run: npx tsx src/12-types-and-interfaces/type-compatibility.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("TYPE COMPATIBILITY  -  what fits where, and where the checker is unsound");

section("Structural typing: the shape counts, the name does not");
syntax(
  ["value: T", "assignable if it has every member T requires; extra members are fine (when it is a variable, not a fresh literal)"],
  ["class with a private or protected member", "that member makes the class effectively nominal"],
);
interface Named { name: string }
const wolf = { name: "wolf", legs: 4 };
const named: Named = wolf;                              // compiles: wolf has a name
show(() => named);
class A { x = 1; }
class B { x = 2; }
const aFromB: A = new B();                              // compiles: two unrelated classes, the same shape
show(() => aFromB instanceof A);                        // false: at run time the prototype chain still knows it is a B
class SecretA { private id = 1; }
class SecretB { private id = 1; }
wontCompile("const s: SecretA = new SecretB()", "TS2322: Types have separate declarations of a private property 'id'");

section("Unsound by design: array covariance and method parameters");
class Animal { constructor(public name: string) {} }
class Dog extends Animal { bark(): string { return "woof"; } }
class Cat extends Animal { meow(): string { return "meow"; } }
const dogs: Dog[] = [new Dog("rex")];
const animals: Animal[] = dogs;                         // allowed: a Dog[] is treated as an Animal[]
const tom = new Cat("tom");
animals.push(tom);                                      // compiles: a Cat is an Animal ... but dogs IS this same array
show(() => dogs.length);                                // TRAP: the Dog[] now holds a Cat
show(() => thrown(() => dogs[1].bark()));               // tsc believed dogs[1] was a Dog
const safe: readonly Animal[] = dogs;                   // readonly arrays are the safe way to say "I only read"
wontCompile("safe.push(tom)", "TS2339: Property 'push' does not exist on type 'readonly Animal[]'");
interface Handlers { handle(a: Animal): string }        // METHOD syntax: parameters stay bivariant even in strict mode
const handlers: Handlers = { handle: (d: Dog) => d.bark() };   // compiles: the method check is bivariant (strictFunctionTypes skips it)
show(() => thrown(() => handlers.handle(tom)));         // TypeError: handle(cat) calls cat.bark()

section("strictFunctionTypes: function-type parameters are contravariant");
syntax(["type H = (a: Animal) => R", "a function that accepts any Animal may stand in for one that gets Dogs, not the other way round"]);
type AnimalHandler = (a: Animal) => string;
type DogHandler = (d: Dog) => string;
const handleAnimal: AnimalHandler = (a) => a.name;
const handleDog: DogHandler = handleAnimal;            // fine: it copes with every Animal, so it copes with a Dog
show(() => handleDog(new Dog("rex")));
wontCompile("const bad: AnimalHandler = (d: Dog) => d.bark()", "TS2322 (it compiles with --strictFunctionTypes false)");

section("{} vs object vs Object, and the any virus");
syntax(
  ["{}", "any value except null and undefined (numbers and strings fit)"],
  ["object", "non-primitives only: objects, arrays, functions"],
  ["Object", "like {}, the type of what Object.prototype offers: do not use it"],
);
const anything: {} = 5;                                 // a number is fine
const nonPrimitive: object = [1];
show(() => [typeof anything, typeof nonPrimitive]);
wontCompile("const o: object = 5", "TS2322: Type 'number' is not assignable to type 'object'");
wontCompile("const e: {} = null", "TS2322: Type 'null' is not assignable to type '{}'");
const parsed = JSON.parse('{"count": 1}');              // JSON.parse returns any
const count: string = parsed.count;                     // compiles: any is assignable to everything
show(() => typeof count);                               // TRAP: the annotation lied, any switched the checker off
note("Fix: type JSON.parse's result as unknown and validate it with a type guard (lesson 18, json-parse)");

section("Branded types and erasure");
syntax(["type UserId = number & { readonly __brand: 'UserId' }", "a number that tsc will not mix up with other numbers"]);
type UserId = number & { readonly __brand: "UserId" };
type OrderId = number & { readonly __brand: "OrderId" };
const userId = (n: number): UserId => n as UserId;
const orderId = (n: number): OrderId => n as OrderId;
const loadUser = (id: UserId): string => `user ${id}`;
show(() => loadUser(userId(7)));
wontCompile("loadUser(orderId(7))", "TS2345: Argument of type 'OrderId' is not assignable to parameter of type 'UserId'");
wontCompile("loadUser(7)", "TS2345: Argument of type 'number' is not assignable to parameter of type 'UserId'");
show(() => typeof userId(7));                           // the brand exists only in the type: at run time it is a plain number
interface Shape { kind: string }
wontCompile("x instanceof Shape", "TS2693: 'Shape' only refers to a type, but is being used as a value here");
const isShape = (x: unknown): x is Shape => typeof x === "object" && x !== null && "kind" in x && typeof x.kind === "string";
show(() => isShape({ kind: "circle" }));                // the run-time check you CAN write: a type guard that inspects the value
show(() => isShape({ radius: 1 }));
note("Types are erased: nothing of an interface, alias or annotation exists at run time, so validate data that comes from outside");
