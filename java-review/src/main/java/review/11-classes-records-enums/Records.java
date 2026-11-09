package review.classes;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 11d. RECORDS: immutable data carriers with equals, hashCode and toString written for you
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.Records
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class Records {

    record Point(int x, int y) {
        static final Point ORIGIN = new Point(0, 0);                 // static members are allowed

        static Point of(int x, int y) { return new Point(x, y); }    // a static factory
        double distanceToOrigin() { return Math.sqrt(x * x + y * y); }   // extra behaviour is fine
        Point withX(int newX) { return new Point(newX, y); }         // records have no copy(): write a "wither" by hand
    }

    record Range(int low, int high) {
        Range {                                                      // compact constructor: no parameter list, runs before the fields are set
            if (low > high) throw new IllegalArgumentException("low > high");
        }
    }

    record Name(String value) {
        Name { value = value.strip(); }                              // normalise: assign the PARAMETER, the field gets it afterwards
    }

    record Team(String name, List<String> members) { }               // the list itself is still mutable
    record SafeTeam(String name, List<String> members) {
        SafeTeam { members = List.copyOf(members); }                 // defensive copy: now nobody holds the original list
    }

    record Packet(int[] data) { }                                    // an array component compares by identity

    public static void main(String[] args) {
        title("RECORDS  -  value equality for free, shallowly immutable");

        section("What a record generates");
        syntax(
                "record Name(T a, U b) { }", "fields (private final), a canonical constructor, accessors a() and b() (not getA), equals, hashCode, toString",
                "Name { ... }", "compact constructor: validate or normalise the parameters; the fields are assigned after it",
                "static of(...) | withA(...)", "a static factory | a hand-written copy with one part changed",
                "implicitly final", "a record cannot extend a class (it can implement interfaces) and nothing can extend it"
        );
        Point p = new Point(1, 2);
        show(() -> p);                                               // Point[x=1, y=2]
        show(() -> p.x());                                           // the accessor is x(), not getX()
        show(() -> p.equals(new Point(1, 2)));                       // true: equals compares every component
        show(() -> p == new Point(1, 2));                            // false: == is identity, these are two objects
        show(() -> p.hashCode() == new Point(1, 2).hashCode());      // true: equal records have equal hash codes
        show(() -> p.withX(9));                                      // p itself is not changed
        show(() -> new Point(3, 4).distanceToOrigin());
        show(() -> Point.of(0, 0).equals(Point.ORIGIN));
        wontCompile("p.x = 3;", "cannot assign a value to final variable x");
        wontCompile("record R(int a) { int extra; }", "field declaration must be static: a record has no instance fields besides its components");
        wontCompile("class Sub extends Point { }", "cannot inherit from final Point");

        section("As a Set element or a Map key");
        Set<Point> unique = new HashSet<>(List.of(new Point(1, 2), new Point(1, 2), new Point(2, 1)));
        show(() -> unique.size());                                   // 2: equal records are the same element
        Map<Point, String> names = Map.of(new Point(0, 0), "origin");
        show(() -> names.get(new Point(0, 0)));                      // found by VALUE: the way to key a grid cell
        note("A plain class needs equals + hashCode written by hand for this (lesson 12)");

        section("Compact constructor: validate and normalise");
        show(() -> thrown(() -> new Range(5, 1)));                   // the constructor rejects the bad state
        show(() -> new Range(1, 5));
        show(() -> new Name("  Ann ").value());                      // strip() was applied before the field was set

        section("Shallow immutability");
        List<String> source = new ArrayList<>(List.of("x"));
        Team team = new Team("A", source);
        team.members().add("y");
        show(() -> team.members());                                  // [x, y]: the record cannot freeze the list it holds
        show(() -> source);                                          // [x, y]: it is the SAME list object as the caller's
        List<String> source2 = new ArrayList<>(List.of("x"));
        SafeTeam safe = new SafeTeam("B", source2);
        source2.add("y");
        show(() -> safe.members());                                  // [x]: List.copyOf took a private immutable copy
        show(() -> thrown(() -> safe.members().add("z")));           // the copy refuses changes
        show(() -> new Packet(new int[] {1}).equals(new Packet(new int[] {1})));   // TRAP: false, equals uses array identity
        note("Record components of type List / array / Date / StringBuilder stay mutable inside: copy them in the compact constructor");
        note("Records and JSON libraries: see lesson 17 (JSON output)");
    }
}
