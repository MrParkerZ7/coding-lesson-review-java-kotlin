package review.sets;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 5d. TREESET AND TREEMAP: a sorted collection that answers "what is next to this value?"
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.TreeNavigation
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class TreeNavigation {

    /** Grade for a score: the band whose lower bound is the greatest key <= score. O(log n). */
    static String grade(TreeMap<Integer, String> bands, int score) {
        Map.Entry<Integer, String> band = bands.floorEntry(score);
        return band == null ? "F" : band.getValue();
    }

    public static void main(String[] args) {
        title("TREE NAVIGATION  -  floor, ceiling, headSet, tailMap");

        section("TreeSet: a sorted set with neighbours");
        syntax(
                "first() | last() | pollFirst()", "smallest | largest | remove and return the smallest",
                "floor(x) | ceiling(x)", "greatest <= x | smallest >= x, or null",
                "lower(x) | higher(x)", "greatest < x | smallest > x, or null",
                "headSet(to) | tailSet(from)", "elements < to | elements >= from (VIEWS)"
        );
        TreeSet<Integer> tree = new TreeSet<>(List.of(10, 20, 30, 40));          // declare the TreeSet type: a plain Set has no floor()
        show(() -> tree.first());
        show(() -> tree.floor(25));                                              // 20: the biggest value that is <= 25
        show(() -> tree.ceiling(25));                                            // 30: the smallest value that is >= 25
        show(() -> tree.floor(5));                                               // null: nothing is that small
        show(() -> tree.ceiling(30));                                            // 30: floor and ceiling accept an equal element ...
        show(() -> tree.higher(30));                                             // 40: ... higher is STRICTLY greater
        show(() -> tree.headSet(30));                                            // [10, 20]: the end is excluded
        show(() -> tree.tailSet(30));                                            // [30, 40]: the start is included
        show(() -> tree.descendingSet());
        show(() -> thrown(() -> new TreeSet<Integer>().first()));                // first() on an empty set throws
        show(() -> thrown(() -> tree.add(null)));                                // a TreeSet must compare, so no null
        show(() -> new HashSet<Integer>().add(null));                            // a HashSet accepts one null

        section("TreeMap: the same questions on keys");
        syntax(
                "firstKey() | lastKey() | firstEntry()", "smallest key | largest key | smallest entry (null when empty)",
                "floorKey(k) | ceilingKey(k)", "greatest <= k | smallest >= k, or null",
                "floorEntry(k) | headMap(to) | tailMap(from)", "the entry | keys < to | keys >= from (VIEWS)"
        );
        TreeMap<Integer, String> bands = new TreeMap<>(Map.of(90, "A", 80, "B", 70, "C", 60, "D"));
        show(() -> bands.firstKey());
        show(() -> bands.floorKey(85));                                          // 80: the band that contains 85
        show(() -> bands.ceilingKey(85));                                        // 90
        show(() -> bands.floorKey(10));                                          // null: below the first band
        show(() -> bands.headMap(80));                                           // {60=D, 70=C}
        show(() -> bands.tailMap(80));                                           // {80=B, 90=A}
        show(() -> bands.firstEntry());
        show(() -> new TreeMap<String, Integer>().firstEntry());                 // null on an empty map ...
        show(() -> thrown(() -> new TreeMap<String, Integer>().firstKey()));     // ... but firstKey() throws
        show(() -> grade(bands, 85));                                            // a lookup table of ranges: floorEntry, no if-else chain
        show(() -> grade(bands, 90));
        show(() -> grade(bands, 12));                                            // below every band: the helper returns F
        note("The view methods (headSet, tailMap, subMap) are windows on the tree: changing the tree changes them");

        section("Cost");
        bigO(
                "add | remove | contains | get", "O(log n)",
                "first | last | floor | ceiling | higher | lower", "O(log n)",
                "headSet | tailSet | headMap | tailMap", "cheap to create (a window, no copy); walking it is O(k)",
                "HashSet / HashMap", "O(1) on average but no order and no floor / ceiling"
        );
    }
}
