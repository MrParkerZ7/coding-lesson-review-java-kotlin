package review.drills

import review.bigO
import review.note
import review.section
import review.show
import review.syntax
import review.title

/**
 * MERGE INTERVALS. Problem: join every overlapping pair (start, end) into one.
 * Idea: sort by start; then each interval either touches the last merged one (extend its end with max) or starts a new one.
 * Time O(n log n) for the sort, space O(n).
 */
fun mergeIntervals(intervals: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
    if (intervals.isEmpty()) return emptyList()
    val sorted = intervals.sortedBy { it.first }
    val merged = mutableListOf(sorted[0])
    for (next in sorted.drop(1)) {
        val last = merged.last()
        if (next.first <= last.second) merged[merged.lastIndex] = last.first to maxOf(last.second, next.second)   // max: next may sit INSIDE last
        else merged.add(next)
    }
    return merged
}

/**
 * NUMBER OF ISLANDS. Problem: count the groups of '1' cells connected up, down, left or right.
 * Idea: scan the grid; at every unvisited '1' count one island and DFS to sink (overwrite with '0') all its land.
 * Time O(rows * cols), space O(rows * cols) for the recursion in the worst case (use an explicit stack for huge grids).
 */
fun numIslands(grid: List<String>): Int {
    val cells = grid.map { it.toCharArray() }
    fun sink(r: Int, c: Int) {
        if (r !in cells.indices || c !in cells[r].indices || cells[r][c] != '1') return
        cells[r][c] = '0'
        sink(r + 1, c)
        sink(r - 1, c)
        sink(r, c + 1)
        sink(r, c - 1)
    }
    var islands = 0
    for (r in cells.indices) {
        for (c in cells[r].indices) {
            if (cells[r][c] == '1') {
                islands++
                sink(r, c)
            }
        }
    }
    return islands
}

/**
 * SHORTEST PATH ON A GRID. Problem: fewest steps from the top-left to the bottom-right; '.' is open, '#' is a wall;
 * moves are up, down, left, right; -1 when there is no path.
 * Idea: BFS with a queue explores in rings of equal distance, so the first time it reaches a cell is the shortest way.
 * Time O(rows * cols), space O(rows * cols).
 */
fun shortestPath(grid: List<String>): Int {
    if (grid.isEmpty() || grid[0].isEmpty()) return -1
    val rows = grid.size
    val cols = grid[0].length
    if (grid[0][0] == '#' || grid[rows - 1][cols - 1] == '#') return -1
    val dist = Array(rows) { IntArray(cols) { -1 } }         // -1 = not reached yet
    val queue = ArrayDeque<Pair<Int, Int>>()
    dist[0][0] = 0
    queue.addLast(0 to 0)
    val moves = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
    while (queue.isNotEmpty()) {
        val (r, c) = queue.removeFirst()
        if (r == rows - 1 && c == cols - 1) return dist[r][c]
        for ((dr, dc) in moves) {
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until rows && nc in 0 until cols && grid[nr][nc] != '#' && dist[nr][nc] == -1) {
                dist[nr][nc] = dist[r][c] + 1
                queue.addLast(nr to nc)
            }
        }
    }
    return -1
}

/**
 * CLIMBING STAIRS (1 or 2 steps at a time). Problem: in how many ways can you climb n steps?
 * Idea: DP. ways(n) = ways(n - 1) + ways(n - 2), because the last move was 1 step or 2 steps; keep only the last two values.
 * Time O(n), space O(1). Long: the Int version overflows from n = 46.
 */
fun climbStairs(n: Int): Long {
    var twoBack = 1L                                     // ways(0)
    var oneBack = 1L                                     // ways(1)
    repeat(n - 1) {
        val now = oneBack + twoBack
        twoBack = oneBack
        oneBack = now
    }
    return oneBack
}

/**
 * COIN CHANGE. Problem: the fewest coins that make amount (unlimited coins of each kind), or -1.
 * Idea: DP. best[a] = 1 + the smallest best[a - coin] over all coins that fit; best[0] = 0.
 * Time O(amount * coins), space O(amount).
 */
