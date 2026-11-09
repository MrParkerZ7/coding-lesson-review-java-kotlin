package review.exceptions;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 15d. EXCEPTION TRAPS: catch width, lambdas, validation, assert, messages, static initializers
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.ExceptionTraps
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ExceptionTraps {

    /** Catches Exception: an Error flies straight through. */
    static String asException(Runnable block) {
        try {
            block.run();
            return "ok";
        } catch (Exception e) {
            return "caught Exception";
        }
    }

    /** Catches Throwable: also stops Errors, which is almost never what you want. */
    static String asThrowable(Runnable block) {
        try {
            block.run();
            return "ok";
        } catch (Throwable t) {
            return "caught " + t.getClass().getSimpleName();
        }
    }

    /** The empty catch: the failure disappears and 0 looks like real data. */
    static int parseOrZero(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    static String read(String name) throws IOException {
        throw new IOException("no file " + name);
    }

    /** Checked exceptions cannot cross a lambda: wrap them in an unchecked one. */
    static String readUnchecked(String name) {
        try {
            return read(name);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The CALLER passed a bad value: IllegalArgumentException. */
    static int setAge(int age) {
        if (age < 0) throw new IllegalArgumentException("age must be >= 0, was " + age);
        return age;
    }

    /** THIS object is in the wrong state for the call: IllegalStateException. */
    static class Door {
        private boolean open;

        void open() {
            open = true;
        }

        String enter() {
            if (!open) throw new IllegalStateException("door is closed");
            return "in";
        }
    }

    /** An exception that skips the stack trace: much cheaper to create, useless to debug. */
    static class Quiet extends RuntimeException {
        Quiet(String message) {
            super(message, null, false, false);
        }
    }

    /** The static initializer fails when the class is first used. */
    static class Broken {
        static final int VALUE = Integer.parseInt("not a number");
    }

    static String messageOf(Runnable block) {
        try {
            block.run();
            return "no exception";
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    public static void main(String[] args) {
        title("EXCEPTION TRAPS  -  what goes wrong in real code");

        section("Catch Exception or Throwable?");
        syntax(
                "catch (Exception e)", "everything a program can reasonably handle; Errors are NOT in it",
                "catch (Throwable t)", "also OutOfMemoryError, StackOverflowError: only at the very top of a thread"
        );
        show(() -> thrown(() -> asException(() -> CommonExceptions.endless(0))));   // the Error escapes catch (Exception)
        show(() -> asThrowable(() -> CommonExceptions.endless(0)));        // Throwable stops it (and hides a real problem)
        show(() -> parseOrZero("12"));
        show(() -> parseOrZero("oops"));                                   // TRAP: 0 looks like real data, the failure is silent
        note("An empty catch block is a bug report you threw away: handle it, or log and rethrow, or let it propagate");
        note("Log OR rethrow, never both: the same error then appears twice in the log");

        section("Exceptions inside lambdas and streams");
        wontCompile("list.forEach(s -> Files.readString(Path.of(s)));", "unreported exception IOException: a lambda cannot throw a checked exception");
        show(() -> thrown(() -> List.of("a.txt").stream().map(ExceptionTraps::readUnchecked).toList()));   // wrapped, so it passes through
        show(() -> messageOf(() -> readUnchecked("a.txt")));               // wrapping only a cause: the message is cause.toString()
        show(() -> thrown(() -> Stream.of("1", "x").map(Integer::parseInt)));            // no terminal operation yet: nothing ran
        show(() -> thrown(() -> Stream.of("1", "x").map(Integer::parseInt).toList()));   // the failure appears only at the terminal operation
        note("A stream pipeline is lazy: the exception surfaces where toList / collect / forEach runs it (lesson 4)");

        section("Validate: argument or state?");
        syntax(
                "throw new IllegalArgumentException(msg)", "the caller passed a bad value",
                "throw new IllegalStateException(msg)", "the object is in the wrong state for this call",
                "Objects.requireNonNull(x, \"msg\")", "NullPointerException with YOUR message, at the door of the method",
                "Objects.checkIndex(i, length)", "IndexOutOfBoundsException with the index and length in the message"
        );
        Door door = new Door();
        show(() -> thrown(() -> setAge(-1)));                              // the argument was wrong
        show(() -> messageOf(() -> setAge(-1)));                           // put the bad value into the message
        show(() -> thrown(() -> door.enter()));                            // the state was wrong
        door.open();
        show(() -> door.enter());                                          // same call, valid state: it works
        show(() -> messageOf(() -> Objects.requireNonNull(CommonExceptions.nothing(), "text is required")));
        show(() -> messageOf(() -> Objects.checkIndex(5, 3)));

        section("assert is off by default");
        show(() -> ExceptionTraps.class.desiredAssertionStatus());         // false unless the JVM was started with -ea
        show(() -> thrown(() -> { assert CommonExceptions.zero() > 0 : "never checked"; return 0; }));   // so by default assert does nothing at all
        note("assert is for the developer's own checks (tests run with -ea); real validation must use if + throw or Objects.requireNonNull");

        section("Messages, stack traces and class loading");
        show(() -> messageOf(() -> CommonExceptions.nothing().length()));  // helpful NPE message (Java 15+ default): says what was null
        show(() -> new RuntimeException("x").getStackTrace().length > 0);  // creating an exception fills in the stack trace: that is the expensive part
        show(() -> new Quiet("x").getStackTrace().length);                 // writableStackTrace = false: no trace at all
        note("Exceptions for normal flow control (instead of an if) are slow and hard to read; the try itself costs nothing");
        show(() -> thrown(() -> Broken.VALUE));                            // first use: the static initializer fails
        show(() -> thrown(() -> Broken.VALUE));                            // TRAP: the class is now unusable: NoClassDefFoundError
        note("InterruptedException: catch it, then call Thread.currentThread().interrupt() so the caller still learns of it");
    }
}
