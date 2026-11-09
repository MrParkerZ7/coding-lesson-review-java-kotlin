/**
 * 15a. UTILITY TYPES: Partial, Required, Readonly, Pick, Omit, Record, Exclude, Extract, NonNullable,
 *      ReturnType, Parameters, Awaited, InstanceType
 *      Run: npx tsx src/15-utility-and-advanced-types/utility-types.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

interface User {
  id: number;
  name: string;
  email?: string;
  readonly role: "admin" | "user";
}

const user: User = { id: 1, name: "Ann", role: "user" };

/** A patch may change name and email but never the id or the role. */
function update(u: User, patch: Partial<Omit<User, "id" | "role">>): User {
  return { ...u, ...patch };
}

/** The type guard narrows T to NonNullable<T>, so the result array has no null or undefined in its type. */
function compact<T>(items: T[]): NonNullable<T>[] {
  return items.filter((x): x is NonNullable<T> => x != null);
}

function makeUser(id: number, name: string) {
  return { id, name, active: true };
}

async function loadCount(): Promise<number> {
  return 3;
}

class Counter {
  constructor(public start: number) {}
}

type Role = "admin" | "user" | "guest";
type Shape = "circle" | "square" | "tri";

title("UTILITY TYPES  -  built-in mapped and conditional types");

section("Object shapes: Partial, Required, Readonly, Pick, Omit");
syntax(
  ["Partial<T> | Required<T>", "every property optional | every property required (removes ?)"],
  ["Readonly<T>", "every property readonly (compile time only)"],
  ["Pick<T, K> | Omit<T, K>", "keep only the keys K | drop the keys K"],
);
const draft: Partial<User> = { name: "draft" };         // id and role may be missing: Partial adds ? to every property
show(() => draft);
const full: Required<User> = { id: 2, name: "Bo", email: "bo@x.io", role: "admin" };   // email is now mandatory
show(() => full);
const view: Pick<User, "id" | "name"> = { id: 1, name: "Ann" };
show(() => view);                                       // only the picked keys exist in the type
const noMail: Omit<User, "email" | "role"> = { id: 1, name: "Ann" };
show(() => noMail);
show(() => update(user, { name: "Bo" }));               // Partial<...> as a patch: only the given keys change
show(() => update(user, { name: undefined }).name);     // TRAP: Partial allows an explicit undefined, the spread overwrites the name, the type still says string
const locked: Readonly<User> = { id: 1, name: "Ann", role: "user" };
Object.assign(locked, { name: "changed" });             // compiles: Object.assign takes any target, Readonly checks assignments only
show(() => locked.name);                                // changed: Readonly never exists at run time (use Object.freeze for that)
wontCompile("const f: Required<User> = { id: 1, name: 'A', role: 'user' };", "TS2741: Property 'email' is missing in type ... but required in type 'Required<User>'");
wontCompile("locked.name = 'x';", "TS2540: Cannot assign to 'name' because it is a read-only property");
wontCompile("type P = Pick<User, 'nope'>;", "TS2344: Type '\"nope\"' does not satisfy the constraint 'keyof User'");
note("TRAP: Omit<User, 'nope'> compiles and changes nothing (its key type is not checked), Pick<User, 'nope'> does not: a typo in Omit is silent");

section("Keys and unions: Record, Exclude, Extract, NonNullable");
syntax(
  ["Record<K, V>", "an object type with every key of K mapped to V"],
  ["Exclude<U, X> | Extract<U, X>", "remove the members of U assignable to X | keep only those"],
  ["NonNullable<T>", "T without null and undefined"],
);
const perms: Record<Role, string[]> = { admin: ["all"], user: ["read", "write"], guest: ["read"] };
show(() => perms.user);                                 // every Role must appear: add a Role and this line stops compiling
const solid: Exclude<Shape, "circle"> = "square";       // "circle" | "square" | "tri" without "circle"
show(() => solid);
const round: Extract<Shape, "circle" | "oval"> = "circle";   // Extract keeps only what Shape has: "oval" is not in it
show(() => round);
show(() => compact([1, null, 2, undefined, 0]));        // number[], not (number | null | undefined)[]; 0 stays because the test is != null
wontCompile("const r: Record<Role, number> = { admin: 1, user: 2 };", "TS2741: Property 'guest' is missing in type ... but required in type 'Record<Role, number>'");
wontCompile("const s: Exclude<Shape, 'tri'> = 'tri';", "TS2322: Type '\"tri\"' is not assignable to type '\"circle\" | \"square\"'");
note("Exclude and Extract are conditional types that distribute over a union, one member at a time (next file)");

section("Functions, promises and classes: ReturnType, Parameters, Awaited, InstanceType");
syntax(
  ["ReturnType<typeof fn> | Parameters<typeof fn>", "the return type | the parameter list as a tuple"],
  ["Awaited<T>", "what await produces: unwraps Promise<Promise<T>> all the way down"],
  ["InstanceType<typeof C> | ConstructorParameters<typeof C>", "the object a class builds | its constructor argument tuple"],
);
const args: Parameters<typeof makeUser> = [7, "Ann"];   // [id: number, name: string]
const made: ReturnType<typeof makeUser> = makeUser(...args);
show(() => made);                                       // the type came from the function: change the function, the type follows
const count: Awaited<ReturnType<typeof loadCount>> = await loadCount();   // ReturnType alone would be Promise<number>
show(() => count);
const ctorArgs: ConstructorParameters<typeof Counter> = [5];
const counter: InstanceType<typeof Counter> = new Counter(...ctorArgs);
show(() => counter);
wontCompile("const a: Parameters<typeof makeUser> = [7];", "TS2322: Source has 1 element(s) but target requires 2");
note("typeof fn is the TYPE of the function value; the utility type reads its return or parameters instead of repeating them");
note("Also built in: Uppercase, Lowercase, Capitalize, Uncapitalize, ThisParameterType, OmitThisParameter, NoInfer");
