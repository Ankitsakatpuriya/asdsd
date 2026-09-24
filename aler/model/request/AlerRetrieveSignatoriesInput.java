package com.ing.bankguarantees.remote.rest.aler.model.request;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import lombok.Builder;

import java.util.Locale;

@Builder
public record AlerRetrieveSignatoriesInput(AccessToken accessToken, String organisationId, Locale translation) {
}