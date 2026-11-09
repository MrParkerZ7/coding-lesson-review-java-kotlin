package review.sets;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 5c. MAP KINDS: HashMap, LinkedHashMap, TreeMap: iteration order and null rules
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.MapKinds
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class MapKinds {

    public static void main(String[] args) {
        title("MAP KINDS  -  order and null rules");

        section("Iteration order");
        syntax(
                "HashMap", "NO order promised: the hash table's order, which can change when the map grows",
                "LinkedHashMap", "insertion order (with a flag: access order, the base of an LRU cache)",
                "TreeMap", "sorted by key, O(log n); keys must be Comparable, or pass a Comparator",
                "Map.of(...) | Set.of(...)", "order unspecified and DIFFERENT on every run: never print or test it"
        );
        List<String> keys = List.of("pear", "apple", "fig", "kiwi");
        Map<String, Integer> hash = new HashMap<>();
        Map<String, Integer> linked = new LinkedHashMap<>();
        Map<String, Integer> tree = new TreeMap<>();
        for (String k : keys) {
            hash.put(k, k.length());
            linked.put(k, k.length());
            tree.put(k, k.length());
        }
        show(() -> hash);                                                        // some order: do not depend on it
        show(() -> linked);                                                      // pear, apple, fig, kiwi: the order they were put
        show(() -> tree);                                                        // alphabetical
        Map<String, Integer> reversed = new TreeMap<>(Comparator.reverseOrder());
        reversed.putAll(tree);                show(() -> reversed);              // a Comparator decides the order of a TreeMap
        show(() -> new TreeMap<>(Map.of("b", 2, "a", 1, "c", 3)));               // sort a Map.of before printing it
        show(() -> hash.equals(linked));                                         // true: Map equality ignores the order
        show(() -> hash.keySet().equals(tree.keySet()));                         // and so does a key set

        section("null keys and null values");
        syntax(
                "HashMap | LinkedHashMap", "ONE null key and any number of null values",
                "TreeMap | Map.of | ConcurrentHashMap", "no null keys; Map.of and ConcurrentHashMap also refuse null values (a TreeMap accepts them)"
        );
        Map<String, Integer> nulls = new HashMap<>();
        nulls.put(null, 1);                   show(() -> nulls);                 // a null key is allowed
        nulls.put("x", null);                 show(() -> nulls);                 // a null VALUE is allowed too
        show(() -> nulls.get("x"));                                              // null: get() cannot tell "absent" from "stored null" ...
        show(() -> nulls.containsKey("x"));                                      // ... containsKey can
        show(() -> nulls.getOrDefault("x", 0));                                  // TRAP: the key exists with a null value, so null comes back, not 0
        show(() -> thrown(() -> new TreeMap<String, Integer>().put(null, 1)));  // a TreeMap has to compare keys
        show(() -> thrown(() -> Map.of("a", null)));                             // Map.of rejects null keys and values
        show(() -> thrown(() -> new ConcurrentHashMap<String, Integer>().put("a", null)));   // so does ConcurrentHashMap (lesson 16)
        note("Keys need a correct equals and hashCode; a key that changes after put() is lost in a HashMap (lesson 12)");

        section("Cost");
        bigO(
                "HashMap | LinkedHashMap get, put", "O(1) on average",
                "TreeMap get, put, firstKey, floorKey", "O(log n)",
                "LinkedHashMap vs HashMap", "the same O(1), plus a little memory for the order links",
                "iterate a TreeMap in order", "O(n), already sorted: no extra sort"
        );
    }
}
