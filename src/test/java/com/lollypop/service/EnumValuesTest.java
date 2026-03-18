package com.lollypop.service;

import com.lollypop.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that enum constants encode the exact values from the RFP.
 * These tests act as living documentation: if someone changes an RFP value
 * in an enum, a test breaks and forces an explicit decision.
 */
class EnumValuesTest {

    // -------------------------------------------------------------------
    // RANTechnologyType
    // -------------------------------------------------------------------

    @Test @DisplayName("G2 has zero data rate (voice only)")
    void g2_voiceOnly()      { assertEquals(0,   RANTechnologyType.G2.getMaxDataRateMbit()); }

    @Test @DisplayName("G3 max data rate is 20 Mbit/s")
    void g3_dataRate()       { assertEquals(20,  RANTechnologyType.G3.getMaxDataRateMbit()); }

    @Test @DisplayName("G4 max data rate is 300 Mbit/s")
    void g4_dataRate()       { assertEquals(300, RANTechnologyType.G4.getMaxDataRateMbit()); }

    // -------------------------------------------------------------------
    // ServiceType required data rates
    // -------------------------------------------------------------------

    @Test @DisplayName("Voice call requires 0 Mbit/s data")
    void voiceCall_noDataRate() { assertEquals(0,  ServiceType.Voice_call.getRequiredDataRateMbit(), 0.001); }

    @Test @DisplayName("Browsing requires 2 Mbit/s")
    void browsing_dataRate()    { assertEquals(2,  ServiceType.Browsing_and_social_networking.getRequiredDataRateMbit(), 0.001); }

    @Test @DisplayName("App download requires 10 Mbit/s")
    void appDownload_dataRate() { assertEquals(10, ServiceType.App_download.getRequiredDataRateMbit(), 0.001); }

    @Test @DisplayName("HD video requires 75 Mbit/s")
    void hdVideo_dataRate()     { assertEquals(75, ServiceType.Adaptive_HD_video.getRequiredDataRateMbit(), 0.001); }

    // -------------------------------------------------------------------
    // SubscriptionType RFP values
    // -------------------------------------------------------------------

    @Test @DisplayName("GreenMobilS: fee=€8, 0 min, €0.08/min, 500 MB")
    void greenMobilS() {
        SubscriptionType s = SubscriptionType.GreenMobilS;
        assertEquals(8.00,  s.getBaseFeeEur(),             0.001);
        assertEquals(0,     s.getIncludedMinutes());
        assertEquals(0.08,  s.getPricePerExtraMinuteEur(), 0.001);
        assertEquals(500.0, s.getDataVolumeMb(),           0.001);
    }

    @Test @DisplayName("GreenMobilM: fee=€22, 100 min, €0.06/min, 2048 MB")
    void greenMobilM() {
        SubscriptionType s = SubscriptionType.GreenMobilM;
        assertEquals(22.00,  s.getBaseFeeEur(),             0.001);
        assertEquals(100,    s.getIncludedMinutes());
        assertEquals(0.06,   s.getPricePerExtraMinuteEur(), 0.001);
        assertEquals(2048.0, s.getDataVolumeMb(),           0.001);
    }

    @Test @DisplayName("GreenMobilL: fee=€42, 150 min, €0.04/min, 5120 MB")
    void greenMobilL() {
        SubscriptionType s = SubscriptionType.GreenMobilL;
        assertEquals(42.00,  s.getBaseFeeEur(),             0.001);
        assertEquals(150,    s.getIncludedMinutes());
        assertEquals(0.04,   s.getPricePerExtraMinuteEur(), 0.001);
        assertEquals(5120.0, s.getDataVolumeMb(),           0.001);
    }

    // -------------------------------------------------------------------
    // TerminalType capabilities
    // -------------------------------------------------------------------

    @Test @DisplayName("PhairPhone: supports G2 and G3 only")
    void phairPhone_g2g3() {
        assertTrue(TerminalType.PhairPhone.getSupportedTechnologies().contains(RANTechnologyType.G2));
        assertTrue(TerminalType.PhairPhone.getSupportedTechnologies().contains(RANTechnologyType.G3));
        assertFalse(TerminalType.PhairPhone.getSupportedTechnologies().contains(RANTechnologyType.G4));
        assertEquals(RANTechnologyType.G3, TerminalType.PhairPhone.getBestDataTechnology());
    }

    @Test @DisplayName("Samsung_S42plus: supports G2, G3, G4 — best tech is G4")
    void samsungS42plus_g4preferred() {
        assertTrue(TerminalType.Samsung_S42plus.getSupportedTechnologies().contains(RANTechnologyType.G4));
        assertEquals(RANTechnologyType.G4, TerminalType.Samsung_S42plus.getBestDataTechnology());
    }

    // -------------------------------------------------------------------
    // Subscriber MCC
    // -------------------------------------------------------------------

    @Test @DisplayName("Germany MCC must be 262")
    void subscriber_mcc_262() {
        com.lollypop.model.Subscriber s = new com.lollypop.model.Subscriber(
                0, 1234567890L, "T", "T",
                TerminalType.PhairPhone, SubscriptionType.GreenMobilS);
        assertEquals(262, s.getMcc(), "Germany MCC must be 262, not 49");
    }
}
