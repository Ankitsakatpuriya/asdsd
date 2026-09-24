package com.ing.bankguarantees.remote.kafka.datalakeevent.utils;

import com.ing.bankguarantees.avro.EventName;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.models.domain.BankGuaranteeRequestData;
import com.ing.bankguarantees.models.enums.BankGuaranteeRequestStatus;
import com.ing.bankguarantees.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;

import static com.ing.bankguarantees.error.exception.ErrorCode.TECHNICAL_ERROR;

@Slf4j
public class DataLakeUtils {

    public static EventName getDataLakeEventName(BankGuaranteeRequestStatus status) {
        return EventName.valueOf(status.name());
    }

    public static Object getGuaranteeDetailsDataAvro(BankGuaranteeRequestData bankGuaranteeRequestData) {
        return JsonUtils.convert(bankGuaranteeRequestData.getGuaranteeDetails().getBankGuarantee(),
                bankGuaranteeRequestData.getGuaranteeDetails().getBgCode().getAvroClassType());
    }

    public static String getDataLakeId(String primaryKey, String delimiter, int count) {
        return String.format("%s-%s-%s", primaryKey, delimiter, count);
    }

    public static final Supplier<String> Host = () -> {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException exception) {
            log.error("host address not found {}", exception.getMessage());
            throw new BgosException(TECHNICAL_ERROR, exception);
        }
    };
}
