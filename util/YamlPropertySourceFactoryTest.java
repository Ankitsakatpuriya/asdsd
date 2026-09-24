package com.ing.bankguarantees.util;

import com.ing.bankguarantees.error.exception.BgosException;
import com.ing.bankguarantees.error.exception.ErrorCode;
import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YamlPropertySourceFactoryTest {

    @Test
    void testYamlPropertySourceFactoryLoadsProperties() {
        YamlPropertySourceFactory factory = new YamlPropertySourceFactory();

        EncodedResource resource = new EncodedResource(new ClassPathResource("utils/test-config.yaml"));
        PropertySource<?> propertySource = factory.createPropertySource("test", resource);

        assertNotNull(propertySource);
        assertEquals("value123", propertySource.getProperty("some.key"));
    }

    @Test
    void testCreatePropertySourceWithMockedResource() throws IOException {
        String configYaml = "utils/config-test.yaml";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(configYaml.getBytes());
        Resource mockResource = Mockito.mock(Resource.class);
        when(mockResource.getInputStream()).thenReturn(inputStream);
        when(mockResource.getFilename()).thenReturn(null);
        EncodedResource mockEncodedResource = Mockito.mock(EncodedResource.class);
        when(mockEncodedResource.getResource()).thenReturn(mockResource);
        YamlPropertySourceFactory factory = new YamlPropertySourceFactory();
        BgosException bgosException = assertThrows(BgosException.class,
                () -> factory.createPropertySource(null, mockEncodedResource));
        assertThat(bgosException.getErrorCode()).isEqualTo(ErrorCode.TECHNICAL_ERROR);

    }
}