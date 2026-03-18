package com.lollypop.model.enums;

/**
 * Radio Access Network technology types supported in the Matsecom network.
 * Data rates are maximum throughput values in Mbit/s (from RFP technology table).
 * G2 carries voice only — no data throughput.
 */
public enum RANTechnologyType {

    G2(0),    // GSM  — voice only, no data throughput
    G3(20),   // HSPA — max 20 Mbit/s
    G4(300);  // LTE  — max 300 Mbit/s

    /** Maximum achievable throughput in Mbit/s. 0 means voice-only. */
    private final int maxDataRateMbit;

    RANTechnologyType(int maxDataRateMbit) {
        this.maxDataRateMbit = maxDataRateMbit;
    }

    public int getMaxDataRateMbit() {
        return maxDataRateMbit;
    }
}
