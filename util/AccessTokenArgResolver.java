package com.ing.bankguarantees.util;

import com.ing.apisdk.toolkit.trust.accesstoken.AccessToken;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.ing.bankguarantees.util.MockHelper.getAccessToken;

public final class AccessTokenArgResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().equals(AccessToken.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter methodParameter,
                                  @Nullable ModelAndViewContainer modelAndViewContainer,
                                  @NotNull NativeWebRequest nativeWebRequest,
                                  @Nullable WebDataBinderFactory webDataBinderFactory) {
        return getAccessToken();
    }
}
