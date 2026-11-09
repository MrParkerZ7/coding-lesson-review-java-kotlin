/**
 * 9d. INTERVALS, GRIDS AND DP: sort and merge, DFS, BFS, dynamic programming
 *     Run: npx tsx src/9-interview-drills/intervals-and-graphs.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

type Interval = [start: number, end: number];

/**
 * MERGE INTERVALS. Problem: join every overlapping pair (start, end) into one.
 * Idea: sort a COPY by start with a numeric comparator; then each interval either touches the last merged one
 * (extend its end with max) or starts a new one. Time O(n log n) for the sort, space O(n).
 */
function mergeIntervals(intervals: Interval[]): Interval[] {
  if (intervals.length === 0) return [];
  const sorted = [...intervals].sort((a, b) => a[0] - b[0]);   // sort() changes the array it is called on: copy first
  const merged: Interval[] = [[sorted[0][0], sorted[0][1]]];
  for (const [start, end] of sorted.slice(1)) {
    const last = merged[merged.length - 1];
    if (start <= last[1]) last[1] = Math.max(last[1], end);    // max: the next one may sit INSIDE the last
    else merged.push([start, end]);
  }
  return merged;
}

/** TRAP: the default sort() turns each [start, end] into the text "start,end" and sorts the TEXT: "10,12" comes before "2,3". */
function mergeIntervalsWrong(intervals: Interval[]): Interval[] {
  if (intervals.length === 0) return [];
  const sorted = [...intervals].sort();
  const merged: Interval[] = [[sorted[0][0], sorted[0][1]]];
  for (const [start, end] of sorted.slice(1)) {
    const last = merged[merged.length - 1];
    if (start <= last[1]) last[1] = Math.max(last[1], end);
    else merged.push([start, end]);
  }
  return merged;
}

/**
 * NUMBER OF ISLANDS. Problem: count the groups of "1" cells connected up, down, left or right.
 * Idea: scan the grid; at every unvisited "1" count one island and DFS to sink (overwrite with "0") all its land.
 * Time O(rows * cols), space O(rows * cols) for the recursion in the worst case (a huge grid overflows the call stack: use an explicit stack).
 */
function numIslands(grid: string[]): number {
  const cells = grid.map((row) => row.split(""));       // strings are immutable: work on arrays of characters
  const sink = (r: number, c: number): void => {
    if (r < 0 || r >= cells.length || c < 0 || c >= cells[r].length || cells[r][c] !== "1") return;
    cells[r][c] = "0";
    sink(r + 1, c);
    sink(r - 1, c);
    sink(r, c + 1);
    sink(r, c - 1);
  };
  let islands = 0;
  for (let r = 0; r < cells.length; r++) {
    for (let c = 0; c < cells[r].length; c++) {
      if (cells[r][c] === "1") {
        islands++;
        sink(r, c);
      }
    }
  }
  return islands;
}

/**
 * SHORTEST PATH ON A GRID. Problem: fewest steps from the top-left to the bottom-right; "." is open, "#" is a wall;
 * moves are up, down, left, right; -1 when there is no path.
 * Idea: BFS with a queue explores in rings of equal distance, so the first time it reaches a cell is the shortest way.
 * The queue is an array read with a moving head index (shift() would cost O(n) per step in theory).
 * Time O(rows * cols), space O(rows * cols).
 */
function shortestPath(grid: string[]): number {
  if (grid.length === 0 || grid[0].length === 0) return -1;
  const rows = grid.length;
  const cols = grid[0].length;
  if (grid[0][0] === "#" || grid[rows - 1][cols - 1] === "#") return -1;
  const dist = Array.from({ length: rows }, () => new Array<number>(cols).fill(-1));   // -1 = not reached yet; one NEW row each
  const queue: [number, number][] = [[0, 0]];
  dist[0][0] = 0;
  const moves = [[1, 0], [-1, 0], [0, 1], [0, -1]];
  for (let head = 0; head < queue.length; head++) {
    const [r, c] = queue[head];
    if (r === rows - 1 && c === cols - 1) return dist[r][c];
    for (const [dr, dc] of moves) {
      const nr = r + dr;
      const nc = c + dc;
      if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] !== "#" && dist[nr][nc] === -1) {
        dist[nr][nc] = dist[r][c] + 1;
        queue.push([nr, nc]);
      }
    }
  }
  return -1;
}

/**
 * CLIMBING STAIRS (1 or 2 steps at a time). Problem: in how many ways can you climb n steps?
 * Idea: DP. ways(n) = ways(n - 1) + ways(n - 2), because the last move was 1 step or 2 steps; keep only the last two values.
 * Time O(n), space O(1). A number is exact only up to 2^53, so large n needs bigint.
 */
function climbStairs(n: number): number {
  let twoBack = 1;                                       // ways(0)
  let oneBack = 1;                                       // ways(1)
  for (let i = 2; i <= n; i++) [twoBack, oneBack] = [oneBack, oneBack + twoBack];
  return oneBack;
}

function climbStairsBig(n: number): bigint {
  let twoBack = 1n;
  let oneBack = 1n;
  for (let i = 2; i <= n; i++) [twoBack, oneBack] = [oneBack, oneBack + twoBack];
  return oneBack;
}

