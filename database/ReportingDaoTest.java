package com.ing.bankguarantees.database;

import com.ing.bankguarantees.database.entity.ReportingEntity;
import com.ing.bankguarantees.database.repository.ReportingRepository;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.Reporting;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.util.MockHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingDaoTest {

    private final static String ID = "82c2d18e-3609-4514-9542-28e29c7c2c02";
    public static final String REQUESTER_ID = "e22274af-5eea-44c4-88fd-a76b1b3a0000";
    public static final String UUID_ORG = "e1fb9416-c1b7-4ff1-966e-4fc2f52b9ec3";
    public static final String STP = "YES";

    @Mock
    private ReportingRepository reportingRepository;

    @InjectMocks
    private ReportingDao reportingDao;


    @Test
    public void saveReportingEntityPositive() {

        when(reportingRepository.save(any(ReportingEntity.class))).thenAnswer(invocationOnMock -> {
            ReportingEntity savedEntity = invocationOnMock.getArgument(0);
            savedEntity.setId(ID);
            return savedEntity;
        });
        Reporting reporting = MockHelper.getReporting(REQUESTER_ID);
        Reporting entity = reportingDao.saveReportingEntity(reporting);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getStatus()).isEqualTo(BankGuaranteeRequestStatus.DRAFT);
        assertThat(entity.getCreatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(entity.getUpdatedBy()).isEqualTo(REQUESTER_ID);
        assertThat(entity.getRequestId()).isNotEmpty();
    }

    @Test
    public void saveBankGuaranteeRequestError() {

        when(reportingRepository.save(any(ReportingEntity.class))).thenThrow(RuntimeException.class);
        Reporting reporting = MockHelper.getReporting(REQUESTER_ID);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> reportingDao.saveReportingEntity(reporting));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);
    }

    @Test
    public void updateStatusByRequestIdPositive() {
        String requestId = UUID.randomUUID().toString();
        doNothing().when(reportingRepository).updateStatusByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT);
        reportingDao.updateStatusByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT);
        verify(reportingRepository, times(1)).updateStatusByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT);
    }

    @Test
    public void updateStatusByRequestIdError() {
        String requestId = UUID.randomUUID().toString();
        doThrow(RuntimeException.class).when(reportingRepository).updateStatusByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> reportingDao.updateStatusByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);

    }

    @Test
    public void updateStatusAndStpByRequestIdPositive() {
        String requestId = UUID.randomUUID().toString();
        doNothing().when(reportingRepository).updateStatusAndStpByRequestId(requestId, BankGuaranteeRequestStatus.FULFILLED, STP);
        reportingDao.updateStatusAndStpByRequestId(requestId, BankGuaranteeRequestStatus.FULFILLED, STP);
        verify(reportingRepository, times(1)).updateStatusAndStpByRequestId(requestId, BankGuaranteeRequestStatus.FULFILLED, STP);
    }

    @Test
    public void updateStatusAndStpByRequestIdError() {
        String requestId = UUID.randomUUID().toString();
        doThrow(RuntimeException.class).when(reportingRepository).updateStatusAndStpByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT,STP);
        BgosException bgosException = assertThrows(BgosException.class,
                () -> reportingDao.updateStatusAndStpByRequestId(requestId, BankGuaranteeRequestStatus.DRAFT, STP));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);

    }

}
