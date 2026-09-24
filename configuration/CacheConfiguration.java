package com.ing.bankguarantees.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Configuration
@EnableScheduling
public class CacheConfiguration {


    @Bean
    public CacheManager cacheManager(InstantCacheProperties cacheProperties) {
        return new InstantCacheManager(cacheProperties);
    }

    public static class InstantCacheManager extends CaffeineCacheManager {
        private static final String DEFAULT = "default";
        private final Map<String, String> cacheSpecs;

        public InstantCacheManager(InstantCacheProperties cacheProperties) {
            super();
            cacheSpecs = createCacheSpecs(cacheProperties);
        }

        @Override
        @NotNull
        protected Cache<Object, Object> createNativeCaffeineCache(@NotNull String name) {
            return Caffeine.from(cacheSpecs.getOrDefault(name, cacheSpecs.get(DEFAULT))).build();
        }

        private Map<String, String> createCacheSpecs(InstantCacheProperties properties) {
            HashMap<String, String> specs = new HashMap<>();
            properties.getCaches().forEach(cache -> specs.put(cache.getName(), cache.getConfiguration()));
            specs.put(DEFAULT, properties.getDefaultConfiguration());
            return specs;
        }
    }

}


