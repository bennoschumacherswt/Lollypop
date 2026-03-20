package com.lollypop.ui;

/**
 * Inline user documentation for the Lollypop Subscriber Management System.
 * Written for non-developers.
 */
public final class Documentation {

    private Documentation() {}

    public static String getText() {
        return """
═══════════════════════════════════════════════════════════════
  LOLLYPOP — Matsecom Subscriber Management System
  User Documentation  |  v1.0
═══════════════════════════════════════════════════════════════

GETTING STARTED
───────────────
When you launch Lollypop the application checks for a safety
backup from the previous session. If the app had crashed before,
it will automatically restore any missing data from that backup.
Once everything is confirmed OK, you can start using the app.

NAVIGATION
──────────
Use the sidebar on the left to switch between sections:

  👥 Subscribers   — View, add, edit and delete subscribers.
  📡 Sessions      — Start usage sessions and view session history.
  🧾 Invoices      — Generate billing invoices for subscribers.
  📂 Import/Export — Load or save subscriber data as CSV files.

You can also use:
  ❓ Help          — Quick reference panel (this summary).
  📖 Documentation — Full documentation (this window).
  ⏻  Exit          — Close the application cleanly.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

SUBSCRIBERS
───────────
The Subscribers section shows a table of all registered subscribers.

Adding a new subscriber:
  1. Fill in the form on the right side:
       • MSIN        — The subscriber's 10-digit identification number.
       • First Name  — Only letters, spaces, hyphens and apostrophes.
       • Last Name   — Same rules as First Name.
       • Terminal    — The type of phone the subscriber uses.
       • Plan        — The subscription tier (S, M or L).
  2. Click "Save".

Editing a subscriber:
  1. Click on any row in the table to select that subscriber.
  2. The form on the right fills automatically.
  3. Make your changes and click "Save".
  Note: The MSIN cannot be changed after creation.

Deleting a subscriber:
  1. Click the subscriber in the table.
  2. Click "Delete".
  3. Confirm the deletion. This also removes all their sessions.

Available plans:
  • GreenMobilS  — €8/month,   0 free minutes,  500 MB data
  • GreenMobilM  — €22/month, 100 free minutes, 2048 MB data
  • GreenMobilL  — €42/month, 150 free minutes, 5120 MB data

Available terminals:
  • PhairPhone       — 2G + 3G (max 20 Mbit/s)
  • Pear_aphone_4s   — 2G + 3G (max 20 Mbit/s)
  • Samsung_S42plus  — 2G + 3G + 4G (max 300 Mbit/s)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

SESSIONS
────────
A session represents a subscriber using a service (e.g. a phone
call or downloading an app).

Starting a session:
  1. Enter the Subscriber ID (shown in the Subscribers table).
  2. Select a Service Type from the dropdown:
       • Voice call              — A voice call.
       • Browsing and social     — Light internet use. Needs 3G+.
       • App download            — Downloading apps. Needs 3G+.
       • Adaptive HD video       — HD streaming. Needs strong 3G+.
  3. Enter the duration in seconds.
  4. Click "Start Session".

A session may fail if:
  • The subscriber's data quota is exhausted.
  • The simulated signal is too weak for the requested service.
  • The subscriber's terminal does not support the required technology.

Viewing sessions:
  1. Enter the Subscriber ID in the "Subscriber ID" box at the top.
  2. Click "Load Sessions" to see all their sessions in the table.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INVOICES
────────
Invoices show a subscriber's billing summary for the current period.

Preview (no changes):
  1. Enter the Subscriber ID.
  2. Click "Preview (no reset)".
  This shows what the invoice would look like without changing anything.

Generate invoice:
  1. Enter the Subscriber ID.
  2. Click "Generate Invoice".
  This produces the final invoice, resets the subscriber's data quota
  back to their plan's allowance, and clears all recorded sessions.

How charges are calculated:
  • Base fee        — Fixed monthly fee for the plan.
  • Voice charge    — Extra minutes beyond the plan's included minutes,
                      multiplied by the per-minute rate.
  • Data            — Covered by the base fee; no extra charge per MB.
  • TOTAL           — Base fee + voice charge.

Extra minute rates:
  • GreenMobilS  — €0.08 per minute (0 included)
  • GreenMobilM  — €0.06 per minute (100 included)
  • GreenMobilL  — €0.04 per minute (150 included)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

IMPORT / EXPORT
───────────────
Import from CSV:
  Click "Browse & Import CSV" and select a .csv or .txt file.

  Expected column order (no header required, header is auto-skipped):
    firstname, lastname, imsi_msin, terminal_index, plan_index

  Terminal index values:  0 = PhairPhone, 1 = Pear_aphone_4s, 2 = Samsung_S42plus
  Plan index values:      0 = GreenMobilS, 1 = GreenMobilM, 2 = GreenMobilL

  Example row:
    John,Doe,1234567890,1,2

  Rows with duplicate IMSI numbers are automatically skipped.
  Invalid rows are logged but do not stop the import.

Export to CSV:
  Click "Save as CSV" and choose where to save the file.
  All current subscribers are exported in the following format:
    firstname, lastname, imsi, terminal_type, subscription_type, remaining_data_mb

The Operation Log on the right shows a line-by-line report of
what was imported or exported.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

SAFETY & CRASH RECOVERY
────────────────────────
Every time you make a change (add/update/delete a subscriber,
generate an invoice, import data), the application automatically
writes a safety snapshot to two files in the same folder as the
application:

  lollypop_safety.lock  — Timestamp log of the current session.
  lollypop_backup.csv   — Latest snapshot of all subscriber data.

If the application crashes or is closed unexpectedly, these files
are left behind. The next time you start the application:
  1. It detects the safety file.
  2. It reads the backup CSV.
  3. It re-inserts any subscribers that are missing from the database.
  4. Normal operation resumes.

When you exit normally using the "Exit" button, both files are
deleted automatically.

You do not need to do anything — recovery is fully automatic.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

DATABASE CONFIGURATION
──────────────────────
The application connects to a MySQL / MariaDB database using the
settings in dbconfig.properties, which must be in the same folder
as the application JAR:

  db.url      = jdbc:mysql://localhost:3306/lollypop?useSSL=false&serverTimezone=UTC
  db.user     = root
  db.password = test

If you need to change the database host, username or password,
edit this file with a text editor before starting the application.

Make sure the database and tables exist by running schema.sql
(found in src/main/resources/) in your MySQL client before
first use.

═══════════════════════════════════════════════════════════════
  End of Documentation
═══════════════════════════════════════════════════════════════
""";
    }
}
