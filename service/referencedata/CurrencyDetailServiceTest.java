package com.ing.bankguarantees.service.referencedata;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.CurrencyDataSet;
import com.ing.bankguarantees.models.cache.CurrencyDataCache;
import com.ing.bankguarantees.models.response.CurrencyDatasetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CurrencyDetailServiceTest {

    private static final String CACHE_KEY = "currencyCodes";
    private static final String CURRENCY_DATASET_FILE = "BGA/CLD/currency_data.json";
    private static final String CURRENCY_BY_CODE = "mappedByCode";
    private static final String CURRENCY_AFN = "AFN";
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_AFN_VALUE = "961";
    private static final String CURRENCY_EUR_VALUE = "003";
    private static final List<String> CURRENCY_CODES = List.of("AFN", "AED");
    private static final List<String> CURRENCY_VALUES = List.of("961", "970");

    private CurrencyDetailService currencyDetailService;
    private CacheManager cacheManager;


    @BeforeEach
    void setUp() {
        cacheManager = spy(mockCacheManager());
        currencyDetailService = new CurrencyDetailService(cacheManager);
        ReflectionTestUtils.setField(currencyDetailService, "cacheKey", CACHE_KEY);
        ReflectionTestUtils.setField(currencyDetailService, "currenciesInputFilePath", CURRENCY_DATASET_FILE);
    }


    @Test
    void getCurrencyDetail() {
        CurrencyDataSet currencyDataSet = currencyDetailService.loadCurrencies();
        initializeCache(currencyDataSet);
        CurrencyDatasetResponse currencyDatasetResponse = currencyDetailService.currencies().join();
        List<CurrencyDatasetResponse.CurrencyDataResponse> currencyList = currencyDatasetResponse.getCurrencies();
        assertThat(currencyList).isNotNull().hasSize(2);
        verify(cacheManager, times(2)).getCache(any());
        List<String> currencyCodeList = currencyList.stream().map(CurrencyDatasetResponse.CurrencyDataResponse::getCode).toList();
        List<String> currencyValueList = currencyList.stream().map(CurrencyDatasetResponse.CurrencyDataResponse::getValue).toList();
        assertThat(currencyCodeList).isNotNull().containsAll(CURRENCY_CODES);
        assertThat(currencyValueList).isNotNull().containsAll(CURRENCY_VALUES);
    }

    @Test
    void getCurrencyCodeByValue() {
        CurrencyDataSet currencyDataSet = currencyDetailService.loadCurrencies();
        initializeCache(currencyDataSet);
        String currencyCode = currencyDetailService.getCurrencyCodeByValue(CURRENCY_AFN_VALUE);
        assertThat(currencyCode).isNotNull().isEqualTo(CURRENCY_AFN);
    }

    @Test
    void getCurrencyCodeByValueError() {
        CurrencyDataSet currencyDataSet = currencyDetailService.loadCurrencies();
        initializeCache(currencyDataSet);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> currencyDetailService.getCurrencyCodeByValue(CURRENCY_EUR_VALUE));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    void getCurrencyValueByCode() {
        CurrencyDataSet currencyDataSet = currencyDetailService.loadCurrencies();
        initializeCache(currencyDataSet);
        String currencyValue = currencyDetailService.getCurrencyValueByCode(CURRENCY_AFN);
        assertThat(currencyValue).isNotNull().isEqualTo(CURRENCY_AFN_VALUE);
    }

    @Test
    void getCurrencyValueByCodeError() {
        CurrencyDataSet currencyDataSet = currencyDetailService.loadCurrencies();
        initializeCache(currencyDataSet);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> currencyDetailService.getCurrencyValueByCode(CURRENCY_EUR));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    void getCurrencyInvalidCacheError() {
        CurrencyDataSet currencyDataSet = currencyDetailService.loadCurrencies();
        initializeCache(currencyDataSet);
        ReflectionTestUtils.setField(currencyDetailService, "cacheKey", "invalid");
        BgosException bgosException = assertThrows(BgosException.class,
                () -> currencyDetailService.getCurrencyValueByCode(CURRENCY_EUR));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    private CacheManager mockCacheManager() {
        CaffeineCacheManager localCacheManager = new CaffeineCacheManager();
        localCacheManager.setCaffeine(Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES));
        return localCacheManager;
    }


    private void initializeCache(CurrencyDataSet currencyDataSet) {
        Map<String, CurrencyDataCache> mappedByCodeForCaching = currencyDetailService.mapByCodeForCaching(currencyDataSet);
        cacheManager.getCache(CACHE_KEY).put(CURRENCY_BY_CODE, mappedByCodeForCaching);
    }


}
