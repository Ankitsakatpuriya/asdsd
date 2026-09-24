package com.ing.bankguarantees.remote.kafka.datalakeevent.model;

import com.ing.bankguarantees.models.domain.BankGuaranteeRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BgRequestDataLakeEventDto extends DataLakeEventDto {
    private BankGuaranteeRequest bankGuaranteeRequest;
}


