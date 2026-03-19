package com.lollypop.service.impl;

import com.lollypop.dao.SubscriberDAO;
import com.lollypop.dao.UserSessionDAO;
import com.lollypop.model.Subscriber;
import com.lollypop.model.UserSession;
import com.lollypop.model.enums.RANTechnologyType;
import com.lollypop.model.enums.ServiceType;
import com.lollypop.model.enums.SubscriptionType;
import com.lollypop.model.enums.TerminalType;
import com.lollypop.service.SubscriberService;

import java.util.List;
import java.util.Optional;

public class SubscriberServiceImpl implements SubscriberService {

    private final SubscriberDAO    subscriberDAO;
    private final UserSessionDAO   userSessionDAO;

    public SubscriberServiceImpl(SubscriberDAO subscriberDAO, UserSessionDAO userSessionDAO) {
        this.subscriberDAO  = subscriberDAO;
        this.userSessionDAO = userSessionDAO;
    }

    // -------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------

    @Override
    public Subscriber addSubscriber(long msin, String firstname, String lastname,
                                    TerminalType terminalType, SubscriptionType subscriptionType) {
        validateMsin(msin);
        requireNonBlank(firstname, "firstname");
        requireNonBlank(lastname,  "lastname");
        requireNotNull(terminalType,     "terminalType");
        requireNotNull(subscriptionType, "subscriptionType");

        Subscriber s = new Subscriber(0, msin, firstname, lastname, terminalType, subscriptionType);
        subscriberDAO.create(s); // sets s.id
        return s;
    }

    @Override
    public Optional<Subscriber> getSubscriber(int id) {
        return subscriberDAO.findById(id);
    }

    @Override
    public List<Subscriber> getAllSubscribers() {
        return subscriberDAO.findAll();
    }

    @Override
    public void updateSubscriber(int id, String firstname, String lastname,
                                 TerminalType terminalType, SubscriptionType subscriptionType) {
        Subscriber s = require(id);
        requireNonBlank(firstname, "firstname");
        requireNonBlank(lastname,  "lastname");
        requireNotNull(terminalType,     "terminalType");
        requireNotNull(subscriptionType, "subscriptionType");
        if(!s.getSubscriptionType().equals(subscriptionType)){
            generateInvoice(id);
        }

        s.setFirstname(firstname);
        s.setLastname(lastname);
        s.setTerminalType(terminalType);
        s.setSubscriptionType(subscriptionType);
        // set data volume to the data volume of the new subscription
        s.setRemainingDataMb(subscriptionType.getDataVolumeMb());

        subscriberDAO.update(s);
    }

    @Override
    public void removeSubscriber(int id) {
        require(id); // throws if not found
        userSessionDAO.deleteBySubscriberId(id);
        subscriberDAO.delete(id);
    }

    // -------------------------------------------------------------------
    // Session simulation
    // -------------------------------------------------------------------

    @Override
    public Optional<String> doUserSession(int subscriberId, ServiceType serviceType, int durationSeconds) {
        if (durationSeconds <= 0) throw new IllegalArgumentException("durationSeconds must be > 0");
        Subscriber s = require(subscriberId);

        // ---- Voice call -----------------------------------------------
        if (serviceType.isVoice()) {
            // All terminals support 2G, so no terminal capability check needed.
            UserSession session = new UserSession(serviceType, durationSeconds);
            // chargesEur stays 0; voice cost is calculated at invoice time (free-minute logic)
            userSessionDAO.create(session, subscriberId);
            s.getSessions().add(session);

            String display = String.format(
                    "[SESSION OK] %s %s | Voice call | %ds",
                    s.getFirstname(), s.getLastname(), durationSeconds);
            System.out.println(display);
            return Optional.of(display);
        }

        // ---- Data session ---------------------------------------------

        // 1. Determine best technology available on terminal (4G > 3G per RFP)
        RANTechnologyType tech = s.getTerminalType().getBestDataTechnology();
        if (tech == null) {
            System.out.println("[SESSION FAILED] Terminal supports 2G only — data services unavailable.");
            return Optional.empty();
        }

        // 2. Simulate signal quality → achievable rate
        //    RFP signal levels: good=50%, medium=25%, low=10%, n/a=0%
        double achievableRateMbit = simulateDataRate(tech.getMaxDataRateMbit());

        // 3. RFP rule: abort if achievable rate < service's required rate
        double requiredRateMbit = serviceType.getRequiredDataRateMbit();
        if (achievableRateMbit < requiredRateMbit) {
            System.out.printf("[SESSION FAILED] %s: achievable %.1f Mbit/s < required %.1f Mbit/s%n",
                    serviceType, achievableRateMbit, requiredRateMbit);
            return Optional.empty();
        }

        // 4. Calculate used data volume
        //    usedMB = duration(s) × requiredRate(Mbit/s) × 0.125
        //    (0.125 = 1/8: converts megabits to megabytes)
        double usedDataMb = durationSeconds * requiredRateMbit * 0.125;

        // 5. RFP rule: deny session if subscriber has insufficient quota
        if (s.getRemainingDataMb() < usedDataMb) {
            System.out.printf("[SESSION FAILED] Quota exhausted: has %.2f MB, needs %.2f MB%n",
                    s.getRemainingDataMb(), usedDataMb);
            return Optional.empty();
        }

        // 6. Deduct data and persist updated subscriber
        s.setRemainingDataMb(s.getRemainingDataMb() - usedDataMb);
        subscriberDAO.update(s);

        // 7. Persist the session with its data and charge
        UserSession session = new UserSession(serviceType, durationSeconds);
        session.setUsedDataVolumeMb(usedDataMb);
        // Data is covered by the subscription base fee — the RFP provides no per-MB overage price.
        // Enforcement is the quota check above: once quota is exhausted, further sessions are denied.
        // chargesEur remains 0 for data sessions; the invoice totals base fee + voice extras only.
        session.setChargesEur(0);
        userSessionDAO.create(session, subscriberId);
        s.getSessions().add(session);

        String display = String.format(
                "[SESSION OK] %s %s | %s | %s | %.1f Mbit/s | %.2f MB used",
                s.getFirstname(), s.getLastname(),
                serviceType, tech, achievableRateMbit, usedDataMb);
        System.out.println(display);
        return Optional.of(display);
    }

