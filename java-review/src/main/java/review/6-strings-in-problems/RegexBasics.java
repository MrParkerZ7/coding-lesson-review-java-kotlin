package review.strings;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 6b. REGEX BASICS: matches, find, groups, replaceAll, split
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.RegexBasics
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class RegexBasics {

    public static void main(String[] args) {
        title("REGEX BASICS  -  patterns for parsing and cleaning text");

        section("Making a Pattern");
        syntax(
                "Pattern.compile(\"\\\\d+\")", "a regex is a String, so every backslash is doubled in the source",
                "Pattern.compile(regex, Pattern.CASE_INSENSITIVE)", "flags; or write (?i) at the start of the regex",
                "pattern.matcher(text)", "a Matcher: it remembers where the last match was"
        );
        Pattern digits = Pattern.compile("\\d+");
        show(() -> digits.pattern());                                   // the pattern holds ONE backslash: the doubling is only in the source
        wontCompile("String re = \"\\d+\";", "in a Java String a lone backslash-d is an illegal escape: write \\\\d");
        wontCompile("a text block containing \\d+", "a text block still processes escapes: Java has no raw strings");
        show(() -> Pattern.compile("hello", Pattern.CASE_INSENSITIVE).matcher("Say HELLO").find());
        show(() -> "Say HELLO".matches("(?i).*hello.*"));               // the inline flag (?i) does the same

        section("matches, lookingAt, find");
        syntax(
                "m.matches() | s.matches(regex)", "does the WHOLE string match?",
                "m.lookingAt()", "does it match at the START (not necessarily to the end)?",
                "m.find()", "is there a match anywhere inside? call again for the next one",
                "m.group() | m.start() | m.end()", "the matched text | where it begins | where it ends (exclusive)"
        );
        show(() -> digits.matcher("123").matches());
        show(() -> digits.matcher("a123").matches());                   // TRAP: false, matches() needs the whole string to fit
        show(() -> "a123".matches("\\d+"));                             // String.matches is the same whole-string test
        show(() -> digits.matcher("123abc").lookingAt());               // true: the start matches
        show(() -> digits.matcher("a123").find());                      // true: a match exists somewhere inside
        Matcher m = digits.matcher("ab 42 cd 7");
        m.find();
        show(() -> m.group());                                          // the first match
        show(() -> m.start());
        show(() -> m.end());                                            // exclusive: "42" sits at 3 and 4
        m.find();
        show(() -> m.group());                                          // find() continues after the previous match
        show(() -> m.find());                                           // false: no third match
        show(() -> thrown(() -> m.group()));                            // TRAP: group() without a current match throws
        List<String> all = new ArrayList<>();
        Matcher each = digits.matcher("ab 42 cd 7");
        while (each.find()) all.add(each.group());
        show(() -> all);                                                // the classic find-all loop
        show(() -> digits.matcher("ab 42 cd 7").results().map(MatchResult::group).toList());   // results() (Java 9) is a Stream of matches
        note("toList() is unmodifiable: the type column shows its internal class ImmutableCollections$ListN");

        section("Groups: parentheses capture parts");
        syntax(
                "m.group(0) | m.group(1) ...", "group 0 is the whole match, then group 1, 2, ... by opening parenthesis",
                "(?<name>...) | m.group(\"name\")", "named groups",
                "m.groupCount()", "how many groups the pattern has (group 0 not counted)"
        );
        Matcher date = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})").matcher("on 2026-11-09 ok");
        date.find();
        show(() -> date.group(0));                                      // the whole match
        show(() -> Integer.parseInt(date.group(1)));                    // group 1 is the year, still a String until parsed
        show(() -> date.group(3) + "/" + date.group(2) + "/" + date.group(1));
        show(() -> date.groupCount());
        Matcher named = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})").matcher("2026-11");
        named.find();
        show(() -> named.group("year"));                                // look a group up by name
        show(() -> thrown(() -> named.group("day")));                   // an unknown name throws
        Matcher optional = Pattern.compile("a(b)?").matcher("a");
        optional.find();
        show(() -> optional.group(1));                                  // TRAP: a group that did not take part is null, not ""

        section("replaceAll and split with a regex");
        syntax(
                "s.replaceAll(regex, replacement)", "every match; $1 $2 in the replacement refer to groups",
                "m.replaceAll(r -> text)", "compute the replacement from each match (Java 9)",
                "s.split(regex)", "split on a pattern, e.g. \\s+ for runs of whitespace"
        );
        show(() -> "ab 42 cd 7".replaceAll("\\d+", "#"));
        show(() -> "2026-11-09".replaceAll("(\\d+)-(\\d+)-(\\d+)", "$3/$2/$1"));
        show(() -> digits.matcher("ab 42 cd 7").replaceAll(r -> String.valueOf(Integer.parseInt(r.group()) * 2)));
        show(() -> "Hello, World 42!".replaceAll("[^A-Za-z0-9]", ""));   // drop everything except letters and digits
        show(() -> "Hello, World 42!".replaceAll("[aeiou]", "*"));
        show(() -> "one  two   three".split("\\s+"));                   // split on runs of spaces
        show(() -> "a.b.c".replace(".", "-"));                          // replace takes plain text
        show(() -> "a.b.c".replaceAll(".", "-"));                       // TRAP: as a regex "." matches every character
        show(() -> thrown(() -> "price 5".replaceAll("5", "$")));       // TRAP: $ in the replacement starts a group reference
        show(() -> "price 5".replaceAll("5", Matcher.quoteReplacement("$")));   // quoteReplacement makes it literal
        show(() -> Pattern.quote("a.b"));                               // turn user text into a literal pattern: \Q...\E
        note("Common pieces: \\d digit, \\w word char, \\s space, . any char, + one or more, * zero or more, ^ start, $ end");

        section("Cost");
        show(() -> Pattern.compile("a") == Pattern.compile("a"));       // false: there is no cache, every compile builds a new Pattern
        bigO(
                "matches | find | replaceAll", "O(n) in the text for ordinary patterns",
                "nested quantifiers like (a+)+", "can backtrack exponentially on bad input: keep patterns simple",
                "Pattern.compile(...)", "parses the regex on every call; s.matches, s.replaceAll and s.split(regex) compile too",
                "a regex used in a loop", "compile it once into a static final Pattern"
        );
    }
}
