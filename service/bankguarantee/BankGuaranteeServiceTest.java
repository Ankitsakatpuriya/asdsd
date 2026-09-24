package com.ing.bankguarantees.service.bankguarantee;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.BankGuaranteeDataSet;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.domain.GuaranteeDetailsData;
import com.ing.bankguarantees.models.enums.BankGuaranteeCode;
import com.ing.bankguarantees.models.guaranteetype.BaseGuaranteeType;
import com.ing.bankguarantees.models.response.BankGuaranteeCodeSetResponse;
import com.ing.bankguarantees.schedulers.BankGuaranteeDataInitializer;
import com.ing.bankguarantees.service.notification.NotificationService;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class BankGuaranteeServiceTest {

    private static final String BANK_GUARANTEE_META_KEY = "bankGuaranteeGeneralInfo";
    private static final String BANK_GUARANTEE_DATASET_KEY = "bankGuaranteeDataSet";
    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private static final String BG_METADATA_FILE = "bankguarantee/bank_guarantee_data.json";

    private BankGuaranteeService bankGuaranteeService;
    private BankGuaranteeDataInitializer bankGuaranteeDataInitializer;
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = spy(mockCacheManager());
        bankGuaranteeService = new BankGuaranteeService(cacheManager);
        bankGuaranteeDataInitializer = new BankGuaranteeDataInitializer(cacheManager);
        ReflectionTestUtils.setField(bankGuaranteeService, "cacheKey", BANK_GUARANTEE_META_KEY);
        ReflectionTestUtils.setField(bankGuaranteeDataInitializer, "cacheKey", BANK_GUARANTEE_META_KEY);
        ReflectionTestUtils.setField(bankGuaranteeDataInitializer, "bgDetailsInputFilePath", BG_METADATA_FILE);
    }

    private CacheManager mockCacheManager() {
        CaffeineCacheManager localCacheManager = new CaffeineCacheManager();
        localCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES));
        return localCacheManager;
    }


    @Test
    void getBankGuaranteeCodes() {
        BankGuaranteeCodeSetResponse bankGuaranteeCodeSetResponse = MockHelper.getBankGuaranteeCodeSetResponse();
        BankGuaranteeCodeSetResponse bankGuaranteeCodeSetResponse1 = bankGuaranteeService.getBankGuaranteeCodes().join();
        assertThat(bankGuaranteeCodeSetResponse1).isNotNull().isEqualTo(bankGuaranteeCodeSetResponse);
    }

    @Test
    void getMatchedBankGuaranteeDataNegative() {
        mockDataInCache();
        BgosException exception = Assertions.assertThrows(BgosException.class,
                () -> bankGuaranteeService.getMatchedBankGuaranteeData(BankGuaranteeCode.CUSTOM_1));
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("BGOS-00-015");
    }

    @Test
    void getMatchedBankGuaranteeDataInvalidCache() {
        BgosException exception = Assertions.assertThrows(BgosException.class,
                () -> bankGuaranteeService.getMatchedBankGuaranteeData(BankGuaranteeCode.CUSTOM_1));
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("BGOS-00-001");
    }

    @ParameterizedTest
    @MethodSource("provideBankGuaranteeCodes")
    void getBgSubTypePositive(BankGuaranteeCode bgCode, String subType) {
        bankGuaranteeDataInitializer.populateBankGuaranteeMetadataDataCache();
        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        GuaranteeDetailsData<BaseGuaranteeType> guaranteeDetailsData = MockHelper.getGuaranteeDetailsData(bgCode);
        bankGuaranteeRequestData.setGuaranteeDetails(guaranteeDetailsData);
        String bgSubType = bankGuaranteeService.getBgSubType(bankGuaranteeRequestData);
        assertThat(bgSubType).isNotNull().isEqualTo(subType);

    }

    private static Stream<Arguments> provideBankGuaranteeCodes() {
        return Stream.of(
                Arguments.of(BankGuaranteeCode.DCK_CDC, "DCK/CDC"),
                Arguments.of(BankGuaranteeCode.WOODS_BG_PRIVATE, "Forestry woods Private-Wallonia"),
                Arguments.of(BankGuaranteeCode.WOODS_BG_DISCHARGE, "Forestry Wallonia - Remaining Amount"),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_VLA, "Standard Promise - Flanders"),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_B, "Blank Promise - Wallonia"),
                Arguments.of(BankGuaranteeCode.WOODS_PROM_A, "Standard Promise - Wallonia"),
                Arguments.of(BankGuaranteeCode.WOODS_BGVLA_PUBLIC, "Forestry Flanders"),
                Arguments.of(BankGuaranteeCode.WOODS_BGWAL_PUBLIC, "Forestry Wallonia with or without cash"),
                Arguments.of(BankGuaranteeCode.ABSTRACT_PROM, "Promise - Call for tender"),
                Arguments.of(BankGuaranteeCode.ADVANCE_PAYMENT, "Advance Payment"),
                Arguments.of(BankGuaranteeCode.PUBLIC_CONTRACT_PROM, "Promise - Call for public contract"),
                Arguments.of(BankGuaranteeCode.STATE_LOTTERY, "State Lottery"),
                Arguments.of(BankGuaranteeCode.RENTAL, "Rental"),
                Arguments.of(BankGuaranteeCode.BID_BOND, "Bid Bond"),
                Arguments.of(BankGuaranteeCode.REAL_ESTATE, "Real Estate"),
                Arguments.of(BankGuaranteeCode.PERFORMANCE_BOND, "Performance Bond"),
                Arguments.of(BankGuaranteeCode.PAYMENT_GUARANTEE, "Payment"),
                Arguments.of(BankGuaranteeCode.MONEY_RETENTION_BOND, "Money Retention Bond"),
                Arguments.of(BankGuaranteeCode.CUSTOMIZED_TEXT, "Customize Text - Rental")
        );
    }

    private void mockDataInCache() {
        Optional.ofNullable(cacheManager.getCache(BANK_GUARANTEE_META_KEY))
                .ifPresent(cache -> {
                    BankGuaranteeDataSet bankGuaranteeDataSet = BankGuaranteeDataSet.builder()
                            .bankGuarantees(new ArrayList<>())
                            .build();
                    cache.put(BANK_GUARANTEE_DATASET_KEY, bankGuaranteeDataSet);
                });
    }
}
