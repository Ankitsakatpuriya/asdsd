package com.ing.bankguarantees.database;

import com.ing.bankguarantees.configuration.testsupport.ExecutorConfig;
import com.ing.bankguarantees.database.entity.BankGuaranteeRequestEntity;
import com.ing.bankguarantees.database.repository.BankGuaranteeRequestRepository;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.saac.BankGuaranteeSemAEventsHandler;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static com.ing.bankguarantees.mapper.BankGuaranteeRequestMapper.BANK_GUARANTEE_REQUEST_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankGuaranteeRequestDaoTest {

    private static final String BG_REQUEST_DATA_FILE = "BGA/BGR/bg_request_data.json";
    private final static String ID = "82c2d18e-3609-4514-9542-28e29c7c2c02";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    private static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    private static final String UUID_INDV = "b59f13b6-0fee-468a-8c7f-c1df1a2b5452";

    @Mock
    private BankGuaranteeRequestRepository bankGuaranteeRequestRepository;

    @Mock
    private BankGuaranteeSemAEventsHandler bankGuaranteeSemAEventsHandler;

    private BankGuaranteeRequestDao bankGuaranteeRequestDao;


    @BeforeEach
    void setUp() {
        ExecutorService executorService = ExecutorConfig.workStealingPool();
        bankGuaranteeRequestDao = new BankGuaranteeRequestDao(bankGuaranteeRequestRepository, executorService, bankGuaranteeSemAEventsHandler);
    }

    @Test
    public void saveBankGuaranteeRequestPositive() {

        BankGuaranteeRequestData bankGuaranteeRequestData = MockHelper.createBankGuaranteeRequestData(BG_REQUEST_DATA_FILE);
        when(bankGuaranteeRequestRepository.save(any(BankGuaranteeRequestEntity.class))).thenAnswer(invocationOnMock -> {
            BankGuaranteeRequestEntity savedEntity = invocationOnMock.getArgument(0);
            savedEntity.setId(ID);
            return savedEntity;
        });
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        bankGuaranteeRequest.setBgRequest(bankGuaranteeRequestData);
        BankGuaranteeRequest entity = bankGuaranteeRequestDao.saveBankGuaranteeRequest(bankGuaranteeRequest);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getStatus()).isEqualTo(BankGuaranteeRequestStatus.DRAFT);
        assertThat(entity.getCreatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(entity.getUpdatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(entity.getIndividualId()).isEqualTo(UUID_INDV);
        assertThat(entity.getOrganisationId()).isEqualTo(UUID_ORG);
        assertThat(entity.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(entity.getRequestId()).isNotEmpty();
        assertThat(entity.getBgRequest()).isEqualTo(bankGuaranteeRequest.getBgRequest());
    }

    @Test
    public void saveBankGuaranteeRequestError() {

        when(bankGuaranteeRequestRepository.save(any(BankGuaranteeRequestEntity.class))).thenThrow(RuntimeException.class);
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> bankGuaranteeRequestDao.saveBankGuaranteeRequest(bankGuaranteeRequest));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    public void updateStatusByRequestIdPositive() {

        String requestId = UUID.randomUUID().toString();
        doNothing().when(bankGuaranteeRequestRepository).updateStatusById(requestId, BankGuaranteeRequestStatus.DRAFT);
        bankGuaranteeRequestDao.updateStatusByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT);
        verify(bankGuaranteeRequestRepository, times(1)).updateStatusById(requestId, BankGuaranteeRequestStatus.DRAFT);

    }

    @Test
    public void updateStatusByRequestIdError() {
        String requestId = UUID.randomUUID().toString();

        doThrow(RuntimeException.class).when(bankGuaranteeRequestRepository).updateStatusById(any(), any());
        BgosException bgosException = assertThrows(BgosException.class,
                () -> bankGuaranteeRequestDao.updateStatusByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);

    }

    @Test
    public void getBankGuaranteeEntityPositive() {
        String requestId = UUID.randomUUID().toString();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        BankGuaranteeRequestEntity entity = BANK_GUARANTEE_REQUEST_MAPPER.toEntity(bankGuaranteeRequest);
        given(bankGuaranteeRequestRepository.findByRequestId(requestId)).willReturn(Optional.of(entity));
        BankGuaranteeRequest actual = bankGuaranteeRequestDao.getBankGuaranteeEntity(requestId).join();
        assertThat(actual).isNotNull().isEqualTo(bankGuaranteeRequest);
    }

    @Test
    public void getBankGuaranteeEntityError() {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<BankGuaranteeRequest> entityFuture = bankGuaranteeRequestDao.getBankGuaranteeEntity(requestId);
        CompletionException exception = assertThrows(CompletionException.class, entityFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-007");
    }

    @Test
    public void findBankGuaranteeRequestByRequestIdError() {
        String requestId = UUID.randomUUID().toString();
        given(bankGuaranteeRequestRepository.findByRequestId(requestId)).willThrow(RuntimeException.class);
        CompletableFuture<BankGuaranteeRequest> entityFuture = bankGuaranteeRequestDao.getBankGuaranteeEntity(requestId);
        CompletionException exception = assertThrows(CompletionException.class, entityFuture::join);
        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNotNull();
        BgosException cause = (BgosException) exception.getCause();
        assertThat(cause.getMessage()).isEqualTo("BGOS-00-001");
    }

    @Test
    public void getBankGuaranteeEntitiesByStatusAndUpdatedByPositive() {
        List<BankGuaranteeRequestStatus> statuses = List.of(BankGuaranteeRequestStatus.DRAFT, BankGuaranteeRequestStatus.CREATED);
        Instant now = Instant.now();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        List<BankGuaranteeRequestEntity> bgEntityList = List.of(BANK_GUARANTEE_REQUEST_MAPPER.toEntity(bankGuaranteeRequest));
        given(bankGuaranteeRequestRepository.findByStatusInAndUpdatedAtLessThan(statuses, now)).willReturn(Optional.of(bgEntityList));
        List<BankGuaranteeRequest> expectedList = bankGuaranteeRequestDao.getBankGuaranteeEntitiesByStatusAndUpdatedAt(statuses, now).join();
        assertThat(expectedList).isNotEmpty();
        assertThat(expectedList).isEqualTo(List.of(bankGuaranteeRequest));
    }

    @Test
    public void getBankGuaranteeEntitiesUpdatedBeforePositive() {
        List<BankGuaranteeRequestStatus> statuses = List.of(BankGuaranteeRequestStatus.DRAFT, BankGuaranteeRequestStatus.CREATED);
        Instant now = Instant.now();
        BankGuaranteeRequest bankGuaranteeRequest = MockHelper.getBankGuaranteeRequest(UUID_ORG, UUID_INDV, SESSION_ID, REQUESTER_ID);
        List<BankGuaranteeRequestEntity> bgEntityList = List.of(BANK_GUARANTEE_REQUEST_MAPPER.toEntity(bankGuaranteeRequest));
        given(bankGuaranteeRequestRepository.findByUpdatedAtGreaterThan(now)).willReturn(Optional.of(bgEntityList));
        List<BankGuaranteeRequest> expectedList = bankGuaranteeRequestDao.getBankGuaranteeEntitiesUpdatedBefore(now).join();
        assertThat(expectedList).isNotEmpty();
        assertThat(expectedList).isEqualTo(List.of(bankGuaranteeRequest));
    }

    @Test
    public void getBankGuaranteeEntitiesByStatusAndUpdatedByEmptyList() {
        List<BankGuaranteeRequestStatus> statuses = List.of(BankGuaranteeRequestStatus.DRAFT, BankGuaranteeRequestStatus.CREATED);
        Instant now = Instant.now();
        List<BankGuaranteeRequest> expectedList = bankGuaranteeRequestDao.getBankGuaranteeEntitiesByStatusAndUpdatedAt(statuses, now).join();
        assertThat(expectedList).isEmpty();
    }

    @Test
    public void deleteByRequestId() {
        String requestId = UUID.randomUUID().toString();
        given(bankGuaranteeRequestRepository.deleteByRequestId(requestId)).willReturn(CompletableFuture.completedFuture(2));
        bankGuaranteeRequestDao.deleteByRequestId(requestId);
        verify(bankGuaranteeRequestRepository, times(1)).deleteByRequestId(requestId);
    }

}
