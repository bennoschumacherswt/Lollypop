package Backend.Models;

import Backend.Models.Enums.RANTechnologyType;
import Backend.Models.Enums.ServiceType;
import Backend.Models.Enums.SubscriptionType;
import Backend.Models.Enums.TerminalType;

import java.util.ArrayList;

public class User {

    /** id to avoid problems with users with same names, primary key in db */
    private int id;

    /** mcc that should be 49 for all German users */
    private int MCC = 49;

    /** mnc that should be 42 for all users that are customers of this company */
    public int MNC = 42;

    /** msin attribute to build the IMSI: (MCC, MNC, 10 digit MSIN) */
    private long msin;

    /** first name of the user */
    private String firstName;

    /** last name of the user */
    private String lastName;

    /** type of terminal aka mobile phone user has, might be null if user does not have one atm */
    private TerminalType terminal;

    /** type of subscription the user has, might be null if not subscribed to any service atm  */
    private SubscriptionType subscription;

    /** list of UserSession to store all the sessions and calculate costs when invoice happens */
    private ArrayList<UserSession> sessions;

    /** attribute to track how much dataVolume a user has left */
    private double dataVolume;


    /**
     * constructor for User objects handing over all attributes besides sessions and dataVolume,
     *  dataVolume is taken out of the subscriptionType enum
     *  sessions is an empty list that gets new sessions added each time the user uses a service
     */
    public User(int id, long msin, String firstName, String lastName, TerminalType terminal,
                SubscriptionType subscription){
        this.id = id;
        this.msin = msin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.terminal = terminal;
        this.subscription = subscription;
        this.sessions = new ArrayList<>();
        this.dataVolume = this.subscription.getDataVolume();
    }


    public void create(int id, long msin, String firstName, String lastName, TerminalType terminal,
                       SubscriptionType subscription){

    }

    /**
     * method used to simulate a UserSession for the current user this.
     */
    public void doUserSession(UserSession session){
        // if it is a voice call it gets added to the list of sessions for the invoice period and calculated
        // at the end
        if(session.getService().equals(ServiceType.Voice_call)){
            this.sessions.add(session);
            return;
        }
        double availableDataRate = 0;

        // check if 4G is available on the phone of this customer and use it if so
         if(this.getTerminal().getSupportedTechnologies().contains(RANTechnologyType.G4)){
             availableDataRate = getDataRate(RANTechnologyType.G4.getDataRate());
         }

         // if 4G is not available or the signal was weak and not there use 3G instead
         if(availableDataRate == 0){
            availableDataRate = getDataRate(RANTechnologyType.G3.getDataRate());
        }
        double usedDataVolume = session.getDuration() * session.getService().getRequiredDataRate() * 0.125;

    }

    /** this function is used to simulate weaker and stronger signals */
    private static double getDataRate(int dataRate){
        int rand = (int) (Math.random() * 4);
        return switch (rand) {
            case 1 -> dataRate * 0.1;
            case 2 -> dataRate * 0.25;
            case 3 -> dataRate * 0.5;
            default -> 0;
        };
    }

    public double calculateCharges(){
        // TODO check if it should be calculated by beginning minutes or if it is ok to add all durations
        // TODO to each other and then calculate the extra minutes that need to be paid for
        double callTime = 0;
        for (UserSession session: sessions){
            if(session.getService().equals(ServiceType.Voice_call)) {
                callTime += session.getDuration();
            }
        }
        return this.subscription.getFee() + callTime / this.subscription.getMinuteFee();
    }

    // TODO Read a user out of the database

    // TODO Update one of the Users

    // TODO Delete a User out of Database

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMNC() {
        return MNC;
    }

    public void setMNC(int MNC) {
        this.MNC = MNC;
    }

    public int getMCC() {
        return MCC;
    }

    public void setMCC(int MCC) {
        this.MCC = MCC;
    }

    public long getMsin() {
        return msin;
    }

    public void setMsin(long msin) {
        this.msin = msin;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public TerminalType getTerminal() {
        return terminal;
    }

    public void setTerminal(TerminalType terminal) {
        this.terminal = terminal;
    }

    public ArrayList<UserSession> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<UserSession> sessions) {
        this.sessions = sessions;
    }

    public double getDataVolume() {
        return dataVolume;
    }

    public void setDataVolume(double dataVolume) {
        this.dataVolume = dataVolume;
    }

    public SubscriptionType getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionType subscription) {
        this.subscription = subscription;
    }
}
