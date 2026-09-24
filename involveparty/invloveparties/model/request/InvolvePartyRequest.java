package com.ing.bankguarantees.remote.rest.involveparty.invloveparties.model.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvolvePartyRequest {

    private boolean individual;
    private String uuid;

}
