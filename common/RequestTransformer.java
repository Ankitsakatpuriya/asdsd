package com.ing.bankguarantees.remote.common;

import com.twitter.finagle.http.Request;
import lombok.Data;

/**
 * Abstract transformer just to include the urlFormat and path for the {@link Request}
 *
 * @param <IN> type of object to be transformed to a {link Req}
 */
@Data
public abstract class RequestTransformer<IN> implements Transformer<IN, Request> {
    private final String urlFormat;

    protected RequestTransformer(String urlFormat) {
        this.urlFormat = urlFormat;
    }
}
