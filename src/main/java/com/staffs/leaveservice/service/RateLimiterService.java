package com.staffs.leaveservice.service;

import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final ConcurrentMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    /**
     * Returns true if the request is allowed, false if rate limited.
     * Tracks attempts per key (e.g. IP address) within a sliding window.
     */
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = attempts.computeIfAbsent(key, k -> new LinkedList<>());

        synchronized (timestamps) {
            // Remove expired entries
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= WINDOW_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_ATTEMPTS) {
                return false;
            }

            timestamps.addLast(now);
            return true;
        }
    }
}
