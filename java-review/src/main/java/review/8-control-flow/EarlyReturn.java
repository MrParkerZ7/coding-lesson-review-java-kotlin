package review.control;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.List;

/**
 * 8d. EARLY RETURN: guard clauses, leaving loops and lambdas, methods that must return
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.EarlyReturn
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class EarlyReturn {

    /** Guard clauses: each bad case leaves at once, the main path stays flat. */
    static int shippingCost(int weightKg, boolean express) {
        if (weightKg <= 0) return 0;
        if (weightKg > 30) return -1;                                   // too heavy: refused
        int base = weightKg <= 5 ? 4 : 9;
        return express ? base * 2 : base;
    }

    /** The same rules with nested ifs: the real work sits three levels deep. */
    static int shippingNested(int weightKg, boolean express) {
        if (weightKg > 0) {
            if (weightKg <= 30) {
                int base = weightKg <= 5 ? 4 : 9;
                return express ? base * 2 : base;
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    /** return leaves BOTH loops at once: no flag, no label. */
    static int[] findPair(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) return new int[] {nums[i], nums[j]};
            }
        }
        return new int[0];
    }

    /** A labeled break leaves the OUTER loop; the code after the loops still runs. */
    static String firstPairLabeled(int[] nums, int target) {
        String found = "none";
        search:
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    found = nums[i] + "+" + nums[j];
                    break search;
                }
            }
        }
        return found;
    }

    /** A labeled continue skips to the next ROW. */
    static int rowsWithoutNegatives(int[][] grid) {
        int rows = 0;
        outer:
        for (int[] row : grid) {
            for (int v : row) {
                if (v < 0) continue outer;
            }
            rows++;
        }
        return rows;
    }

    /** A for loop can return from the method. */
    static int firstNegativeIndex(List<Integer> xs) {
        for (int i = 0; i < xs.size(); i++) {
            if (xs.get(i) < 0) return i;
        }
        return -1;
    }

    /** return inside a lambda only leaves the LAMBDA: it acts like continue, and a forEach cannot stop early. */
    static int sumPositives(List<Integer> xs) {
        int[] total = {0};
        xs.forEach(x -> {
            if (x <= 0) return;
            total[0] += x;
        });
        return total[0];
    }

    /** A recursive method: the early return IS the base case. */
    static long factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }

    /** A method that never returns normally: it always throws. */
    static int fail(String message) {
        throw new IllegalStateException(message);
    }

    static int halve(int n) {
        return n % 2 == 0 ? n / 2 : fail("odd: " + n);
    }

    public static void main(String[] args) {
        title("EARLY RETURN  -  leave as soon as the answer is known");

        section("Guard clauses");
        syntax("if (bad case) return value;", "handle the odd cases first and leave; the main path stays flat");
        show(() -> shippingCost(3, false));
        show(() -> shippingCost(8, true));                              // 9 * 2 for express
        show(() -> shippingCost(0, true));                              // guard: nothing to ship
        show(() -> shippingCost(31, false));                            // guard: too heavy
        show(() -> shippingNested(8, true) == shippingCost(8, true));   // same result, but the nested version is harder to read

        section("return, break and continue out of loops");
        syntax(
                "return value;", "leaves the method, from any depth of loops",
                "label: for (...) { ... break label; ... continue label; }", "leave | skip to the next round of the OUTER loop",
                "list.forEach(x -> { ... return; })", "return only leaves the LAMBDA (like continue)"
        );
        show(() -> findPair(new int[] {2, 7, 11, 15}, 18));             // found in the inner loop: both loops end at once
        show(() -> findPair(new int[] {1, 2}, 100));
        show(() -> firstPairLabeled(new int[] {2, 7, 11, 15}, 18));
        show(() -> rowsWithoutNegatives(new int[][] {{1, 2}, {3, -4}, {5, 6}}));   // the middle row is skipped by continue outer
        show(() -> firstNegativeIndex(List.of(3, 5, -1, 4)));
        show(() -> firstNegativeIndex(List.of(3, 5)));
        show(() -> sumPositives(List.of(3, -1, 4, 0, 2)));              // return skipped -1 and 0 only, the lambda ran for every element
        note("A forEach lambda cannot break out of the loop: use a for loop (or a stream with takeWhile / findFirst)");

        section("Every path must return, and nothing may follow a return");
        wontCompile("static int f(int n) { if (n > 0) return 1; }", "missing return statement: every path must return or throw");
        wontCompile("return 1; System.out.println(\"never\");", "unreachable statement");
        show(() -> halve(8));
        show(() -> thrown(() -> halve(7)));                             // fail() throws, so the ternary never gives a value
        show(() -> factorial(5));
        show(() -> factorial(20));                                      // the largest factorial that fits in a long
        show(() -> factorial(21));                                      // TRAP: overflow wraps around to a wrong negative number
        note("FizzBuzz, leap year, grade, day name and sign: next file ClassicsWithControlFlow");
    }
}
