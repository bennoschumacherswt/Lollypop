package com.lollypop.dao.impl;

import com.lollypop.dao.DatabaseConnection;
import com.lollypop.dao.UserSessionDAO;
import com.lollypop.model.UserSession;
import com.lollypop.model.enums.ServiceType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * All SQL column names are taken verbatim from schema.sql.
 * Mapping:
 *   DB: subscriber_id         ↔ Java: session.getSubscriberId()
 *   DB: service_type          ↔ Java: session.getServiceType().name()
 *   DB: duration_seconds      ↔ Java: session.getDurationSeconds()
 *   DB: used_data_volume_mb   ↔ Java: session.getUsedDataVolumeMb()
 *   DB: charges_eur           ↔ Java: session.getChargesEur()
 */
public class UserSessionDAOImpl implements UserSessionDAO {

    private static final String INSERT =
            "INSERT INTO session (subscriber_id, service_type, duration_seconds, used_data_volume_mb, charges_eur) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT id, subscriber_id, service_type, duration_seconds, used_data_volume_mb, charges_eur " +
            "FROM session WHERE id = ?";

    private static final String SELECT_BY_SUBSCRIBER =
            "SELECT id, subscriber_id, service_type, duration_seconds, used_data_volume_mb, charges_eur " +
            "FROM session WHERE subscriber_id = ?";

    private static final String DELETE_BY_ID =
            "DELETE FROM session WHERE id = ?";

    private static final String DELETE_BY_SUBSCRIBER =
            "DELETE FROM session WHERE subscriber_id = ?";

    // -------------------------------------------------------------------

    @Override
    public void create(UserSession session, int subscriberId) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1,    subscriberId);
            stmt.setString(2, session.getServiceType().name());
            stmt.setInt(3,    session.getDurationSeconds());
            stmt.setDouble(4, session.getUsedDataVolumeMb());
            stmt.setDouble(5, session.getChargesEur());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    session.setId(keys.getInt(1));
                    session.setSubscriberId(subscriberId);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserSessionDAO.create failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UserSession> findById(int id) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserSessionDAO.findById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<UserSession> findBySubscriberId(int subscriberId) {
        List<UserSession> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(SELECT_BY_SUBSCRIBER)) {

            stmt.setInt(1, subscriberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("UserSessionDAO.findBySubscriberId failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean delete(int id) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(DELETE_BY_ID)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("UserSessionDAO.delete failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int deleteBySubscriberId(int subscriberId) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(DELETE_BY_SUBSCRIBER)) {

            stmt.setInt(1, subscriberId);
            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("UserSessionDAO.deleteBySubscriberId failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------

    private UserSession mapRow(ResultSet rs) throws SQLException {
        ServiceType service  = ServiceType.valueOf(rs.getString("service_type"));
        int         duration = rs.getInt("duration_seconds");

        UserSession s = new UserSession(service, duration);
        s.setId(rs.getInt("id"));
        s.setSubscriberId(rs.getInt("subscriber_id"));
        s.setUsedDataVolumeMb(rs.getDouble("used_data_volume_mb"));
        s.setChargesEur(rs.getDouble("charges_eur"));
        return s;
    }
}
