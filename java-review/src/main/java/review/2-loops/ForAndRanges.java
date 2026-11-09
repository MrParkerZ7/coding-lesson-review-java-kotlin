package review.loops;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 2a. FOR LOOPS AND RANGES
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.ForAndRanges
 *     Or click the green arrow beside `main` in IntelliJ.
 *     Each demo collects what the loop visits in a list, so the result is printed.
 */
public class ForAndRanges {

    /** Sums a list with for-each: a null list throws NullPointerException. */
    private static int total(List<Integer> xs) {
        int sum = 0;
        for (int x : xs) sum += x;
        return sum;
    }

    /** Removes a value while looping over the same list with for-each. */
    private static List<Integer> removeWhileLooping(int value) {
        List<Integer> xs = new ArrayList<>(List.of(1, 2, 3));
        for (Integer x : xs) {
            if (x == value) xs.remove(x);
        }
        return xs;
    }

    /** i <= Integer.MAX_VALUE is always true: i++ wraps to MIN_VALUE, so only the guard stops the loop. */
    private static int roundsNearTheTop() {
        int rounds = 0;
        for (int i = Integer.MAX_VALUE - 1; i <= Integer.MAX_VALUE && rounds < 5; i++) rounds++;
        return rounds;
    }

    public static void main(String[] args) {
        title("FOR LOOPS AND RANGES");

        section("The classic for loop");
        syntax(
                "for (int i = a; i < b; i++)", "b EXCLUDED (int i = 0; i < n is the index loop)",
                "for (int i = a; i <= b; i++)", "b INCLUDED",
                "for (int i = b; i >= a; i--)", "counting down, a included",
                "for (int i = a; i < b; i += n)", "every n-th value",
                "for (;;) { ... break; }", "the three parts are optional: endless unless something breaks"
        );
        List<Integer> inclusive = new ArrayList<>();
        for (int i = 1; i <= 5; i++) inclusive.add(i);   show(() -> inclusive);   // <= includes the end
        List<Integer> exclusive = new ArrayList<>();
        for (int i = 1; i < 5; i++) exclusive.add(i);   show(() -> exclusive);   // < excludes the end
        List<Integer> down = new ArrayList<>();
        for (int i = 5; i >= 1; i--) down.add(i);   show(() -> down);
        List<Integer> step = new ArrayList<>();
        for (int i = 0; i <= 10; i += 3) step.add(i);   show(() -> step);   // 0, 3, 6, 9: the last step that still fits
        List<Character> letters = new ArrayList<>();
        for (char c = 'a'; c <= 'e'; c++) letters.add(c);   show(() -> letters);   // a char counts like a number
        List<Integer> wrongWay = new ArrayList<>();
        for (int i = 5; i <= 1; i++) wrongWay.add(i);   show(() -> wrongWay);   // TRAP: wrong direction = zero rounds, no error (i-- with i <= 5 runs 2147483650 rounds until i wraps: it looks like a hang)
        List<Integer> endless = new ArrayList<>();
        for (int i = 0; ; i++) {
            if (i == 3) break;
            endless.add(i);
        }
        show(() -> endless);                                           // the for (;;) shape: break is the only way out
        show(() -> roundsNearTheTop());                                // TRAP: should be 2; MAX_VALUE + 1 wraps, the condition stays true
        note("The loop variable lives only inside the loop (lesson 1, ScopeAndDefaults); changing it in the body is legal but confusing");
        note("Never count with a double: adding 0.1 ten times gives 0.9999999999999999, not 1.0 (DecimalTypes). Use an int counter");

        section("Enhanced for (for-each)");
        syntax(
                "for (Type x : collectionOrArray)", "each element, no index (x is a COPY of the element)",
                "for (Map.Entry<K, V> e : map.entrySet())", "each key and value; also map.keySet() and map.values()",
                "for (int i = 0; i < xs.size(); i++)", "when you need the index: Java has no withIndex"
        );
        List<String> fruits = List.of("apple", "banana", "cherry");
        List<Integer> lengths = new ArrayList<>();
        for (String f : fruits) lengths.add(f.length());   show(() -> lengths);
        List<String> indexed = new ArrayList<>();
        for (int i = 0; i < fruits.size(); i++) indexed.add(i + "=" + fruits.get(i));
        show(() -> indexed);                                           // index and element together
        List<Character> chars = new ArrayList<>();
        for (char c : "hey".toCharArray()) chars.add(c);   show(() -> chars);   // a String is not Iterable: loop over toCharArray()
        Map<String, Integer> ages = new LinkedHashMap<>();
        ages.put("ann", 30);
        ages.put("bob", 25);
        List<String> pairs = new ArrayList<>();
        for (Map.Entry<String, Integer> e : ages.entrySet()) pairs.add(e.getKey() + "=" + e.getValue());
        show(() -> pairs);                                             // LinkedHashMap keeps insertion order; a HashMap gives no order guarantee
        wontCompile("for (var e : map) { }", "for-each not applicable to expression type: a Map is not Iterable, loop over map.entrySet()");
        List<String> backwards = new ArrayList<>();
        for (int i = fruits.size() - 1; i >= 0; i--) backwards.add(fruits.get(i));
        show(() -> backwards);                                         // backwards by index
        List<String> reversed = new ArrayList<>();
        for (String f : fruits.reversed()) reversed.add(f);   show(() -> reversed);   // Java 21: reversed() is a reverse view of any List
        List<String> triangle = new ArrayList<>();
        for (int i = 0; i < 3; i++) for (int j = i + 1; j < 3; j++) triangle.add("" + i + j);
        show(() -> triangle);                                          // all pairs i < j: the nested loop of brute force
        int[] nums = {1, 2, 3};
        for (int x : nums) x *= 2;
        show(() -> nums);                                              // TRAP: x is a copy, the array is unchanged
        show(() -> thrown(() -> total(null)));                         // TRAP: for-each over null throws NullPointerException
        show(() -> thrown(() -> removeWhileLooping(1)));               // TRAP: changing the list inside for-each fails on the next round
        show(() -> removeWhileLooping(2));                             // but removing the second-to-last element ends the loop silently: 3 is never visited
        note("Safe ways to remove while looping: Iterator.remove (WhileAndControl), removeIf, a backwards index loop (lesson 3)");

        section("IntStream ranges (no downTo or step keywords in Java)");
        syntax(
                "IntStream.range(a, b)", "a .. b-1: b EXCLUDED",
                "IntStream.rangeClosed(a, b)", "a .. b: b INCLUDED",
                "IntStream.iterate(a, i -> i <= b, i -> i + n)", "with a step (Java 9)"
        );
        show(() -> IntStream.range(1, 5).toArray());
        show(() -> IntStream.rangeClosed(1, 5).toArray());
        show(() -> IntStream.iterate(0, i -> i <= 10, i -> i + 3).toArray());
        show(() -> IntStream.rangeClosed(1, 5).map(i -> 6 - i).toArray());   // counting down: map the values, there is no downTo
        show(() -> IntStream.rangeClosed(5, 1).toArray());             // TRAP: ranges only count up, so this is EMPTY (no error)
        show(() -> IntStream.rangeClosed(1, 10).sum());                // a range is also a value you can sum or count
        show(() -> IntStream.rangeClosed(1, 10).count());              // count() returns a long
        note("The type column shows the wrapper: an int result prints as Integer, a long as Long, an int[] as int[]");
        note("Streams in depth: lesson 4. A plain for loop is the clearest choice when you need break, continue or the index");

        section("Cost");
        bigO(
                "one loop over n elements", "O(n)",
                "two nested loops over n", "O(n^2)",
                "for (i = 0; i < n; i++) for (j = i + 1; j < n; j++)", "still O(n^2): about n*n/2 pairs",
                "a loop that halves the range each round", "O(log n)"
        );
    }
}
