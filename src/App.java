import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class App {
    protected Scanner scanner = new Scanner(System.in);
    protected SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected Ledger ledger = new Ledger();

    App() {
        this.ledger.load();
        this.showHomeScreen();
        this.ledger.save();
    }

    protected void addTransaction(boolean isDeposit) {
        do {
            LocalDate ld = null;
            while(ld == null) {
                System.out.println("\nEnter Date and Time (yyyy-MM-dd HH:mm:ss): ");
                Date date;
                try {
                    date = fmt.parse(scanner.nextLine().trim());
                    ld = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                } catch (ParseException e) {
                    System.out.println("TRY AGAIN:");
                }
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
            ledger.add(new Transaction(ld, description, vendor, amount));

            System.out.print("\nAdd Another?");
        } while (scanner.nextLine().trim().toLowerCase().matches("true|yes|t|y"));
    }

    protected void showHomeScreen() {
        ArrayList<Action> actions =  new ArrayList<>(List.of(
                new Action("D", "Add Deposit", () -> addTransaction(true)),
                new Action("P", "Make Payment (Debit)", () -> addTransaction(false)),
                new Action("L", "Ledger", this::showLedgerScreen)
        ));
        Menu m = new Menu("Home Screen", actions, "X");
        m.go();
    }

    protected void showLedgerScreen() {
        ArrayList<Action> actions =  new ArrayList<>(List.of(
                new Action("A", "All", () -> ledger.showTransactions("ALL")),
                new Action("D", "Deposits", () -> ledger.showTransactions("DEPOSITS")),
                new Action("P", "Payments", () -> ledger.showTransactions("PAYMENTS")),
                new Action("R", "Reports", this::showReportsScreen)
        ));
        Menu m = new Menu("Home Screen", actions, "H");
        m.go();
    }

    public void searchByVendor() {
        System.out.println("\nENTER VENDOR:");
        String vendor = scanner.nextLine().trim().toUpperCase();
        for (Transaction t : ledger.getByVendor(vendor)) {
            System.out.println(t);
        }
    }

    public void showReportsScreen() {
        ArrayList<Action> actions =  new ArrayList<>(List.of(
                new Action("1", "Month To Date", () -> ledger.showPeriod("Month To Date")),
                new Action("2", "Previous Month", () -> ledger.showPeriod("Previous Month")),
                new Action("3", "Year To Date", () -> ledger.showPeriod("Year To Date")),
                new Action("4", "Previous Year", () -> ledger.showPeriod("Previous Year")),
                new Action("5", "Search by Vendor", this::searchByVendor)
        ));
        Menu m = new Menu("REPORTS SCREEN", actions, "0");
        m.go();
    }
}
