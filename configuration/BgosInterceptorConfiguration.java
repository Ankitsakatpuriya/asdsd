package com.ing.bankguarantees.configuration;

import com.ing.bankguarantees.interceptors.BgosRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class BgosInterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private BgosRequestInterceptor bgosRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bgosRequestInterceptor);
    }
}
