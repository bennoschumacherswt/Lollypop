package com.lollypop.service.impl;

import com.lollypop.dao.UserSessionDAO;
import com.lollypop.model.UserSession;
import com.lollypop.service.UserSessionService;

import java.util.List;

public class UserSessionServiceImpl implements UserSessionService {

    private final UserSessionDAO userSessionDAO;

    public UserSessionServiceImpl(UserSessionDAO userSessionDAO) {
        this.userSessionDAO = userSessionDAO;
    }

    @Override
    public List<UserSession> getSessionsForSubscriber(int subscriberId) {
        return userSessionDAO.findBySubscriberId(subscriberId);
    }

    @Override
    public void clearSessionsForSubscriber(int subscriberId) {
        userSessionDAO.deleteBySubscriberId(subscriberId);
    }

    @Override
    public String formatSessions(List<UserSession> sessions) {
        if (sessions.isEmpty()) return "No sessions recorded.\n";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s %-35s %-12s %-14s %n",
                "ID", "Service", "Duration(s)", "DataUsed(MB)"));
        sb.append("-".repeat(80)).append("\n");

        for (UserSession s : sessions) {
            sb.append(String.format("%-5d %-35s %-12d %-14.2f %n",
                    s.getId(),
                    s.getServiceType().name(),
                    s.getDurationSeconds(),
                    s.getUsedDataVolumeMb()
//                    , s.getChargesEur()
            ));
        }
        return sb.toString();
    }
}
