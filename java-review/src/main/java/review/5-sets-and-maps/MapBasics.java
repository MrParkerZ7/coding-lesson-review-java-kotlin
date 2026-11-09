package review.sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 5b. MAPS: key -> value
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.MapBasics
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class MapBasics {

    static int ageOf(Map<String, Integer> ages, String name) {
        return ages.get(name);                                                   // the Integer is unboxed to an int
    }

    /** Removes every key inside a for-each over keySet() and returns the exception it caused. */
    static String removeWhileLooping(Map<String, Integer> map) {
        return thrown(() -> {
            for (String key : map.keySet()) map.remove(key);
            return null;
        });
    }

    public static void main(String[] args) {
        title("MAPS  -  look a value up by key");

        section("Creating");
        syntax(
                "new HashMap<>() | new LinkedHashMap<>() | new TreeMap<>()", "no order promised | insertion order | sorted by key (5c)",
                "Map.of(k1, v1, k2, v2)", "IMMUTABLE, up to 10 pairs, no nulls, order differs from run to run",
                "Map.ofEntries(Map.entry(k, v), ...) | Map.copyOf(m)", "immutable, any size | immutable copy"
        );
        Map<String, Integer> ages = new LinkedHashMap<>();
        ages.put("ann", 30);  ages.put("bob", 25);   show(() -> ages);
        show(() -> new TreeMap<>(Map.of("b", 1, "a", 2)));                       // a TreeMap sorted by key (a Map.of has no order to print)
        show(() -> Map.entry("a", 1));                                           // one key=value pair
        show(() -> ages.size());
        show(() -> ages.keySet());
        show(() -> ages.values());

        section("Reading");
        syntax(
                "m.get(key) | m.getOrDefault(key, d)", "value, or null when missing | value, or a default",
                "m.containsKey(key) | m.containsValue(v)", "is there such a key (fast) | such a value (a full scan)"
        );
        show(() -> ages.get("ann"));
        show(() -> ages.get("zed"));                                             // null when the key is missing, no exception
        show(() -> ages.getOrDefault("zed", 0));
        show(() -> ages.containsKey("bob"));
        show(() -> thrown(() -> ageOf(ages, "zed")));                            // TRAP: the missing key gives null, and unboxing null throws

        section("Changing (a mutable Map only)");
        syntax(
                "m.put(k, v) | m.putIfAbsent(k, v)", "add or overwrite | only when absent; both return the OLD value (null if none)",
                "m.computeIfAbsent(k, f)", "absent: compute, STORE and return the value (the grouping idiom)",
                "m.merge(k, v, f)", "absent: store v | present: store f(old, v) (the counting idiom)",
                "m.remove(k) | m.replaceAll((k, v) -> ...)", "remove, returns the old value | change every value"
        );
        Map<String, Integer> m = new LinkedHashMap<>(Map.of("a", 1));
        m.put("b", 2);                        show(() -> m);
        show(() -> m.put("a", 10));                                              // returns the OLD value 1; the same key overwrites
        show(() -> m.putIfAbsent("a", 99));                                      // present: nothing changes, returns the existing 10
        show(() -> m.putIfAbsent("c", 3));                                       // absent: stored, returns null
        show(() -> m.computeIfAbsent("d", key -> 4));                            // absent: computes 4 AND stores it
        m.merge("a", 5, Integer::sum);        show(() -> m);                     // present: 10 + 5
        m.merge("e", 5, Integer::sum);        show(() -> m);                     // absent: just stores 5
        show(() -> m.remove("b"));                                               // returns the removed value
        m.replaceAll((key, value) -> value * 10);   show(() -> m);
        List<String> words = List.of("fig", "kiwi", "apple", "plum", "pear");
        Map<Integer, List<String>> byLength = new TreeMap<>();
        for (String w : words) byLength.computeIfAbsent(w.length(), key -> new ArrayList<>()).add(w);
        show(() -> byLength);                                                    // computeIfAbsent: create the list on first use, then add

        section("Looping");
        syntax(
                "for (Map.Entry<K, V> e : m.entrySet())", "e.getKey(), e.getValue(), e.setValue(v)",
                "m.forEach((k, v) -> ...) | m.keySet() | m.values()", "a lambda over entries | the keys | the values"
        );
        List<String> pairs = new ArrayList<>();
        for (Map.Entry<String, Integer> e : ages.entrySet()) pairs.add(e.getKey() + "=" + e.getValue());
        show(() -> pairs);
        Map<String, Integer> doomed = new LinkedHashMap<>(ages);
        show(() -> removeWhileLooping(doomed));                                  // TRAP: removing inside the loop = ConcurrentModificationException
        Map<String, Integer> older = new LinkedHashMap<>(ages);
        older.entrySet().removeIf(e -> e.getValue() < 26);   show(() -> older);  // the safe way; entrySet, keySet and values are VIEWS

        section("Transforming");
        show(() -> Collections.max(ages.entrySet(), Map.Entry.comparingByValue()).getKey());   // the key with the biggest value
        show(() -> ages.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList());   // keys ordered by value
        show(() -> ages.entrySet().stream().filter(e -> e.getValue() > 26).map(Map.Entry::getKey).toList());   // keys with a matching value
        note("There is no Map.map or Map.filter: stream the entrySet() (lesson 4), or loop");

        section("Cost");
        bigO(
                "HashMap get | put | containsKey | remove", "O(1) on average",
                "TreeMap get | put | containsKey | remove", "O(log n), sorted by key",
                "containsValue | search by value", "O(n): a full scan",
                "iterating all entries", "O(n)"
        );
    }
}
