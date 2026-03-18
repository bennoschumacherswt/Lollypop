package com.lollypop.model;

import com.lollypop.model.enums.SubscriptionType;
import com.lollypop.model.enums.TerminalType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a mobile subscriber in the Matsecom network.
 */
public class Subscriber {

    /** Primary key — assigned by the database. */
    private int id;

    /** Mobile Country Code — always 262 for Germany. */
    private final int mcc = 262;

    /** Mobile Network Code — always 42 for Matsecom. */
    private final int mnc = 42;

    /** 10-digit Mobile Subscriber Identification Number. */
    private long msin;

    private String firstname;
    private String lastname;

    /** The handset type this subscriber owns. */
    private TerminalType terminalType;

    /** The subscriber's active contract / plan. */
    private SubscriptionType subscriptionType;

    /**
     * Remaining 3G/4G data quota in MB for the current billing period.
     * Initialised from subscriptionType.getDataVolumeMb() on construction.
     * Decremented each successful data session.
     * Reset to the plan's full allocation after invoicing.
     *
     * Column name in DB: remaining_data_mb
     */
    private double remainingDataMb;

    /**
     * In-memory session list for the current billing period.
     * Populated by the service layer; not stored as a single JSON blob — each
     * session is its own row in the session table.
     */
    private List<UserSession> sessions;

    // -------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------

    public Subscriber(int id, long msin, String firstname, String lastname,
                      TerminalType terminalType, SubscriptionType subscriptionType) {
        this.id               = id;
        this.msin             = msin;
        this.firstname        = firstname;
        this.lastname         = lastname;
        this.terminalType     = terminalType;
        this.subscriptionType = subscriptionType;
        this.remainingDataMb  = subscriptionType.getDataVolumeMb();
        this.sessions         = new ArrayList<>();
    }

    // -------------------------------------------------------------------
    // Getters & setters
    // -------------------------------------------------------------------

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public int    getMcc()              { return mcc; }
    public int    getMnc()              { return mnc; }

    public long   getMsin()             { return msin; }
    public void   setMsin(long msin)    { this.msin = msin; }

    public String getFirstname()                    { return firstname; }
    public void   setFirstname(String firstname)    { this.firstname = firstname; }

    public String getLastname()                     { return lastname; }
    public void   setLastname(String lastname)      { this.lastname = lastname; }

    public TerminalType getTerminalType()                       { return terminalType; }
    public void         setTerminalType(TerminalType t)         { this.terminalType = t; }

    public SubscriptionType getSubscriptionType()               { return subscriptionType; }
    public void             setSubscriptionType(SubscriptionType s) { this.subscriptionType = s; }

    public double getRemainingDataMb()                          { return remainingDataMb; }
    public void   setRemainingDataMb(double remainingDataMb)    { this.remainingDataMb = remainingDataMb; }

    public List<UserSession> getSessions()                      { return sessions; }
    public void              setSessions(List<UserSession> s)   { this.sessions = s; }

    /** Convenience: full IMSI formatted as MCC-MNC-MSIN */
    public String getImsi() {
        return String.format("%d%02d%010d", mcc, mnc, msin);
    }

    @Override
    public String toString() {
        return String.format("Subscriber{id=%d, name='%s %s', IMSI=%s, terminal=%s, plan=%s, dataMb=%.2f}",
                id, firstname, lastname, getImsi(), terminalType, subscriptionType, remainingDataMb);
    }
}
