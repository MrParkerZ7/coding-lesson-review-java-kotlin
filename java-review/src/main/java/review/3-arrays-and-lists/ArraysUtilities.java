package review.lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 3b. THE Arrays CLASS: sort, copy, fill, compare, search, and the array pitfalls
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ArraysUtilities
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ArraysUtilities {

    public static void main(String[] args) {
        title("THE Arrays CLASS  -  java.util.Arrays");

        section("Everyday operations");
        syntax(
                "Arrays.toString(a) | Arrays.deepToString(a)", "text of a 1D array | of a nested array",
                "Arrays.sort(a) | Arrays.fill(a, x)", "change the array IN PLACE (they return void)",
                "Arrays.copyOf(a, n) | copyOfRange(a, from, to)", "copy padded or cut to n | a slice (to is exclusive)",
                "Arrays.equals(a, b) | Arrays.hashCode(a)", "same contents? | a hash computed from the contents",
                "Arrays.deepEquals(a, b)", "same contents of nested arrays (2D)",
                "Arrays.setAll(a, i -> f(i))", "fill each cell from its index",
                "Arrays.stream(a)", "an IntStream (or Stream<T>): sum, max, average ... (lesson 4)"
        );
        int[] data = {5, 3, 8, 1};
        show(() -> Arrays.copyOf(data, 2));                                      // cut to 2 cells
        show(() -> Arrays.copyOf(data, 6));                                      // TRAP: a longer copy is padded with zeros
        show(() -> Arrays.copyOfRange(data, 1, 3));                              // end index is exclusive
        show(() -> Arrays.stream(data).sum());
        show(() -> Arrays.stream(data).max().getAsInt());                        // max() returns an OptionalInt, getAsInt() unwraps it
        Arrays.sort(data);                    show(() -> data);                  // sorts IN PLACE, ascending, returns nothing
        int[] filled = new int[3];
        Arrays.fill(filled, 7);               show(() -> filled);
        int[] squares = new int[5];
        Arrays.setAll(squares, i -> i * i);   show(() -> squares);               // index -> value
        int[][] left = {{1}, {2}};
        int[][] right = {{1}, {2}};
        show(() -> Arrays.equals(left, right));                                  // TRAP: false, the rows are compared with equals() and arrays use ==
        show(() -> Arrays.deepEquals(left, right));                              // true: goes into the rows
        note("There is no reverse() or contains() for int[]: use a two-pointer loop (lesson 2) or a stream");

        section("Sorting down: only object arrays take a Comparator");
        Integer[] boxed = {5, 3, 8, 1};
        Arrays.sort(boxed, Collections.reverseOrder());   show(() -> boxed);     // Integer[] + Comparator: descending
        wontCompile("Arrays.sort(data, Collections.reverseOrder());", "a Comparator needs an object array: use Integer[], or sort and reverse");
        String[] words = {"pear", "fig", "apple"};
        Arrays.sort(words);                   show(() -> words);                 // objects sort by compareTo (Strings: alphabetical)
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Arrays.sort(words, byLength);         show(() -> words);                 // by length

        section("Trap: Arrays.asList");
        syntax("Arrays.asList(array)", "a fixed-size List that is a VIEW of an object array (more in 3d)");
        show(() -> Arrays.asList(data).size());                                  // TRAP: asList(int[]) is a List with ONE element, the array
        show(() -> Arrays.asList(boxed).size());                                 // an Integer[] spreads into 4 elements
        show(() -> Arrays.stream(data).boxed().toList());                        // int[] to List<Integer>: box with a stream

        section("Trap: array covariance");
        Object[] objs = new String[2];        show(() -> thrown(() -> objs[0] = 1));   // compiles, fails at run time: it is really a String[]
        note("String[] is an Object[], so the compiler allows the store; the JVM knows the real type. Generics (lesson 14) are stricter");

        section("Cost");
        bigO(
                "Arrays.sort(int[])", "O(n log n): dual-pivot quicksort",
                "Arrays.sort(Object[]) | List.sort", "O(n log n): stable merge sort (TimSort)",
                "Arrays.binarySearch(a, x)", "O(log n), only on a SORTED array",
                "Arrays.equals | Arrays.fill | copyOf", "O(n)"
        );
    }
}
