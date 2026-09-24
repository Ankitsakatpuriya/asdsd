package com.ing.bankguarantees.database;

import com.ing.bankguarantees.database.entity.BankGuaranteeRequestEntity;
import com.ing.bankguarantees.database.repository.BankGuaranteeRequestRepository;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.saac.BankGuaranteeSemAEventsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.ing.bankguarantees.mapper.BankGuaranteeRequestMapper.BANK_GUARANTEE_REQUEST_MAPPER;
import static com.ing.bankguarantees.models.enums.SemEvent.REQUEST_ID_NOT_FOUND;

@Slf4j
@Repository
public class BankGuaranteeRequestDao {

    private final BankGuaranteeRequestRepository bankGuaranteeRequestRepository;
    private final BankGuaranteeSemAEventsHandler bankGuaranteeSemAEventsHandler;
    private final ExecutorService executorService;

    public BankGuaranteeRequestDao(BankGuaranteeRequestRepository bankGuaranteeRequestRepository,
                                   @Qualifier("workStealingPool") ExecutorService executorService,
                                   BankGuaranteeSemAEventsHandler bankGuaranteeSemAEventsHandle
    ) {
        this.bankGuaranteeRequestRepository = bankGuaranteeRequestRepository;
        this.executorService = executorService;
        this.bankGuaranteeSemAEventsHandler = bankGuaranteeSemAEventsHandle;
    }

