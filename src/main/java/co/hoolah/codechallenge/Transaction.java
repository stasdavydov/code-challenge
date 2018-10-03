package co.hoolah.codechallenge;

import de.siegmar.fastcsv.reader.CsvRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static co.hoolah.codechallenge.CsvFilter.DATE_TIME_FORMATTER;


public class Transaction {
    public enum Type {
        PAYMENT, REVERSAL
    }

    private enum Index {
        ID, LOCAL_DATE_TIME, AMOUNT, MERCHANT, TYPE, RELATED_ID
    }

    private String id;
    private LocalDateTime localDateTime;
    private BigDecimal amount;
    private String merchant;
    private Type type;
    private String relatedId;

    public Transaction(CsvRow row) throws DataFormatException {
        int fieldCount = row.getFieldCount();
        if (fieldCount < 5 || (
                fieldCount < 6 && Type.REVERSAL == Type.valueOf(row.getField(Index.TYPE.ordinal()))
        )) {
            throw new DataFormatException("Wrong field count " + fieldCount + " in the row " + row);
        }

        id = row.getField(Index.ID.ordinal()).trim();
        localDateTime = LocalDateTime.parse(getField(row, Index.LOCAL_DATE_TIME), DATE_TIME_FORMATTER);
        amount = new BigDecimal(getField(row, Index.AMOUNT));
        merchant = getField(row, Index.MERCHANT);
        type = Type.valueOf(getField(row, Index.TYPE));
        if (type == Type.REVERSAL) {
            relatedId = getField(row, Index.RELATED_ID);
            if (relatedId.length() == 0) {
                throw new DataFormatException("Related transaction ID is not set for row " + row);
            }
        }
    }

    private String getField(CsvRow row, Index field) {
        String value = row.getField(field.ordinal());
        return value == null ? "" : value.trim();
    }

    public boolean isReversal() {
        return type == Type.REVERSAL;
    }

    public boolean isPayment() {
        return type == Type.PAYMENT;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMerchant() {
        return merchant;
    }

    public Type getType() {
        return type;
    }

    public String getRelatedId() {
        return relatedId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", localDateTime=" + localDateTime +
                ", amount=" + amount +
                ", merchant='" + merchant + '\'' +
                ", type=" + type +
                ", relatedId='" + relatedId + '\'' +
                '}';
    }
}
