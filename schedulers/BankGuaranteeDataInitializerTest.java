package com.ing.bankguarantees.schedulers;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ing.bankguarantees.models.BankGuaranteeDataSet;
import com.ing.bankguarantees.models.BankGuaranteeDataSet.BankGuaranteeData;
import com.ing.bankguarantees.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@Slf4j
class BankGuaranteeDataInitializerTest {

    private static final String CACHE_KEY = "bankGuaranteeGeneralInfo";
    private static final String BANK_GUARANTEE_DATASET_KEY = "bankGuaranteeDataSet";
    private static final String BANK_GUARANTEE_FILE = "BGA/BGD/bank_guarantee_data.json";
    private static final String IN_SCOPE_GUARANTEE_TYPE = "Performance";
    private static final String IN_SCOPE_GUARANTEE_SUBTYPE = "Public Contract";
    private static final String OUT_SCOPE_GUARANTEE_TYPE = "OVAM";
    private static final String OUT_SCOPE_GUARANTEE_SUBTYPE = "OVAM -  Soil decontamination";
    private static final String OUT_SCOPE_GUARANTEE_TYPE_NUMBER = "133";

    private CacheManager cacheManager;
    private BankGuaranteeDataInitializer bankGuaranteeDataInitializer;


    @BeforeEach
    void setUp() {
        cacheManager = spy(mockCacheManager());
        bankGuaranteeDataInitializer = new BankGuaranteeDataInitializer(cacheManager);
        ReflectionTestUtils.setField(bankGuaranteeDataInitializer, "cacheKey", CACHE_KEY);
        ReflectionTestUtils.setField(bankGuaranteeDataInitializer, "bgDetailsInputFilePath", BANK_GUARANTEE_FILE);
    }

    @Test
    @DisplayName("Populate Currency Cache Data ")
    void populateCurrencyDataCache() {
        bankGuaranteeDataInitializer.populateBankGuaranteeMetadataDataCache();
        BankGuaranteeDataSet bankGuaranteeDataSet = CacheUtils.getFromCache(cacheManager, CACHE_KEY, BANK_GUARANTEE_DATASET_KEY)
                .map(BankGuaranteeDataSet.class::cast).get();

        assertThat(bankGuaranteeDataSet).isNotNull();
        Optional<BankGuaranteeData> inscopeOptional = bankGuaranteeDataSet.getBankGuarantees().stream()
                .filter(BankGuaranteeData::isInScope).findFirst();
        Optional<BankGuaranteeData> outscopeOptional = bankGuaranteeDataSet.getBankGuarantees().stream()
                .filter(bankGuaranteeData -> !bankGuaranteeData.isInScope()).findFirst();
        assertThat(inscopeOptional).isPresent();
        assertThat(outscopeOptional).isPresent();
        assertThat(inscopeOptional.get().isInScope()).isTrue();
        assertThat(inscopeOptional.get().getType()).isEqualTo(IN_SCOPE_GUARANTEE_TYPE);
        assertThat(inscopeOptional.get().getSubType()).isEqualTo(IN_SCOPE_GUARANTEE_SUBTYPE);
        assertThat(inscopeOptional.get().getTypeNumber()).isBlank();
        assertThat(outscopeOptional.get().getType()).isEqualTo(OUT_SCOPE_GUARANTEE_TYPE);
        assertThat(outscopeOptional.get().getSubType()).isEqualTo(OUT_SCOPE_GUARANTEE_SUBTYPE);
        assertThat(outscopeOptional.get().getTypeNumber()).isEqualTo(OUT_SCOPE_GUARANTEE_TYPE_NUMBER);

    }


    private CacheManager mockCacheManager() {
        CaffeineCacheManager localCacheManager = new CaffeineCacheManager();
        localCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES));
        return localCacheManager;
    }


}
