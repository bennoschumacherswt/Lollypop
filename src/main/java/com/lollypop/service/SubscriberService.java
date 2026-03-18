package com.lollypop.service;

import com.lollypop.model.Subscriber;
import com.lollypop.model.enums.ServiceType;
import com.lollypop.model.enums.SubscriptionType;
import com.lollypop.model.enums.TerminalType;

import java.util.List;
import java.util.Optional;

public interface SubscriberService {

    Subscriber addSubscriber(long msin, String firstname, String lastname,
                             TerminalType terminalType, SubscriptionType subscriptionType);

    Optional<Subscriber> getSubscriber(int id);

    List<Subscriber> getAllSubscribers();

    void updateSubscriber(int id, String firstname, String lastname,
                          TerminalType terminalType, SubscriptionType subscriptionType);

    void removeSubscriber(int id);

    /**
     * Simulates one subscriber session (voice or data).
     * Returns a formatted session summary, or empty if the session failed
     * (signal too weak, data quota exhausted, or incompatible terminal).
     */
    Optional<String> doUserSession(int subscriberId, ServiceType serviceType, int durationSeconds);

    /**
     * Generates a formatted invoice for the subscriber.
     * Resets remaining data quota and clears all sessions after invoicing.
     */
    String generateInvoice(int subscriberId);
}
