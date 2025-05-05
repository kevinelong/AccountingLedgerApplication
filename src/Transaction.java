import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Transaction {
    protected LocalDate datetime;
    protected String description;
    protected String vendor;
    protected double amount;

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

    public LocalDate getDatetime() {
        return datetime;
    }
}
