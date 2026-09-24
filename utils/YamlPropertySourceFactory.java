package com.ing.bankguarantees.remote.utils;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Properties;

/**
 * factory for yaml file
 */
@Slf4j
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public @NotNull PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();
        String filename = encodedResource.getResource().getFilename();
        if (filename == null || properties == null) {
            log.error("Resource file not found {}", encodedResource.getResource().getFilename());
            throw new BgosException(ErrorCode.TECHNICAL_ERROR);
        }
        return new PropertiesPropertySource(filename, properties);
    }
}