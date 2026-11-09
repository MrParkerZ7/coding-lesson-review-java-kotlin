package review.objects;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;

/**
 * 12e. STATIC and INITIALIZATION ORDER: shared fields, static init, when a class is initialized
 *      Run: mvn -q compile exec:java -Dmain.class=review.objects.StaticAndInit
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class StaticAndInit {

    static final List<String> LOG = new ArrayList<>();

    static String log(String event) {
        LOG.add(event);
        return event;
    }

    static class Parent {
        static String parentStatic = log("Parent static field");
        static { log("Parent static block"); }

        String parentField = log("Parent instance field");
        { log("Parent instance block"); }

        Parent() { log("Parent constructor"); }
    }

    static class Child extends Parent {
        static String childStatic = log("Child static field");
        static { log("Child static block"); }

        String childField = log("Child instance field");
        { log("Child instance block"); }

        Child() { log("Child constructor"); }
    }

    static class Lazy {
        static final int CONSTANT = 5;                               // a compile-time constant: javac copies the 5 into the caller
        static final String NAME = log("Lazy.NAME");                 // computed by a method call: needs the class to be initialized
        static { log("Lazy static block"); }

        static void touch() { }
    }

    static class Counter {
        static int count = 0;                                        // ONE copy for the whole class
        final int id;

        Counter() { id = ++count; }

        static int total() { return count; }                         // a static method works on static state only
    }

    public static void main(String[] args) {
        title("STATIC AND INIT ORDER  -  what runs when");

        section("static fields and methods");
        syntax(
                "static int count;", "one copy per CLASS, shared by every object (and reachable as Counter.count)",
                "static int total() { ... }", "needs no object; it cannot use this or instance fields",
                "static final int MAX = 10;", "a constant: UPPER_SNAKE_CASE; a compile-time constant is inlined into its users"
        );
        new Counter();
        new Counter();
        Counter third = new Counter();
        show(() -> third.id);                                        // 3: id belongs to the object
        show(() -> Counter.total());                                 // 3: count belongs to the class
        wontCompile("static int f() { return id; }", "non-static variable id cannot be referenced from a static context");
        note("Static mutable state is global state: every test and thread shares it. Prefer instances, keep static for constants and pure helpers");

        section("Initialization order of a class hierarchy");
        syntax(
                "1. class initialization (once)", "static fields and static blocks, in written order, parent class FIRST",
                "2. per object", "parent instance fields + blocks, parent constructor, then child fields + blocks, child constructor"
        );
        LOG.clear();
        new Child();
        List<String> firstTime = new ArrayList<>(LOG);
        show(() -> firstTime);                                       // statics of Parent, statics of Child, then the instance parts
        LOG.clear();
        new Child();
        List<String> secondTime = new ArrayList<>(LOG);
        show(() -> secondTime);                                      // the static parts do not run again
        note("Instance order for ONE class (field initializers, init blocks, constructors, this(...)) is in lesson 11, ClassBasics");

        section("A class is initialized lazily, on first real use");
        syntax(
                "triggers", "new, a static method call, reading or writing a static field that is not a compile-time constant",
                "no trigger", "mentioning the class name, or reading a compile-time constant (static final int / String literal)"
        );
        LOG.clear();
        show(() -> Lazy.CONSTANT);                                   // 5, and the log stays empty: the constant was inlined
        List<String> afterConstant = new ArrayList<>(LOG);
        show(() -> afterConstant);                                   // []: Lazy was not initialized yet
        Lazy.touch();
        List<String> afterTouch = new ArrayList<>(LOG);
        show(() -> afterTouch);                                      // the first real use ran the field initializer and the static block, in order
        Lazy.touch();
        show(() -> LOG.size());                                      // 2: they never run a second time
        note("Class initialization runs once per class loader and the JVM makes it thread-safe: that is why the holder singleton works (next file)");
    }
}
