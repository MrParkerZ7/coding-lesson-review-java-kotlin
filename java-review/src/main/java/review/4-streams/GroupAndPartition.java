package review.streams;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.teeing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 4c. STREAMS: groupingBy, partitioningBy, word counts, sorting a Map by value
 *     Run: mvn -q compile exec:java -Dmain.class=review.streams.GroupAndPartition
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class GroupAndPartition {

    record Employee(String name, String dept, int salary) {
    }

    public static void main(String[] args) {
        title("GROUP AND PARTITION  -  List to Map, split in two, count, sort");
        note("Collectors are imported by name here: import static java.util.stream.Collectors.groupingBy; and so on");

        List<String> words = List.of("bat", "ant", "ape", "bee", "cow", "bee");
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6);
        List<Employee> staff = List.of(new Employee("Ann", "dev", 50), new Employee("Bob", "ops", 40),
                new Employee("Cid", "dev", 60), new Employee("Dee", "ops", 40));

        section("groupingBy: one key, many elements");
        syntax(
                "groupingBy(keyFn)", "Map<K, List<T>>: a HashMap, so the key order is not promised",
                "groupingBy(keyFn, downstream)", "reduce each group with another Collector: counting(), mapping(f, toList()), summingInt(f)",
                "groupingBy(keyFn, TreeMap::new, downstream)", "pick the map: TreeMap sorts the keys, LinkedHashMap keeps first-seen order"
        );
        show(() -> words.stream().collect(groupingBy(w -> w.charAt(0))));        // the lists keep the input order; these keys only LOOK sorted
        show(() -> words.stream().collect(groupingBy(w -> w.charAt(0), TreeMap::new, toList())));   // TreeMap: keys sorted, guaranteed
        show(() -> staff.stream().collect(groupingBy(Employee::dept, TreeMap::new, counting())));   // how many per department
        show(() -> staff.stream().collect(groupingBy(Employee::dept, TreeMap::new, summingInt(Employee::salary))));   // total salary per department
        show(() -> staff.stream().collect(groupingBy(Employee::dept, TreeMap::new, averagingInt(Employee::salary))));   // average per department
        show(() -> staff.stream().collect(groupingBy(Employee::dept, TreeMap::new, mapping(Employee::name, toList()))));   // only the names

        section("partitioningBy: split in two");
        syntax("partitioningBy(condition)", "Map<Boolean, List<T>> with BOTH keys, even when one side is empty");
        show(() -> nums.stream().collect(partitioningBy(n -> n % 2 == 0)));
        show(() -> nums.stream().collect(partitioningBy(n -> n > 9)));           // the true key is still there, with an empty list
        show(() -> nums.stream().collect(partitioningBy(n -> n % 2 == 0, counting())));   // a downstream works here too
        Map<Boolean, List<Integer>> parts = nums.stream().collect(partitioningBy(n -> n % 2 == 0));
        show(() -> parts.get(true));                                             // read one side

        section("Counting words and characters");
        Map<String, Long> freq = words.stream().collect(groupingBy(w -> w, TreeMap::new, counting()));
        show(() -> freq);                                                        // the classic word-frequency idiom
        show(() -> freq.get("bee"));                                             // the value is a Long, not an Integer
        show(() -> freq.get("bee").equals(2));                                   // TRAP: false, a Long is never equal to an Integer (2 is an int)
        show(() -> freq.get("bee") == 2);                                        // true: == with a primitive unboxes first
        show(() -> "banana".chars().mapToObj(c -> (char) c).collect(groupingBy(c -> c, TreeMap::new, counting())));   // characters
        show(() -> words.stream().collect(toMap(w -> w.charAt(0), w -> w, (a, b) -> a, LinkedHashMap::new)).values().stream().toList());   // like distinctBy: the FIRST per key

        section("Sort a Map by value");
        Comparator<Map.Entry<String, Long>> byCount = Map.Entry.comparingByValue(Comparator.reverseOrder());
        show(() -> freq.entrySet().stream().sorted(byCount).map(Map.Entry::getKey).toList());   // keys by count, descending; the tie keeps TreeMap order
        show(() -> freq.entrySet().stream().sorted(byCount).limit(2).map(Map.Entry::getKey).toList());   // top 2
        show(() -> Collections.max(freq.entrySet(), Map.Entry.comparingByValue()).getKey());   // the most frequent word
        note("A Map has no sort(): sort a stream of its entries, or copy them into a List and sort that");

        section("Sorting objects, two collectors in one pass");
        Comparator<Employee> bySalary = Comparator.comparingInt(Employee::salary);
        show(() -> staff.stream().sorted(bySalary.reversed().thenComparing(Employee::name)).map(Employee::name).toList());   // salary descending, then name
        wontCompile("Comparator.comparingInt(e -> e.salary()).reversed()", "e is inferred as Object here: use Employee::salary or <Employee>comparingInt");
        show(() -> nums.stream().collect(teeing(counting(), summingInt(n -> n), (count, sum) -> sum / (double) count)));   // average from count and sum (Java 12)

        section("Cost");
        bigO(
                "groupingBy | partitioningBy | toMap | counting", "O(n) time, O(n) space: a hash map underneath",
                "sorted | Map.Entry sort", "O(n log n) time, O(n) for the new list",
                "groupingBy(k, counting())", "O(n) and no lists: cheaper than groupingBy(k) then size()",
                "TreeMap::new as the map factory", "O(n log n) instead of O(n): the price of sorted keys"
        );
    }
}
