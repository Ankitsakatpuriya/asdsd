package com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.mapper;

import com.ing.bankguarantees.remote.rest.amsklc.booking.response.RequestType;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.CreditLineArrangementProperties;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementInput;
import com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request.CreditLineArrangementRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.ing.bankguarantees.utils.CommonUtils.getCurrentDate;

@Component
@RequiredArgsConstructor
public class CreditLineArrangementRequestMapper {

    private final CreditLineArrangementProperties creditLineArrangementProperties;

    public CreditLineArrangementInput prepareCreditLineArrangementInput(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.builder()
                .headerMessage(prepareHeaderMessage())
                .arrangements(List.of(prepareArrangement(creditLineArrangementRequest)))
                .build();
    }

    private CreditLineArrangementInput.Arrangement prepareArrangement(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.Arrangement.builder()
                .specificElements(prepareSpecificElement(creditLineArrangementRequest))
                .arrangement(prepareArrangementItem(creditLineArrangementRequest))
                .build();
    }

    private CreditLineArrangementInput.Arrangement.SpecificElement prepareSpecificElement(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.Arrangement.SpecificElement.builder()
                .lineOfCreditSpecificElements(prepareLineOfCreditSpecificElements())
                .eventType(RequestType.DELETE == creditLineArrangementRequest.requestType() ? creditLineArrangementProperties.getEventType()
                        : creditLineArrangementProperties.getCreateEventType())
                .arrangementFormatCode(creditLineArrangementProperties.getArrangementFormatCode())
                .applicationTAPLcode(creditLineArrangementProperties.getApplicationTaplCode())
                .generalRegulationOfCreditsVersion(creditLineArrangementProperties.getGeneralRegulationOfCreditsVersion())
                .lineOfCreditSpecificElements(prepareLineOfCreditSpecificElements(creditLineArrangementRequest))
                .collateralSpecificElements(prepareCollateralSpecificElements())
                .build();
    }

    private CreditLineArrangementInput.Arrangement.SpecificElement.LineOfCreditSpecificElements prepareLineOfCreditSpecificElements() {
        return CreditLineArrangementInput.Arrangement.SpecificElement.LineOfCreditSpecificElements.builder()
                .lineOfCreditComponentType(creditLineArrangementProperties.getLineOfCreditComponentType())
                .lineOfCreditComponentLifecycleStatus(creditLineArrangementProperties.getLineOfCreditComponentLifecycleStatus())
                .build();
    }

    private CreditLineArrangementInput.Arrangement.SpecificElement.LineOfCreditSpecificElements prepareLineOfCreditSpecificElements(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.Arrangement.SpecificElement.LineOfCreditSpecificElements.builder()
                .authorizedUsageFlag(creditLineArrangementProperties.isAuthorizedUsageFlag())
                .conventionCondition(creditLineArrangementProperties.getConventionCondition())
                .securityFlag(creditLineArrangementProperties.isSecurityFlag())
                .lineOfCreditComponentType(creditLineArrangementProperties.getLineOfCreditComponentType())
                .coordinationCenterFlag(creditLineArrangementProperties.isCoordinationCenterFlag())
                .capitalCreditFlag(creditLineArrangementProperties.isCapitalCreditFlag())
                .lineOfCreditComponentAmount(prepareLineOfCreditComponentAmount(creditLineArrangementRequest))
                .lineOfCreditComponentAutomaticExtensionFlag(creditLineArrangementProperties.isAutomaticExtensionFlag())
                .lineOfCreditComponentForeignAmount(prepareLineOfCreditComponentAmount(creditLineArrangementRequest))
                .lineOfCreditComponentFullyCollectedFlag(creditLineArrangementProperties.isFullyCollectedFlag())
                .lineOfCreditComponentLifecycleStatus(RequestType.DELETE == creditLineArrangementRequest.requestType()
                        ? creditLineArrangementProperties.getDeleteLineOfCreditComponentLifecycleStatus()
                        : creditLineArrangementProperties.getLineOfCreditComponentLifecycleStatus())
                .lineOfCreditType(creditLineArrangementProperties.getLineOfCreditType())
                .usageCode(creditLineArrangementProperties.getUsageCode())
                .codeXY(creditLineArrangementProperties.getCodeXy())
                .isSubjectOfLaw(creditLineArrangementProperties.isSubjectOfLaw())
                .build();
    }

