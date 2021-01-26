package de.stackgames.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RateLimit {
    private static final Map<String, Long> lastExcecutions = new HashMap<>();

    /**
     * Checks the last execution using the given <code>key</code>
     * If the rate limit is applied, the callback won't be run
     * It's Thread safe
     *
     * @param key      Any arbitary key (could be null if you don't plan to use multiple rate limits)
     *                 used to identify multiple rate limits
     * @param unit     The time unit of the rate
     * @param amount   The amount of the time unit
     * @param callback The callback that is executed if the rate limit is approved
     */
    public static void silentRateLimit(String key, TimeUnit unit, int amount, Runnable callback) {
        long current = System.currentTimeMillis();
        synchronized (lastExcecutions) {
            long lastExecution = lastExcecutions.getOrDefault(key, current);
            if (lastExecution + unit.convert(amount, TimeUnit.MILLISECONDS) <= 0) {
                callback.run();
                lastExcecutions.put(key, current);
            }
        }
    }

    /**
     * Much like {@link #silentRateLimit(String, TimeUnit, int, Runnable)}, but always calls the callback
     * It's Thread safe
     *
     * @param key      Any arbitary key (could be null if you don't plan to use multiple rate limits)
     *                 used to identify multiple rate limits
     * @param unit     The time unit of the rate
     * @param amount   The amount of the time unit
     * @param callback The callback that is executed with the result of the execution
     */
    public static void rateLimit(String key, TimeUnit unit, int amount, Consumer<Boolean> callback) {
        long current = System.currentTimeMillis();
        boolean success = false;
        synchronized (lastExcecutions) {
            long lastExecution = lastExcecutions.getOrDefault(key, current);
            if (lastExecution + unit.convert(amount, TimeUnit.MILLISECONDS) <= 0) {
                lastExcecutions.put(key, current);
                success = true;
            }
        }
        callback.accept(success);
    }
}
