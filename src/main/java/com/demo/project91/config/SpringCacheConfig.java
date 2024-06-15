package com.demo.project91.config;

import org.apache.ignite.cache.spring.SpringCacheManager;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class SpringCacheConfig {
    @Bean
    public SpringCacheManager cacheManager() {
        SpringCacheManager cacheManager = new SpringCacheManager();
        cacheManager.setConfiguration(getSpringCacheIgniteConfiguration());
        return cacheManager;
    }

    private IgniteConfiguration getSpringCacheIgniteConfiguration() {
        return new IgniteConfiguration()
                .setIgniteInstanceName("spring-ignite-instance")
                .setMetricsLogFrequency(0);
    }
}
