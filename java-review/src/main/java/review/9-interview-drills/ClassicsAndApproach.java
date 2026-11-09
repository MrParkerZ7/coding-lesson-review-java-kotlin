package review.drills;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 9e. CLASSICS AND THE INTERVIEW ROUTINE
 *     Run: mvn -q compile exec:java -Dmain.class=review.drills.ClassicsAndApproach
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ClassicsAndApproach {

    /**
     * FIZZBUZZ. Problem: for 1..n give "Fizz" for multiples of 3, "Buzz" for 5, "FizzBuzz" for both, else the number.
     * Idea: test the combined case (15) FIRST, or the earlier tests swallow it.
     * Time O(n), space O(n) for the list.
     */
    static List<String> fizzBuzz(int n) {
        List<String> out = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            if (i % 15 == 0) out.add("FizzBuzz");
            else if (i % 3 == 0) out.add("Fizz");
            else if (i % 5 == 0) out.add("Buzz");
            else out.add(String.valueOf(i));
        }
        return out;
    }

    /**
     * PALINDROME NUMBER. Problem: does an int read the same backwards? No String allowed.
     * Idea: reverse only the second half of the digits and compare it with the first half (no overflow possible).
     * Negatives are never palindromes; a positive number ending in 0 cannot be one either. Time O(log10 x), space O(1).
     */
    static boolean isPalindromeNumber(int x) {
        if (x < 0 || (x % 10 == 0 && x != 0)) return false;
        int rest = x;
        int reversedHalf = 0;
        while (rest > reversedHalf) {
            reversedHalf = reversedHalf * 10 + rest % 10;
            rest /= 10;
        }
        return rest == reversedHalf || rest == reversedHalf / 10;       // the second test drops the middle digit of an odd length
    }

    /**
     * REVERSE INTEGER. Problem: reverse the digits of an int; return 0 when the result does not fit in an int.
     * Idea: pop the last digit with % 10 and push it with * 10; check BEFORE the multiplication whether it would overflow.
     * Time O(log10 x), space O(1).
     */
    static int reverseInteger(int x) {
        int rest = x;
        int reversed = 0;
        while (rest != 0) {
            int digit = rest % 10;
            rest /= 10;
            if (reversed > Integer.MAX_VALUE / 10 || (reversed == Integer.MAX_VALUE / 10 && digit > 7)) return 0;
            if (reversed < Integer.MIN_VALUE / 10 || (reversed == Integer.MIN_VALUE / 10 && digit < -8)) return 0;
            reversed = reversed * 10 + digit;
        }
        return reversed;
    }

    /**
     * GREATEST COMMON DIVISOR. Problem: the largest number that divides both a and b (a, b >= 0).
     * Idea: Euclid. gcd(a, b) = gcd(b, a % b), and gcd(a, 0) = a. Time O(log min(a, b)), space O(log) of call stack:
     * Java does not remove tail calls, so a loop is the O(1) version.
     */
    static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /**
     * IS PRIME. Problem: is n a prime (greater than 1, divisible only by 1 and itself)?
     * Idea: trial division up to the square root, odd divisors only. Written as i <= n / i to avoid the int overflow of i * i.
     * Time O(sqrt n), space O(1).
     */
    static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n % 2 == 0) return n == 2;
        for (int i = 3; i <= n / i; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    /** FIBONACCI, iterative: two variables. O(n) time, O(1) space. long holds up to fib(92). */
    static long fib(int n) {
        long a = 0;
        long b = 1;
        for (int i = 0; i < n; i++) {
            long next = a + b;
            a = b;
            b = next;
        }
        return a;
    }

    /** FIBONACCI, memoized recursion: each n is computed once and cached. O(n) time, O(n) space. */
    static long fibMemo(int n, Map<Integer, Long> memo) {
        if (n <= 1) return n;
        Long cached = memo.get(n);
        if (cached != null) return cached;
        long value = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
        memo.put(n, value);
        return value;
    }

    /** The tempting one-liner: computeIfAbsent with a recursive call. A HashMap detects that it is changed during the call and throws. */
    static long fibComputeIfAbsent(int n, Map<Integer, Long> memo) {
        if (n <= 1) return n;
        return memo.computeIfAbsent(n, k -> fibComputeIfAbsent(k - 1, memo) + fibComputeIfAbsent(k - 2, memo));
    }

    /** How many calls the plain recursion fib(n) = fib(n - 1) + fib(n - 2) makes: the cost of NOT caching. O(2^n). */
    static long fibNaiveCalls(int n) {
        return n <= 1 ? 1 : 1 + fibNaiveCalls(n - 1) + fibNaiveCalls(n - 2);
    }

    public static void main(String[] args) {
        title("CLASSICS  -  the small problems every interview starts with");

        section("FizzBuzz");
        show(() -> fizzBuzz(15));                                       // 15 is FizzBuzz, not Fizz
        show(() -> fizzBuzz(0));                                        // 1..0 is empty, so an empty list
        show(() -> fizzBuzz(1));

        section("Palindrome number (no String)");
        show(() -> isPalindromeNumber(121));
        show(() -> isPalindromeNumber(-121));                           // the minus sign makes it not a palindrome
        show(() -> isPalindromeNumber(10));                             // TRAP: reversed it would be 01
        show(() -> isPalindromeNumber(0));
        show(() -> isPalindromeNumber(12321));                          // odd length: the middle digit is dropped by / 10
        show(() -> isPalindromeNumber(1221));

        section("Reverse integer (overflow trap)");
        syntax("if (reversed > Integer.MAX_VALUE / 10 || ...) return 0;", "test BEFORE reversed * 10 + digit, afterwards it is too late");
        show(() -> reverseInteger(123));
        show(() -> reverseInteger(-123));
        show(() -> reverseInteger(120));                                // trailing zero disappears: 21
        show(() -> reverseInteger(1534236469));                         // 9646324351 does not fit in an int: 0
        show(() -> reverseInteger(Integer.MIN_VALUE));                  // -2147483648 reversed is -8463847412: out of range, so 0
        show(() -> thrown(() -> Integer.parseInt(new StringBuilder("1534236469").reverse().toString())));   // the String shortcut crashes on the same input
        show(() -> reverseInteger(0));
        int partial = 964632435;                                        // the digits reversed so far, one digit still to push
        show(() -> partial * 10 + 1);                                   // TRAP: without the check 9646324351 wraps to a wrong number, no exception

        section("GCD, LCM and primes");
        syntax("gcd(a, b) = b == 0 ? a : gcd(b, a % b)", "Euclid; Java has no tailrec, but the recursion is only O(log) deep");
        show(() -> gcd(12, 18));
        show(() -> gcd(7, 13));                                         // coprime: 1
        show(() -> gcd(0, 5));
        show(() -> gcd(5, 0));
        show(() -> 12 / gcd(12, 18) * 18);                              // lcm = a / gcd * b (divide first: a * b may overflow)
        show(() -> isPrime(97));
        show(() -> isPrime(91));                                        // 7 * 13
        show(() -> isPrime(2));                                         // the only even prime
        show(() -> isPrime(1));                                         // 1 is not prime
        show(() -> isPrime(-7));
        show(() -> isPrime(Integer.MAX_VALUE));                         // 2^31 - 1 is prime; i <= n / i keeps i * i from overflowing

        section("Fibonacci: iterative, memoized, and why not plain recursion");
        show(() -> fib(10));
        show(() -> fib(0));
        show(() -> fibMemo(50, new HashMap<>()));                       // instant, thanks to the cache
        show(() -> fib(92));                                            // the largest Fibonacci number that fits in a long
        show(() -> fib(93));                                            // TRAP: overflow wraps to a negative number
        show(() -> thrown(() -> Math.addExact(fib(92), fib(91))));      // addExact throws instead of wrapping: fib(93) does not fit
        show(() -> thrown(() -> fibComputeIfAbsent(10, new HashMap<>())));   // TRAP: computeIfAbsent must not change the map from inside
        show(() -> fibNaiveCalls(20));                                  // calls for fib(20) without a cache; the loop does 20 additions
        show(() -> fibNaiveCalls(35));                                  // almost 30 million calls for fib(35): exponential growth

        section("The interview routine");
        syntax(
                "sorted input + a pair or target", "two pointers or binary search",
                "subarray or substring with a condition", "sliding window",
                "seen before? count? duplicates?", "HashMap or HashSet",
                "matching brackets, undo, nesting", "stack (ArrayDeque)",
                "shortest path with equal steps", "BFS with a queue",
                "count the ways | best value from smaller cases", "dynamic programming",
                "top K | K-th", "heap (PriorityQueue), or bucket by count",
                "intervals", "sort by start, then sweep"
        );
        note("1 CLARIFY: input size and range, empty input, duplicates, negatives, sorted or not, one answer or all");
        note("2 EXAMPLES: walk one normal example and one edge case by hand before coding");
        note("3 BRUTE FORCE: say the obvious solution out loud with its cost (often O(n^2))");
        note("4 OPTIMIZE: name the pattern from the table above and the trade: extra space for less time");
        note("5 CODE: guard clauses first, clear names, no clever one-liners; talk while you type");
        note("6 TEST: run your example through the code; then empty, single element, duplicates, negatives, overflow");
        note("7 COMPLEXITY: always say time AND space, and what the worst case looks like");

        section("Cost");
        bigO(
                "fizzBuzz | isPalindromeNumber | reverseInteger", "O(n) | O(log10 x) | O(log10 x) time",
                "gcd (Euclid)", "O(log min(a, b)) time",
                "isPrime (trial division)", "O(sqrt n) time, O(1) space",
                "fib iterative | memoized", "O(n) time; O(1) space | O(n) space",
                "fib plain recursion", "O(2^n) time: do not use it"
        );
    }
}
