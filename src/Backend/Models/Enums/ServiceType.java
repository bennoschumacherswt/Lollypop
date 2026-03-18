package Backend.Models.Enums;

import java.util.Set;

public enum ServiceType {

    Voice_call("Voice call", 0,Set.of(RANTechnologyType.G2) ),
    Browsing_and_social_networking("Browsing and social networking", 10, Set.of(RANTechnologyType.G3, RANTechnologyType.G4)),
    App_download("App download", 20, Set.of(RANTechnologyType.G3, RANTechnologyType.G4)),
    Adaptive_HD_video("Adaptive HD video", 75, Set.of(RANTechnologyType.G3, RANTechnologyType.G4));

    private final String label;
    private final int requiredDataRate;
    private final Set<RANTechnologyType> technologyTypes;

    ServiceType(String label, int requiredDataRate, Set<RANTechnologyType> technologyTypes){
        this.label = label;
        this.requiredDataRate = requiredDataRate;
        this.technologyTypes = technologyTypes;
    }

    public String getLabel(){
        return this.label;
    }

    public int getRequiredDataRate(){
        return this.requiredDataRate;
    }

    public Set<RANTechnologyType> getTechnologyTypes(){
        return this.technologyTypes;
    }
}