    // -------------------------------------------------------------------
    // Invoicing
    // -------------------------------------------------------------------

    @Override
    public String generateInvoice(int subscriberId) {
        Subscriber       s        = require(subscriberId);
        List<UserSession> sessions = userSessionDAO.findBySubscriberId(subscriberId);
        SubscriptionType plan     = s.getSubscriptionType();

        double totalVoiceSeconds = 0;
        double totalDataMb       = 0;
        double totalDataCharges  = 0;

        for (UserSession session : sessions) {
            if (session.getServiceType().isVoice()) {
                totalVoiceSeconds += session.getDurationSeconds();
            } else {
                totalDataMb      += session.getUsedDataVolumeMb();
                totalDataCharges += session.getChargesEur();
            }
        }

        double totalVoiceMinutes = totalVoiceSeconds / 60.0;

        // Voice charge:
        //   FIXED: original code divided by minute fee instead of multiplying.
        //   FIXED: free minutes were not subtracted before calculating extras.
        double includedMinutes = plan.getIncludedMinutes();
        double extraMinutes    = Math.max(0, totalVoiceMinutes - includedMinutes);
        double voiceChargeEur  = extraMinutes * plan.getPricePerExtraMinuteEur();

        double totalChargesEur = plan.getBaseFeeEur() + voiceChargeEur + totalDataCharges;

        String invoice = String.format(
                "================================================%n" +
                "              MATSECOM INVOICE                  %n" +
                "================================================%n" +
                "Subscriber   : %s %s%n" +
                "IMSI         : %s%n" +
                "Plan         : %s%n" +
                "------------------------------------------------%n" +
                "Voice minutes used   : %.2f min%n" +
                "  Included minutes   : %.0f min%n" +
                "  Extra minutes      : %.2f min%n" +
                "  Voice charge       : €%.2f%n" +
                "------------------------------------------------%n" +
                "Data used            : %.2f MB%n" +
                "------------------------------------------------%n" +
                "Base fee             : €%.2f%n" +
                "TOTAL                : €%.2f%n" +
                "================================================%n",
                s.getFirstname(), s.getLastname(),
                s.getImsi(),
                plan.name(),
                totalVoiceMinutes, includedMinutes, extraMinutes, voiceChargeEur,
                totalDataMb,
                plan.getBaseFeeEur(), totalChargesEur
        );

        System.out.println(invoice);

        // RFP: after invoicing, reset quota and wipe sessions
        s.setRemainingDataMb(plan.getDataVolumeMb());
        subscriberDAO.update(s);
        userSessionDAO.deleteBySubscriberId(subscriberId);
        s.getSessions().clear();

        return invoice;
    }

    // -------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------

    /**
     * Simulates signal quality for 3G/4G per RFP table.
     * good=50%, medium=25%, low=10%, n/a=0% — each with 25% probability.
     */
    private double simulateDataRate(int maxRateMbit) {
        return switch ((int) (Math.random() * 4)) {
            case 0  -> maxRateMbit * 0.50;
            case 1  -> maxRateMbit * 0.25;
            case 2  -> maxRateMbit * 0.10;
            default -> 0.0;
        };
    }

    private Subscriber require(int id) {
        return subscriberDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No subscriber found with id=" + id));
    }

    private void validateMsin(long msin) {
        if (String.valueOf(msin).length() != 10)
            throw new IllegalArgumentException("MSIN must be exactly 10 digits.");
    }

    private void requireNonBlank(String value, String label) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(label + " must not be blank.");
    }

    private void requireNotNull(Object value, String label) {
        if (value == null)
            throw new IllegalArgumentException(label + " must not be null.");
    }
}
