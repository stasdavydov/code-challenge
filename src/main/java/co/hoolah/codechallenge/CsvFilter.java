package co.hoolah.codechallenge;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CsvFilter {
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private LocalDateTime dateTimeFrom;
    private LocalDateTime dateTimeTo;
    private String merchant;

    private Reader reader;
    private Map<String, Transaction> transactions;
    private BigDecimal totalAmount;

    public CsvFilter(Reader csvContentReader, String dateFrom, String dateTo, String merchantName) throws IOException, DataFormatException {
        reader = csvContentReader;
        dateTimeFrom = LocalDateTime.parse(dateFrom, DATE_TIME_FORMATTER);
        dateTimeTo = LocalDateTime.parse(dateTo, DATE_TIME_FORMATTER);
        merchant = merchantName;

        evaluate();
    }

    private void evaluate() throws IOException, DataFormatException {
        if(transactions == null) {
            CsvReader csvReader = new CsvReader();
            csvReader.setContainsHeader(true);

            try (CsvParser parser = csvReader.parse(reader)) {
                transactions = new HashMap<>();

                CsvRow row;
                while ((row = parser.nextRow()) != null) {
                    Transaction transaction = new Transaction(row);

                    // 1. skip past transactions
                    if (transaction.getLocalDateTime().isBefore(dateTimeFrom)) {
                        continue;
                    }

                    // 2. skip other merchants
                    if (! merchant.equals(transaction.getMerchant())) {
                        continue;
                    }

                    // 3. collect payment transactions before filtered date
                    if (transaction.isPayment()
                            && transaction.getLocalDateTime().isBefore(dateTimeTo)) {
                        transactions.put(transaction.getId(), transaction);
                    } else
                        // 4. remove related transaction from list
                        if (transaction.isReversal()) {
                            transactions.remove(transaction.getRelatedId());
                        }
                }
            }
        }
    }

    public int getTransactionNumber() throws Exception {
        if (transactions == null) {
            throw new Exception("CsvFilter should be evaluated first");
        }
        return transactions.size();
    }

    private void sum(Transaction transaction) {
        totalAmount = totalAmount.add(transaction.getAmount());
    }

    public BigDecimal getAverageTransactionValue() throws Exception {
        if (transactions == null) {
            throw new Exception("CsvFilter should be evaluated first");
        }
        if (totalAmount == null) {
            totalAmount = new BigDecimal("0.00");
            transactions.values().forEach(this::sum);
        }
        return totalAmount.divide(new BigDecimal(getTransactionNumber()), 2, BigDecimal.ROUND_CEILING);
    }

    public static void main(String[] args) {
        if(args.length != 4) {
            System.err.println("Required arguments are " +
                    "<csv file path> <date from> <date to> <merchant>");
            return;
        }
        String filePath = args[0];
        String dateFrom = args[1];
        String dateTo = args[2];
        String merchant = args[3];

        System.out.println("Filter " + filePath +
                " by dateFrom=" + dateFrom + ", dateTo=" + dateTo + ", merchant=" + merchant);

        try {
            CsvFilter csvFilter = new CsvFilter(new FileReader(filePath), dateFrom, dateTo, merchant);
            int transactionNumber = csvFilter.getTransactionNumber();
            BigDecimal averageTransactionValue = csvFilter.getAverageTransactionValue();

            System.out.println("Transaction Number: " + transactionNumber);
            System.out.println("Average Transaction Value: " + averageTransactionValue);
        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
