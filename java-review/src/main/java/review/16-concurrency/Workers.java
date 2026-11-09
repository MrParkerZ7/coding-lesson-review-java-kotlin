package review.concurrency;

import java.util.ArrayList;
import java.util.List;

/** Small helpers shared by the files of this lesson (no main): run a body on several threads and wait for them. */
public final class Workers {

    private Workers() {
    }

    /** A body that may be interrupted while it waits (sleep, await, take, put ...). */
    @FunctionalInterface
    public interface Interruptible {
        void run() throws InterruptedException;
    }

    /** Turns an Interruptible into a Runnable: on interrupt it restores the flag and stops quietly. */
    public static Runnable quietly(Interruptible body) {
        return () -> {
            try {
                body.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    /** Starts `threads` threads that each run `body`, then waits until every one of them has finished. */
    public static void runAll(int threads, Runnable body) {
        List<Thread> started = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(body);
            started.add(t);
            t.start();
        }
        joinAll(started);
    }

    /** Starts `threads` threads that each run `operation` `perThread` times, then waits for all of them. */
    public static void hammer(int threads, int perThread, Runnable operation) {
        runAll(threads, () -> {
            for (int n = 0; n < perThread; n++) operation.run();
        });
    }

    /** Waits for every thread; join also makes everything they wrote visible to the caller. */
    public static void joinAll(List<Thread> threads) {
        try {
            for (Thread t : threads) t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
