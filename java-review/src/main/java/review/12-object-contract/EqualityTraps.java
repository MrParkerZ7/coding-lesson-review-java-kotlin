package review.objects;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 12b. EQUALITY TRAPS: inheritance breaks symmetry and transitivity, a mutable key gets lost
 *      Run: mvn -q compile exec:java -Dmain.class=review.objects.EqualityTraps
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class EqualityTraps {

    static class Point {
        final int x;
        final int y;

        Point(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) { return o instanceof Point p && p.x == x && p.y == y; }

        @Override
        public int hashCode() { return Objects.hash(x, y); }
    }

    /** Adds a field to equals: symmetry breaks against a plain Point. */
    static class ColorPoint extends Point {
        final String color;

        ColorPoint(int x, int y, String color) { super(x, y); this.color = color; }

        @Override
        public boolean equals(Object o) { return o instanceof ColorPoint c && super.equals(c) && color.equals(c.color); }

        @Override
        public int hashCode() { return Objects.hash(super.hashCode(), color); }
    }

    /** The "fix" that restores symmetry by ignoring the color against a plain Point: transitivity breaks instead. */
    static class LooseColorPoint extends Point {
        final String color;

        LooseColorPoint(int x, int y, String color) { super(x, y); this.color = color; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            if (!(o instanceof LooseColorPoint c)) return o.equals(this);   // a plain Point: compare x and y only
            return super.equals(o) && color.equals(c.color);
        }

        @Override
        public int hashCode() { return super.hashCode(); }
    }

    /** getClass() instead of instanceof: symmetric, but a subclass object is never equal to its parent. */
    static class Strict {
        final int id;

        Strict(int id) { this.id = id; }

        @Override
        public boolean equals(Object o) { return o != null && getClass() == o.getClass() && ((Strict) o).id == id; }

        @Override
        public int hashCode() { return id; }
    }

    /** A key whose equals / hashCode depend on a field that changes. */
    static class Cell {
        int x;

        Cell(int x) { this.x = x; }

        @Override public boolean equals(Object o) { return o instanceof Cell c && c.x == x; }
        @Override public int hashCode() { return x; }
    }

    public static void main(String[] args) {
        title("EQUALITY TRAPS  -  subclasses, getClass, mutable keys");

        section("Symmetry: a subclass that adds a field");
        Point plain = new Point(1, 2);
        ColorPoint red = new ColorPoint(1, 2, "red");
        show(() -> plain.equals(red));                               // true: Point only compares x and y
        show(() -> red.equals(plain));                               // false: a plain Point is not a ColorPoint. x.equals(y) != y.equals(x)
        show(() -> List.of(plain).contains(red));                    // false: List.contains asks red.equals(element) ...
        show(() -> List.of(red).contains(plain));                    // true: ... and here plain.equals(element). Same two objects, two answers
        note("A collection calls arg.equals(element): an asymmetric equals makes contains() depend on which side holds the object");

        section("Transitivity: the 'fix' that breaks it");
        LooseColorPoint loose1 = new LooseColorPoint(1, 2, "red");
        LooseColorPoint loose2 = new LooseColorPoint(1, 2, "blue");
        show(() -> loose1.equals(plain));                            // true
        show(() -> plain.equals(loose2));                            // true
        show(() -> loose1.equals(loose2));                           // false: a == b and b == c, yet a != c
        syntax(
                "equals with instanceof", "subclasses can equal their parent, but adding a field to equals breaks symmetry or transitivity",
                "equals with getClass()", "symmetric and transitive, but a subclass object never equals its parent object",
                "the real fix", "composition instead of inheritance, a final class, or a record (final, nothing to extend)"
        );
        Strict base = new Strict(1);
        Strict anonymousChild = new Strict(1) { };
        show(() -> base.equals(anonymousChild));                     // false: getClass() differs, in BOTH directions
        show(() -> anonymousChild.equals(base));

        section("A mutable key gets lost");
        Cell cell = new Cell(1);
        Map<Cell, String> map = new HashMap<>();
        map.put(cell, "start");
        cell.x = 2;                                                  // the hash code changes while the key sits in the map
        show(() -> map.get(cell));                                   // null: the map now looks in the bucket for 2, the entry sits in the bucket for 1
        show(() -> map.containsKey(cell));
        show(() -> map.size());                                      // 1: the entry is still there ...
        show(() -> map.containsValue("start"));                      // true: ... only unreachable by key
        cell.x = 1;
        show(() -> map.get(cell));                                   // start: put the field back and the entry is found again
        note("Use immutable keys (String, Integer, records of immutable parts). Never change a field that equals / hashCode read while the object is in a HashSet / HashMap");
    }
}
