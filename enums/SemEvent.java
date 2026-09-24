package com.ing.bankguarantees.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SemEvent {

    /**
     * Event if legal rep is not associated with legal entity
     */
    LEGAL_REP_ORG_MISMATCH("Legal representative check", "Logged in customer is not a legal rep of instructing party."),

    /**
     * Event if basket from COA foes not match with record in storage
     */
    DOC_SIGN_CALLBACK_DATA_MISMATCH("Doc sign data check", "Information from doc sign request does not match"),

    /**
     * Event if legal entity's uuid is not found in response from permission API
     */
    NO_PERMISSION("PermissionAPI check for legal entity starting the journey", "Legal entity's uuid is not found in response from permission API."),

    /**
     * Event if required service activity is not found in response from permission API
     */
    NO_SERVICE_ACTIVITY_FOUND("Service Activity check for legal entity starting the journey", "Service Activity is not found in response from permission API."),

    /**
     * Event if Trying to sign an loan application with an invalid credential
     */
    UNAUTHORISED("Authorisation check", "User is not allowed to do this action"),

    /**
     * Event if required requestId not found in database
     */
    REQUEST_ID_NOT_FOUND("RequestId check before for fetching Bank Guarantee record.", "Invalid Request id received to fetch bank guarantee record from database.");

    private final String eventName;
    private final String message;


}