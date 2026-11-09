package review.collectionsdepth;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 13c. COMPARABLE and COMPARATOR: natural order, chained orders, the a - b overflow trap, TreeSet
 *      Run: mvn -q compile exec:java -Dmain.class=review.collectionsdepth.ComparableAndComparator
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ComparableAndComparator {

    /** Natural order of a class: implement Comparable. Only the SIGN of the result counts. */
    record Version(int major, int minor) implements Comparable<Version> {
        @Override
        public int compareTo(Version other) {
            int byMajor = Integer.compare(major, other.major);
            return byMajor != 0 ? byMajor : Integer.compare(minor, other.minor);
        }
    }

    record Person(String name, int age) { }

    /** Looks right, but a - b overflows when the true difference does not fit in an int. */
    static int badCompare(int a, int b) { return a - b; }

    /** A sorted COPY, so the demo list stays as it was. */
    static <T> List<T> sorted(List<T> xs, Comparator<? super T> order) {
        List<T> copy = new ArrayList<>(xs);
        copy.sort(order);
        return copy;
    }

    public static void main(String[] args) {
        title("COMPARABLE AND COMPARATOR  -  one natural order, many chosen orders");

        section("Comparable: the natural order of a class");
        syntax(
                "class A implements Comparable<A>", "compareTo(other): negative, 0 or positive. Gives Collections.sort, TreeSet, TreeMap, max, binarySearch",
                "Integer.compare(a, b)", "the safe way to compare ints; Long.compare, Double.compare, String.compareTo for the rest"
        );
        show(() -> new Version(1, 2).compareTo(new Version(1, 10)) < 0);   // true: numbers, where the text "1.2" would sort AFTER "1.10"
        List<Version> versions = new ArrayList<>(List.of(new Version(2, 0), new Version(1, 10), new Version(1, 2)));
        Collections.sort(versions);
        show(() -> versions);
        show(() -> Collections.max(versions));
        show(() -> badCompare(Integer.MAX_VALUE, -1));               // TRAP: negative, as if MAX_VALUE were smaller than -1
        show(() -> Integer.compare(Integer.MAX_VALUE, -1));          // 1: the right answer
        List<Integer> broken = new ArrayList<>(List.of(-1, Integer.MAX_VALUE));
        broken.sort((a, b) -> a - b);
        show(() -> broken);                                          // wrong order: the overflow in a - b made the sort believe MAX_VALUE < -1
        note("Only the SIGN of compareTo / compare counts. Never return a - b: use Integer.compare(a, b)");

        section("Comparator: an order chosen from outside");
        syntax(
                "Comparator.comparing(T::key)", "order by a key (a Comparable); comparingInt / comparingDouble avoid boxing",
                ".thenComparing(...)", "the next key breaks ties",
                ".reversed()", "reverses everything BEFORE it in the chain",
                "Comparator.naturalOrder()  |  reverseOrder()", "the Comparable order of the elements, or its opposite",
                "nullsFirst(c)  |  nullsLast(c)", "wrap a comparator so null elements are allowed"
        );
        List<String> words = List.of("bb", "a", "ab", "c");
        Comparator<String> byLengthThenAlpha = Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder());
        show(() -> sorted(words, byLengthThenAlpha));
        show(() -> sorted(words, byLengthThenAlpha.reversed()));     // reversed() flips BOTH keys
        show(() -> sorted(words, Comparator.comparing(String::length).reversed().thenComparing(Comparator.naturalOrder())));   // longest first, ties A to Z
        show(() -> sorted(words, Comparator.comparingInt(String::length)));   // stable: "bb" stays before "ab", their keys are equal
        show(() -> sorted(words, Comparator.reverseOrder()));
        List<Person> people = List.of(new Person("Bo", 30), new Person("Ann", 30), new Person("Cy", 25));
        show(() -> sorted(people, Comparator.comparingInt(Person::age).thenComparing(Person::name)));
        List<String> withNull = Arrays.asList("b", null, "a");
        show(() -> thrown(() -> sorted(withNull, Comparator.naturalOrder())));   // natural order cannot compare null
        show(() -> sorted(withNull, Comparator.nullsFirst(Comparator.naturalOrder())));
        show(() -> sorted(withNull, Comparator.nullsLast(Comparator.naturalOrder())));
        wontCompile("Comparator<String> c = Comparator.comparing(s -> s.length()).reversed();", "cannot find symbol length() on Object: the lambda has no target type before reversed(), so use String::length");

        section("Sorted collections trust the comparator, not equals");
        syntax("new TreeSet<>(comparator)", "two elements with compare == 0 are the SAME element: the second add is ignored");
        Set<String> byLength = new TreeSet<>(Comparator.comparingInt(String::length));
        byLength.add("aa");
        byLength.add("bb");
        byLength.add("c");
        show(() -> byLength);                                        // [c, aa]: "bb" has the same length as "aa", so it counts as a duplicate
        List<BigDecimal> prices = List.of(new BigDecimal("2.0"), new BigDecimal("2.00"));
        show(() -> new HashSet<>(prices).size());                    // 2: BigDecimal.equals compares the scale too
        show(() -> new TreeSet<>(prices).size());                    // 1: compareTo says 2.0 and 2.00 are the same number
        show(() -> thrown(() -> new TreeSet<Object>().add(new Object())));   // not Comparable and no comparator: ClassCastException

        section("Cost");
        bigO(
                "list.sort(c)  |  Collections.sort(list)", "O(n log n) comparisons (TimSort: O(n) when already sorted), stable",
                "TreeSet.add / contains / remove", "O(log n) comparisons",
                "one compareTo call", "O(1) for numbers, O(length) for strings",
                "Collections.max(list)", "O(n): one pass, no sorting"
        );
    }
}
