import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import static java.time.temporal.TemporalAdjusters.*;

public class App{
    protected String filePath = "transactions.csv";
    protected ArrayList<Transaction> transactionList = new ArrayList<>();
    protected Scanner scanner = new Scanner(System.in);
    protected SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void run(){
        this.loadTransactions();
        this.showHomeScreen();
        this.saveTransactions();
    }

    protected void addTransaction(boolean isDeposit) {
        do {
            System.out.println("\nEnter Date and Time (yyyy-MM-dd HH:mm:ss): ");
            Date date;
            try {
                date = fmt.parse(scanner.nextLine().trim());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            LocalDate ld = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

            System.out.print("\nEnter Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("\nEnter Vendor Name: ");
            String vendor = scanner.nextLine().trim();

            System.out.print("\nEnter Amount (0.0)");
            double amount = scanner.nextDouble();

            if (isDeposit) {
                amount = -amount; //negative
            }

            transactionList.add(new Transaction(ld, description, vendor, amount));

            System.out.print("\nAdd Another?");
        }while(scanner.nextLine().trim().toLowerCase().matches("true|yes|t|y"));
    }

    protected void showHomeScreen() {
        String c;
        do {
            System.out.print("""
                        HOME SCREEN:
                            D) Add Deposit - prompt user for the deposit information and save it to the csv file
                            P) Make Payment (Debit) - prompt user for the debit information and save it to the csv file
                            L) Ledger - display the ledger screen
                            X) Exit - exit the application
                    """);
            c = scanner.nextLine().trim().toUpperCase();
            switch (c) {
                case "D" -> addTransaction(true);
                case "P" -> addTransaction(false);
                case "L" -> showLedgerScreen();
                case "X" -> System.out.println("Leaving Home Screen.");
                default -> System.out.println("Try again.");
            }
        }while(!"X".equals(c));
    }

    protected void showTransactions(String kind) {
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

    protected void showLedgerScreen() {
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

    protected static boolean isDateInRange(LocalDate dateToCheck, LocalDate startDate, LocalDate endDate) {
        return !(dateToCheck.isBefore(startDate) || dateToCheck.isAfter(endDate));
    }

    protected void showPeriod(String timeframe) {
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

    public void searchByVendor() {
        System.out.println("\nENTER VENDOR:");
        String vendor = scanner.nextLine().trim().toUpperCase();
        for (Transaction t : transactionList) {
            if (t.vendor.equalsIgnoreCase(vendor)) {
                System.out.println(t);
            }
        }
    }

    public void showReportsScreen() {
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

    public void loadTransactions() {
        int DATE = 0, TIME = 1, DESCRIPTION = 2, VENDOR = 3, AMOUNT = 4;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String _ = reader.readLine(); //skip first
            String line;
            int i = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                Date d;
                LocalDate ld;
                try {
                    d = fmt.parse(parts[DATE] + " " + parts[TIME]);
                    ld = Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                }catch (ParseException e){
                    System.out.printf("Error parsing date on line %d", i);
                    ld = LocalDate.now();
                }
                transactionList.add(
                        new Transaction(ld, parts[DESCRIPTION], parts[VENDOR], Double.parseDouble(parts[AMOUNT]))
                );
                i++;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public void saveTransactions() {
        try {
            BufferedWriter writer  = new BufferedWriter(new FileWriter(filePath));
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
