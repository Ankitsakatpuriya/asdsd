package com.ing.bankguarantees.remote.rest.cla.creditlinearrangement;

import com.ing.bankguarantees.remote.utils.YamlPropertySourceFactory;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("remote.rest.creditlinearrangement")
@PropertySource(value = "classpath:props/beRemoteRestIntegrationsInput.yml", factory = YamlPropertySourceFactory.class)
public class CreditLineArrangementProperties {

    @NotBlank
    private String originTaplCode;

    @NotBlank
    private String messageTypeCmsg;

    @NotBlank
    private String cupdCode;

    @NotBlank
    private String name;

    @NotBlank
    private String eventType;

    @NotBlank
    private String createEventType;

    @NotBlank
    private String applicationTaplCode;

    @NotBlank
    private String arrangementFormatCode;

    @NotBlank
    private String generalRegulationOfCreditsVersion;

    @NotBlank
    private String lineOfCreditComponentLifecycleStatus;

    @NotBlank
    private String deleteLineOfCreditComponentLifecycleStatus;

    @NotBlank
    private String lineOfCreditComponentType;

    private boolean authorizedUsageFlag;
    @NotBlank
    private String conventionCondition;
    private boolean securityFlag;
    private boolean coordinationCenterFlag;
    private boolean capitalCreditFlag;
    private boolean automaticExtensionFlag;
    private boolean fullyCollectedFlag;
    @NotBlank
    private String lineOfCreditType;
    @NotBlank
    private String usageCode;
    @NotBlank
    private String codeXy;
    private boolean isSubjectOfLaw;
    @NotBlank
    private String collateralArrangementLifecyclestatus;
}