/**
 * COIN CHANGE. Problem: the fewest coins that make amount (unlimited coins of each kind), or -1.
 * Idea: DP. best[a] = 1 + the smallest best[a - coin] over all coins that fit; best[0] = 0; Infinity marks "unreachable".
 * Time O(amount * coins), space O(amount).
 */
function coinChange(coins: number[], amount: number): number {
  const best = new Array<number>(amount + 1).fill(Infinity);
  best[0] = 0;
  for (let a = 1; a <= amount; a++) {
    for (const coin of coins) {
      if (coin <= a) best[a] = Math.min(best[a], best[a - coin] + 1);
    }
  }
  return best[amount] === Infinity ? -1 : best[amount];
}

/** The tempting greedy answer: biggest coin first. Fast, but wrong for some coin sets. */
function coinChangeGreedy(coins: number[], amount: number): number {
  let left = amount;
  let count = 0;
  for (const coin of [...coins].sort((a, b) => b - a)) {
    count += Math.floor(left / coin);                    // "/" is not an integer division
    left %= coin;
  }
  return left === 0 ? count : -1;
}

title("INTERVALS, GRIDS AND DP  -  sort it, search it, remember it");

section("Merge intervals (sort by start)");
show(() => mergeIntervals([[1, 3], [2, 6], [8, 10], [15, 18]]));
show(() => mergeIntervals([[1, 4], [4, 5]]));           // touching intervals merge too (start <= last end)
show(() => mergeIntervals([[1, 10], [2, 3]]));          // TRAP: contained interval, so take the MAX of the ends
show(() => mergeIntervals([[8, 10], [1, 3], [2, 6]]));  // unsorted input is sorted first
show(() => mergeIntervals([]));
show(() => mergeIntervals([[10, 12], [2, 3], [1, 2]]));
show(() => mergeIntervalsWrong([[10, 12], [2, 3], [1, 2]]));   // TRAP: [ [ 1, 2 ], [ 10, 12 ] ]: the default sort() compared "10,12" with "2,3" as text
note("array.sort() sorts IN PLACE and returns the same array: copy first when the caller's array must stay as it was");

section("Number of islands (DFS)");
syntax(["sink(r, c): mark visited, recurse in 4 directions", "flood fill: stop outside the grid and on water"]);
show(() => numIslands(["11000", "11000", "00100", "00011"]));
show(() => numIslands(["10", "01"]));                   // diagonals do not connect: 2 islands
show(() => numIslands(["1"]));
show(() => numIslands(["000"]));
show(() => numIslands([]));

section("Shortest path on a grid (BFS)");
syntax(["pop a cell, push unseen open neighbours", "dist + 1 each ring: the first arrival is the shortest"]);
show(() => shortestPath(["...", ".#.", "..."]));        // around the wall: 4 steps
show(() => shortestPath([".#...", ".#.#.", "...#."]));  // 10 steps: down, across, up, across, down. NOT the Manhattan distance 6
show(() => shortestPath(["..#", "###", "..."]));        // walled off: -1
show(() => shortestPath(["."]));                        // start is the goal: 0 steps
show(() => shortestPath(["#."]));                       // blocked start
const shared = new Array<number[]>(2).fill(new Array<number>(2).fill(-1));
shared[0][0] = 0;
show(() => shared);                                     // TRAP: both rows changed: fill() put ONE array into every slot
const separate = Array.from({ length: 2 }, () => new Array<number>(2).fill(-1));
separate[0][0] = 0;
show(() => separate);                                   // one NEW row per slot: only row 0 changed

section("Climb stairs (DP) and exact numbers");
syntax(["ways(n) = ways(n - 1) + ways(n - 2)", "Fibonacci in disguise; two variables are enough"]);
show(() => climbStairs(0));                             // one way to climb nothing: do nothing
show(() => climbStairs(1));
show(() => climbStairs(5));                             // 1, 1, 2, 3, 5, 8: Fibonacci shifted by one
show(() => climbStairs(50));                            // 20365011074: still exact
show(() => Number.isSafeInteger(climbStairs(80)));      // TRAP: false, the answer is above 2^53 and the digits are no longer exact
show(() => climbStairsBig(80));                         // the exact answer with bigint (lesson 0)
show(() => BigInt(climbStairs(80)) === climbStairsBig(80));   // false: the number version drifted

section("Coin change (DP)");
syntax(["best[a] = 1 + min(best[a - coin])", "build the answers for 0..amount from smaller amounts"]);
show(() => coinChange([1, 2, 5], 11));                  // 5 + 5 + 1
show(() => coinChange([2], 3));                         // impossible: -1
show(() => coinChange([1], 0));                         // amount 0 needs no coins
show(() => coinChange([1, 3, 4], 6));                   // 3 + 3
show(() => coinChangeGreedy([1, 3, 4], 6));             // TRAP: greedy takes 4 + 1 + 1, which is 3 coins
note("Greedy is right only for special coin sets (like 1, 5, 10, 25); in general use DP");

section("Cost");
bigO(
  ["mergeIntervals", "O(n log n) time (the sort), O(n) space"],
  ["numIslands (DFS) | shortestPath (BFS)", "O(rows * cols) time and space"],
  ["climbStairs", "O(n) time, O(1) space (the plain recursion is O(2^n))"],
  ["coinChange", "O(amount * coins) time, O(amount) space"],
  ["queue.shift() in a BFS loop", "O(n) per call in theory: a head index keeps every step O(1)"],
);
