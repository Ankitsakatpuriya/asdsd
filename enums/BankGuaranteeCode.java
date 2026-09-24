package com.ing.bankguarantees.models.enums;

import com.ing.bankguarantees.models.guaranteetype.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;

@Getter
@RequiredArgsConstructor
public enum BankGuaranteeCode {

    PUBLIC_CONTRACT("A-BLNBG01", "121", PublicContract.class, com.ing.bankguarantees.avro.guaranteetype.PublicContract.class),

    PERFORMANCE_BOND("A-BLNBG07", "115", PerformanceBond.class, com.ing.bankguarantees.avro.guaranteetype.PerformanceBond.class),

    RENTAL("A-BLNBG04", "114", Rental.class, com.ing.bankguarantees.avro.guaranteetype.Rental.class),

    ADVANCE_PAYMENT("A-BLNBG07", "117", AdvancePayment.class, com.ing.bankguarantees.avro.guaranteetype.AdvancePayment.class),

    PAYMENT_GUARANTEE("A-BLNBG07", "116", Payment.class, com.ing.bankguarantees.avro.guaranteetype.Payment.class),

    MONEY_RETENTION_BOND("A-BLNBG07", "118", MoneyRetentionBond.class, com.ing.bankguarantees.avro.guaranteetype.MoneyRetentionBond.class),

    BID_BOND("A-BLNBG07", "119", BidBond.class, com.ing.bankguarantees.avro.guaranteetype.BidBond.class),

    REAL_ESTATE("A-BLNBG02", "138", RealEstate.class, com.ing.bankguarantees.avro.guaranteetype.RealEstate.class),

    STATE_LOTTERY("A-BLNBG03", "131", StateLottery.class, com.ing.bankguarantees.avro.guaranteetype.StateLottery.class),

    CUSTOM_1("A-BLNBG06", "124", CustomTypeOne.class, com.ing.bankguarantees.avro.guaranteetype.CustomTypeOne.class),

    CUSTOM_2("A-BLNBG06", "126", CustomTypeTwo.class, com.ing.bankguarantees.avro.guaranteetype.CustomTypeTwo.class),

    CUSTOM_4("A-BLNBG06", "123", CustomTypeFour.class, com.ing.bankguarantees.avro.guaranteetype.CustomTypeFour.class),

    CUSTOM_5("A-BLNBG06", "125", CustomTypeFive.class, com.ing.bankguarantees.avro.guaranteetype.CustomTypeFive.class),

    OVAM("A-BLNBG05", "134", Ovam.class, com.ing.bankguarantees.avro.guaranteetype.Ovam.class),

    DCK_CDC("A-BLNBG05", "154", Dck.class, com.ing.bankguarantees.avro.guaranteetype.Dck.class),

    WOODS_PROM_A("A-BLNBG08", "304", WoodsWalloniaStandardPromise.class, com.ing.bankguarantees.avro.guaranteetype.WoodsWalloniaStandardPromise.class),

    WOODS_PROM_B("A-BLNBG08", "305", WoodsBlankPromise.class, com.ing.bankguarantees.avro.guaranteetype.WoodsBlankPromise.class),

    WOODS_PROM_VLA("A-BLNBG08", "304", WoodsFlandersStandardPromise.class, com.ing.bankguarantees.avro.guaranteetype.WoodsFlandersStandardPromise.class),

    WOODS_BGWAL_PUBLIC("A-BLNBG08", "143", WoodsWALPublic.class, com.ing.bankguarantees.avro.guaranteetype.WoodsWALPublic.class),

    WOODS_BG_PRIVATE("A-BLNBG07", "143", WoodsPrivate.class, com.ing.bankguarantees.avro.guaranteetype.WoodsPrivate.class),

    WOODS_BG_DISCHARGE("A-BLNBG08", "143", WoodsDischarge.class, com.ing.bankguarantees.avro.guaranteetype.WoodsDischarge.class),

    WOODS_BGVLA_PUBLIC("A-BLNBG08", "143", WoodsVLAPublic.class, com.ing.bankguarantees.avro.guaranteetype.WoodsVLAPublic.class),

    ABSTRACT_PROM("A-BLNBG09", "300", PromiseAbstract.class, com.ing.bankguarantees.avro.guaranteetype.PromiseAbstract.class),

    PUBLIC_CONTRACT_PROM("A-BLNBG09", "301", PromisePublicContract.class, com.ing.bankguarantees.avro.guaranteetype.PromisePublicContract.class),

    GOODS_TRANSPORT("A-BLNBG10", "127", GoodsTransport.class, com.ing.bankguarantees.avro.guaranteetype.GoodsTransport.class),

    PASSENGER_TRANSPORT("A-BLNBG10", "128", PassengerTransport.class, com.ing.bankguarantees.avro.guaranteetype.PassengerTransport.class),

    OPERATORS_TRANSPORT("A-BLNBG10", "129", OperatorsTransport.class, com.ing.bankguarantees.avro.guaranteetype.OperatorsTransport.class),

    CUSTOMIZED_TEXT("A-BLNBG08", "145", CustomizedText.class, com.ing.bankguarantees.avro.guaranteetype.CustomizedText.class);

    private final String templateName;
    private final String typeCode;
    private final Class<? extends BaseGuaranteeType> classType;
    private final Class<? extends SpecificRecord> avroClassType;

}
