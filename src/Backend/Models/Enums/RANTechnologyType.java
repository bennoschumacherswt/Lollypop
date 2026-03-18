package Backend.Models.Enums;

public enum RANTechnologyType {

    G2(0, "2G"),
    G3(20, "3G"),
    G4(75, "4G");

    private final int dataRate;
    private final String label;

    RANTechnologyType(int dataRate, String label) {
        this.dataRate = dataRate;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public int getDataRate(){
        return this.dataRate;
    }
}