package com.lollypop;

import com.lollypop.model.Subscriber;
import com.lollypop.model.UserSession;
import com.lollypop.model.enums.ServiceType;
import com.lollypop.model.enums.SubscriptionType;
import com.lollypop.model.enums.TerminalType;
import com.lollypop.service.SubscriberService;
import com.lollypop.service.UserSessionService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
                case "8" -> importData();
                case "9" -> exportData();
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

    private static void importData(){
        System.out.println("Please enter the name of the input file. \nMake sure the file can be found " +
                "in the same directory as the jar-file you are using to run the program! " +
                "\nInput file needs to be a .txt file but pleas provide the name without the type specification");
        String filename = scanner.nextLine() + ".txt";
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for(String line: lines){
                String[] content = line.split(",");
                TerminalType terminalType;
                switch (Integer.parseInt(content[3])){
                    case 0: terminalType = TerminalType.PhairPhone; break;
                    case 1: terminalType = TerminalType.Pear_aphone_4s; break;
                    default: terminalType = TerminalType.Samsung_S42plus;
                }
                SubscriptionType subscriptionType;
                switch (Integer.parseInt(content[4])){
                    case 0: subscriptionType = SubscriptionType.GreenMobilS; break;
                    case 1: subscriptionType = SubscriptionType.GreenMobilM; break;
                    default: subscriptionType = SubscriptionType.GreenMobilL;
                }
                long msin = Long.parseLong(content[2].substring(content[2].length()-10));
                subscriberService.addSubscriber(msin, content[0], content[1], terminalType, subscriptionType);
            }
            System.out.println("done! non-duplicate data read from " + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void exportData(){
        System.out.println("Please enter the file name for the output file. \nWill create name.txt afterwards:");
        String filename = scanner.nextLine() + ".txt";
        System.out.println(filename + " will be created shortly...");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            List<Subscriber> data = subscriberService.getAllSubscribers();
            for(Subscriber sub: data){
                // firstname,lastname,IMSI,TerminalType,SubscriptionType
                writer.append(sub.getFirstname() + "," + sub.getLastname() + "," +
                        sub.getImsi().substring(0,3) + " " + sub.getImsi().substring(3,5) + " " + sub.getImsi().substring(5)
                        + "," + sub.getTerminalType().getId() + "," + sub.getSubscriptionType().getId() + "\n");
            }
            writer.flush();
            writer.close();
            System.out.println("done! " + filename + " created successfully!");
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                 8  Import data from csv-file
                 9  Export data to csv-file
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
