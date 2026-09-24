package com.ing.bankguarantees.remote.utils;


import com.ing.bankguarantees.remote.rest.dar.model.request.Translations;
import com.ing.bankguarantees.utils.JsonUtils;

public class TranslationsLoader {

    private static Translations translations;
    private static final String FILE_PATH = "translations/onlinesignature.json";

    private TranslationsLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static synchronized Translations loadTranslations() {
        if (translations == null) {
            translations = JsonUtils.mapFileToObject(FILE_PATH, Translations.class);
        }
        return translations;
    }
}