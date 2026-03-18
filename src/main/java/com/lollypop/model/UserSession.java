package com.lollypop.model;

import com.lollypop.model.enums.ServiceType;

/**
 * Represents a single usage session by a subscriber.
 *
 * FIXED: usedDataVolumeMb and chargesEur were missing — added both.
 *        Without them the DAO cannot persist session results and the invoice
 *        service cannot sum charges.
 * FIXED: subscriber_id renamed to subscriberId (Java convention).
 * FIXED: duration is in SECONDS throughout the codebase (column was 'duration_minutes' — renamed).
 */
public class UserSession {

    /** Primary key — assigned by the database. */
    private int id;

    /** FK to subscriber — set by the DAO after INSERT. */
    private int subscriberId;

    /** Service used during this session. */
    private ServiceType serviceType;

    /**
     * Duration of the session in SECONDS.
     * DB column: duration_seconds
     */
    private int durationSeconds;

    /**
     * Data consumed in this session in MB.
     * 0 for voice calls.
     * DB column: used_data_volume_mb
     */
    private double usedDataVolumeMb;

    /**
     * Charge for this session in EUR.
     * Voice call charges are calculated at invoice time (free-minute logic),
     * so this stays 0 for voice and is set during data sessions.
     * DB column: charges_eur
     */
    private double chargesEur;

    // -------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------

    public UserSession(ServiceType serviceType, int durationSeconds) {
        this.serviceType       = serviceType;
        this.durationSeconds   = durationSeconds;
        this.usedDataVolumeMb  = 0;
        this.chargesEur        = 0;
    }

    // -------------------------------------------------------------------
    // Getters & setters
    // -------------------------------------------------------------------

    public int         getId()                          { return id; }
    public void        setId(int id)                    { this.id = id; }

    public int         getSubscriberId()                { return subscriberId; }
    public void        setSubscriberId(int sid)         { this.subscriberId = sid; }

    public ServiceType getServiceType()                 { return serviceType; }

    public int         getDurationSeconds()             { return durationSeconds; }

    public double      getUsedDataVolumeMb()            { return usedDataVolumeMb; }
    public void        setUsedDataVolumeMb(double mb)   { this.usedDataVolumeMb = mb; }

    public double      getChargesEur()                  { return chargesEur; }
    public void        setChargesEur(double eur)        { this.chargesEur = eur; }

    @Override
    public String toString() {
        return String.format("Session{id=%d, service=%s, duration=%ds, data=%.2fMB, charge=€%.4f}",
                id, serviceType, durationSeconds, usedDataVolumeMb, chargesEur);
    }
}
