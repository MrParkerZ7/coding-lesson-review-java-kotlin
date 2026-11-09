package review.exceptions;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.ArrayList;
import java.util.List;

/**
 * 15d. TRY-WITH-RESOURCES: automatic closing, closing order, suppressed exceptions  (events are recorded in a list)
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.TryWithResources
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class TryWithResources {

    /** A resource that records when it is opened, used and closed, and can fail on close. */
    static class Res implements AutoCloseable {
        private final String name;
        private final List<String> log;
        private final boolean failOnClose;

        Res(String name, List<String> log, boolean failOnClose) {
            this.name = name;
            this.log = log;
            this.failOnClose = failOnClose;
            log.add("open " + name);
        }

        void use() {
            log.add("use " + name);
        }

        @Override
        public void close() {
            log.add("close " + name);
            if (failOnClose) throw new IllegalStateException("close " + name + " failed");
        }
    }

    /** A constructor that fails: the resource never exists, so there is nothing to close for it. */
    static Res openOrFail(String name, List<String> log, boolean fail) {
        if (fail) throw new IllegalStateException("cannot open " + name);
        return new Res(name, log, false);
    }

    /** Body fails AND close fails: try-with-resources keeps the body exception and attaches the other. */
    static String withResources(List<String> log) {
        try (Res a = new Res("A", log, true)) {
            a.use();
            throw new IllegalStateException("body failed");
        } catch (IllegalStateException e) {
            return e.getMessage() + " + suppressed " + e.getSuppressed().length + ": " + e.getSuppressed()[0].getMessage();
        }
    }

    /** The old way: the exception from finally REPLACES the body exception. */
    static String withFinallyClose(List<String> log) {
        Res a = new Res("A", log, true);
        try {
            a.use();
            throw new IllegalStateException("body failed");
        } finally {
            a.close();
        }
    }

    /** What a block threw: class name and message, or "no exception". */
    static String caught(Runnable block) {
        try {
            block.run();
            return "no exception";
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        title("TRY-WITH-RESOURCES  -  closed for you, in reverse order");

        section("Closing order");
        syntax(
                "try (Res a = open(); Res b = open()) { ... }", "close() runs on exit, last opened first, BEFORE any catch or finally",
                "class Res implements AutoCloseable", "the only requirement: a close() method (Closeable is the I/O flavour)",
                "try (existing) { ... }", "Java 9+: an effectively final variable can be the resource"
        );
        List<String> order = new ArrayList<>();
        try (Res a = new Res("A", order, false); Res b = new Res("B", order, false)) {
            a.use();
            b.use();
        }
        show(() -> order);                                                 // B closes before A
        List<String> failing = new ArrayList<>();
        try (Res a = new Res("A", failing, false)) {
            a.use();
            throw new IllegalStateException("body failed");
        } catch (IllegalStateException e) {
            failing.add("catch");
        }
        show(() -> failing);                                               // close A happens BEFORE catch
        List<String> half = new ArrayList<>();
        try (Res a = new Res("A", half, false); Res b = openOrFail("B", half, true)) {
            a.use();
            b.use();
        } catch (IllegalStateException e) {
            half.add("catch " + e.getMessage());
        }
        show(() -> half);                                                  // B never opened, A (opened) is still closed before the catch
        List<String> existing = new ArrayList<>();
        Res shared = new Res("S", existing, false);
        try (shared) {
            shared.use();
        }
        show(() -> existing);                                              // Java 9: no need to declare a new variable

        section("Trap: two exceptions at once");
        List<String> logA = new ArrayList<>();
        List<String> logB = new ArrayList<>();
        show(() -> withResources(logA));                                   // the body exception wins, the close failure is attached
        show(() -> caught(() -> withFinallyClose(logB)));                  // TRAP: with plain finally the close failure REPLACED the body failure
        note("try-with-resources keeps the body exception and attaches the close failure as e.getSuppressed()");
        note("Use it for every file, stream, connection and lock-like object: a forgotten close() is a leak");
    }
}
