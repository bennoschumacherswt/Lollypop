package com.lollypop.model.enums;

/**
 * Subscription plans available in the Matsecom network.
 * All values taken directly from the RFP product sheet.
 *
 * Enum names match the DB ENUM values exactly — do not rename without updating the schema.
 */
public enum SubscriptionType {

    //                        fee    inclMin  pricePerExtraMin  dataMB
    GreenMobilS(  8.00,    0,   0.08,   500.0),
    GreenMobilM( 22.00,  100,   0.06,  2048.0),   // 2 GB
    GreenMobilL( 42.00,  150,   0.04,  5120.0);   // 5 GB

    /** Monthly base fee in EUR. */
    private final double baseFeeEur;

    /** Voice minutes included in the base fee. */
    private final int includedMinutes;

    /** Price per extra minute (beyond included) in EUR. */
    private final double pricePerExtraMinuteEur;

    /** Total 3G/4G data volume in MB included per billing period. */
    private final double dataVolumeMb;

    SubscriptionType(double baseFeeEur, int includedMinutes,
                     double pricePerExtraMinuteEur, double dataVolumeMb) {
        this.baseFeeEur = baseFeeEur;
        this.includedMinutes = includedMinutes;
        this.pricePerExtraMinuteEur = pricePerExtraMinuteEur;
        this.dataVolumeMb = dataVolumeMb;
    }

    public double getBaseFeeEur()              { return baseFeeEur; }
    public int    getIncludedMinutes()         { return includedMinutes; }
    public double getPricePerExtraMinuteEur()  { return pricePerExtraMinuteEur; }
    public double getDataVolumeMb()            { return dataVolumeMb; }
}
