package review;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Print helpers so every lesson reads as plain Java and prints in the same layout.
 *
 * IN THE SOURCE (plain code, one line each, the comment is the note):
 *
 *     syntax("int x = value;", "declare and initialise")           // pairs: form, meaning
 *     int i = 42;  show(() -> i);                                   // a declaration may come before show on the same line
 *     show(() -> 7 / 2);                                            // int / int truncates toward zero
 *     wontCompile("long l = 1; int i = l;", "narrowing needs a cast");
 *
 * IN THE OUTPUT (the code text and the comment are read back from the source file, so nothing is typed twice):
 *
 *     syntax  int x = value;                    declare and initialise
 *     int i = 42                       -> 42        Integer  type of the value (a primitive prints as its wrapper)
 *     7 / 2                            -> 3         Integer  int / int truncates toward zero
 *     x  long l = 1; int i = l;        does NOT compile: narrowing needs a cast
 *
 * Rules for a `show` line: one line only; `show(() -> ...)` is the last statement on it; the note is the trailing `// comment`;
 * a declaration may come before it on the same line and is then printed as the code.
 *
 * Colors (ANSI) and box lines (UTF-8) look right in the IntelliJ Run window, Windows Terminal and Git Bash.
 * If a terminal shows garbage instead, switch to plain ASCII with either:
 *     mvn -q compile exec:java -Dstyle=plain        or        set the environment variable NO_COLOR=1
 */
public final class Output {

    private Output() {
    }

    private static final int WIDTH = 92;
    private static final int LABEL_WIDTH = 44;
    private static final int VALUE_WIDTH = 22;
    private static final int TYPE_WIDTH = 10;
    private static final int SYNTAX_WIDTH = 54;

    private static final boolean PLAIN = System.getenv("NO_COLOR") != null || "plain".equals(System.getProperty("style"));

