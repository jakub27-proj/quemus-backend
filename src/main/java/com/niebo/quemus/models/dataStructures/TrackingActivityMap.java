package com.niebo.quemus.models.dataStructures;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TrackingActivityMap<K, V> {

    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, Long> lastActivity = new ConcurrentHashMap<>();

    public V put(K key, V value) {
        lastActivity.put(key, System.currentTimeMillis());
        return map.put(key, value);
    }

    public V get(K key) {
        V value = map.get(key);
        if (value != null) {
            lastActivity.put(key, System.currentTimeMillis());
        }
        return value;
    }

    public V remove(K key) {
        lastActivity.remove(key);
        return map.remove(key);
    }

    public Set<K> inactiveSince(long cutoffTime) {
        return lastActivity.entrySet().stream()
                .filter(e -> e.getValue() < cutoffTime)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}

