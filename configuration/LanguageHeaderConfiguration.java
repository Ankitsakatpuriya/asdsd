package com.ing.bankguarantees.configuration;

import org.apache.commons.lang3.LocaleUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Configuration for manipulating the language header
 */
@Configuration
public class LanguageHeaderConfiguration {
    /**
     * @return {@link AcceptHeaderLocaleResolver} which transforms the header in a {@link Locale}
     */
    @Bean
    public AcceptHeaderLocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(LocaleUtils.toLocale("fr_BE"));
        resolver.setSupportedLocales(List.of(
                LocaleUtils.toLocale("nl_BE"),
                LocaleUtils.toLocale("fr_BE"),
                Locale.UK));
        return resolver;
    }
}
