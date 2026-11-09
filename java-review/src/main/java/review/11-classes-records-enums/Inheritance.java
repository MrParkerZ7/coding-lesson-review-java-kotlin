package review.classes;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;

/**
 * 11b. INHERITANCE: extends, @Override, super, final, abstract, field hiding, constructor traps
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.Inheritance
 *      Or click the green arrow beside `main` in IntelliJ.
 *      (Interfaces with default methods are the next file: Interfaces.)
 */
public class Inheritance {

    static class Vehicle {
        final int wheels;

        Vehicle(int wheels) { this.wheels = wheels; }

        String honk() { return "beep"; }                            // overridable: Java methods are open by default
        final String describe() { return wheels + " wheels"; }      // final: subclasses cannot override it
    }

    static class Car extends Vehicle {
        Car() { super(4); }                                         // super(...) calls the parent constructor, first statement

        @Override
        String honk() { return super.honk() + " beep"; }            // super.f() = the parent's version
    }

    abstract static class Figure {
        abstract int area();                                        // no body: every concrete subclass must write it
        String summary() { return "area=" + area(); }               // a concrete method may call the abstract one
    }

    static class Rect extends Figure {
        private final int w;
        private final int h;

        Rect(int w, int h) { this.w = w; this.h = h; }

        @Override
        int area() { return w * h; }
    }

    static class Parent {
        String name = "parent";                                     // a field
        String who() { return "parent"; }                           // a method
        static String kind() { return "Parent.kind"; }
    }

    static class Child extends Parent {
        String name = "child";                                      // HIDES Parent.name: there are now two fields
        @Override String who() { return "child"; }
        static String kind() { return "Child.kind"; }               // hides (not overrides) the static method
    }

    /** TRAP: the base constructor calls an overridable method before the subclass is initialized. */
    static class Base {
        Base(List<String> log) { log.add("Base sees: " + label()); }

        String label() { return "base"; }
    }

    static class Derived extends Base {
        private String tag = "derived";                             // assigned AFTER the Base constructor has run
        Derived(List<String> log) { super(log); }

        @Override String label() { return tag; }
    }

    static class DerivedConstant extends Base {
        private final String tag = "derived";                       // final + literal = a compile-time constant, inlined by javac
        DerivedConstant(List<String> log) { super(log); }

        @Override String label() { return tag; }
    }

    public static void main(String[] args) {
        title("INHERITANCE  -  extends, @Override, final, abstract, constructor traps");

        section("extends, @Override, super, final");
        syntax(
                "class B extends A { }", "B inherits A's non-private members; ONE superclass only (Java has no multiple inheritance of classes)",
                "@Override", "the compiler checks that the method really overrides one: always write it",
                "super.f()  |  super(args)", "the parent's version  |  the parent's constructor (first statement)",
                "final class  |  final method", "cannot be extended  |  cannot be overridden (Java is OPEN by default, Kotlin is final by default)"
        );
        Car car = new Car();
        show(() -> car.honk());                                      // the parent's "beep" plus the child's own
        show(() -> car.describe());
        Vehicle asVehicle = new Car();
        show(() -> asVehicle.honk());                                // the RUN-TIME class decides: Car's override runs
        wontCompile("class Truck extends Vehicle { String describe() ... }", "describe() in Truck cannot override describe() in Vehicle; overridden method is final");
        wontCompile("@Override String hornk() { ... }", "method does not override or implement a method from a supertype (the typo is caught)");

        section("abstract class");
        syntax(
                "abstract class A { abstract T f(); }", "cannot be instantiated; an abstract method has no body",
                "class C extends A { T f() { ... } }", "a concrete subclass must implement every abstract method"
        );
        show(() -> new Rect(3, 4).summary());                        // summary() is written once, area() by each subclass
        wontCompile("new Figure();", "Figure is abstract; cannot be instantiated");

        section("TRAP: fields are not polymorphic, methods are");
        Parent p = new Child();
        show(() -> p.name);                                          // parent: a field is chosen by the DECLARED type
        show(() -> p.who());                                         // child: a method is chosen by the REAL type
        show(() -> ((Child) p).name);                                // child: cast first, then the Child field
        show(() -> p.kind());                                        // Parent.kind: a static method is chosen by the declared type too
        show(() -> Child.kind());
        note("Static methods are hidden, not overridden. Never re-declare a field with the same name: expose it with a method instead");

        section("TRAP: constructor chain and overridable methods");
        List<String> seen = new ArrayList<>();
        new Derived(seen);
        show(() -> seen);                                            // null: Derived.tag is not assigned yet when Base's constructor runs
        List<String> seenConstant = new ArrayList<>();
        new DerivedConstant(seenConstant);
        show(() -> seenConstant);                                    // derived: a constant is inlined, so even the early call sees it
        wontCompile("class Kid extends Adult { Kid() { } }", "Adult has only Adult(int): constructor Adult in class Adult cannot be applied to given types");
        note("Never call an overridable method from a constructor: the subclass is not initialized yet. Make the method final or private");

        section("instanceof and casts");
        Vehicle plain = new Vehicle(2);
        Vehicle none = null;
        show(() -> asVehicle instanceof Car);                        // true: the real class is Car
        show(() -> plain instanceof Car);
        show(() -> none instanceof Car);                             // false: instanceof is false for null, never an exception
        show(() -> thrown(() -> (Car) plain));                       // a downcast to the wrong class fails at RUN time
        show(() -> asVehicle instanceof Car c ? c.wheels : -1);      // pattern form: test, cast and name in one step (lesson 8)
        note("Prefer composition over inheritance; for 'is-a with a few kinds' use sealed (SealedAndPatterns)");
    }
}
