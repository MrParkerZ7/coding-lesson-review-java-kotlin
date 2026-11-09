/**
 * Helper for modules.ts. It only EXPORTS (it prints nothing), so any file may import it safely.
 * Not a lesson file: do not run it on its own.
 */
export const VERSION = "1.0.0";

/** Types and interfaces are erased: they exist for tsc only and are not in the module at run time. */
export type Point = { x: number; y: number };
export interface Shape {
  area(): number;
}

export function add(a: number, b: number): number {
  return a + b;
}

/** An exported let is a LIVE binding: importers always see its current value. */
export let counter = 0;
export function increment(): void {
  counter++;
}

/** The module body runs once, the first time anyone imports it: this array records that. */
export const loads: string[] = [];
loads.push("evaluated");

export default class Greeter {
  greet(name: string): string {
    return `Hello ${name}`;
  }
}
