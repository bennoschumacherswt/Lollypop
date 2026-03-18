package com.lollypop.service;

import com.lollypop.model.UserSession;

import java.util.List;

public interface UserSessionService {

    List<UserSession> getSessionsForSubscriber(int subscriberId);

    void clearSessionsForSubscriber(int subscriberId);

    /** Returns a tabular, human-readable summary of the given sessions. */
    String formatSessions(List<UserSession> sessions);
}
