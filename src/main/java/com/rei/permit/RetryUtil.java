package com.rei.permit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for handling retries with exponential backoff.
 * This class provides methods to retry operations that might fail temporarily.
 */
public class RetryUtil {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);
    
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_INITIAL_DELAY_MS = 1000; // 1 second
    private static final long DEFAULT_MAX_DELAY_MS = 10000; // 10 seconds

    /**
     * Retries an operation with exponential backoff.
     *
     * @param operation The operation to retry
     * @param <T> The return type of the operation
     * @return The result of the operation if successful
     * @throws RuntimeException if all retries fail
     */
    public static <T> T retry(Supplier<T> operation) {
        return retry(operation, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY_MS, DEFAULT_MAX_DELAY_MS);
    }

    /**
     * Retries an operation with exponential backoff and custom retry parameters.
     *
     * @param operation The operation to retry
     * @param maxRetries Maximum number of retry attempts
     * @param initialDelayMs Initial delay between retries in milliseconds
     * @param maxDelayMs Maximum delay between retries in milliseconds
     * @param <T> The return type of the operation
     * @return The result of the operation if successful
     * @throws RuntimeException if all retries fail
     */
    public static <T> T retry(Supplier<T> operation, int maxRetries, long initialDelayMs, long maxDelayMs) {
        int retries = 0;
        long delay = initialDelayMs;

        while (true) {
            try {
                return operation.get();
            } catch (Exception e) {
                retries++;
                if (retries > maxRetries) {
                    logger.error("Operation failed after {} retries", maxRetries, e);
                    throw new RuntimeException("Operation failed after " + maxRetries + " retries", e);
                }

                logger.warn("Operation failed, retrying in {} ms (attempt {}/{})", delay, retries, maxRetries);
                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }

                // Exponential backoff with jitter
                delay = Math.min(delay * 2, maxDelayMs);
            }
        }
    }
} 