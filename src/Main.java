import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import static java.time.temporal.TemporalAdjusters.*;

class Transaction {
    LocalDate datetime;
    String description;
    String vendor;
    double amount;

    Transaction(LocalDate datetime, String description, String vendor, double amount) {
        this.datetime = datetime;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    public String toString() {
        Date date = Date.from(datetime.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss");
        String dts = fmt.format(date);
        return String.format("%s|%s|%s|%.2f", dts, description, vendor, amount);
    }
}

public class Main {
    static String filePath = "transactions.csv";

    static ArrayList<Transaction> transactionList = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);
    static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {

        //LOAD TRANSACTIONS
        loadTransactions();

        //SHOW MAIN MENU
        showHomeScreen();

        //SAVE TRANSACTIONS
        saveTransactions();
    }

    public static void addTransaction(boolean isDeposit) {
        boolean depositing = true;
        while (depositing) {
            System.out.println("\nEnter Date and Time (yyyy-MM-dd HH:mm:ss): ");
            Date date;

            try {
                date = fmt.parse(scanner.nextLine().trim());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            System.out.print("\nEnter Description: ");
            String description = scanner.nextLine().trim();
            System.out.print("\nEnter Vendor Name: ");
            String vendor = scanner.nextLine().trim();
            System.out.print("\nEnter Amount (0.0)");
            double amount = scanner.nextDouble();

            if (isDeposit) {
                amount = -amount; //negative
            }

            transactionList.add(
                    new Transaction(
                            Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate(),
                            description,
                            vendor,
                            amount
                    )
            );

            System.out.print("\nAdd Another?");
            depositing = scanner.nextLine().trim().toLowerCase().matches("true|yes|t|y");
        }
    }

    public static void showHomeScreen() {
        boolean looping = true;
        while (looping) {
            System.out.print("""
                        D) Add Deposit - prompt user for the deposit information and save it to the csv file
                        P) Make Payment (Debit) - prompt user for the debit information and save it to the csv file
                        L) Ledger - display the ledger screen
                        X) Exit - exit the application
                    """);
            switch (scanner.nextLine().trim().toUpperCase()) {
                case "D" -> addTransaction(true);
                case "P" -> addTransaction(false);
                case "L" -> showLedgerScreen();
                case "X" -> looping = false;
                default -> System.out.println("Try again.");
            }
        }
    }

    public static void showTransactions(String kind) {
        System.out.println(kind + ":");
        for (Transaction t : transactionList) {
            if (kind.equalsIgnoreCase("ALL")
                    ||
                    (kind.equalsIgnoreCase("DEPOSITS") && t.amount > 0)
                    ||
                    (kind.equalsIgnoreCase("PAYMENTS") && t.amount < 0)
            ) {
                System.out.println(t);
            }
        }
    }

    public static void showLedgerScreen() {
        boolean looping = true;
        while (looping) {
            System.out.print("""
                            LEDGER SCREEN:
                                A) All
                                D) Deposits
                                P) Payments
                                R) Reports
                                H) Home
                    """);
            switch (scanner.nextLine().trim().toUpperCase()) {
                case "A" -> showTransactions("ALL");
                case "D" -> showTransactions("DEPOSITS");
                case "P" -> showTransactions("PAYMENTS");
                case "R" -> showReportsScreen();
                case "X" -> looping = false;
                default -> System.out.println("Try again.");
            }
        }
    }

    public static boolean isDateInRange(LocalDate dateToCheck, LocalDate startDate, LocalDate endDate) {
        return !(dateToCheck.isBefore(startDate) || dateToCheck.isAfter(endDate));
    }

    public static void showPeriod(String timeframe) {
        LocalDate today = LocalDate.now();

        LocalDate firstDay = switch (timeframe) {
            case "Month To Date" -> today.with(firstDayOfMonth());
            case "Previous Month" -> today.minusMonths(1).with(firstDayOfMonth());
            case "Year To Date" -> today.with(firstDayOfYear());
            case "Previous Year" -> today.minusYears(1).with(firstDayOfYear());
            default -> today;
        };

        LocalDate lastDay = switch (timeframe) {
            case "Previous Month" -> today.minusMonths(1).with(lastDayOfMonth());
            case "Previous Year" -> today.minusYears(1).with(lastDayOfYear());
            default -> today;
        };

        for (Transaction t : transactionList) {
            if (isDateInRange(t.datetime, firstDay, lastDay.plusDays(1))) {
                System.out.println(t);
            }
        }
    }

    public static void searchByVendor() {
        System.out.println("\nENTER VENDOR:");
        String vendor = scanner.nextLine().trim().toUpperCase();
        for (Transaction t : transactionList) {
            if (t.vendor.equalsIgnoreCase(vendor)) {
                System.out.println(t);
            }
        }
    }

    public static void showReportsScreen() {
        boolean looping = true;
        while (looping) {
            System.out.print("""
                    REPORTS SCREEN:
                        1) Month To Date
                        2) Previous Month
                        3) Year To Date
                        4) Previous Year
                        5) Search by Vendor
                        0) Back
                    """);
            switch (scanner.nextLine().trim().toUpperCase()) {
                case "1" -> showPeriod("Month To Date");
                case "2" -> showPeriod("Previous Month");
                case "3" -> showPeriod("Year To Date");
                case "4" -> showPeriod("Previous Year");
                case "5" -> searchByVendor();
                case "0" -> looping = false;
                default -> System.out.println("Try again.");
            }
        }
    }

    public static void loadTransactions() {
        int DATE = 0;
        int TIME = 1;
        int DESCRIPTION = 2;
        int VENDOR = 3;
        int AMOUNT = 4;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String _ = reader.readLine(); //skip first
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                Date d = fmt.parse(parts[DATE] + " " + parts[TIME]);
                LocalDate ld = Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                transactionList.add(new Transaction(
                        ld,
                        parts[DESCRIPTION],
                        parts[VENDOR],
                        Double.parseDouble(parts[AMOUNT])
                ));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveTransactions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("date|time|description|vendor|amount\n");
            for (Transaction t : transactionList) {
                writer.write(t.toString() + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }
}