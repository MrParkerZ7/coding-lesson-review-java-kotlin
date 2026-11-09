/**
 * 3e. TWO-DIMENSIONAL ARRAYS: grids, transpose, rotate, sums, flat
 *     Run: npx tsx src/3-arrays/two-dimensional.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title } from "../output";

type Grid = number[][];

/** Rows become columns. O(rows * cols). Assumes a non-empty rectangular grid. */
function transpose(g: Grid): Grid {
  return g[0].map((_, c) => g.map((row) => row[c]));
}

/** Rotate 90 degrees clockwise: transpose, then reverse every row. O(rows * cols). */
function rotateClockwise(g: Grid): Grid {
  return transpose(g).map((row) => row.toReversed());
}

/** In-bounds neighbours (up, right, down, left) of cell (r, c): the grid-walk helper of many problems. */
function neighbours(g: Grid, r: number, c: number): number[] {
  const out: number[] = [];
  for (const [dr, dc] of [[-1, 0], [0, 1], [1, 0], [0, -1]] as const) {
    const row = g[r + dr];
    if (row !== undefined && c + dc >= 0 && c + dc < row.length) out.push(row[c + dc]);
  }
  return out;
}

title("TWO-DIMENSIONAL ARRAYS  -  an array of arrays");

section("Creating a grid: the right way and the wrong way");
syntax(
  ["Array.from({ length: R }, () => new Array<number>(C).fill(0))", "R rows, each built by its own call: separate arrays"],
  ["new Array(R).fill(new Array(C).fill(0))", "WRONG: ONE row object shared R times"],
);
const wrong = new Array<number[]>(3).fill(new Array<number>(3).fill(0));
wrong[0][0] = 1;  show(() => wrong);                   // TRAP: the "first row" change shows in ALL rows
show(() => wrong[0] === wrong[1]);                      // true: the same array three times
const grid = Array.from({ length: 3 }, (_, r) => Array.from({ length: 3 }, (_, c) => r * 3 + c));
show(() => grid);                                       // the function sees r and c: no second pass needed
grid[0][0] = 100;
show(() => grid[1][0]);                                 // 3: only row 0 changed (the rows are separate arrays)
const blank = Array.from({ length: 2 }, () => new Array<number>(4).fill(0));
show(() => blank);                                      // a 2 x 4 grid of zeros: rows first, then columns
note("Grid size: rows = grid.length, columns = grid[0].length (check the grid is not empty first)");
note("Reads like grid[r][c] compile because noUncheckedIndexedAccess is off; with it on, they need ! or a check");

section("Reading and writing");
show(() => grid[1][2]);                                // row 1, column 2
show(() => grid[0][9]);                                // undefined: a column out of range is just undefined
show(() => thrown(() => grid[5][0]));                  // TRAP: TypeError: a row out of range is undefined, and undefined[0] throws
show(() => grid[5]?.[0]);                               // the safe read: ?. stops at the missing row
const jagged: number[][] = [[1], [2, 3], [4, 5, 6]];
show(() => jagged.map((row) => row.length));            // rows may have different lengths (a jagged array)

section("Sums, diagonal, flat");
const m = [[1, 2, 3], [4, 5, 6]];
const sum = (xs: number[]): number => xs.reduce((a, b) => a + b, 0);
show(() => m.map(sum));                                 // sum of each row
show(() => transpose(m).map(sum));                      // sum of each column: transpose, then sum the rows
const square = [[1, 2], [3, 4]];
show(() => square.reduce((total, row, i) => total + row[i], 0));   // main diagonal: 1 + 4
show(() => m.flat());                                   // one level of nesting removed
show(() => [[1, [2]], [3, [4, [5]]]].flat(2));          // flat(depth): [1, 2, 3, 4, [5]] after two levels
show(() => [[1, [2]], [3, [4, [5]]]].flat(Infinity));   // all the way down
show(() => m.flatMap((row) => row.map((x) => x * 10)));   // map and flatten in one step
show(() => Math.max(...m.flat()));                      // the biggest cell of a grid

section("Transpose, rotate, neighbours");
show(() => transpose(m));                               // 2 x 3 becomes 3 x 2
show(() => rotateClockwise(m));                         // [[4, 1], [5, 2], [6, 3]]
let turned = m;
for (let i = 0; i < 4; i++) turned = rotateClockwise(turned);
show(() => turned);                                     // four quarter turns: back to the start
const g = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
show(() => neighbours(g, 1, 1));                        // the middle cell has four neighbours
show(() => neighbours(g, 0, 0));                        // a corner has two: the bounds check skips the rest
show(() => g.map((row) => row.join(" ")).join(" / "));  // a grid as text, row by row

section("Looping over a grid");
let total = 0;
for (let r = 0; r < g.length; r++) {
  for (let c = 0; c < g[r].length; c++) total += g[r][c];
}
show(() => total);                                      // indices: when you need r and c
let total2 = 0;
for (const row of g) for (const cell of row) total2 += cell;
show(() => total2);                                     // for...of: when you need only the values
show(() => [...g.entries()].map(([r, row]) => r + ":" + row.length));   // entries() gives [index, row]

section("Cost");
bigO(
  ["grid[r][c]", "O(1)"],
  ["build | flat | transpose | rotate | sum of all cells", "O(R * C) time, O(R * C) space for the new grid"],
  ["rotate in place (square grid)", "O(R * C) time, O(1) space: swap four cells at a time"],
  ["neighbours of one cell", "O(1): four directions, a bounds check each"],
  ["new Array(R).fill(row)", "the same cost as the right way, but it is WRONG: one shared row"],
);