fun coinChange(coins: IntArray, amount: Int): Int {
    val unreachable = amount + 1                         // larger than any real answer
    val best = IntArray(amount + 1) { unreachable }
    best[0] = 0
    for (a in 1..amount) {
        for (coin in coins) {
            if (coin <= a && best[a - coin] + 1 < best[a]) best[a] = best[a - coin] + 1
        }
    }
    return if (best[amount] == unreachable) -1 else best[amount]
}

/** The tempting greedy answer: biggest coin first. Fast, but wrong for some coin sets. */
fun coinChangeGreedy(coins: IntArray, amount: Int): Int {
    var left = amount
    var count = 0
    for (coin in coins.sortedDescending()) {
        count += left / coin
        left %= coin
    }
    return if (left == 0) count else -1
}

/**
 * 9d. INTERVALS, GRIDS AND DP: sort and merge, DFS, BFS, dynamic programming
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.IntervalsAndGraphsKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("INTERVALS, GRIDS AND DP  -  sort it, search it, remember it")

    section("Merge intervals (sort by start)")
    show { mergeIntervals(listOf(1 to 3, 2 to 6, 8 to 10, 15 to 18)) }
    show { mergeIntervals(listOf(1 to 4, 4 to 5)) }                            // touching intervals merge too (next.first <= last.second)
    show { mergeIntervals(listOf(1 to 10, 2 to 3)) }                           // TRAP: contained interval, so take the MAX of the ends
    show { mergeIntervals(listOf(8 to 10, 1 to 3, 2 to 6)) }                   // unsorted input is sorted first
    show { mergeIntervals(emptyList()) }

    section("Number of islands (DFS)")
    syntax("sink(r, c): mark visited, recurse in 4 directions" to "flood fill: stop outside the grid and on water")
    show { numIslands(listOf("11000", "11000", "00100", "00011")) }
    show { numIslands(listOf("10", "01")) }                                    // diagonals do not connect: 2 islands
    show { numIslands(listOf("1")) }
    show { numIslands(listOf("000")) }
    show { numIslands(emptyList()) }

    section("Shortest path on a grid (BFS)")
    syntax("pop a cell, push unseen open neighbours" to "dist + 1 each ring: the first arrival is the shortest")
    show { shortestPath(listOf("...", ".#.", "...")) }                         // around the wall: 4 steps
    show { shortestPath(listOf(".#...", ".#.#.", "...#.")) }                   // a snake: down, across, up, across, down. NOT the Manhattan distance 6
    show { shortestPath(listOf("..#", "###", "...")) }                         // walled off: -1
    show { shortestPath(listOf(".")) }                                         // start is the goal: 0 steps
    show { shortestPath(listOf("#.")) }                                        // blocked start

    section("Climb stairs (DP)")
    syntax("ways(n) = ways(n - 1) + ways(n - 2)" to "Fibonacci in disguise; two variables are enough")
    show { climbStairs(0) }                                                    // one way to climb nothing: do nothing
    show { climbStairs(1) }
    show { climbStairs(5) }                                                    // 1, 1, 2, 3, 5, 8: Fibonacci shifted by one
    show { climbStairs(45) }                                                   // the biggest answer that still fits in an Int
    show { climbStairs(50) }                                                   // needs a Long

    section("Coin change (DP)")
    syntax("best[a] = 1 + min(best[a - coin])" to "build the answers for 0..amount from smaller amounts")
    show { coinChange(intArrayOf(1, 2, 5), 11) }                               // 5 + 5 + 1
    show { coinChange(intArrayOf(2), 3) }                                      // impossible: -1
    show { coinChange(intArrayOf(1), 0) }                                      // amount 0 needs no coins
    show { coinChange(intArrayOf(1, 3, 4), 6) }                                // 3 + 3
    show { coinChangeGreedy(intArrayOf(1, 3, 4), 6) }                          // TRAP: greedy takes 4 + 1 + 1, which is 3 coins
    note("Greedy is right only for special coin sets (like 1, 5, 10, 25); in general use DP")

    section("Cost")
    bigO(
        "mergeIntervals" to "O(n log n) time (the sort), O(n) space",
        "numIslands (DFS) | shortestPath (BFS)" to "O(rows * cols) time and space",
        "climbStairs" to "O(n) time, O(1) space (the plain recursion is O(2^n))",
        "coinChange" to "O(amount * coins) time, O(amount) space",
    )
}
