package com.ing.bankguarantees.error.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This class defines the error sources for all internal/external endpoints
 */
@AllArgsConstructor
@Getter
public enum ErrorSource {

    BGOS("BGOS-00-"),       // Bank Guarantee Online Service Applications

    PMS("BGOS-01-"),        // Permission API Error

    IPA("BGOS-02-"),        // Involve Party Api

    ABA("BGOS-03-"),        // Account Balance Api

    CLAA("BGOS-04-"),       // CustomerLendingArrangements API

    CHA("BGOS-05-"),        // CSI Hub API

    PAA("BGOS-06-"),        // Product Agreement Api

    RDA("BGOS-07-"),        // Reference Data API

    CCA("BGOS-08-"),        // Common Core As A Service API

    CDA("BGOS-09-"),        // Connect Dot API

    CRA("BGOS-10-"),        // Credit Risk API

    GAC("BGOS-11-"),        // GARAssetsAndCollaterals API

    NSA("BGOS-12-"),        // KYC Name Screening RTNS API

    PQC("BGOS-13-"),        // PAM Qualification Checks API

    DAR("BGOS-14-"),        // DocumentActionRequest API

    PEGA("BGOS-15-"),       // TFSApp API

    MRA("BGOS-16-"),        // Master Reference Generation API

    AKB("BGOS-17-"),        // AMS BE KLC API

    ABG("BGOS-18-"),        // Active Bank Guarantees API

    GESS("BGOS-19-"),       // GESS - Global Electronic Signature Service API

    RMB("BGOS-20-"),        // Representation Management BE API

    PFEGR("BGOS-21-"),      // PaymentsForexEmeaGenericRates API

    HCA("BGOS-22-");        // Holiday Calender API

    private final String prefix;
}