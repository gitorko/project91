package com.demo.project91.config;

import org.apache.ignite.cache.spring.SpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SpringCacheManager cacheManager = new SpringCacheManager();
        cacheManager.setIgniteInstanceName("testIgniteInstance");
        return cacheManager;
    }
}
