package com.lollypop.model.enums;

/**
 * Service types available in the Matsecom network.
 * requiredDataRateMbit = minimum Mbit/s the terminal must achieve for the service to work.
 * 0 means the service is not data-based (voice call).
 *
 * Enum names match the DB ENUM values exactly — do not rename without updating the schema.
 */
public enum ServiceType {

    Voice_call(0, RANTechnologyType.G2),
    Browsing_and_social_networking(2,  RANTechnologyType.G3),
    App_download(10,  RANTechnologyType.G3),
    Adaptive_HD_video(75, RANTechnologyType.G3);

    /** Minimum data rate in Mbit/s required for this service. */
    private final double requiredDataRateMbit;

    /** Minimum RAN technology needed (G2 = voice, G3 = any data service). */
    private final RANTechnologyType minimumTechnology;

    ServiceType(double requiredDataRateMbit, RANTechnologyType minimumTechnology) {
        this.requiredDataRateMbit = requiredDataRateMbit;
        this.minimumTechnology = minimumTechnology;
    }

    public double getRequiredDataRateMbit() {
        return requiredDataRateMbit;
    }

    public RANTechnologyType getMinimumTechnology() {
        return minimumTechnology;
    }

    public boolean isVoice() {
        return this == Voice_call;
    }

    public boolean isDataService() {
        return !isVoice();
    }
}
