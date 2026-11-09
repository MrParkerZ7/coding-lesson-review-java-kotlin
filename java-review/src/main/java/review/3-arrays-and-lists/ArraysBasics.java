package review.lists;

import java.util.Arrays;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 3a. ARRAYS: fixed size, fast, the input type of many interview problems
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ArraysBasics
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ArraysBasics {

    public static void main(String[] args) {
        title("ARRAYS  -  int[], String[], int[][]");

        section("Creating and reading");
        syntax(
                "int[] a = {1, 2, 3};", "array from values (this short form works only in a declaration)",
                "int[] a = new int[size];", "array of zeros (false for boolean, null for objects)",
                "new int[] {1, 2, 3}", "array from values anywhere: as an argument or in a return",
                "a.length | a[i] | a[i] = x", "length is a FIELD (no parentheses) | read | write, index from 0"
        );
        int[] nums = {3, 1, 2};               show(() -> nums);                  // an int[] is an object, not a collection
        int[] zeros = new int[4];             show(() -> zeros);                 // size 4, all zeros
        boolean[] flags = new boolean[2];     show(() -> flags);                 // false
        String[] names = new String[2];       show(() -> names);                 // objects start as null
        show(() -> nums.length);                                                 // fixed for the life of the array
        show(() -> nums[nums.length - 1]);                                       // there is no last(): the last index is length - 1
        nums[0] = 10;                         show(() -> nums);                  // elements can change
        note("The type column shows the wrapper of a primitive: nums.length is an int, shown as Integer (lesson 0)");
        note("Three spellings: array.length (field)  string.length() (method)  collection.size() (method)");

        section("Errors");
        show(() -> thrown(() -> nums[3]));                                       // index out of range
        int size = -1;
        show(() -> thrown(() -> new int[size]));                                 // a negative size
        wontCompile("int[] a = new int[3] {1, 2, 3};", "give a size OR values, never both");
        wontCompile("int[] a; a = {1, 2, 3};", "{...} alone is only allowed in a declaration");
        wontCompile("int n = nums.length();", "length is a field of arrays: write nums.length");
        wontCompile("Integer[] boxed = new int[] {1, 2};", "int[] is not an Integer[]: primitive and object arrays do not mix");

        section("Traps: an array is an object");
        show(() -> nums.toString().startsWith("[I@"));                           // printing an array gives a type tag and a hash like [I@1b6d3586
        show(() -> Arrays.toString(nums));                                       // Arrays.toString is how an array becomes text
        int[] same = {10, 1, 2};
        show(() -> nums == same);                                                // false: == compares REFERENCES
        show(() -> nums.equals(same));                                           // false: arrays do not override equals
        show(() -> Arrays.equals(nums, same));                                   // true: compares the contents
        int[] alias = nums;  alias[1] = 99;   show(() -> nums);                  // alias is the SAME array, so nums changed too
        int[] copy = nums.clone();  copy[2] = 0;   show(() -> nums);             // clone() is an independent copy: nums is untouched
        note("Arrays.asList(int[]) and other array pitfalls: next file");

        section("2D arrays");
        syntax(
                "int[][] g = new int[rows][cols];", "a matrix of zeros: every row is its own int[]",
                "int[][] g = new int[rows][];", "rows not created yet (null): 'jagged' arrays, each row its own length",
                "g.length | g[0].length | g[r][c]", "rows | columns of row 0 | one cell"
        );
        int[][] grid = new int[2][3];
        grid[1][2] = 7;                       show(() -> grid);                  // a 2D array prints as nested lists here
        show(() -> Arrays.toString(grid).startsWith("[[I@"));                    // TRAP: toString shows the inner arrays as hashes
        show(() -> Arrays.deepToString(grid));                                   // deepToString goes all the way down
        show(() -> grid.length);                                                 // number of rows
        show(() -> grid[0].length);                                              // number of columns
        int[][] triangle = new int[3][];      show(() -> triangle);              // the rows are null until you create them
        for (int r = 0; r < triangle.length; r++) triangle[r] = new int[r + 1];
        show(() -> Arrays.deepToString(triangle));                               // jagged: row r has r + 1 cells
        int[] row = new int[2];
        int[][] shared = {row, row};
        shared[0][0] = 5;                     show(() -> shared);                // TRAP: both rows are the same object
        int[][] shallow = grid.clone();
        shallow[1][2] = 99;                   show(() -> grid[1][2]);            // clone() copies the row REFERENCES, not the rows
        note("A deep copy of a 2D array needs a copy per row: for each r, copy[r] = grid[r].clone()");

        section("Cost");
        bigO(
                "a[i] | a[i] = x", "O(1)",
                "a.clone() | Arrays.copyOf(a, n)", "O(n)",
                "search by value (no sort)", "O(n): a loop, Arrays has no contains",
                "g[r][c]", "O(1): two index steps"
        );
    }
}
