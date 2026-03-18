package Backend.Models.Enums;

public enum RANTechnologyType {

    G2(0, "2G"),
    G3(20, "3G"),
    G4(75, "4G");

    private final double dataRate;
    private final String label;

    RANTechnologyType(double dataRate, String label) {
        this.dataRate = dataRate;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public double getDataRate(){
        int rand = (int) (Math.random() * 4);
        return switch (rand) {
            case 1 -> this.dataRate * 0.1;
            case 2 -> this.dataRate * 0.25;
            case 3 -> this.dataRate * 0.5;
            default -> 0;
        };

    }
}