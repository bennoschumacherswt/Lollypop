package Backend.Models;

import Backend.Models.Enums.SubscriptionType;
import Backend.Models.Enums.TerminalType;

import java.util.ArrayList;

public class User {

    /** id to avoid problems with users with same names */
    private int id;

    /** imsi attribute IMSI (MCC, MNC, 10 digit MSIN) ,  */
    private long imsi;

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
    public User(int id, long imsi, String firstName, String lastName, TerminalType terminal,
                SubscriptionType subscription){
        this.id = id;
        this.imsi = imsi;
        this.firstName = firstName;
        this.lastName = lastName;
        this.terminal = terminal;
        this.subscription = subscription;
        this.sessions = new ArrayList<>();
        this.dataVolume = this.subscription.getDataVolume();
    }


    public void create(int id, long imsi, String firstName, String lastName, TerminalType terminal,
                       SubscriptionType subscription){

    }

            Read

    Update

    Calculate charges


    Delete
}
