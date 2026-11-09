package review.objects;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 12a. EQUALS and HASHCODE: the contract, the lost HashSet entry, Objects.hash, records
 *      Run: mvn -q compile exec:java -Dmain.class=review.objects.EqualsAndHashCode
 *      Or click the green arrow beside `main` in IntelliJ.
 *      (Inheritance and mutable-key traps are the next file: EqualityTraps.)
 */
public class EqualsAndHashCode {

    /** equals WITHOUT hashCode: the classic bug. */
    static class BadKey {
        final int id;

        BadKey(int id) { this.id = id; }

        @Override
        public boolean equals(Object o) { return o instanceof BadKey k && k.id == id; }
    }

    /** The standard recipe: same fields in both methods, null-safe, hashCode via Objects.hash. */
    static class GoodKey {
        final int id;
        final String name;

        GoodKey(int id, String name) { this.id = id; this.name = name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;                              // fast path, and reflexive
            if (!(o instanceof GoodKey k)) return false;             // also false for null
            return id == k.id && Objects.equals(name, k.name);       // Objects.equals: null-safe
        }

        @Override
        public int hashCode() { return Objects.hash(id, name); }     // the same fields as equals
    }

    /** Legal but useless: every object lands in one bucket. */
    static class SlowKey {
        final int id;

        SlowKey(int id) { this.id = id; }

        @Override public boolean equals(Object o) { return o instanceof SlowKey k && k.id == id; }
        @Override public int hashCode() { return 42; }
    }

    /** TRAP: equals(Weak) is an OVERLOAD, not an override: collections never call it. */
    static class Weak {
        final int id;

        Weak(int id) { this.id = id; }

        public boolean equals(Weak other) { return other.id == id; }
        @Override public int hashCode() { return id; }
    }

    record Point(int x, int y) { }

    public static void main(String[] args) {
        title("EQUALS AND HASHCODE  -  the contract every Java interview asks about");

        section("The contract");
        syntax(
                "equals(Object o)", "reflexive x.equals(x), symmetric x.equals(y) == y.equals(x), transitive, consistent, x.equals(null) is false",
                "hashCode()", "equal objects MUST return equal hash codes; unequal objects MAY share one (a collision)",
                "the rule of thumb", "override both or neither, with the SAME fields; compute hashCode with Objects.hash(a, b, ...)",
                "double / array fields", "Double.compare(a, b) == 0 for a double; Arrays.equals and Arrays.hashCode for an array (array.equals is identity)"
        );
        show(() -> Objects.hash(1, 2));                              // 994 = 31 * (31 * 1 + 1) + 2; specified, so the same on every JVM
        show(() -> "abc".hashCode());                                // 96354: String.hashCode is specified too
        show(() -> List.of(1, 2).hashCode());                        // 994: a List hashes like Objects.hash of its elements
        show(() -> "Aa".hashCode() == "BB".hashCode());              // true: different strings, same hash = a collision (allowed)
        show(() -> Objects.equals(null, null));                      // true: Objects.equals never throws
        show(() -> Objects.hashCode(null));                          // 0

        section("equals without hashCode: the lost HashSet entry");
        show(() -> new BadKey(1).equals(new BadKey(1)));             // true: equals alone looks fine ...
        show(() -> List.of(new BadKey(1)).contains(new BadKey(1)));  // true: List.contains uses equals only
        Set<BadKey> bad = new HashSet<>();
        bad.add(new BadKey(1));
        bad.add(new BadKey(1));
        show(() -> bad.size());                                      // 2: Object.hashCode is per object, so the two land in different buckets
        show(() -> bad.contains(new BadKey(1)));                     // false: the set looks in the wrong bucket and never calls equals
        Set<GoodKey> good = new HashSet<>();
        good.add(new GoodKey(1, "a"));
        good.add(new GoodKey(1, "a"));
        show(() -> good.size());                                     // 1
        show(() -> good.contains(new GoodKey(1, "a")));              // true
        show(() -> new GoodKey(1, null).equals(new GoodKey(1, null)));   // true: Objects.equals handles null fields
        note("HashMap.get and HashSet.contains compare hashCode FIRST and call equals only inside the right bucket");

        section("equals(Weak) is not equals(Object)");
        Weak w1 = new Weak(1);
        Weak w2 = new Weak(1);
        Object asObject = w2;
        show(() -> w1.equals(w2));                                   // true: the compiler picks the overload equals(Weak)
        show(() -> w1.equals(asObject));                             // false: equal ids, but the static type Object picks Object.equals (identity)
        Set<Weak> weak = new HashSet<>();
        weak.add(w1);
        show(() -> weak.contains(w2));                               // false: collections call equals(Object)
        wontCompile("@Override public boolean equals(Weak other)", "method does not override or implement a method from a supertype");
        note("Always write @Override on equals, hashCode, toString: a wrong parameter type becomes a compile error");

        section("Records and the cost");
        show(() -> new Point(1, 2).equals(new Point(1, 2)));         // true: generated from the components
        show(() -> new Point(1, 2).hashCode() == new Point(1, 2).hashCode());
        Set<SlowKey> slow = new HashSet<>(List.of(new SlowKey(1), new SlowKey(2), new SlowKey(3)));
        show(() -> slow.contains(new SlowKey(3)));                   // true: a constant hashCode is CORRECT, only slow
        bigO(
                "HashSet.contains, HashMap.get (good hashCode)", "O(1) average",
                "all keys share one hashCode (SlowKey)", "O(n) per lookup (O(log n) if the keys are Comparable: HashMap turns long buckets into trees)",
                "equals / hashCode themselves", "O(number of fields); String caches its hash code",
                "Objects.hash(a, b)", "allocates a varargs array and boxes ints: write the 31 * h + x loop by hand in a hot path"
        );
    }
}
