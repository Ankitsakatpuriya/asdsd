package com.ing.bankguarantees.schedulers;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ing.bankguarantees.models.cache.CurrencyDataCache;
import com.ing.bankguarantees.service.referencedata.CurrencyDetailService;
import com.ing.bankguarantees.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@Slf4j
class CurrencyDataInitializerTest {

    private static final String CACHE_KEY = "currencyCodes";
    private static final String CURRENCY_DATASET_FILE = "BGA/CLD/currency_data.json";
    private static final String CURRENCY_BY_CODE = "mappedByCode";
    private static final String CURRENCY_AFN = "AFN";
    private static final String CURRENCY_AED = "AED";
    private static final String CURRENCY_AFN_VALUE = "961";
    private static final String CURRENCY_AED_VALUE = "970";


    private CacheManager cacheManager;
    private CurrencyDataInitializer currencyDataInitializer;


    @BeforeEach
    void setUp() {
        cacheManager = spy(mockCacheManager());
        CurrencyDetailService currencyDetailService = new CurrencyDetailService(cacheManager);
        ReflectionTestUtils.setField(currencyDetailService, "cacheKey", CACHE_KEY);
        ReflectionTestUtils.setField(currencyDetailService, "currenciesInputFilePath", CURRENCY_DATASET_FILE);
        currencyDataInitializer = new CurrencyDataInitializer(cacheManager, currencyDetailService);
        ReflectionTestUtils.setField(currencyDataInitializer, "cacheKey", CACHE_KEY);
    }

    @Test
    @DisplayName("Populate Currency Cache Data ")
    void populateCurrencyDataCache() {
        currencyDataInitializer.populateCurrencyDataCache();
        Map<String, CurrencyDataCache> currencyDataCacheMap = CacheUtils.getFromCache(cacheManager, CACHE_KEY, CURRENCY_BY_CODE)
                .map(cacheCountries -> (Map<String, CurrencyDataCache>) cacheCountries).get();

        assertThat(currencyDataCacheMap).isNotNull().hasSize(2);
        assertThat(currencyDataCacheMap.get(CURRENCY_AFN)).isNotNull();
        assertThat(currencyDataCacheMap.get(CURRENCY_AFN).getValueEN()).isNotNull().isEqualTo(CURRENCY_AFN_VALUE);
        assertThat(currencyDataCacheMap.get(CURRENCY_AED)).isNotNull();
        assertThat(currencyDataCacheMap.get(CURRENCY_AED).getValueEN()).isNotNull().isEqualTo(CURRENCY_AED_VALUE);


    }


    private CacheManager mockCacheManager() {
        CaffeineCacheManager localCacheManager = new CaffeineCacheManager();
        localCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES));
        return localCacheManager;
    }


}
