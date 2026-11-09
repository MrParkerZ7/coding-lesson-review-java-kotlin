/**
 * 14e. ERASURE AND VARIANCE: types vanish at run time; in / out / in out; arrays are covariant
 *      Run: npx tsx src/14-generics/erasure-and-variance.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

class Tray<T> {
  items: T[] = [];
}

/** T is erased, so "is it a T?" cannot be asked: the caller passes a constructor ... */
function filterByClass<T>(items: unknown[], ctor: abstract new (...args: any[]) => T): T[] {
  return items.filter((x): x is T => x instanceof ctor);
}

/** ... or a type guard, which is the only way to test a primitive. */
type Guard<T> = (x: unknown) => x is T;
const isNumber: Guard<number> = (x): x is number => typeof x === "number";
function filterWith<T>(items: unknown[], guard: Guard<T>): T[] {
  return items.filter(guard);
}

/** The compiler trusts the cast and nothing checks it at run time. */
function cast<T>(x: unknown): T {
  return x as T;
}

class Animal {
  constructor(readonly name: string) {}
}
class Dog extends Animal {
  bark(): string {
    return `${this.name}: woof`;
  }
}
class Cat extends Animal {
  meow(): string {
    return `${this.name}: meow`;
  }
}

interface Producer<out T> {
  get(): T;
}
interface Consumer<in T> {
  accept(item: T): string;
}
interface Cell<in out T> {
  get(): T;
  set(value: T): void;
}

type Handler<T> = (x: T) => void;

title("GENERICS  -  erasure and variance");

section("Erasure: the type arguments are gone at run time");
syntax(["class Tray<T> { ... }  =>  class Tray { ... }", "tsc deletes every <...> and every annotation; tsx does the same"]);
const numbers = new Tray<number>();
const texts = new Tray<string>();
show(() => Object.getPrototypeOf(numbers) === Object.getPrototypeOf(texts));   // one class at run time for every T
wontCompile("function isT<T>(x: unknown) { return x instanceof T; }", "TS2693: 'T' only refers to a type, but is being used as a value here");
wontCompile("function is<T>(x: unknown) { return typeof x === T; }", "TS2693: 'T' only refers to a type, but is being used as a value here");
const lie = JSON.parse('{"id":"oops"}') as { id: number };   // a cast checks nothing: the JSON really holds a string
show(() => typeof lie.id);                              // string, although the declared type says number
const n = cast<number>("text");                         // compiles: unknown -> T is always allowed
show(() => thrown(() => n.toFixed(1)));                 // the lie surfaces later, far from the cast
note("Types are checked by tsc only; any value that comes from outside (JSON, fetch, input) needs a run-time check (lesson 18)");

section("Instead of instanceof T: pass a constructor or a type guard");
syntax(
  ["fn<T>(ctor: abstract new (...args: any[]) => T)", "the class value exists at run time and carries T (the one normal use of any)"],
  ["type Guard<T> = (x: unknown) => x is T", "a function that really tests the value: works for primitives too"],
);
const mixed: unknown[] = [1, "two", new Date(Date.UTC(2026, 10, 9)), 3, new Date(0)];
show(() => filterByClass(mixed, Date).map((d) => d.toISOString()));   // the class does the test: T = Date
show(() => filterWith(mixed, isNumber));                // T = number, inferred from the guard
const primitive: unknown = "a";
show(() => primitive instanceof String);                // TRAP: false for a primitive string, so instanceof cannot test number or string
show(() => Array.isArray([1, "a"]));                    // true: no run-time check can tell number[] from string[]
note("Java does the same with Class<T>, Kotlin with reified: here the value you pass in is the proof");

section("Variance: how Producer<Dog> relates to Producer<Animal>");
syntax(
  ["interface Producer<out T>", "covariant: T only comes OUT (return types): Producer<Dog> is a Producer<Animal>"],
  ["interface Consumer<in T>", "contravariant: T only goes IN (parameters): Consumer<Animal> is a Consumer<Dog>"],
  ["interface Cell<in out T>", "invariant: T goes both ways, so neither direction is allowed"],
);
const dogProducer: Producer<Dog> = { get: () => new Dog("Rex") };
const animalProducer: Producer<Animal> = dogProducer;   // out: reading an Animal from a Dog producer is safe
show(() => animalProducer.get());
const animalConsumer: Consumer<Animal> = { accept: (a) => `accepted ${a.name}` };
const dogConsumer: Consumer<Dog> = animalConsumer;      // in: whatever accepts any Animal accepts a Dog
show(() => dogConsumer.accept(new Dog("Max")));
const byName: (a: Animal, b: Animal) => number = (a, b) => a.name.localeCompare(b.name);
show(() => [new Dog("Zed"), new Dog("Abe")].toSorted(byName).map((d) => d.name));   // a comparator of Animals sorts Dogs
declare const dogCell: Cell<Dog>;
wontCompile("const p: Producer<Dog> = animalProducer;", "TS2322: Producer<Animal> is not assignable to Producer<Dog> (bark is missing)");
wontCompile("const c: Consumer<Animal> = dogConsumer;", "TS2322: Consumer<Dog> is not assignable to Consumer<Animal>");
wontCompile("const x: Cell<Animal> = dogCell;", "TS2322: Cell<Dog> is not assignable to Cell<Animal> (in out = invariant)");
wontCompile("interface Bad<in T> { get(): T }", "TS2636: Type 'Bad<super-T>' is not assignable to type 'Bad<sub-T>' as implied by variance annotation");
note("out = read only the T (Java: ? extends, Kotlin: out). in = write only the T (Java: ? super, Kotlin: in). Read and write: invariant");
note("Annotations (TS 4.7) are optional: TS infers variance. They are a checked promise (and can speed up the compiler on big generic types)");

section("Traps: arrays are covariant, methods are bivariant");
const dogs: Dog[] = [new Dog("Rex")];
const animals: Animal[] = dogs;                         // compiles: TS treats Array<T> as covariant, which is unsound
animals.push(new Cat("Tom"));                           // compiles too: a Cat is an Animal
show(() => dogs.length);                                // the Dog[] now holds 2 items, one of them a Cat
show(() => thrown(() => dogs[1].bark()));               // dogs[1] is typed Dog but is a Cat: bark is not a function
const handleDog: Handler<Dog> = (a: Animal) => void a;  // a handler of Animals can stand in for a handler of Dogs
show(() => handleDog(new Dog("Rex")));                  // undefined: the handler returns nothing
wontCompile("const h: Handler<Animal> = (d: Dog) => {};", "TS2322: (d: Dog) => void is not assignable to Handler<Animal> (strictFunctionTypes)");
interface WithMethod {
  handle(x: Animal): void;
}
const viaMethod: WithMethod = { handle(d: Dog) { void d; } };   // compiles: method syntax is still checked bivariantly
show(() => viaMethod.handle(new Cat("Tom")));           // a Cat reaches a Dog parameter with no complaint
note("Write callbacks as function-typed properties (handle: (x: T) => void) to get the strict check; methods stay bivariant");
note("Lesson 12 (type-compatibility) covers assignability in general");
