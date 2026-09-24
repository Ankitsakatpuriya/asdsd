package com.ing.bankguarantees.configuration;

import com.ing.apisdk.toolkit.logging.kafka.C3LogMarker;
import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import com.ing.apisdk.toolkit.trust.accesstoken.AccessTokenClaimsSet;
import com.ing.bankguarantees.utils.TracingHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Aspect
@Order(1)
@Component
public class LoggingAspect {

    private static final String SESSION_ID = "sessionId";

    @Pointcut("execution(* com.ing.bankguarantees.resource.oneweb.*.*(..))")
    private void interceptRequests() {
    }

    @Before("interceptRequests()")
    public void beforeRequest(JoinPoint joinPoint) {

        Arrays.stream(joinPoint.getArgs())
                .filter(AccessToken.class::isInstance)
                .findFirst()
                .ifPresent(it -> {
                    AccessToken accessToken = (AccessToken) it;
                    AccessTokenClaimsSet claimsSet = accessToken.getClaimsSet();
                    log.info("calling endpoint {} for profile :{} and person : {}",
                            joinPoint.getSignature().getName(), claimsSet.getRequester().getProfile(), claimsSet.getRequester().getPerson());
                    TracingHelper.setBaggageItem(SESSION_ID, claimsSet.getSetId());
                });
    }

    @AfterReturning(pointcut = "interceptRequests()", returning = "result")
    public void afterAdvice(JoinPoint joinPoint, Object result) {
        String res;
        try {
            Object response = ((CompletableFuture) result).join();
            res = Optional.ofNullable(response).map(Object::toString).orElse(null);
        } catch (Exception exception) {
            res = exception.getMessage();
        }
        log.info(C3LogMarker.marker, "Response sent from BGOS endpoint {}: {}", joinPoint.getSignature().getName(), res);
    }

}
