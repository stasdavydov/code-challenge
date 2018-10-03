package co.hoolah.codechallenge;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.math.BigDecimal;

class CsvFilterTest {
    @Test
    void test() throws Exception {
        String from = "20/08/2018 12:00:00";
        String to = "20/08/2018 13:00:00";
        String merchant = "Kwik-E-Mart";

        CsvFilter csvFilter = new CsvFilter(
                new InputStreamReader(this.getClass().getResourceAsStream("/test.csv")),
                from,
                to,
                merchant);

        int expectedTransactionNumber = 1;
        BigDecimal expectedAmount = new BigDecimal("59.99");

        Assertions.assertEquals(expectedTransactionNumber, csvFilter.getTransactionNumber());
        Assertions.assertEquals(expectedAmount, csvFilter.getAverageTransactionValue());
    }
}
