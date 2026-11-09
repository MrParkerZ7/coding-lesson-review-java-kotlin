package review.drills;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * 9d. INTERVALS, GRIDS AND DP: sort and merge, DFS, BFS, dynamic programming
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.IntervalsAndGraphs
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class IntervalsAndGraphs {

    /** Builds intervals from a flat list: start, end, start, end ... (keeps the examples short). */
    static int[][] intervals(int... flat) {
        int[][] result = new int[flat.length / 2][];
        for (int i = 0; i < result.length; i++) result[i] = new int[] {flat[2 * i], flat[2 * i + 1]};
        return result;
    }

    /**
     * MERGE INTERVALS. Problem: join every overlapping pair {start, end} into one.
     * Idea: sort by start; then each interval either touches the last merged one (extend its end with max) or starts a new one.
     * Time O(n log n) for the sort, space O(n).
     */
    static int[][] mergeIntervals(int[][] intervals) {
        if (intervals.length == 0) return new int[0][];
        int[][] sorted = intervals.clone();                             // sort a copy: the caller's array stays as it was
        Arrays.sort(sorted, Comparator.comparingInt(iv -> iv[0]));      // not (x, y) -> x[0] - y[0]: a subtraction can overflow
        List<int[]> merged = new ArrayList<>();
        merged.add(sorted[0].clone());                                  // clone: extending the end below must not change the input
        for (int i = 1; i < sorted.length; i++) {
            int[] last = merged.get(merged.size() - 1);
            if (sorted[i][0] <= last[1]) last[1] = Math.max(last[1], sorted[i][1]);   // max: next may sit INSIDE last
            else merged.add(sorted[i].clone());
        }
        return merged.toArray(new int[0][]);
    }

    /** Sorts the list in place: fails on an immutable list such as List.of(...). */
    static List<Integer> sortInPlace(List<Integer> xs) {
        Collections.sort(xs);
        return xs;
    }

    /** Sorts a copy: works for any list and leaves the input alone. */
    static List<Integer> sortedCopy(List<Integer> xs) {
        List<Integer> copy = new ArrayList<>(xs);
        Collections.sort(copy);
        return copy;
    }

    /**
     * NUMBER OF ISLANDS. Problem: count the groups of '1' cells connected up, down, left or right.
     * Idea: scan the grid; at every unvisited '1' count one island and DFS to sink (overwrite with '0') all its land.
     * Time O(rows * cols), space O(rows * cols) for the recursion in the worst case (a huge grid needs an explicit stack: Java's call stack is small).
     */
    static int numIslands(String... grid) {
        char[][] cells = new char[grid.length][];
        for (int r = 0; r < grid.length; r++) cells[r] = grid[r].toCharArray();
        int islands = 0;
        for (int r = 0; r < cells.length; r++) {
            for (int c = 0; c < cells[r].length; c++) {
                if (cells[r][c] == '1') {
                    islands++;
                    sink(cells, r, c);
                }
            }
        }
        return islands;
    }

    private static void sink(char[][] cells, int r, int c) {
        if (r < 0 || r >= cells.length || c < 0 || c >= cells[r].length || cells[r][c] != '1') return;
        cells[r][c] = '0';
        sink(cells, r + 1, c);
        sink(cells, r - 1, c);
        sink(cells, r, c + 1);
        sink(cells, r, c - 1);
    }

    /**
     * SHORTEST PATH ON A GRID. Problem: fewest steps from the top-left to the bottom-right; '.' is open, '#' is a wall;
     * moves are up, down, left, right; -1 when there is no path.
     * Idea: BFS with a queue explores in rings of equal distance, so the first time it reaches a cell is the shortest way.
     * Time O(rows * cols), space O(rows * cols).
     */
    static int shortestPath(String... grid) {
        if (grid.length == 0 || grid[0].isEmpty()) return -1;
        int rows = grid.length;
        int cols = grid[0].length();
        if (grid[0].charAt(0) == '#' || grid[rows - 1].charAt(cols - 1) == '#') return -1;
        int[][] dist = new int[rows][cols];
        for (int[] row : dist) Arrays.fill(row, -1);                    // -1 = not reached yet
        Deque<int[]> queue = new ArrayDeque<>();
        dist[0][0] = 0;
        queue.add(new int[] {0, 0});
        int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int r = cell[0];
            int c = cell[1];
            if (r == rows - 1 && c == cols - 1) return dist[r][c];
            for (int[] move : moves) {
                int nr = r + move[0];
                int nc = c + move[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr].charAt(nc) != '#' && dist[nr][nc] == -1) {
                    dist[nr][nc] = dist[r][c] + 1;
                    queue.add(new int[] {nr, nc});
                }
            }
        }
        return -1;
    }

    /**
     * CLIMBING STAIRS (1 or 2 steps at a time). Problem: in how many ways can you climb n steps?
     * Idea: DP. ways(n) = ways(n - 1) + ways(n - 2), because the last move was 1 step or 2 steps; keep only the last two values.
     * Time O(n), space O(1). long: an int version overflows from n = 46.
     */
    static long climbStairs(int n) {
        long twoBack = 1;                                               // ways(0)
        long oneBack = 1;                                               // ways(1)
        for (int i = 2; i <= n; i++) {
            long now = oneBack + twoBack;
            twoBack = oneBack;
            oneBack = now;
        }
        return oneBack;
    }

    /**
     * COIN CHANGE. Problem: the fewest coins that make amount (unlimited coins of each kind), or -1.
     * Idea: DP. best[a] = 1 + the smallest best[a - coin] over all coins that fit; best[0] = 0.
     * Time O(amount * coins), space O(amount).
     */
    static int coinChange(int[] coins, int amount) {
        int unreachable = amount + 1;                                   // larger than any real answer
        int[] best = new int[amount + 1];
        Arrays.fill(best, unreachable);
        best[0] = 0;
        for (int a = 1; a <= amount; a++) {
            for (int coin : coins) {
                if (coin <= a && best[a - coin] + 1 < best[a]) best[a] = best[a - coin] + 1;
            }
        }
        return best[amount] == unreachable ? -1 : best[amount];
    }

    /** The tempting greedy answer: biggest coin first. Fast, but wrong for some coin sets. */
    static int coinChangeGreedy(int[] coins, int amount) {
        int[] sorted = coins.clone();
        Arrays.sort(sorted);                                            // ascending: walk it from the end for biggest first
        int left = amount;
        int count = 0;
        for (int i = sorted.length - 1; i >= 0; i--) {
            count += left / sorted[i];
            left %= sorted[i];
        }
        return left == 0 ? count : -1;
    }

    public static void main(String[] args) {
        title("INTERVALS, GRIDS AND DP  -  sort it, search it, remember it");

        section("Merge intervals (sort by start)");
        show(() -> mergeIntervals(intervals(1, 3, 2, 6, 8, 10, 15, 18)));
        show(() -> mergeIntervals(intervals(1, 4, 4, 5)));              // touching intervals merge too (next start <= last end)
        show(() -> mergeIntervals(intervals(1, 10, 2, 3)));             // TRAP: contained interval, so take the MAX of the ends
        show(() -> mergeIntervals(intervals(8, 10, 1, 3, 2, 6)));       // unsorted input is sorted first
        show(() -> mergeIntervals(intervals()));
        List<Integer> fixed = List.of(3, 1, 2);
        show(() -> thrown(() -> sortInPlace(fixed)));                   // TRAP: List.of is immutable, Collections.sort throws
        show(() -> sortedCopy(fixed));                                  // copy first, then sort the copy
        int big = 2_000_000_000;
        int small = -2_000_000_000;
        show(() -> big - small);                                        // TRAP: -294967296: a comparator (x, y) -> x - y would call big smaller
        show(() -> Integer.compare(big, small));                        // 1: Integer.compare never overflows

        section("Number of islands (DFS)");
        syntax("sink(cells, r, c): mark visited, recurse in 4 directions", "flood fill: stop outside the grid and on water");
        show(() -> numIslands("11000", "11000", "00100", "00011"));
        show(() -> numIslands("10", "01"));                             // diagonals do not connect: 2 islands
        show(() -> numIslands("1"));
        show(() -> numIslands("000"));
        show(() -> numIslands());                                       // no rows at all

        section("Shortest path on a grid (BFS)");
        syntax("poll a cell, add unseen open neighbours", "dist + 1 each ring: the first arrival is the shortest");
        show(() -> shortestPath("...", ".#.", "..."));                  // around the wall: 4 steps
        show(() -> shortestPath(".#...", ".#.#.", "...#."));            // a snake: down, across, up, across, down. NOT the Manhattan distance 6
        show(() -> shortestPath("..#", "###", "..."));                  // walled off: -1
        show(() -> shortestPath("."));                                  // start is the goal: 0 steps
        show(() -> shortestPath("#."));                                 // blocked start

        section("Climb stairs (DP)");
        syntax("ways(n) = ways(n - 1) + ways(n - 2)", "Fibonacci in disguise; two variables are enough");
        show(() -> climbStairs(0));                                     // one way to climb nothing: do nothing
        show(() -> climbStairs(1));
        show(() -> climbStairs(5));                                     // 1, 1, 2, 3, 5, 8: Fibonacci shifted by one
        show(() -> climbStairs(45));                                    // the biggest answer that still fits in an int
        show(() -> climbStairs(50));                                    // needs a long
        show(() -> (int) climbStairs(46));                              // TRAP: casting back to int wraps to a negative number

        section("Coin change (DP)");
        syntax("best[a] = 1 + min(best[a - coin])", "build the answers for 0..amount from smaller amounts");
        show(() -> coinChange(new int[] {1, 2, 5}, 11));                // 5 + 5 + 1
        show(() -> coinChange(new int[] {2}, 3));                       // impossible: -1
        show(() -> coinChange(new int[] {1}, 0));                       // amount 0 needs no coins
        show(() -> coinChange(new int[] {1, 3, 4}, 6));                 // 3 + 3
        show(() -> coinChangeGreedy(new int[] {1, 3, 4}, 6));           // TRAP: greedy takes 4 + 1 + 1, which is 3 coins
        note("Greedy is right only for special coin sets (like 1, 5, 10, 25); in general use DP");

        section("Cost");
        bigO(
                "mergeIntervals", "O(n log n) time (the sort), O(n) space",
                "numIslands (DFS) | shortestPath (BFS)", "O(rows * cols) time and space",
                "climbStairs", "O(n) time, O(1) space (the plain recursion is O(2^n))",
                "coinChange", "O(amount * coins) time, O(amount) space"
        );
    }
}
