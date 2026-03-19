package com.lollypop.dao.impl;

import com.lollypop.dao.DatabaseConnection;
import com.lollypop.dao.SubscriberDAO;
import com.lollypop.model.Subscriber;
import com.lollypop.model.enums.SubscriptionType;
import com.lollypop.model.enums.TerminalType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * All SQL column names are taken verbatim from schema.sql.
 * Mapping:
 *   DB: firstname           ↔ Java: subscriber.getFirstname()
 *   DB: lastname            ↔ Java: subscriber.getLastname()
 *   DB: imsi_mcc            ↔ Java: subscriber.getMcc()       (constant 262)
 *   DB: imsi_mnc            ↔ Java: subscriber.getMnc()       (constant 42)
 *   DB: imsi_msin           ↔ Java: subscriber.getMsin()
 *   DB: terminal_type       ↔ Java: subscriber.getTerminalType().name()
 *   DB: subscription_type   ↔ Java: subscriber.getSubscriptionType().name()
 *   DB: remaining_data_mb   ↔ Java: subscriber.getRemainingDataMb()
 */
public class SubscriberDAOImpl implements SubscriberDAO {

    private static final String INSERT =
            "INSERT INTO subscriber " +
            "(firstname, lastname, imsi_mcc, imsi_mnc, imsi_msin, terminal_type, subscription_type, remaining_data_mb) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID =
            "SELECT id, firstname, lastname, imsi_mcc, imsi_mnc, imsi_msin, " +
            "terminal_type, subscription_type, remaining_data_mb " +
            "FROM subscriber WHERE id = ?";

    private static final String SELECT_ALL =
            "SELECT id, firstname, lastname, imsi_mcc, imsi_mnc, imsi_msin, " +
            "terminal_type, subscription_type, remaining_data_mb " +
            "FROM subscriber";

    private static final String UPDATE =
            "UPDATE subscriber SET firstname = ?, lastname = ?, terminal_type = ?, " +
            "subscription_type = ?, remaining_data_mb = ? WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM subscriber WHERE id = ?";

    // -------------------------------------------------------------------

    @Override
    public void create(Subscriber s) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, s.getFirstname());
            stmt.setString(2, s.getLastname());
            stmt.setInt(3,    s.getMcc());
            stmt.setInt(4,    s.getMnc());
            stmt.setLong(5,   s.getMsin());
            stmt.setString(6, s.getTerminalType().name());
            stmt.setString(7, s.getSubscriptionType().name());
            stmt.setDouble(8, s.getRemainingDataMb());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt(1));
            }

        } catch (SQLException e) {
            System.out.println("SubscriberDAO.create failed: " + e.getMessage());
        }
    }

    @Override
    public Optional<Subscriber> findById(int id) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("SubscriberDAO.findById failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Subscriber> findAll() {
        List<Subscriber> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getInstance();
             Statement stmt  = con.createStatement();
             ResultSet rs    = stmt.executeQuery(SELECT_ALL)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.out.println("SubscriberDAO.findAll failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean update(Subscriber s) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(UPDATE)) {

            stmt.setString(1, s.getFirstname());
            stmt.setString(2, s.getLastname());
            stmt.setString(3, s.getTerminalType().name());
            stmt.setString(4, s.getSubscriptionType().name());
            stmt.setDouble(5, s.getRemainingDataMb());
            stmt.setInt(6,    s.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("SubscriberDAO.update failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement stmt = con.prepareStatement(DELETE)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("SubscriberDAO.delete failed: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------

    private Subscriber mapRow(ResultSet rs) throws SQLException {
        int             id       = rs.getInt("id");
        long            msin     = rs.getLong("imsi_msin");
        String          fn       = rs.getString("firstname");
        String          ln       = rs.getString("lastname");
        TerminalType    terminal = TerminalType.valueOf(rs.getString("terminal_type"));
        SubscriptionType plan    = SubscriptionType.valueOf(rs.getString("subscription_type"));
        double          dataMb   = rs.getDouble("remaining_data_mb");

        Subscriber s = new Subscriber(id, msin, fn, ln, terminal, plan);
        s.setRemainingDataMb(dataMb); // override constructor default with persisted value
        return s;
    }
}
