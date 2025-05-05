import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import static java.time.temporal.TemporalAdjusters.*;


public class Ledger {
    protected String filePath = "transactions.csv";
    protected ArrayList<Transaction> transactionList = new ArrayList<>();
    protected SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected void add(Transaction t) {
        transactionList.add(t);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Transaction t : getAll()) {
            sb.append(t);
        }
        return sb.toString();
    }

    public ArrayList<Transaction> getAll() {
        ArrayList<Transaction> list = new ArrayList<>(this.transactionList);
        list.sort(Comparator.comparing(Transaction::getDatetime));
        return new ArrayList<Transaction>(list.reversed());
    }

    protected static boolean isDateInRange(LocalDate dateToCheck, LocalDate startDate, LocalDate endDate) {
        return !(dateToCheck.isBefore(startDate) || dateToCheck.isAfter(endDate));
    }

//    public ArrayList<Transaction> getPeriod(LocalDate firstDay, LocalDate lastDay) {
//        ArrayList<Transaction> list = new ArrayList<>();
//        for (Transaction t : transactionList) {
//            if (isDateInRange(t.datetime, firstDay, lastDay.plusDays(1))) {
//                list.add(t);
//            }
//        }
//        return list;
//    }

    public ArrayList<Transaction> getByVendor(String vendor) {
        ArrayList<Transaction> list = new ArrayList<>();
        for (Transaction t : getAll()) {
            if (t.vendor.equalsIgnoreCase(vendor)) {
                list.add(t);
            }
        }
        return list;
    }

    protected void load() {
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
                } catch (ParseException e) {
                    System.err.printf("Error parsing date on line %d", i);
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

    protected void save() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("date|time|description|vendor|amount\n");
            for (Transaction t : transactionList) {
                writer.write(t.toString() + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
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

        for (Transaction t : getAll()) {
            if (isDateInRange(t.datetime, firstDay, lastDay.plusDays(1))) {
                System.out.println(t);
            }
        }
    }

    protected void showTransactions(String kind) {
        System.out.println(kind + ":");

        for (Transaction t : getAll()) {
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
}
