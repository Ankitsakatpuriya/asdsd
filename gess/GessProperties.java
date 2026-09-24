package com.ing.bankguarantees.remote.rest.gess;

import com.ing.bankguarantees.remote.rest.gess.model.request.SignPosition;
import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Data
@Validated
@Configuration
@ConfigurationProperties("remote.rest.gess")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class GessProperties {

    private SignPosition signPosition;

    private GessCoordinateConfigProperties coordinateConfig;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GessCoordinateConfigProperties {

        private Map<String, List<Integer>> publicContract;
        private Map<String, List<Integer>> abstractModel;
        private Map<String, List<Integer>> realEstate;
        private Map<String, List<Integer>> rental;
        private Map<String, List<Integer>> customOne;
        private Map<String, List<Integer>> customTwo;
        private Map<String, List<Integer>> customFive;
        private Map<String, List<Integer>> customFourDutch;
        private Map<String, List<Integer>> customFourFrench;
        private Map<String, List<Integer>> ovam;
        private Map<String, List<Integer>> stateLottery;
        private Map<String, List<Integer>> woods;
        private Map<String, List<Integer>> contractLetter;
    }

}
