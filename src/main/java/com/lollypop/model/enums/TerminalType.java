package com.lollypop.model.enums;

import java.util.List;

/**
 * Terminal (handset) types available in the Matsecom network.
 * Supported technologies are listed in priority order: preferred technology last
 * so that selectBestDataTechnology() can simply return the last element.
 *
 * Enum names match the DB ENUM values exactly — do not rename without updating the schema.
 *
 * RFP rule: a terminal always uses 4G instead of 3G for data when 4G is available.
 */
public enum TerminalType {

    PhairPhone(      List.of(RANTechnologyType.G2, RANTechnologyType.G3)),
    Pear_aphone_4s(  List.of(RANTechnologyType.G2, RANTechnologyType.G3)),
    Samsung_S42plus( List.of(RANTechnologyType.G2, RANTechnologyType.G3, RANTechnologyType.G4));

    private final List<RANTechnologyType> supportedTechnologies;

    TerminalType(List<RANTechnologyType> supportedTechnologies) {
        this.supportedTechnologies = supportedTechnologies;
    }

    public List<RANTechnologyType> getSupportedTechnologies() {
        return supportedTechnologies;
    }

    /**
     * Returns the best available data technology for this terminal.
     * Prefers 4G → 3G as per RFP.
     * Returns null if the terminal does NOT support data services (i.e. 2G only).
     */
    public RANTechnologyType getBestDataTechnology() {
        if (supportedTechnologies.contains(RANTechnologyType.G4)) return RANTechnologyType.G4;
        if (supportedTechnologies.contains(RANTechnologyType.G3)) return RANTechnologyType.G3;
        return null; // 2G only terminal — no data capable
    }
}
