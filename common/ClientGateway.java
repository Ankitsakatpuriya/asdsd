package com.ing.bankguarantees.remote.common;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.twitter.finagle.http.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;


@Slf4j
public class ClientGateway<IN, OUT, RES> {
    private final JavaService<Request, RES> restClient;
    private final Function<IN, Request> requestTransformer;
    private final BiFunction<RES, IN, OUT> responseTransformer;
    private final Function<RES, RES> validatorFunction;
    private final ExecutorService executorService;

    public ClientGateway(JavaService<Request, RES> restClient,
                         Function<IN, Request> requestTransformer,
                         BiFunction<RES, IN, OUT> responseTransformer,
                         Function<RES, RES> validatorFunction,
                         @Qualifier("workStealingPool") ExecutorService executorService) {
        this.restClient = restClient;
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
        this.validatorFunction = validatorFunction;
        this.executorService = executorService;
    }


    public ClientGateway(JavaService<Request, RES> restClient,
                         Function<IN, Request> requestTransformer,
                         Function<RES, RES> validatorFunction,
                         @Qualifier("workStealingPool") ExecutorService executorService) {
        this.restClient = restClient;
        this.requestTransformer = requestTransformer;
        this.validatorFunction = validatorFunction;
        this.executorService = executorService;
        this.responseTransformer = null;
    }

    public CompletableFuture<OUT> performRequest(IN in) {
        return restClient.apply(requestTransformer.apply(in))
                .thenApplyAsync(validatorFunction,
                        executorService)
                .thenApplyAsync(res -> responseTransformer.apply(res, in),
                        executorService);
    }

    public CompletableFuture<RES> performRequestWithValidate(IN in) {
        return restClient.apply(requestTransformer.apply(in))
                .thenApplyAsync(validatorFunction,
                        executorService);
    }


}
