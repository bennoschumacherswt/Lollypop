package com.lollypop;

import com.lollypop.model.Subscriber;
import com.lollypop.model.UserSession;
import com.lollypop.model.enums.ServiceType;
import com.lollypop.model.enums.SubscriptionType;
import com.lollypop.model.enums.TerminalType;
import com.lollypop.service.SubscriberService;
import com.lollypop.service.UserSessionService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry point for the Matsecom Subscriber Management System.
 *
 * Menu:
 *  1 – Add subscriber
 *  2 – List all subscribers
 *  3 – Update subscriber
 *  4 – Remove subscriber
 *  5 – Start session
 *  6 – List sessions for subscriber
 *  7 – Generate invoice
 *  0 – Exit
 */
public class Main {

    private static final Scanner      scanner;
    private static final SubscriberService  subscriberService;
    private static final UserSessionService userSessionService;

    static {
        scanner = new Scanner(System.in);
        ServiceFactory sf = new ServiceFactory();
        subscriberService  = sf.getSubscriberService();
        userSessionService = sf.getUserSessionService();
    }

    // -------------------------------------------------------------------
    // Entry
    // -------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Matsecom Subscriber Management System   ");
        System.out.println("===========================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addSubscriber();
                case "2" -> listSubscribers();
                case "3" -> updateSubscriber();
                case "4" -> removeSubscriber();
                case "5" -> startSession();
                case "6" -> listSessions();
                case "7" -> generateInvoice();
                case "0" -> running = false;
                default  -> System.out.println("  Unknown option, please try again.");
            }
        }

        System.out.println("Goodbye.");
    }

    // -------------------------------------------------------------------
    // Menu handlers
    // -------------------------------------------------------------------

    private static void addSubscriber() {
        System.out.println("\n--- Add Subscriber ---");

        long msin = promptLong("MSIN (10 digits): ");
        String fn = prompt("First name: ");
        String ln = prompt("Last name: ");
        TerminalType    terminal = promptEnum("Terminal type",    TerminalType.values());
        SubscriptionType plan    = promptEnum("Subscription plan", SubscriptionType.values());

        try {
            Subscriber s = subscriberService.addSubscriber(msin, fn, ln, terminal, plan);
            System.out.printf("  Created: %s%n", s);
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void listSubscribers() {
        System.out.println("\n--- All Subscribers ---");
        List<Subscriber> all = subscriberService.getAllSubscribers();
        if (all.isEmpty()) {
            System.out.println("  No subscribers found.");
        } else {
            all.forEach(s -> System.out.println("  " + s));
        }
    }

    private static void updateSubscriber() {
        System.out.println("\n--- Update Subscriber ---");
        int id = promptInt("Subscriber ID: ");

        Optional<Subscriber> existing = subscriberService.getSubscriber(id);
        if (existing.isEmpty()) {
            System.out.println("  No subscriber with id=" + id);
            return;
        }
        System.out.println("  Current: " + existing.get());

        String fn = prompt("New first name: ");
        String ln = prompt("New last name: ");
        TerminalType    terminal = promptEnum("New terminal type", TerminalType.values());
        SubscriptionType plan    = promptEnum("New subscription",  SubscriptionType.values());

        try {
            subscriberService.updateSubscriber(id, fn, ln, terminal, plan);
            System.out.println("  Updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void removeSubscriber() {
        System.out.println("\n--- Remove Subscriber ---");
        int id = promptInt("Subscriber ID: ");
        try {
            subscriberService.removeSubscriber(id);
            System.out.println("  Subscriber " + id + " removed.");
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void startSession() {
        System.out.println("\n--- Start Session ---");
        int id              = promptInt("Subscriber ID: ");
        ServiceType service = promptEnum("Service type", ServiceType.values());
        int duration        = promptInt("Duration (seconds): ");

        try {
            Optional<String> result = subscriberService.doUserSession(id, service, duration);
            if (result.isEmpty()) {
                System.out.println("  Session could not be completed (see reason above).");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void listSessions() {
        System.out.println("\n--- Sessions ---");
        int id = promptInt("Subscriber ID: ");
        List<UserSession> sessions = userSessionService.getSessionsForSubscriber(id);
        System.out.println(userSessionService.formatSessions(sessions));
    }

    private static void generateInvoice() {
        System.out.println("\n--- Generate Invoice ---");
        int id = promptInt("Subscriber ID: ");
        try {
            subscriberService.generateInvoice(id);
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------

    private static void printMenu() {
        System.out.println("""
                
                ─── Main Menu ───────────────────────
                 1  Add subscriber
                 2  List all subscribers
                 3  Update subscriber
                 4  Remove subscriber
                 5  Start session
                 6  List sessions
                 7  Generate invoice
                 0  Exit
                ─────────────────────────────────────
                Choice:\s""");
    }

    private static String prompt(String label) {
        System.out.print("  " + label);
        return scanner.nextLine().trim();
    }

    private static int promptInt(String label) {
        while (true) {
            try { return Integer.parseInt(prompt(label)); }
            catch (NumberFormatException e) { System.out.println("  Please enter a valid integer."); }
        }
    }

    private static long promptLong(String label) {
        while (true) {
            try { return Long.parseLong(prompt(label)); }
            catch (NumberFormatException e) { System.out.println("  Please enter a valid number."); }
        }
    }

    private static <T extends Enum<T>> T promptEnum(String label, T[] values) {
        System.out.println("  " + label + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.printf("    %d. %s%n", i + 1, values[i].name());
        }
        while (true) {
            int choice = promptInt("  Enter number: ");
            if (choice >= 1 && choice <= values.length) return values[choice - 1];
            System.out.println("  Invalid choice, try again.");
        }
    }
}
