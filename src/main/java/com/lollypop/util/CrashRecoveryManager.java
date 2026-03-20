package com.lollypop.util;

import com.lollypop.dao.DatabaseConnection;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Safety-file crash-recovery system.
 *
 * On startup  : if safety.lock exists → last run crashed → attempt DB restore from backup CSV.
 * While running: safety.lock is kept alive with a timestamp of last known state.
 * On clean exit: safety.lock is deleted.
 *
 * The "backup" is a simple CSV snapshot written alongside the lock file.
 */
public class CrashRecoveryManager {

    private static final Logger LOG = Logger.getLogger(CrashRecoveryManager.class.getName());

    public static final Path SAFETY_FILE  = Paths.get("lollypop_safety.lock");
    public static final Path BACKUP_CSV   = Paths.get("lollypop_backup.csv");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Called on startup: if safety file exists the previous run crashed. */
    public void checkAndRecover() {
        if (Files.exists(SAFETY_FILE)) {
            LOG.warning("Safety file found — previous session may have crashed. Attempting recovery...");
            restoreFromBackup();
        }
    }

    /**
     * Writes (or overwrites) the safety lock file with the current timestamp.
     * Also snapshots the subscriber table to CSV as a lightweight backup.
     */
    public void writeSafetyFile() {
        try {
            String ts = LocalDateTime.now().format(FMT);
            Files.writeString(SAFETY_FILE, "started=" + ts + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            snapshotToBackup();
        } catch (IOException e) {
            LOG.warning("Could not write safety file: " + e.getMessage());
        }
    }

    /** Appends a timestamped checkpoint to the safety file. Called after any DB mutation. */
    public void checkpoint(String event) {
        try {
            String line = LocalDateTime.now().format(FMT) + " | " + event + System.lineSeparator();
            Files.writeString(SAFETY_FILE, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            snapshotToBackup();
        } catch (IOException e) {
            LOG.warning("Checkpoint write failed: " + e.getMessage());
        }
    }

    /** Deletes the safety file — called on clean application exit. */
    public void deleteSafetyFile() {
        try {
            Files.deleteIfExists(SAFETY_FILE);
            Files.deleteIfExists(BACKUP_CSV);
            LOG.info("Safety files deleted — clean exit.");
        } catch (IOException e) {
            LOG.warning("Could not delete safety file: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private void snapshotToBackup() {
        try (Connection con = DatabaseConnection.getInstance();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id,firstname,lastname,imsi_mcc,imsi_mnc,imsi_msin," +
                     "terminal_type,subscription_type,remaining_data_mb FROM subscriber");
             ResultSet rs = ps.executeQuery();
             BufferedWriter bw = Files.newBufferedWriter(BACKUP_CSV,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            bw.write("id,firstname,lastname,imsi_mcc,imsi_mnc,imsi_msin,terminal_type,subscription_type,remaining_data_mb");
            bw.newLine();
            while (rs.next()) {
                bw.write(
                    rs.getInt("id") + "," +
                    escape(rs.getString("firstname")) + "," +
                    escape(rs.getString("lastname")) + "," +
                    rs.getInt("imsi_mcc") + "," +
                    rs.getInt("imsi_mnc") + "," +
                    rs.getLong("imsi_msin") + "," +
                    rs.getString("terminal_type") + "," +
                    rs.getString("subscription_type") + "," +
                    rs.getDouble("remaining_data_mb")
                );
                bw.newLine();
            }
        } catch (Exception e) {
            LOG.warning("Backup snapshot failed: " + e.getMessage());
        }
    }

    private void restoreFromBackup() {
        if (!Files.exists(BACKUP_CSV)) {
            LOG.info("No backup CSV found — skipping restore.");
            return;
        }
        try (Connection con = DatabaseConnection.getInstance();
             BufferedReader br = Files.newBufferedReader(BACKUP_CSV)) {

            String header = br.readLine(); // skip header
            if (header == null) return;

            String line;
            int restored = 0;
            while ((line = br.readLine()) != null) {
                String[] f = line.split(",", -1);
                if (f.length < 9) continue;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO subscriber " +
                        "(id,firstname,lastname,imsi_mcc,imsi_mnc,imsi_msin,terminal_type,subscription_type,remaining_data_mb) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)")) {
                    ps.setInt(1,    Integer.parseInt(f[0].trim()));
                    ps.setString(2, unescape(f[1]));
                    ps.setString(3, unescape(f[2]));
                    ps.setInt(4,    Integer.parseInt(f[3].trim()));
                    ps.setInt(5,    Integer.parseInt(f[4].trim()));
                    ps.setLong(6,   Long.parseLong(f[5].trim()));
                    ps.setString(7, f[6].trim());
                    ps.setString(8, f[7].trim());
                    ps.setDouble(9, Double.parseDouble(f[8].trim()));
                    ps.executeUpdate();
                    restored++;
                }
            }
            LOG.info("Recovery: inserted " + restored + " subscriber row(s).");
        } catch (Exception e) {
            LOG.warning("Restore failed: " + e.getMessage());
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace(",", ";").replace("\n", " ");
    }

    private String unescape(String s) {
        return s == null ? "" : s.trim();
    }
}
