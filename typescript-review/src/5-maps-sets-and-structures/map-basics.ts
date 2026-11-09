/**
 * 5b. MAPS: key -> value, any key type, insertion order
 *     Run: npx tsx src/5-maps-sets-and-structures/map-basics.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("MAPS  -  look a value up by key");

section("Creating");
syntax(
  ["new Map<K, V>([[k1, v1], [k2, v2]])", "from an iterable of [key, value] pairs (an array of pairs, or another Map)"],
  ["new Map(Object.entries(obj))", "from a plain object (the keys become strings)"],
  ["Object.fromEntries(map)", "back to a plain object (every key becomes a string)"],
);
const ages = new Map([["ann", 30], ["bob", 25]]);       // inferred as Map<string, number>
show(() => ages);
show(() => ages.size);                                  // a property, like Set
show(() => new Map(Object.entries({ x: 1, y: 2 })));
show(() => Object.fromEntries(ages));
show(() => new Map<string, number>());                  // an empty Map needs its type arguments

section("Reading, changing");
syntax(
  ["m.get(k) | m.has(k)", "the value or undefined | does the key exist"],
  ["m.set(k, v) | m.delete(k) | m.clear()", "add or overwrite (returns the MAP) | remove (true if it existed) | empty"],
);
show(() => ages.get("ann"));
show(() => ages.get("zed"));                            // undefined when missing, never an exception
show(() => ages.get("zed") ?? -1);                      // ?? : a default only for undefined and null
const stock = new Map([["apple", 0]]);
show(() => stock.get("apple") || 99);                   // TRAP: 99, because 0 is falsy and || replaces it
show(() => stock.get("apple") ?? 99);                   // 0: the right default operator (lesson 7)
show(() => stock.has("apple"));                         // true even when the value is 0 or undefined
wontCompile("const n: number = ages.get('ann')", "TS2322: get returns number | undefined (always, strict or not)");
const m = new Map<string, number>();
m.set("a", 1).set("b", 2);  show(() => m);              // set returns the Map, so calls chain
m.set("a", 10);  show(() => m);                         // overwrite: the key keeps its position
m.delete("a");
m.set("a", 1);  show(() => m);                          // delete then set: it moves to the END
show(() => m.delete("zzz"));                            // false: nothing to delete, no exception

section("Looping: in insertion order");
syntax(
  ["for (const [k, v] of m)", "destructure each [key, value] entry"],
  ["m.keys() | m.values() | m.entries()", "iterators (not arrays): spread them or use for...of"],
  ["m.forEach((value, key) => ...)", "TRAP: the VALUE comes first, the key second"],
);
const lines: string[] = [];
for (const [k, v] of ages) lines.push(k + "=" + v);
show(() => lines);
show(() => [...ages.keys()]);
show(() => [...ages.values()]);
const forEachArgs: string[] = [];
ages.forEach((value, key) => forEachArgs.push(key + ":" + value));
show(() => forEachArgs);                                // the callback is (value, key), the reverse of for...of [key, value]
show(() => [...ages].map(([k, v]) => k + v));           // spread a Map into [key, value] pairs, then use array methods
show(() => [...ages].filter(([, v]) => v > 26));        // filter entries by value: still pairs
show(() => new Map([...ages].filter(([, v]) => v > 26)));   // and back into a Map
show(() => new Map([...ages].sort(([, a], [, b]) => a - b)));   // a Map cannot sort itself: sort the pairs, build a new Map
show(() => Math.max(...ages.values()));                 // the biggest value
show(() => [...ages].reduce((best, e) => (e[1] > best[1] ? e : best))[0]);   // the KEY with the biggest value

section("Map vs plain object as a dictionary");
const typed = new Map<number | string, string>([[1, "number"], ["1", "string"]]);
show(() => typed.size);                                 // 2: a Map keeps the key TYPE (the key type must say so: number | string) ...
const asObject: Record<string, string> = {};
asObject[1] = "number";
asObject["1"] = "string";
show(() => asObject);                                   // ... an object turns every key into a string: ONE key, the last value wins
show(() => new Map([[NaN, "nan"]]).get(NaN));           // NaN works as a key (SameValueZero), as in a Set
const k1 = {};
const k2 = {};
show(() => new Map([[k1, "one"]]).get(k2));             // undefined: object keys are compared by reference
show(() => new Map([[k1, "one"]]).get(k1));             // "one"
show(() => JSON.stringify(new Map([["a", 1]])));        // TRAP: "{}", a Map is not serializable as it is (lesson 18)
show(() => JSON.stringify([...new Map([["a", 1]])]));   // convert first: an array of pairs
show(() => JSON.stringify(Object.fromEntries(new Map([["a", 1]]))));   // or an object
note("Use a Map when keys are not fixed strings, when you add and remove often, or when you need .size and a clean order. Use an object for a fixed record or JSON data");

section("WeakMap in two lines");
const meta = new WeakMap<object, string>();
const user = { name: "ann" };
meta.set(user, "extra data");
show(() => meta.get(user));                             // keys must be objects; the entry vanishes when the key is garbage collected
show(() => thrown(() => meta.set("s" as unknown as object, "x")));   // TypeError: a primitive cannot be a WeakMap key
wontCompile("meta.set('s', 'x')", "TS2345: argument of type 'string' is not assignable to parameter of type 'object'");
note("A WeakMap has no size, no keys(), no iteration: you cannot list it. Use it to attach private data to an object without leaking it");

section("Cost");
bigO(
  ["Map get | set | has | delete", "O(1) on average"],
  ["Map size", "O(1)"],
  ["iterate keys | values | entries | spread", "O(n)"],
  ["find a key by VALUE", "O(n): a Map is indexed by key only"],
  ["sort the entries", "O(n log n), and it builds a new Map"],
);