    @Transactional
    public BankGuaranteeRequest saveBankGuaranteeRequest(BankGuaranteeRequest bankGuaranteeRequest) {
        log.info("BankGuaranteeRequestPersistentService [saveBankGuaranteeRequest] Call");
        try {
            BankGuaranteeRequestEntity savedEntity = bankGuaranteeRequestRepository.save(BANK_GUARANTEE_REQUEST_MAPPER.toEntity(bankGuaranteeRequest));
            log.info("Bank Guarantee Request Entity saved in database with request id {}", bankGuaranteeRequest.getRequestId());
            return BANK_GUARANTEE_REQUEST_MAPPER.toDomainModel(savedEntity);
        } catch (Exception ex) {
            log.error("Notifiable Database exception: Error occurred while saving BG_REQUEST for request id {} message {}",
                    bankGuaranteeRequest.getRequestId(), ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    @Transactional
    public void updateStatusByRequestId(String requestId, BankGuaranteeRequestStatus status) {
        try {
            bankGuaranteeRequestRepository.updateStatusById(requestId, status);
            log.info("Updated Bank Guarantee  for requestId {},  with status {}", requestId, status);
        } catch (Exception ex) {
            log.error(" Notifiable Database exception: Error occurred when updating status to {} for Bank Guarantee Status {} ", requestId, status);
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    public CompletableFuture<BankGuaranteeRequest> getBankGuaranteeEntity(String requestId) {
        log.info("BankGuaranteeRequestPersistentService [getBankGuaranteeEntity] Call for request id {}", requestId);
        return CompletableFuture.supplyAsync(() -> findBankGuaranteeRequestByRequestId(requestId)
                .orElseThrow(() -> {
                    log.error("Informational Database exception: Bank Guarantee  request   Entity is not found for request id {}", requestId);
                    bankGuaranteeSemAEventsHandler.publishSemEvent(REQUEST_ID_NOT_FOUND);
                    return new BgosException(ErrorCode.ENTITY_NOT_FOUND);
                }), executorService);
    }

    public Optional<BankGuaranteeRequest> findBankGuaranteeRequestByRequestId(String requestID) {
        log.info("BankGuaranteeRequestPersistentService [findBankGuaranteeRequestByRequestId] Call for request id {}", requestID);
        try {
            return bankGuaranteeRequestRepository.findByRequestId(requestID).map(BANK_GUARANTEE_REQUEST_MAPPER::toDomainModel);
        } catch (Exception ex) {
            log.error(""" 
                            Notifiable Database exception: Error occurred when fetching Bank guarantee request  with \
                            Request Id {} with exception {}
                            """,
                    requestID, ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    public CompletableFuture<List<BankGuaranteeRequest>> getBankGuaranteeEntitiesByStatusAndUpdatedAt(
            List<BankGuaranteeRequestStatus> statusList, Instant updatedAt) {
        log.info("BankGuaranteeRequestPersistentService [getBankGuaranteeEntitiesByStatusAndUpdatedBy] call for status {} and updatedAt {} ", statusList, updatedAt);
        return CompletableFuture.supplyAsync(() -> {
            List<BankGuaranteeRequestEntity> bankGuaranteeRequestEntities = bankGuaranteeRequestRepository.findByStatusInAndUpdatedAtLessThan(statusList, updatedAt)
                    .orElse(Collections.emptyList());
            return BANK_GUARANTEE_REQUEST_MAPPER.toDomainModelList(bankGuaranteeRequestEntities);
        }, executorService);
    }

    public CompletableFuture<Integer> deleteByRequestId(String requestId) {

        var deletedRecords = bankGuaranteeRequestRepository.deleteByRequestId(requestId);
        log.info("""
                BankGuaranteeRequestPersistentService [deleteByRequestId] deleting {}\
                 bank guarantee request record for request id  {}""", deletedRecords, requestId);
        return deletedRecords;


    }

    public CompletableFuture<List<BankGuaranteeRequest>> getBankGuaranteeEntitiesUpdatedBefore(Instant updatedAt) {
        log.info("BankGuaranteeRequestPersistentService [getBankGuaranteeEntitiesUpdatedBy] call for status {}", updatedAt);
        return CompletableFuture.supplyAsync(() -> {
            List<BankGuaranteeRequestEntity> bankGuaranteeRequestEntities = bankGuaranteeRequestRepository.findByUpdatedAtGreaterThan(updatedAt)
                    .orElse(Collections.emptyList());
            return BANK_GUARANTEE_REQUEST_MAPPER.toDomainModelList(bankGuaranteeRequestEntities);
        }, executorService);
    }

    public Optional<BankGuaranteeRequest> findBankGuaranteeRequestByMasterReference(String masterReferenceId) {
        try {
            log.info("Fetch Bank Guarantee request record by master reference  {}", masterReferenceId);
            return bankGuaranteeRequestRepository.findByMasterReferenceNumber(masterReferenceId).map(BANK_GUARANTEE_REQUEST_MAPPER::toDomainModel);
        } catch (Exception ex) {
            log.error(""" 
                            Notifiable Database exception: Error occurred when fetching Bank guarantee request  with \
                            master reference {} with exception {}
                            """,
                    masterReferenceId, ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }

    public CompletableFuture<BankGuaranteeRequest> getBankGuaranteeEntityByMasterReference(String masterReferenceNumber) {

        return CompletableFuture.supplyAsync(() -> findBankGuaranteeRequestByMasterReference(masterReferenceNumber)
                .orElseThrow(() -> {
                    log.error("Informational Database exception: Bank Guarantee request Entity is not found for master reference {}", masterReferenceNumber);
                    return new BgosException(ErrorCode.ENTITY_NOT_FOUND);
                }), executorService);
    }

    public CompletableFuture<List<BankGuaranteeRequest>> getBankGuaranteeEntitiesByStatus(BankGuaranteeRequestStatus status) {
        log.info("Fetch Bank Guarantee request record by status  {}", status);
        return CompletableFuture.supplyAsync(() -> {
            List<BankGuaranteeRequestEntity> bankGuaranteeRequestEntities = bankGuaranteeRequestRepository.findByStatus(status)
                    .orElse(Collections.emptyList());
            return BANK_GUARANTEE_REQUEST_MAPPER.toDomainModelList(bankGuaranteeRequestEntities);
        }, executorService);
    }
}
