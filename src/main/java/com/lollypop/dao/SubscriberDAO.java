package com.lollypop.dao;

import com.lollypop.model.Subscriber;

import java.util.List;
import java.util.Optional;

public interface SubscriberDAO {

    /** Inserts a new subscriber row; sets subscriber.id from generated key. */
    void create(Subscriber subscriber);

    Optional<Subscriber> findById(int id);

    List<Subscriber> findAll();

    /** Updates all mutable scalar fields (name, terminal, plan, remainingDataMb). */
    boolean update(Subscriber subscriber);

    /** Deletes subscriber and all their sessions (ON DELETE CASCADE). */
    boolean delete(int id);
}
