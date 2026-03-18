package Backend.Models.Enums;

public enum SubscriptionType {
    GreenMobilS("GreenMobilS", 500, 0, 8, 0.08 ),
    GreenMobilM("GreenMobilM", 2000, 100, 22, 0.06 ),
    GreenMobilL("GreenMobilL", 5000, 150, 42, 0.04 );


    private final String label;

    private final int dataVolume;
    private final int freeMinutes;
    private final int fee;
    private final double minuteFee;

    SubscriptionType(String label, int dataVolume, int freeMinutes, int fee, double minuteFee){
        this.dataVolume = dataVolume;
        this.freeMinutes = freeMinutes;
        this.label = label;
        this.fee = fee;
        this.minuteFee = minuteFee;
    }

    public int getDataVolume() {
        return dataVolume;
    }

    public int getFreeMinutes(){
        return freeMinutes;
    }

    public String getLabel(){
        return label;
    }

    public int getFee(){
        return fee;
    }

    public double getMinuteFee(){
        return minuteFee;
    }
}
