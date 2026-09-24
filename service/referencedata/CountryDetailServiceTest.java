package com.ing.bankguarantees.service.referencedata;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ClientException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.cache.CountryDataCache;
import com.ing.bankguarantees.models.response.CountryDatasetResponse;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeOutput;
import com.ing.bankguarantees.remote.rest.referencedata.attributes.model.response.ReferenceDataAttributeResponse;
import com.ing.bankguarantees.remote.rest.referencedata.multilingual.model.response.ReferenceDataMultilingualResponse;
import com.ing.bankguarantees.util.MockHelper;
import com.ing.bankguarantees.utils.CacheUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CountryDetailServiceTest {

    private static final String CACHE_KEY = "countryNameTranslations";
    private static final String COUNTRY_DATASET_FILE = "BGA/CLD/country_data.json";
    private static final String COUNTRY_DATASET_INVALID_FILE = "BGA/BGD/bank_guarantee_data.json";
    private static final String FRENCH_BUSINESS_KEY = "PR";
    private static final String FRENCH_TRANSLATION = "Porto Rico";
    private static final String INVALID_BUSINESS_KEY = "PD";
    private static final Set<String> LANGUAGE_KEY_SET = Set.of("EN", "FR");
    private static final String ENGLISH_LANGUAGE_CODE = "EN";
    private static final String FRENCH_LANGUAGE_CODE = "FR";
    private static final String COUNTRY_BE = "Belgium";
    private static final String COUNTRY_PT = "Portugal";
    private static final String PT_BUSINESS_KEY = "PT";

    private CountryDetailService countryDetailService;
    private CacheManager cacheManager;
    @Mock
    private ClientGateway<Void, List<ReferenceDataMultilingualResponse.ReferenceData>, ReferenceDataMultilingualResponse> referenceDataMultilingualClientGateway;
    @Mock
    private ClientGateway<Void, ReferenceDataAttributeOutput, ReferenceDataAttributeResponse> referenceDataAttributeClientGateway;

    @BeforeEach
    void setUp() {
        cacheManager = spy(mockCacheManager());
        countryDetailService = new CountryDetailService(referenceDataMultilingualClientGateway, referenceDataAttributeClientGateway, cacheManager);
        ReflectionTestUtils.setField(countryDetailService, "cacheKey", CACHE_KEY);
    }


    @ParameterizedTest
    @MethodSource("inputLanguage")
    void getCountryDetails(Locale locale) {
        List<CountryDataCache> countryDataset = MockHelper.createCountryDataset(COUNTRY_DATASET_FILE);
        loadCountryData(countryDataset);
        CountryDatasetResponse countryDatasetResponse = countryDetailService.getCountryReferenceData(locale).join();
        CountryDataCache expectedCountryData = getCountryDataFromLanguage(countryDataset, locale.getLanguage());
        assertThat(countryDatasetResponse).isNotNull();
        assertThat(countryDatasetResponse.getCountryData()).isNotEmpty();
        assertThat(countryDatasetResponse.getCountryData().get(0).getLanguage()).isNotNull().isEqualTo(expectedCountryData.getLanguage());
        assertThat(countryDatasetResponse.getCountryData().get(0).getTranslation()).isNotNull().isEqualTo(expectedCountryData.getTranslation());
        assertThat(countryDatasetResponse.getCountryData().get(0).getBusinessKey()).isNotNull().isEqualTo(expectedCountryData.getBusinessKey());
    }

    @Test
    void getCountryNameByCode() {
        List<CountryDataCache> countryDataset = MockHelper.createCountryDataset(COUNTRY_DATASET_FILE);
        loadCountryData(countryDataset);
        assertThat(countryDetailService.getCountryNameByCode(LocaleUtils.toLocale("fr_BE"), FRENCH_BUSINESS_KEY))
                .isNotNull()
                .isEqualTo(FRENCH_TRANSLATION);
    }

    @Test
    void getCountryNameByCodeError() {
        List<CountryDataCache> countryDataset = MockHelper.createCountryDataset(COUNTRY_DATASET_FILE);
        loadCountryData(countryDataset);
        Locale frenchLocale = LocaleUtils.toLocale("fr_BE");
        CompletionException completionException = assertThrows(CompletionException.class,
                () -> countryDetailService.getCountryNameByCode(frenchLocale, INVALID_BUSINESS_KEY));
        assertThat(completionException.getMessage()).isEqualTo("com.ing.bankguarantees.error.exception.BgosException: BGOS-00-016");
    }

    @Test
    void readJsonFileError() {
        BgosException bgosException = assertThrows(BgosException.class,
                () -> MockHelper.createCountryDataset(COUNTRY_DATASET_INVALID_FILE));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }


    private CacheManager mockCacheManager() {
        CaffeineCacheManager localCacheManager = new CaffeineCacheManager();
        localCacheManager.setCaffeine(Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES));
        return localCacheManager;
    }


    private void loadCountryData(List<CountryDataCache> countryDataset) {

        for (CountryDataCache countryDataCache : countryDataset) {
            cacheManager.getCache(CACHE_KEY).put(countryDataCache.getLanguage(), List.of(countryDataCache));
        }
    }

    private CountryDataCache getCountryDataFromLanguage(List<CountryDataCache> countryDataset, String language) {

        for (CountryDataCache countryDataCache : countryDataset) {
            if (countryDataCache.getLanguage().equals(language.toUpperCase())) {
                return countryDataCache;
            }
        }
        return null;
    }

    private static Stream<Locale> inputLanguage() {

        return Stream.of(LocaleUtils.toLocale("fr_BE"), Locale.UK,
                LocaleUtils.toLocale("nl_BE"), LocaleUtils.toLocale("de_BE"));
    }

    @Test
    @DisplayName("Country multilingual Data from  client successful")
    void getCountryReferenceDataFromClientAndUpdateCacheSuccess() {

        ReferenceDataMultilingualResponse referenceDataResponse = MockHelper.getReferenceDataMLResponse();
        ReferenceDataAttributeOutput referenceDataAttributesResponse = MockHelper.getReferenceDataAttributesOutput();
        given(referenceDataMultilingualClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataResponse.getData()));
        given(referenceDataAttributeClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataAttributesResponse));
        Map<String, List<CountryDataCache>> actualRefStringListMap = countryDetailService.populateCountryDataCache().join();
        assertThat(actualRefStringListMap.keySet()).hasSize(2).containsAll(LANGUAGE_KEY_SET);
        List<CountryDataCache> countryDataCaches = CacheUtils.getFromCache(cacheManager, CACHE_KEY, ENGLISH_LANGUAGE_CODE)
                .map(cacheCountries -> (List<CountryDataCache>) cacheCountries).get();
        assertThat(countryDataCaches).isNotNull().hasSize(1);
        List<String> countryNameList = countryDataCaches.stream().map(CountryDataCache::getTranslation).toList();
        assertThat(countryNameList).containsAll(List.of(COUNTRY_BE));


    }

    @Test
    @DisplayName("Country multilingual Data with Filtered by Attributes from  client successful")
    void getCountryReferenceDataFromClientWithAttributeFilteredAndUpdateCacheSuccess() {

        ReferenceDataMultilingualResponse referenceDataResponse = MockHelper.getReferenceDataMLResponse();
        ReferenceDataAttributeOutput referenceDataAttributesResponse = MockHelper.getReferenceDataAttributesOutput();
        referenceDataAttributesResponse.setBusinessKeyList(List.of(PT_BUSINESS_KEY));
        given(referenceDataMultilingualClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataResponse.getData()));
        given(referenceDataAttributeClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataAttributesResponse));
        Map<String, List<CountryDataCache>> actualRefStringListMap = countryDetailService.populateCountryDataCache().join();
        assertThat(actualRefStringListMap.keySet()).hasSize(1).contains(FRENCH_LANGUAGE_CODE);
        List<CountryDataCache> countryDataCaches = CacheUtils.getFromCache(cacheManager, CACHE_KEY, FRENCH_LANGUAGE_CODE)
                .map(cacheCountries -> (List<CountryDataCache>) cacheCountries).get();
        assertThat(countryDataCaches).isNotNull().hasSize(1);
        List<String> countryNameList = countryDataCaches.stream().map(CountryDataCache::getTranslation).toList();
        assertThat(countryNameList).containsAll(List.of(COUNTRY_PT));
    }

    @Test
    @DisplayName("Country multilingual Data with Filtered by Attributes for empty Cache")
    void getCountryReferenceDataFromClientWithAttributeFilteredAndUpdateCacheEmpty() {

        ReferenceDataMultilingualResponse referenceDataResponse = MockHelper.getReferenceDataMLResponse();
        ReferenceDataAttributeOutput referenceDataAttributesResponse = MockHelper.getReferenceDataAttributesOutput();
        referenceDataAttributesResponse.setBusinessKeyList(List.of(PT_BUSINESS_KEY));
        given(referenceDataMultilingualClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataResponse.getData()));
        given(referenceDataAttributeClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataAttributesResponse));
        Map<String, List<CountryDataCache>> actualRefStringListMap = countryDetailService.populateCountryDataCache().join();
        assertThat(actualRefStringListMap.keySet()).hasSize(1).contains(FRENCH_LANGUAGE_CODE);
        Optional<List<CountryDataCache>> countryDataCaches = CacheUtils.getFromCache(cacheManager, CACHE_KEY, ENGLISH_LANGUAGE_CODE)
                .map(cacheCountries -> (List<CountryDataCache>) cacheCountries);
        assertThat(countryDataCaches).isNotNull().isEmpty();


    }

    @Test
    @DisplayName("Country multilingual Data from  client unsuccessful")
    void getCountryReferenceDataFromClientUnsuccessfully() {
        ReferenceDataAttributeOutput referenceDataAttributesResponse = MockHelper.getReferenceDataAttributesOutput();
        given(referenceDataAttributeClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataAttributesResponse));
        doReturn(CompletableFuture.failedFuture(new ClientException("BGOS-08-01", ErrorSource.RDA, new ErrorItem())))
                .when(referenceDataMultilingualClientGateway).performRequest(null);
        CompletableFuture<Map<String, List<CountryDataCache>>> referenceDataFuture = countryDetailService.populateCountryDataCache();
        CompletionException exception = assertThrows(CompletionException.class, referenceDataFuture::join);

        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        final ClientException cause = (ClientException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-08-01");
        verify(cacheManager, never()).getCache(any());

    }

    @Test
    @DisplayName("Empty Country multilingual Data from  client unsuccessful")
    void getEmptyCountryReferenceDataFromClient() {
        ReferenceDataAttributeOutput referenceDataAttributesResponse = MockHelper.getReferenceDataAttributesOutput();
        given(referenceDataAttributeClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataAttributesResponse));
        given(referenceDataMultilingualClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Map<String, List<CountryDataCache>>> referenceDataFuture = countryDetailService.populateCountryDataCache();
        CompletionException exception = assertThrows(CompletionException.class, referenceDataFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        final BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
        verify(cacheManager, never()).getCache(any());

    }

    @Test
    @DisplayName("Empty Country multilingual Data from  client unsuccessful")
    void getEmptyCountryAttributesReferenceDataFromClient() {
        ReferenceDataMultilingualResponse referenceDataResponse = MockHelper.getReferenceDataMLResponse();
        given(referenceDataMultilingualClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(referenceDataResponse.getData()));
        given(referenceDataAttributeClientGateway.performRequest(null)).willReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Map<String, List<CountryDataCache>>> referenceDataFuture = countryDetailService.populateCountryDataCache();
        CompletionException exception = assertThrows(CompletionException.class, referenceDataFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        final BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
        verify(cacheManager, never()).getCache(any());

    }
}
