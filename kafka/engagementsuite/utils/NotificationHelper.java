package com.ing.bankguarantees.remote.kafka.engagementsuite.utils;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.EmailNotificationInput;
import com.ing.bankguarantees.remote.kafka.engagementsuite.model.Source;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.ing.bankguarantees.remote.kafka.engagementsuite.utils.NotificationEventParams.*;
import static com.ing.bankguarantees.utils.ConstantUtils.ENV_PRD;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
public class NotificationHelper {

    protected static final String NOT_APPLICABLE = "N.A";
    private static final String DATE_TIME_FORMAT_DD_MM_YYYY_HH = "dd-MM-yyyy HH:mm:ss";
    private static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_DD_MM_YYYY_HH = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_DD_MM_YYYY_HH);
    public static final DateTimeFormatter DATE_TIME_FORMATTER_YYYY_MM_DD_HH = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_YYYY_MM_DD_HH);
    private static final String STP_RESULT_NAME = "name";
    private static final String STP_RESULT_TYPE = "type";
    private static final String STP_RESULT_STATUS = "status";
    private static final String STP_RESULT_TIMESTAMP = "timestamp";
    private static final String STP_RESULT_JUSTIFICATION = "justification";

    public static Map<String, String> sanitizeParams(Map<String, String> source) {
        return source.keySet().stream().collect(Collectors.toMap(k -> k,
                k -> engagementSuiteSanitize(Optional.ofNullable(source.get(k)).orElse(NOT_APPLICABLE))));
    }


    private static String engagementSuiteSanitize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replace("\n", "|") // newline, replace with pipe
                .replace("\r", "")    // carriage return, remove
                .replace("\t", " ")   // tab, replace with space
                .replace("\b", "")    // backspace, remove
                .replace("\f", "");   // formfeed, remove
    }

    public static String formatDate(LocalDate date, DateTimeFormatter dateTimeFormatter) {
        return date != null
                ? date.format(dateTimeFormatter)
                : EMPTY;
    }

    public static String formatDateTime(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormat) {
        return localDateTime != null
                ? localDateTime.format(dateTimeFormat)
                : EMPTY;
    }

    public static String formatAmount(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return amount != null ? df.format(amount) : StringUtils.EMPTY;
    }

    public static Map<String, String> prepareMetadataParam(EmailNotificationInput emailNotificationInput) {
        Map<String, String> metadataParams = new HashMap<>(10);
        BankGuaranteeRequestData bankGuaranteeRequestData = emailNotificationInput.getBankGuaranteeRequest().getBgRequest();
        metadataParams.put(APPLICATION_ID, bankGuaranteeRequestData.getRequestId());
        metadataParams.put(PROFILE_ID, emailNotificationInput.getProfileId());
        metadataParams.put(TRACE_ID, emailNotificationInput.getTraceId());
        metadataParams.put(SPAN_ID, emailNotificationInput.getSpanId());
        metadataParams.put(PARENT_ID, emailNotificationInput.getParentId());
        metadataParams.put(SESSION_ID, emailNotificationInput.getSessionId());
        metadataParams.put(LOCALE, bankGuaranteeRequestData.getTranslationLanguage().toLanguageTag());
        return NotificationHelper.sanitizeParams(metadataParams);
    }

    public static void prepareStpResultSet(Map<String, String> attachmentParams, BankGuaranteeRequest bankGuaranteeRequest) {
        bankGuaranteeRequest.getBgRequest().getStpResultDataSet().getStpResults()
                .forEach(stpResultData -> {
                    if (stpResultData.getType() != StpCriteriaType.LEGAL_REP_CDD) {
                        String prefix = stpResultData.getType().name().toLowerCase().trim();
                        attachmentParams.put(String.join("_", prefix, STP_RESULT_NAME), stpResultData.getType().getName());
                        attachmentParams.put(String.join("_", prefix, STP_RESULT_TYPE), stpResultData.getType().name());
                        attachmentParams.put(String.join("_", prefix, STP_RESULT_STATUS), String.valueOf(stpResultData.isStpPossible()));
                        attachmentParams.put(String.join("_", prefix, STP_RESULT_TIMESTAMP), formatDateTime(stpResultData.getTimestamp(), DATE_TIME_FORMATTER_DD_MM_YYYY_HH));
                        attachmentParams.put(String.join("_", prefix, STP_RESULT_JUSTIFICATION), stpResultData.getJustification());
                        attachmentParams.put(String.join("_", prefix, "result"), stpResultData.isStpPossible() ? "OK" : "NOK");
                    }
                });
    }

    public static List<String> getCCAddress(List<String> defaultCcAddresses, String environment) {
        if (Strings.CS.equals(environment, ENV_PRD))
            return Collections.emptyList();
        return defaultCcAddresses;
    }

    public static Source getSource() {
        try {
            return Source.builder()
                    .instanceId(InetAddress.getLocalHost().getHostAddress())
                    .hostname(InetAddress.getLocalHost().getHostName())
                    .build();
        } catch (UnknownHostException ex) {
            log.error("Unknown Host found :{}", ex.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, ex);
        }
    }
}
