package com.ing.bankguarantees.error.config;

import com.ing.bankguarantees.error.config.modeljson.ErrorItem;
import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Component to load the error items from the json files
 */
@Slf4j
@Component
public class ErrorDataLoader {

    private final String errorInputFilePath;
    private final ObjectMapper jsonMapper;

    /**
     * Loads all the errors defined in json files under errors resource directory
     *
     * @param jsonMapper         Object Mapper
     * @param errorInputFilePath Error file path
     */
    public ErrorDataLoader(ObjectMapper jsonMapper,
                           @Value("${bgos.errors.input-file-path:errors/%s_errors.json}") String errorInputFilePath) {
        this.jsonMapper = jsonMapper;
        this.errorInputFilePath = errorInputFilePath;
    }

    /**
     * Get list error items for the system
     *
     * @param errorSource source system
     * @return list of error items
     */
    public List<ErrorItem> getList(ErrorSource errorSource) {
        String inputDataFile = String
                .format(errorInputFilePath, errorSource.name().toLowerCase());
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(inputDataFile)) {
            return jsonMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (JacksonException exception) {
            log.error("JacksonException occurred for file : {} with exception {}",
                    inputDataFile, exception.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, exception);
        } catch (IOException exception) {
            log.error("IOException occurred for file : {} with exception {}",
                    inputDataFile, exception.getMessage());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, exception);
        } catch (Exception exception) {
            log.error("Error {} occurred while loading file : {}",
                    exception.getMessage(), inputDataFile);
            throw new BgosException(ErrorCode.TECHNICAL_ERROR, exception);
        }
    }
}
