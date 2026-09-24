package com.ing.bankguarantees.remote.rest.cla.creditlinearrangement.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import java.util.List;

@Builder
public record CreditLineArrangementInput(HeaderMessage headerMessage, List<Arrangement> arrangements) {

    @Builder
    public record HeaderMessage(String originTAPLcode, String messageTypeCMSG, String cupdCode) {
    }

    @Builder
    public record Arrangement(SpecificElement specificElements, ArrangementItem arrangement) {

        @Builder
        public record SpecificElement(String eventType,
                                      String applicationTAPLcode,
                                      String arrangementFormatCode,
                                      String generalRegulationOfCreditsVersion,
                                      LineOfCreditSpecificElements lineOfCreditSpecificElements,
                                      CollateralSpecificElements collateralSpecificElements) {
            @Builder
            public record LineOfCreditSpecificElements(String lineOfCreditComponentType,
                                                       String lineOfCreditComponentLifecycleStatus,
                                                       boolean authorizedUsageFlag,
                                                       String conventionCondition,
                                                       boolean securityFlag,
                                                       boolean coordinationCenterFlag,
                                                       boolean capitalCreditFlag,
                                                       LineOfCreditComponentAmount lineOfCreditComponentAmount,
                                                       boolean lineOfCreditComponentAutomaticExtensionFlag,
                                                       LineOfCreditComponentAmount lineOfCreditComponentForeignAmount,
                                                       boolean lineOfCreditComponentFullyCollectedFlag,
                                                       String lineOfCreditType,
                                                       String usageCode,
                                                       String codeXY,
                                                       boolean isSubjectOfLaw) {
                @Builder
                public record LineOfCreditComponentAmount(double theCurrencyAmount,
                                                          String theCurrencyCode){

                }

            }

            @Builder
            public record CollateralSpecificElements(String collateralArrangementLifecyclestatus){
            }

        }
    }

    @Builder
    public record ArrangementItem(LineOfCreditArrangement lineOfCreditArrangement) {
        @Builder
        public record LineOfCreditArrangement(List<ArrangementIdentifier> arrangementIdentifier,
                                              List<Integer> approvalDate,
                                              List<IsOwnedBy> isOwnedBy) {


        }

        @Builder
        public record ArrangementIdentifier(@JsonProperty("arrangementIdentifier")
                                            String arrangementIdentifierItem) {

        }
        @Builder
        public record IsOwnedBy(Intervenient intervenient){

            @Builder
            public record Intervenient(List<RoleIsParticipantIn> roleIsParticipantIn){

                @Builder
                public record RoleIsParticipantIn(CurrentAccountArrangement currentAccountArrangement){

                    @Builder
                    public record CurrentAccountArrangement(List<ArrangementIdentifier> arrangementIdentifier,
                                                            String name){

                    }
                }
            }

        }
    }
}
