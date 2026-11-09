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
 * 11a. CLASS BASICS: constructors, init order, access modifiers
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.ClassBasics
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ClassBasics {

    static final List<String> LOG = new ArrayList<>();

    static String log(String event) {
        LOG.add(event);
        return event;
    }

    static class Person {
        private final String name;
        private int age;

        Person(String name, int age) {
            this.name = name;                                       // this.name = the field, name = the parameter
            this.age = age;
        }

        Person(String name) { this(name, 18); }                     // this(...): another constructor of the SAME class, first statement

        String getName() { return name; }
        int getAge() { return age; }

        Person setAge(int age) {
            if (age < 0) throw new IllegalArgumentException("age < 0");   // a setter can validate, a public field cannot
            this.age = age;
            return this;                                            // returning this lets a call sit inside show(() -> ...) and chain
        }

        @Override
        public String toString() { return "Person{name=" + name + ", age=" + age + "}"; }
    }

    static class Plain { }                                          // no toString: Object's version prints ClassName@hash

    static class NoDefault {
        NoDefault(int x) { }                                        // declaring ANY constructor removes the free no-argument one
    }

    /** Field initializers and init blocks run in SOURCE order, then the constructor body. */
    static class InitOrder {
        String first = log("field first");
        { log("init block 1"); }
        String second = log("field second");
        { log("init block 2"); }

        InitOrder() {
            this(7);
            log("constructor()");
        }

        InitOrder(int n) { log("constructor(int " + n + ")"); }
    }

    /** TRAP: the first initializer calls a method that reads a field declared below it. */
    static class EarlyRead {
        final String seen = describe();                             // runs before size is assigned
        int size = 5;

        String describe() { return "size=" + size; }
    }

    public static void main(String[] args) {
        title("CLASS BASICS  -  constructors, init order, access modifiers");

        section("Fields, constructors, getters");
        syntax(
                "class Name { private final T f; Name(T f) { this.f = f; } }", "fields, then a constructor: no return type, same name as the class",
                "new Name(args)", "creates the object and runs the constructor",
                "Name(T f) { this(f, default); }", "constructor chaining: this(...) must be the FIRST statement",
                "getF() | isF() | setF(T f)", "getter, boolean getter, setter: encapsulation (a record gives accessors for free)"
        );
        Person ann = new Person("Ann");
        show(() -> ann.getName());
        show(() -> ann.getAge());                                    // 18: the default from Person(String)
        ann.setAge(19);
        show(() -> ann.getAge());
        show(() -> thrown(() -> ann.setAge(-1)));                    // the setter guards the invariant
        show(() -> ann);                                             // show prints String.valueOf(ann), which calls toString()
        show(() -> new Plain().toString().startsWith("review.classes.ClassBasics$Plain@"));   // true: Object.toString() = full class name + @ + hex hash
        final int limit = 5;
        show(() -> limit);
        wontCompile("final int limit = 5; limit = 6;", "cannot assign a value to final variable limit");
        wontCompile("new NoDefault();", "constructor NoDefault in class NoDefault cannot be applied to given types");

        section("Order of initialization (one object)");
        syntax(
                "1. super constructor", "the parent class is built first (implicit super(); see Inheritance)",
                "2. field initializers + init blocks", "in the order they are WRITTEN in the class",
                "3. constructor body", "last; with this(...) the target constructor runs fully before the rest of the caller"
        );
        LOG.clear();
        new InitOrder();
        show(() -> LOG);                                             // top to bottom, then constructor(int), then the delegating constructor
        show(() -> new EarlyRead().seen);                            // TRAP: size is still 0 (the default) when describe() runs
        wontCompile("{ int a = b; }  int b = 5;", "illegal forward reference (only a plain name is checked, a method call is not)");
        note("Fields start at 0 / false / null; static fields and the static init order are in lesson 12 (StaticAndInit)");

        section("Access modifiers");
        syntax(
                "public", "visible everywhere",
                "protected", "the same package AND subclasses in other packages",
                "(no keyword) package-private", "the same package only: the default, unlike Kotlin's public",
                "private", "the top-level class only (a nested class counts as part of it)"
        );
        Vault vault = new Vault();
        show(() -> vault.label);                                     // public
        show(() -> vault.guard);                                     // protected: this class is in the same package
        show(() -> vault.level);                                     // package-private: the same package again
        show(() -> vault.reveal());                                  // a public method may expose what a private field holds
        wontCompile("new Vault().secret", "secret has private access in Vault");
        show(() -> ann.name);                                        // legal: Person is nested in THIS top-level class, so private does not block it
        note("Make fields private and expose behaviour: the class can change its insides without breaking callers");
        note("static members (one copy per class, not per object) and the static init order: lesson 12, StaticAndInit");
    }
}