    /** Fancy mode writes UTF-8 bytes itself, so box lines survive any default console encoding. */
    private static final PrintStream OUT =
            PLAIN ? System.out : new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);

    // The escape character is built at run time so the source file contains no raw control bytes.
    private static final String ESC = (char) 27 + "[";

    private static final String BOLD_CYAN = "1;96";
    private static final String BOLD_WHITE = "1;97";
    private static final String BOLD_YELLOW = "1;93";
    private static final String BOLD_GREEN = "1;92";
    private static final String BOLD_RED = "1;91";
    private static final String MAGENTA = "95";
    private static final String YELLOW = "93";
    private static final String BLUE = "94";
    private static final String GRAY = "90";

    private static String paint(String code, String text) {
        return PLAIN ? text : ESC + code + "m" + text + ESC + "0m";
    }

    private static String pad(String text, int width) {
        return text.length() >= width ? text : text + " ".repeat(width - text.length());
    }

    // ---------------------------------------------------------------------------------------------
    // Layout helpers
    // ---------------------------------------------------------------------------------------------

    /** Big banner at the top of a file's output. */
    public static void title(String text) {
        if (PLAIN) {
            OUT.println("=".repeat(WIDTH));
            OUT.println("  " + text);
            OUT.println("=".repeat(WIDTH));
            return;
        }
        int inner = WIDTH - 2;
        OUT.println(paint(BOLD_CYAN, "╭" + "─".repeat(inner) + "╮"));
        OUT.println(paint(BOLD_CYAN, "│") + paint(BOLD_WHITE, pad("  " + text, inner)) + paint(BOLD_CYAN, "│"));
        OUT.println(paint(BOLD_CYAN, "╰" + "─".repeat(inner) + "╯"));
    }

    /** Sub-heading inside a file's output. */
    public static void section(String text) {
        OUT.println();
        if (PLAIN) OUT.println(padWith("--- " + text + " ", WIDTH, '-'));
        else OUT.println(paint(BOLD_YELLOW, padWith("── " + text + " ", WIDTH, '─')));
    }

    private static String padWith(String text, int width, char fill) {
        return text.length() >= width ? text : text + String.valueOf(fill).repeat(width - text.length());
    }

    /** The syntax template(s) of a section: pairs of (form, meaning), e.g. {@code syntax("int x = v;", "declare")}. */
    public static void syntax(String... formsAndMeanings) {
        templates("syntax", formsAndMeanings);
    }

    /** The cost of operations: pairs of (operation, cost), e.g. {@code bigO("list.get(i)", "O(1)")}. Complexity is a core interview topic. */
    public static void bigO(String... costsAndMeanings) {
        templates("big-O", costsAndMeanings);
    }

    private static void templates(String tag, String[] rows) {
        if (rows.length % 2 != 0) throw new IllegalArgumentException(tag + "(...) takes pairs: form, meaning");
        for (int i = 0; i < rows.length; i += 2) {
            String label = i == 0 ? tag : "";
            String left = rows[i];
            String right = rows[i + 1];
            if (PLAIN) {
                OUT.println(("  " + pad(label, 8) + pad(left, SYNTAX_WIDTH) + " " + right).stripTrailing());
            } else {
                OUT.println("  " + paint(MAGENTA, pad(label, 8)) + paint(BOLD_GREEN, pad(left, SYNTAX_WIDTH)) + " " + paint(GRAY, right));
            }
        }
    }

    /**
     * One result line. Write it as {@code show(() -> expression);  // note} or {@code int x = 1; show(() -> x);  // note}.
     * The code text and the note come from the line of the lesson's source file that called this method.
     */
    public static void show(Supplier<?> block) {
        Object value = block.get();
        StackTraceElement[] stack = new Throwable().getStackTrace();
        StackTraceElement frame = stack.length > 1 ? stack[1] : null;
        CallSite site = frame == null ? null : callSite(frame);
        printRow(site != null ? site.label : "show(...)  (source not found: run from the java-review folder)",
                value, site != null ? site.note : "");
    }

    /** A call that may throw (also checked exceptions and Errors): returns the exception's simple class name, or "no exception". */
    public interface Risky {
        Object run() throws Throwable;
    }

    public static String thrown(Risky risky) {
        try {
            risky.run();
            return "no exception";
        } catch (Throwable t) {
            return t.getClass().getSimpleName();
        }
    }

    /** Code that the compiler rejects, so it cannot be run: shown with the reason. */
    public static void wontCompile(String code, String why) {
        if (PLAIN) {
            OUT.println("  XX  " + pad(code, LABEL_WIDTH - 4) + " does NOT compile: " + why);
        } else {
            OUT.println("  " + paint(BOLD_RED, "×") + "  " + paint(YELLOW, pad(code, LABEL_WIDTH - 3)) + " "
                    + paint(BOLD_RED, "does NOT compile:") + " " + paint(GRAY, why));
        }
    }

    /** A plain remark, for things that cannot be printed (an exception, a rule, a tip). */
    public static void note(String text) {
        if (PLAIN) OUT.println("  >> " + text);
        else OUT.println("  " + paint(BOLD_YELLOW, "!") + "  " + paint(YELLOW, text));
    }

    /** A multi-line text, such as pretty-printed JSON, printed as an indented block (each line behind a bar). */
    public static void block(String text) {
        for (String line : text.lines().toList()) {
            if (PLAIN) OUT.println("  | " + line);
            else OUT.println("  " + paint(GRAY, "│") + " " + paint(BOLD_CYAN, line));
        }
    }

    private static void printRow(String label, Object value, String note) {
        String type = typeName(value);
        if (PLAIN) {
            String noteText = note.isEmpty() ? "" : "# " + note;
            OUT.println(("  " + pad(label, LABEL_WIDTH) + " = " + pad(display(value), VALUE_WIDTH) + " " + pad(type, TYPE_WIDTH) + " " + noteText).stripTrailing());
            return;
        }
        String typeText = note.isEmpty() ? type : pad(type, TYPE_WIDTH);
        String noteText = note.isEmpty() ? "" : " " + paint(GRAY, note);
        OUT.println("  " + pad(label, LABEL_WIDTH) + " " + paint(GRAY, "→") + " "
                + paint(valueColor(value), pad(display(value), VALUE_WIDTH)) + " " + paint(BLUE, typeText) + noteText);
    }

    /** The run-time class: java.* names lose their package, an inner class keeps its outer name (Arrays$ArrayList). */
    private static String typeName(Object value) {
        if (value == null) return "null";
        Class<?> c = value.getClass();
        if (c.isArray()) return c.getSimpleName();
        String name = c.getName();
        if (name.startsWith("java.")) return name.substring(name.lastIndexOf('.') + 1);
        return c.getSimpleName();
    }

    private static String display(Object value) {
        if (value == null) return "null";
        if (value instanceof Character c) return "'" + c + "'";
        if (value instanceof String s) return "\"" + s + "\"";
        if (value.getClass().isArray()) {
            String text = Arrays.deepToString(new Object[] {value});   // works for every array type, also nested ones
            return text.substring(1, text.length() - 1);
        }
        return String.valueOf(value);
    }

    private static String valueColor(Object value) {
        if (Boolean.TRUE.equals(value)) return "1;92";                    // green
        if (Boolean.FALSE.equals(value)) return "1;91";                   // red
        if (value instanceof Character || value instanceof String) return "1;93";   // yellow
        if (value == null) return "1;95";                                 // magenta
        if (value instanceof Number) return "1;96";                       // cyan
        return "1";
    }

    // ---------------------------------------------------------------------------------------------
    // Reading the call site back from the lesson's source file
    // ---------------------------------------------------------------------------------------------

    private record CallSite(String label, String note) {
    }

    private static final Map<String, List<String>> SOURCE_CACHE = new HashMap<>();
    private static final Pattern SHOW_CALL = Pattern.compile("\\bshow\\s*\\(");
    private static final Pattern LAMBDA_HEAD = Pattern.compile("^\\(\\s*\\)\\s*->\\s*");

    private static CallSite callSite(StackTraceElement frame) {
        String fileName = frame.getFileName();
        if (fileName == null) return null;
        String className = frame.getClassName();
        int dot = className.lastIndexOf('.');
        String pkg = dot < 0 ? "" : className.substring(0, dot);
        String key = pkg + "/" + fileName;
        List<String> lines;
        if (SOURCE_CACHE.containsKey(key)) {
            lines = SOURCE_CACHE.get(key);
        } else {
            lines = findSource(fileName, pkg);
            SOURCE_CACHE.put(key, lines);
        }
        if (lines == null || frame.getLineNumber() < 1 || frame.getLineNumber() > lines.size()) return null;
        return parseShowLine(lines.get(frame.getLineNumber() - 1));
    }

    /** Splits {@code int i = 42;  show(() -> i);   // note} into the code ("int i = 42") and the note ("note"). */
    private static CallSite parseShowLine(String text) {
        Matcher m = SHOW_CALL.matcher(text);
        if (!m.find()) return null;
        String before = text.substring(0, m.start()).strip();
        while (before.endsWith(";")) before = before.substring(0, before.length() - 1).strip();
        int bodyStart = m.end();
        int depth = 1;
        int i = bodyStart;
        boolean inString = false;
        boolean inChar = false;
        while (i < text.length() && depth > 0) {
            char c = text.charAt(i);
            if (inString) {
                if (c == '\\') i++;
                else if (c == '"') inString = false;
            } else if (inChar) {
                if (c == '\\') i++;
                else if (c == '\'') inChar = false;
            } else if (c == '"') {
                inString = true;
            } else if (c == '\'') {
                inChar = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
            i++;
        }
        if (depth != 0) return null;
        String body = text.substring(bodyStart, i - 1).strip();
        body = LAMBDA_HEAD.matcher(body).replaceFirst("");
        String rest = text.substring(i);
        int slashes = rest.indexOf("//");
        String note = slashes < 0 ? "" : rest.substring(slashes + 2).strip();
        return new CallSite(before.isEmpty() ? body : before, note);
    }

    private static List<String> findSource(String fileName, String pkg) {
        for (File root : sourceRoots()) {
            File hit = search(root, fileName, pkg);
            if (hit != null) {
                try {
                    return Files.readAllLines(hit.toPath(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static File search(File dir, String fileName, String pkg) {
        File[] children = dir.listFiles();
        if (children == null) return null;
        for (File child : children) {
            if (child.isDirectory()) {
                File hit = search(child, fileName, pkg);
                if (hit != null) return hit;
            } else if (child.getName().equals(fileName) && declaresPackage(child, pkg)) {
                return child;
            }
        }
        return null;
    }

    private static boolean declaresPackage(File file, String pkg) {
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                if (line.startsWith("package ")) {
                    return line.substring("package ".length()).replace(";", "").strip().equals(pkg);
                }
            }
        } catch (IOException e) {
            return false;
        }
        return pkg.isEmpty();
    }

    /** Candidate {@code src/main/java} folders: from the working folder and from where the compiled classes live. */
    private static List<File> sourceRoots() {
        List<File> starts = new ArrayList<>();
        starts.add(new File("").getAbsoluteFile());
        try {
            File dir = new File(Output.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            for (int n = 0; n < 5 && dir != null; n++) {
                starts.add(dir);
                dir = dir.getParentFile();
            }
        } catch (Exception ignored) {
            // fall back to the working folder only
        }
        List<File> roots = new ArrayList<>();
        for (File start : starts) {
            for (String sub : new String[] {"src/main/java", "java-review/src/main/java"}) {
                File candidate = new File(start, sub);
                if (candidate.isDirectory() && !roots.contains(candidate)) roots.add(candidate);
            }
        }
        return roots;
    }
}
