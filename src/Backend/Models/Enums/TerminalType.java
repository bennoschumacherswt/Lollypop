package Backend.Models.Enums;

import java.util.Set;

public enum TerminalType {

    PhairPhone(Set.of(RANTechnologyType.G2, RANTechnologyType.G3), "PhairPhone"),
    Pear_aphone_4s(Set.of(RANTechnologyType.G2, RANTechnologyType.G3), "Pear_aphone_4s"),
    Samsung_S42plus(Set.of(RANTechnologyType.G2, RANTechnologyType.G3, RANTechnologyType.G4), "Samsung S42plus");

    private final Set<RANTechnologyType> supportedTechnologies;
    private final String label;

    TerminalType(Set<RANTechnologyType> supportedTechnologies, String label) {
        this.supportedTechnologies = supportedTechnologies;
        this.label = label;
    }

    public Set<RANTechnologyType> getSupportedTechnologies() {
        return supportedTechnologies;
    }

    public String getLabel(){
        return this.label;
    }
}
