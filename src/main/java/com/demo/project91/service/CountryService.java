package com.demo.project91.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;

/**
 * Interact with Ignite as key-value store (non-persistent store)
 */
@Service
@RequiredArgsConstructor
public class CountryService {

    final Ignite ignite;
    IgniteCache<String, String> cache;

    @PostConstruct
    public void postInit() {
        cache = ignite.cache("country-cache");
    }

    public void insert(String key, String value) {
        cache.put(key, value);
    }

    public String getValue(String key) {
        return cache.get(key);
    }
}
