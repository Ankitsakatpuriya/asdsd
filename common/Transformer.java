package com.ing.bankguarantees.remote.common;


import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilderException;

public interface Transformer<IN, OUT> {

    OUT transform(IN input) throws RichHttpRequestBuilderException;
}
