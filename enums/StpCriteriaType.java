package com.ing.bankguarantees.models.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StpCriteriaType {

    LGL_REP_COUNT("Legal Representative Count Status"),
    CURRENCY_EUR("Currency EUR Status"),
    DELIVERY_MODE_EMAIL("Delivery Mode Email Status"),
    APPLICANT_UNAVAILABLE("Applicant Unavailability Status"),
    PARTY_FROM_BELGIUM("Party From Belgium Status"),
    SDS_RESPONSE("SDS Response Status"),
    BENEFICIARY_NAME_SCREENING("Beneficiary Name Screening Status"),
    APPLICANT_NAME_SCREENING("Applicant Name Screening Status"),
    INSTRUCTING_PARTY_CDD("Instructing Party CDD Status"),
    LEGAL_REP_CDD("Legal Representative CDD Status"),
    BID_BOND_NOT_OTHER("Bid Bond End Date Status"),
    GUARANTEE_SCOPE("Guarantee Type Status"),
    CREDIT_LINE_BALANCE("Credit Line Information Status"),
    GUARANTEE_STP_ALLOWED("Guarantee STP Allowed Status"),
    PROMISE_REPLACER("Promise replacing status"),
    CUSTOMER_SIGN_ALLOWED("Customer signing allowed Status"),
    SIGNER_POWER("Signer Eligibility  Status");

    private final String name;
}
