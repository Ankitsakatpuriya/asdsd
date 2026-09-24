package com.ing.bankguarantees.models.domain;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ing.bankguarantees.models.enums.StpCriteriaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ing.bankguarantees.utils.ConstantUtils.DATE_TIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StpResultDataSet {

    private static final List<StpCriteriaType> EXCLUDE_STP_TYPE_LIST = List.of(StpCriteriaType.CREDIT_LINE_BALANCE, StpCriteriaType.SDS_RESPONSE);
    private List<STPResultData> stpResults;


    @JsonIgnore
    public Optional<STPResultData> getStpResultByType(StpCriteriaType type) {

        return stpResults.stream()
                .filter(stpResultData -> type.equals(stpResultData.getType()))
                .findFirst();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class STPResultData {

        private String name;
        private StpCriteriaType type;
        private boolean stpPossible;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime timestamp;

        private String justification;
    }

}
