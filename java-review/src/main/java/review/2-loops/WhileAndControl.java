package review.loops;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 2b. WHILE, DO-WHILE, BREAK, CONTINUE, LABELS
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.WhileAndControl
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class WhileAndControl {

    /** TRAP: the semicolon right after the for header IS the loop body, so count++ runs once, after the loop. */
    private static int strayLoopSemicolon() {
        int count = 0;
        for (int i = 0; i < 3; i++);
        count++;
        return count;
    }

    /** TRAP: break inside an old-style switch leaves the SWITCH, the loop goes on. */
    private static List<Integer> breakInSwitch() {
        List<Integer> out = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            switch (i) {
                case 3:
                    break;
                default:
                    out.add(i);
            }
        }
        return out;
    }

    /** To leave the loop from inside a switch the break needs the loop's label. */
    private static List<Integer> breakLoopFromSwitch() {
        List<Integer> out = new ArrayList<>();
        loop:
        for (int i = 1; i <= 5; i++) {
            switch (i) {
                case 3:
                    break loop;
                default:
                    out.add(i);
            }
        }
        return out;
    }

    public static void main(String[] args) {
        title("WHILE LOOPS AND LOOP CONTROL");

        section("while and do-while");
        syntax(
                "while (condition) { }", "checks BEFORE each round: may run zero times",
                "do { } while (condition);", "runs the body first: at least once (note the final semicolon)",
                "while (true) { ... break; }", "loop until something inside says stop"
        );
        List<Integer> countdown = new ArrayList<>();
        int n = 3;
        while (n > 0) countdown.add(n--);   show(() -> countdown);   // n-- uses the value, THEN decrements
        List<String> tries = new ArrayList<>();
        int attempt = 0;
        do {
            attempt++;
            tries.add("try" + attempt);
        } while (attempt < 2);
        show(() -> tries);                                             // the body ran once before the first check
        List<Integer> once = new ArrayList<>();
        int k = 10;
        do {
            once.add(k);
        } while (k < 5);
        show(() -> once);                                              // do-while: one round although the condition is false from the start
        List<Integer> never = new ArrayList<>();
        int m = 10;
        while (m < 5) never.add(m);   show(() -> never);   // while: the check comes first, zero rounds
        List<Integer> found = new ArrayList<>();
        int pos = 0;
        while (true) {
            if (pos == 3) break;
            found.add(pos++);
        }
        show(() -> found);
        Deque<Integer> queue = new ArrayDeque<>(List.of(1, 2, 3));
        List<Integer> drained = new ArrayList<>();
        while (!queue.isEmpty()) drained.add(queue.removeFirst());   show(() -> drained);   // the shape of BFS: take from the queue until it is empty
        show(() -> strayLoopSemicolon());                              // TRAP: 1, not 3
        wontCompile("while (1) { }", "incompatible types: int cannot be converted to boolean (the condition must be a boolean)");
        wontCompile("while (false) { work(); }", "unreachable statement: the body can never run");

        section("break and continue");
        syntax(
                "break;", "leave the loop now",
                "continue;", "skip to the next round",
                "label: for (...) { break label; }", "leave (or continue label;) an OUTER loop"
        );
        List<Integer> evens = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            if (i % 2 == 1) continue;
            evens.add(i);
        }
        show(() -> evens);                                             // skip the odd numbers
        List<Integer> stopped = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if (i == 4) break;
            stopped.add(i);
        }
        show(() -> stopped);                                           // stop at 4
        List<String> pairs = new ArrayList<>();
        outer:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (j == 2) continue outer;                            // jump to the NEXT i
                if (i == 3) break outer;                               // leave BOTH loops
                pairs.add("(" + i + "," + j + ")");
            }
        }
        show(() -> pairs);                                             // labels reach the outer loop
        show(() -> breakInSwitch());                                   // TRAP: 3 is skipped, but the loop does not stop
        show(() -> breakLoopFromSwitch());                             // break loop; really leaves the loop
        note("An arrow switch (case 3 ->) behaves the same: break leaves only the switch; continue does continue the loop");
        note("break and continue inside a forEach lambda: ForEachAndIterator");
    }
}
