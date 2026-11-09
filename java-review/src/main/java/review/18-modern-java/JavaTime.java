package review.modern;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

/**
 * 18b. java.time (Java 8): dates, durations, zones, with FIXED dates so the output never changes
 *      Run: mvn -q compile exec:java -Dmain.class=review.modern.JavaTime
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class JavaTime {

    public static void main(String[] args) {
        title("JAVA.TIME  -  immutable dates and times (never LocalDate.now() in a demo)");

        section("The types");
        syntax(
                "LocalDate | LocalTime | LocalDateTime", "a date | a time | both, with NO time zone (a calendar entry)",
                "Instant", "a point on the UTC timeline (machine time, epoch seconds)",
                "ZonedDateTime", "date + time + zone: the local time of a real place",
                "Duration | Period", "an amount of TIME (hours, seconds) | an amount of DATE (years, months, days)",
                "DateTimeFormatter", "text <-> date; thread-safe, unlike the old SimpleDateFormat"
        );

        section("LocalDate");
        LocalDate d = LocalDate.of(2026, 11, 9);
        show(() -> d);                                                     // ISO format yyyy-MM-dd
        show(() -> d.getDayOfWeek());
        show(() -> d.plusDays(30));                                        // plus... returns a NEW date
        show(() -> d.plusMonths(3));
        show(() -> LocalDate.of(2026, 1, 31).plusMonths(1));               // TRAP: no 31 February, so it clamps to the last day of the month
        show(() -> d);                                                     // d itself never changed: java.time is immutable
        show(() -> d.isLeapYear());
        show(() -> d.withDayOfMonth(1).getDayOfWeek());                    // with... replaces one field
        show(() -> thrown(() -> LocalDate.of(2026, 2, 31)));               // an invalid date fails at once: DateTimeException
        show(() -> LocalDate.parse("2026-11-09").equals(d));               // parse the ISO text; equals compares the values
        show(() -> d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));   // dd day, MM month, yyyy year
        LocalDate lateDecember = LocalDate.of(2026, 12, 29);
        show(() -> lateDecember.format(DateTimeFormatter.ofPattern("YYYY-MM-dd", Locale.US)));   // TRAP: YYYY is the WEEK-based year: 2027 here
        show(() -> LocalDateTime.of(2026, 11, 9, 14, 30).format(DateTimeFormatter.ofPattern("mm")));   // TRAP: mm is minutes, MM is month
        show(() -> d.isBefore(LocalDate.of(2027, 1, 1)));

        section("Between two dates");
        show(() -> ChronoUnit.DAYS.between(LocalDate.of(2026, 1, 1), d));  // days from 1 January to 9 November
        show(() -> Period.between(LocalDate.of(2025, 3, 15), d));          // P1Y7M25D: 1 year, 7 months, 25 days (a calendar difference)
        show(() -> Period.between(LocalDate.of(2025, 3, 15), d).getMonths());   // the MONTHS part (7), not the total months
        show(() -> ChronoUnit.MONTHS.between(LocalDate.of(2025, 3, 15), d));    // the total: 19

        section("Time of day, Duration, Instant, zones");
        LocalDateTime meeting = LocalDateTime.of(2026, 11, 9, 14, 30);
        show(() -> meeting);                                               // no zone: just the wall clock reading
        show(() -> meeting.plusHours(10));                                 // 14:30 + 10 h crosses midnight into the next day
        show(() -> Duration.between(meeting, meeting.plusHours(5).plusMinutes(20)));   // PT5H20M
        show(() -> Duration.ofSeconds(3725));                              // PT1H2M5S
        show(() -> Duration.ofMinutes(135).toHours());                     // 2: whole hours
        Instant fiveUtc = Instant.parse("2026-11-09T05:00:00Z");
        show(() -> fiveUtc.atZone(ZoneId.of("Asia/Bangkok")));             // the same instant as Bangkok's wall clock: UTC+7, so noon
        show(() -> Instant.ofEpochSecond(0));                              // the epoch: 1970-01-01T00:00:00Z
        show(() -> Duration.between(fiveUtc, fiveUtc.plusSeconds(90)).getSeconds());
        Clock fixed = Clock.fixed(Instant.parse("2026-11-09T00:00:00Z"), ZoneOffset.UTC);
        show(() -> LocalDate.now(fixed));                                  // now(clock): a clock you pass in, so tests and demos are repeatable
        note("Real code takes a Clock (or the date) as a parameter instead of calling now() inside: that is what makes it testable");

        section("The old API (java.util.Date, Calendar) - why it was replaced");
        show(() -> Calendar.NOVEMBER);                                     // TRAP: Calendar months count from 0, so November is 10
        show(() -> Month.NOVEMBER.getValue());                             // java.time: November is 11, as people say
        show(() -> DayOfWeek.MONDAY.getValue());                           // 1 to 7, Monday first (ISO)
        note("Date and Calendar are mutable and not thread-safe; SimpleDateFormat too. Convert at the edge: date.toInstant(), Date.from(instant)");
    }
}
