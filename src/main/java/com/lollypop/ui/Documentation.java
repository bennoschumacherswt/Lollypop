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
        			LOLLYPOP INC. — Matsecom Subscriber Management System
        			User Documentation  |  v1.0
        		═══════════════════════════════════════════════════════════════

									NAVIGATION
				──────────
				Use the sidebar on the left to switch between sections:

					👥 Subscribers   — View, add and delete subscriber profiles. Edit personal data.
					📡 Sessions      — Start usage sessions and view session history.
					🧾 Invoices      — Generate billing invoices for subscribers.
					📂 Import/Export — Load or save subscriber data as .csv files.

				You can also use:
				  ❓ Help          — Quick reference panel (this summary).It will provide the user with contact
				  					information for the Technical Support Team.
				  📖 Documentation — Full documentation (current window).
				  ⏻  Exit          — Close the application.
				
				━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
				
				SUBSCRIBERS
				───────────
				The Subscribers section shows a table of all registered and currently active subscribers.
				
				Adding a new subscriber:
				  1. Fill in the form on the right side:
				       • MSIN        — The subscriber's 10-digit identification number.
				       • First Name  — Only letters, spaces, hyphens and apostrophes are allowed to be submitted.
				       • Last Name   — Same rules as First Name.
				       • Terminal    — The phone model used by the subscriber.
				       • Plan        — The subscription tier (S, M or L).
				  2. Click "Save".
				
				Editing a subscriber:
				  1. Click on any row in the overview table to select that subscriber.
				  2. The form on the right automatically displays the subscriber's personal data.
				  3. Make changes by editing the text fields.  
				  4. Click "Save".
				  Note: The MSIN cannot be edited.
				
				Deleting a subscriber profile:
				  1. Click on the subscriber in the overview table.
				  2. Click "Delete".
				  3. Confirm. This also removes all records of the subscriber's past sessions.
				
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
				call or download of an app).
				
				Starting a session:
				  1. Enter the Subscriber ID (shown in the Subscribers table).
				  2. Select a service type from the dropdown:
				       • Voice call              — A voice phone call.
				       • Browsing and social     — Light internet use. Requires 3G+.
				       • App download            — Downloading apps. Requires 3G+.
				       • Adaptive HD video       — HD streaming. Requires strong 3G+.
				  3. Enter the session duration time (in seconds).
				  4. Click "Start Session".
				
				A session may fail if:
				  • The maximum capacity of a subscriber's data volume is exceeded.
				  • The simulated signal is too weak for the requested service.
				  • The subscriber's terminal type does not support the required technology.
				
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
				  This produces the final invoice, resets the subscriber's data volume value
				  back to their plan's monthly allowance and clears all recorded sessions.
				
				How charges are calculated:
				  • Base fee        — Fixed monthly fee for the plan.
				  • Voice charge    — Extra minutes beyond the plan's included minutes,
				                      multiplied by the per-minute rate.
				  • Data            — Covered by the base fee - no extra charge per MB.
				  • TOTAL           — Sum of base fee and voice charge.
				
				Extra minute rates:
				  • GreenMobilS  — €0.08 per minute (0 included)
				  • GreenMobilM  — €0.06 per minute (100 included)
				  • GreenMobilL  — €0.04 per minute (150 included)
				
				━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
				
				IMPORT / EXPORT
				───────────────
				Import from CSV:
				  Click "Browse & Import CSV" and select a .csv.
				
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
				
				The operation log on the right shows a line-by-line report of
				what was imported or exported.
				
				━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
				
				SAFETY & CRASH RECOVERY
				────────────────────────
				Every time you make a change (add/update/delete a subscriber,
				generate an invoice, import data), the application automatically
				creates two files in the same folder as the application:
				
				  lollypop_safety.lock  — Timestamp log of the current session.
				  lollypop_backup.csv   — Latest record of all subscriber data.
				
				If the application crashes or is closed unexpectedly, these files
				are left behind. The next time you start the application:
				  1. It detects the safety file.
				  2. It reads the backup .csv.
				  3. It reinserts any subscribers that are missing from the database.
				  4. Normal operation resumes.
				
				When you exit normally using the "Exit" button, both files are
				deleted automatically.
				
				You do not need to do anything — recovery is fully automatic.
				
				━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
				
				DATABASE CONFIGURATION
				──────────────────────
				The application connects to a MySQL / MariaDB database using the
				default settings in dbconfig.properties, which must be in the same folder
				as the application JAR:
				
				  db.url      = jdbc:mysql://localhost:3306/lollypop?useSSL=false&serverTimezone=UTC
				  db.user     = root
				  db.password = test
				
				If you need to change the database host, username or password,
				edit this file with a text editor before starting the application.
				
				Make sure the database and tables exist by running schema.sql
				(found in src/main/resources/) in your MySQL client before
				launching the application.
				
				═══════════════════════════════════════════════════════════════
				  End of Documentation
				═══════════════════════════════════════════════════════════════
				""";
				    }
				}
