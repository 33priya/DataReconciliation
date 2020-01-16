package data.reconciliation.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.out;

public class DataReconciliationEngine {

  static class TransactionPair {
    private final Transaction left;
    private final Transaction right;

    public TransactionPair(Transaction left, Transaction right) {
      this.left = left;
      this.right = right;
    }

    public Transaction getLeft() {
      return left;
    }

    public Transaction getRight() {
      return right;
    }
  }

  public static void main(String[] args) {
    String filePathX = "x.txt"; // args[0];
    String filePathY = "y.txt"; // args[1];
    List<Transaction> x_transactions = getTransactions(filePathX);
    List<Transaction> y_transactions = getTransactions(filePathY);

    List<TransactionPair> exact_matches = new ArrayList<>();
    List<TransactionPair> weak_matches = new ArrayList<>();
    List<Transaction> x_breaks = new ArrayList<>();
    List<Transaction> y_breaks = new ArrayList<>();

    int x_index = 0, y_index = 0;
    while (x_index < x_transactions.size() && y_index < y_transactions.size()) {
      Transaction left = x_transactions.get(x_index);
      Transaction right = y_transactions.get(y_index);

      if (left.equals(right)) {
        x_index++;
        y_index++;
        exact_matches.add(new TransactionPair(left, right));
        continue;
      }

      int account_compare_result = left.getAccountId().compareTo(right.getAccountId());
      if (account_compare_result == 0) {

        if (left.partiallyEquals(right)) {
          weak_matches.add(new TransactionPair(left, right));
          x_index++;
          y_index++;
        }

        else if (left.lessThanUpperBound(right)) {
          y_breaks.add(right);
          y_index++;
        }

        else {
          x_breaks.add(left);
//          y_breaks.add(right);
          x_index++;
//          y_index++;
        }
      }
      else if (account_compare_result < 0) {
        x_breaks.add(left);
        x_index++;
      }
      else {
        y_breaks.add(right);
        y_index++;
      }
    }

    while (x_index != x_transactions.size()) {
      x_breaks.add(x_transactions.get(x_index++));
    }

    while (y_index != y_transactions.size()) {
      y_breaks.add(y_transactions.get(y_index++));
    }

    // Done with calculations, printing the results
    displayTransactionPairs("Report\n#XY Exact Matches\n", exact_matches);
    displayTransactionPairs("\n\n#XY Weak Matches\n", weak_matches);
    displayTransaction("X", x_breaks);
    displayTransaction("Y", y_breaks);
  }

  private static void displayTransactionPairs(String message, List<TransactionPair> matches) {
    out.println(message);
    for (TransactionPair pair : matches)
      out.print(String.format("%s%s, ", pair.left.getTransactionId(), pair.right.getTransactionId()));
  }

  private static void displayTransaction(String message, List<Transaction> breaks) {
    out.println("\n\n#" + message + "Breaks\n");
    for (Transaction transaction : breaks) {
      out.print(String.format("%s,", transaction.getTransactionId()));
    }
  }

  private static List<Transaction> getTransactions(String filePath) {
    try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
      List<Transaction> transactions = new ArrayList<>();
      stream.forEach(line -> transactions.add(Transaction.getInstance(line)));
      return transactions;
    } catch (IOException e) {
      throw new DREException("Unable to read line");
    }
  }

}
