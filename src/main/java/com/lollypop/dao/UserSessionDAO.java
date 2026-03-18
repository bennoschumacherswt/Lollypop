package com.lollypop.dao;

import com.lollypop.model.UserSession;

import java.util.List;
import java.util.Optional;

public interface UserSessionDAO {

    /** Inserts a session row; sets session.id and session.subscriberId from result. */
    void create(UserSession session, int subscriberId);

    Optional<UserSession> findById(int id);

    List<UserSession> findBySubscriberId(int subscriberId);

    boolean delete(int id);

    /** Deletes all sessions for a subscriber — called after invoice is generated. */
    int deleteBySubscriberId(int subscriberId);
}
