package com.ing.bankguarantees.database;

import com.ing.bankguarantees.database.entity.ReportingEntity;
import com.ing.bankguarantees.models.domain.Reporting;
import com.ing.bankguarantees.database.repository.ReportingRepository;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.error.exception.BgosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.ing.bankguarantees.mapper.ReportingMapper.REPORTING_MAPPER;
import static com.ing.bankguarantees.error.exception.ErrorCode.TECHNICAL_ERROR;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReportingDao {

    private final ReportingRepository reportingRepository;

    @Transactional
    public Reporting saveReportingEntity(Reporting reporting) {
        log.info("ReportingPersistentService [saveBankGuaranteeRequest] Call");
        try {
            ReportingEntity savedEntity = reportingRepository.save(REPORTING_MAPPER.toEntity(reporting));
            log.info("Reporting  Entity saved in database with request id {}", savedEntity.getRequestId());
            return REPORTING_MAPPER.toDomainModelList(savedEntity);
        } catch (Exception ex) {
            log.error("Notifiable Database exception: Error occurred while saving BG_REQUEST for request id {} message {}",
                    reporting.getRequestId(), ex.getMessage());
            throw new BgosException(TECHNICAL_ERROR, ex);
        }
    }

    @Transactional
    public void updateStatusByRequestId(String requestId, BankGuaranteeRequestStatus status) {
        try {
            reportingRepository.updateStatusByRequestId(requestId, status);
            log.info("Updated Reporting  for requestId {},  with status {}", requestId, status);
        } catch (Exception ex) {
            log.error(" Notifiable Database exception: Error occurred when updating Reporting status to {} for request id  {} ",
                    status, requestId);
            throw new BgosException(TECHNICAL_ERROR, ex);
        }
    }

    @Transactional
    public void updateStatusAndStpByRequestId(String requestId, BankGuaranteeRequestStatus status, String stp) {
        try {
            reportingRepository.updateStatusAndStpByRequestId(requestId, status, stp);
            log.info("Updated Reporting  for requestId {},  with status {} and stp {}", requestId, status, stp);
        } catch (Exception ex) {
            log.error(" Notifiable Database exception: Error occurred when updating Reporting status to {} and stp {} for request id  {} ",
                    status, stp, requestId);
            throw new BgosException(TECHNICAL_ERROR, ex);
        }
    }
}
