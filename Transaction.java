package data.reconciliation.engine;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public final class Transaction {
  private static final double AMOUNT_DIFF = 0.01;
  private static final long DATE_DIFF = 1;

  private final String txnId;
  private final String accountId;
  private final Date date;
  private final int dateOfYear;
  private final int dayOfWeek;
  private final double amount;

  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
  public static Transaction getInstance(String transactionStmt) {
    String[] params = transactionStmt.split(";");
    Date date = null;
    try {
      date = simpleDateFormat.parse(params[2]);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    assert date != null;
    return new Transaction(params[0], params[1], date, Double.parseDouble(params[3]));
  }
  private Transaction(final String txnId, final String accountId, final Date postingDate, final double amount) {
    this.txnId = txnId;
    this.accountId = accountId;
    this.date = new Date (postingDate.getTime());

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(this.date);
    this.dateOfYear = calendar.get(Calendar.DAY_OF_YEAR);
    this.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    this.amount = amount;
  }

  public String getTransactionId() {
    return txnId;
  }

  public String getAccountId() {
    return accountId;
  }

  public Date getDate() {
    return new Date (date.getTime());
  }

  public double getAmount() {
    return amount;
  }

  public boolean partiallyEquals(Transaction transaction) {
    if (!this.accountId.equals(transaction.accountId)) return false;
    int day_diff = this.dateOfYear - transaction.dateOfYear;

    if (Math.abs(day_diff) == 3) { // taking care of weekends
      if (!((this.dayOfWeek == Calendar.MONDAY && transaction.dayOfWeek == Calendar.FRIDAY && day_diff == 3) ||
              (this.dayOfWeek == Calendar.FRIDAY && transaction.dayOfWeek == Calendar.MONDAY && day_diff == -3))) {
        return false;
      }
    }
    else if (Math.abs(this.dateOfYear - transaction.dateOfYear) > DATE_DIFF) return false;

    DecimalFormat df = new DecimalFormat("#.##");
    double diff = Double.parseDouble(df.format(Math.abs(this.amount - transaction.amount)));
    return !(diff > AMOUNT_DIFF);
  }

  /**
   * this + buffer >= transaction
   */
  public boolean lessThanUpperBound(Transaction transaction) {
    if (!this.accountId.equals(transaction.accountId)) return false;
    if ((this.dateOfYear + DATE_DIFF) < transaction.dateOfYear) return false;
    return !((this.amount + AMOUNT_DIFF) < transaction.amount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Transaction that = (Transaction) o;
    return Double.compare(that.amount, amount) == 0 &&
            accountId.equals(that.accountId) &&
            date.equals(that.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, date, amount);
  }

}