    private CreditLineArrangementInput.Arrangement.SpecificElement.LineOfCreditSpecificElements.LineOfCreditComponentAmount prepareLineOfCreditComponentAmount(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.Arrangement.SpecificElement.LineOfCreditSpecificElements.LineOfCreditComponentAmount.builder()
                .theCurrencyAmount(creditLineArrangementRequest.bgAmount())
                .theCurrencyCode(creditLineArrangementRequest.currency())
                .build();
    }

    private CreditLineArrangementInput.Arrangement.SpecificElement.CollateralSpecificElements prepareCollateralSpecificElements() {
        return CreditLineArrangementInput.Arrangement.SpecificElement.CollateralSpecificElements.builder()
                .collateralArrangementLifecyclestatus(creditLineArrangementProperties.getCollateralArrangementLifecyclestatus())
                .build();
    }

    private CreditLineArrangementInput.HeaderMessage prepareHeaderMessage() {
        return CreditLineArrangementInput.
                HeaderMessage.builder()
                .originTAPLcode(creditLineArrangementProperties.getOriginTaplCode())
                .cupdCode(creditLineArrangementProperties.getCupdCode())
                .messageTypeCMSG(creditLineArrangementProperties.getMessageTypeCmsg())
                .build();
    }

    private CreditLineArrangementInput.ArrangementItem prepareArrangementItem(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.ArrangementItem.builder()
                .lineOfCreditArrangement(prepareLineOfCreditArrangement(creditLineArrangementRequest))
                .build();
    }

    private CreditLineArrangementInput.ArrangementItem.LineOfCreditArrangement prepareLineOfCreditArrangement(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.ArrangementItem.LineOfCreditArrangement.builder()
                .arrangementIdentifier(List.of(prepareArrangementIdentifier(creditLineArrangementRequest)))
                .approvalDate(getCurrentDate())
                .isOwnedBy(List.of(prepareIsOwnedBy(creditLineArrangementRequest)))
                .build();
    }

    private CreditLineArrangementInput.ArrangementItem.ArrangementIdentifier prepareArrangementIdentifier(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.ArrangementItem.ArrangementIdentifier.builder()
                .arrangementIdentifierItem(creditLineArrangementRequest.reservationNumber())
                .build();
    }

    private CreditLineArrangementInput.ArrangementItem.IsOwnedBy prepareIsOwnedBy(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.ArrangementItem.IsOwnedBy.builder()
                .intervenient(CreditLineArrangementInput.ArrangementItem.IsOwnedBy.Intervenient.builder()
                        .roleIsParticipantIn(List.of(prepareRoleIsParticipantIn(creditLineArrangementRequest)))
                        .build())
                .build();
    }

    private CreditLineArrangementInput.ArrangementItem.IsOwnedBy.Intervenient.RoleIsParticipantIn prepareRoleIsParticipantIn(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.ArrangementItem.IsOwnedBy.Intervenient.RoleIsParticipantIn.builder()
                .currentAccountArrangement(prepareCurrentAccountArrangement(creditLineArrangementRequest))
                .build();
    }

    private CreditLineArrangementInput.ArrangementItem.IsOwnedBy.Intervenient.RoleIsParticipantIn.CurrentAccountArrangement prepareCurrentAccountArrangement(CreditLineArrangementRequest creditLineArrangementRequest) {
        return CreditLineArrangementInput.ArrangementItem.IsOwnedBy.Intervenient.RoleIsParticipantIn.CurrentAccountArrangement.builder()
                .arrangementIdentifier(List.of(CreditLineArrangementInput.ArrangementItem.ArrangementIdentifier.builder()
                        .arrangementIdentifierItem(creditLineArrangementRequest.accountNumber())
                        .build()))
                .name(creditLineArrangementProperties.getName())
                .build();
    }
}
