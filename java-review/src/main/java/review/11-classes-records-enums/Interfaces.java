package review.classes;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 11c. INTERFACES: abstract, default, static and private methods, constants, the diamond rule
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.Interfaces
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class Interfaces {

    interface Greeter {
        String name();                                               // abstract, implicitly public

        default String greet() { return decorate("Hello, " + name()); }   // has a body: classes inherit it, may override it

        static Greeter of(String name) { return () -> name; }        // static: called as Greeter.of(...), not inherited

        private String decorate(String text) { return text + "!"; }  // private helper (Java 9), invisible outside the interface
    }

    static class Friend implements Greeter {
        private final String name;

        Friend(String name) { this.name = name; }

        @Override
        public String name() { return name; }                        // public is required: an interface method is public
    }

    interface Walker { default String move() { return "walk"; } }
    interface Swimmer { default String move() { return "swim"; } }

    /** Two defaults with the same signature: the class MUST pick, and may call each with Type.super.f(). */
    static class Duck implements Walker, Swimmer {
        @Override
        public String move() { return Walker.super.move() + "+" + Swimmer.super.move(); }
    }

    static class Machine {
        public String move() { return "roll"; }
    }

    static class Robot extends Machine implements Walker { }         // no override needed: the class's method beats the default

    interface Limits {
        int MAX = 10;                                                // fields in an interface are public static final
    }

    public static void main(String[] args) {
        title("INTERFACES  -  default, static, private, constants, the diamond rule");

        section("What an interface can hold");
        syntax(
                "interface I { T f(); }", "an abstract method (public): the contract",
                "default T g() { ... }", "a method WITH a body, built on the abstract ones; classes inherit or override it",
                "static T h() { ... }", "a helper called as I.h(); not inherited by implementing classes",
                "private T p() { ... }", "a helper shared by the default methods (Java 9)",
                "int MAX = 10;", "a constant: public static final. No instance fields, no constructors"
        );
        Friend ann = new Friend("Ann");
        show(() -> ann.greet());                                     // the default method calls the abstract name()
        show(() -> Greeter.of("Bo").greet());                        // the static factory returns a lambda: Greeter has ONE abstract method
        Greeter cy = () -> "Cy";
        show(() -> cy.greet());                                      // a lambda implements the abstract method, the default comes along
        show(() -> Limits.MAX);
        wontCompile("Limits.MAX = 11;", "cannot assign a value to static final variable MAX");
        wontCompile("class Fr implements Greeter { String name() { ... } }", "name() in Fr cannot implement name() in Greeter; attempting to assign weaker access privileges; was public");
        wontCompile("interface I { default String toString() { ... } }", "default method toString in interface I overrides a member of java.lang.Object");

        section("Two interfaces with the same default method");
        syntax(
                "class C implements A, B", "any number of interfaces (a class extends only one class)",
                "Type.super.f()", "inside the class: call the default of ONE specific interface",
                "class wins", "a method inherited from a class beats an interface default"
        );
        show(() -> new Duck().move());                               // walk+swim: the class chose, calling both
        show(() -> new Robot().move());                              // roll: Machine.move() wins over Walker's default
        wontCompile("class Mute implements Walker, Swimmer { }", "types Walker and Swimmer are incompatible; class Mute inherits unrelated defaults for move()");
        note("Interface or abstract class? An interface is a role a class can add (Comparable, Runnable); an abstract class shares state and constructor code");
        note("An interface with exactly one abstract method can be implemented by a lambda (lesson 10)");
    }
}
