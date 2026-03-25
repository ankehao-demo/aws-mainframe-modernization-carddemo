package com.carddemo.batch;

import com.carddemo.model.*;
import com.carddemo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FixedWidthDataLoaderTest {

    @Mock private AccountRepository accountRepository;
    @Mock private CardRepository cardRepository;
    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private DailyTransactionRepository dailyTransactionRepository;
    @Mock private DisclosureGroupRepository disclosureGroupRepository;
    @Mock private TranCatBalanceRepository tranCatBalanceRepository;
    @Mock private TranTypeRepository tranTypeRepository;
    @Mock private TranCategoryRepository tranCategoryRepository;

    @InjectMocks
    private FixedWidthDataLoader loader;

    @Test
    void shouldParseAccountLine() {
        // First line from acctdata.txt
        String line = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000                                                                                                                                                                                            ";

        Account a = loader.parseAccount(line);

        assertThat(a.getAcctId()).isEqualTo(1L);
        assertThat(a.getActiveStatus()).isEqualTo("Y");
        assertThat(a.getCurrBal()).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(a.getCreditLimit()).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(a.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("1020.00"));
        assertThat(a.getOpenDate()).hasToString("2014-11-20");
        assertThat(a.getExpirationDate()).hasToString("2025-05-20");
        assertThat(a.getReissueDate()).hasToString("2025-05-20");
        assertThat(a.getCurrCycCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(a.getCurrCycDebit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldParseCardLine() {
        String line = "050002445376574000000000050747Aniya Von                                         2023-03-09Y                                                           ";

        Card c = loader.parseCard(line);

        assertThat(c.getCardNum()).isEqualTo("0500024453765740");
        assertThat(c.getAcctId()).isEqualTo(50L);
        assertThat(c.getExpirationDate()).hasToString("2023-03-09");
        assertThat(c.getActiveStatus()).isEqualTo("Y");
    }

    @Test
    void shouldParseCardXrefLine() {
        String line = "050002445376574000000005000000000050";

        CardXref x = loader.parseCardXref(line);

        assertThat(x.getCardNum()).isEqualTo("0500024453765740");
        assertThat(x.getCustId()).isEqualTo(50L);
        assertThat(x.getAcctId()).isEqualTo(50L);
    }

    @Test
    void shouldParseCustomerLine() {
        String line = "000000001Immanuel                 Madeline                 Kessler                  618 Deshaun Route                                 Apt. 802                                          Altenwerthshire                                   NCUSA12546     (908)119-8310  (373)693-8684  020973888000000000000493684371961-06-080053581756Y274                                                                                                                                                                        ";

        Customer c = loader.parseCustomer(line);

        assertThat(c.getCustId()).isEqualTo(1L);
        assertThat(c.getFirstName()).isEqualTo("Immanuel");
        assertThat(c.getMiddleName()).isEqualTo("Madeline");
        assertThat(c.getLastName()).isEqualTo("Kessler");
        assertThat(c.getAddrStateCd()).isEqualTo("NC");
        assertThat(c.getAddrCountryCd()).isEqualTo("USA");
        assertThat(c.getSsn()).isEqualTo("020973888");
        assertThat(c.getDob()).hasToString("1961-06-08");
        assertThat(c.getPriCardHolderInd()).isEqualTo("Y");
        assertThat(c.getFicoCreditScore()).isEqualTo((short) 274);
    }

    @Test
    void shouldParseDailyTransactionLine() {
        String line = "0000000000683580010001POS TERM  Purchase at Abshire-Lowe                                                                            0000005047G800000000Abshire-Lowe                                      North Enoshaven                                   72112     48594526128770652022-06-10 19:27:53.000000                                              ";

        DailyTransaction t = loader.parseDailyTransaction(line);

        assertThat(t.getTranId()).isEqualTo("0000000000683580");
        assertThat(t.getTypeCd()).isEqualTo("01");
        assertThat(t.getCatCd()).isEqualTo(1);
        assertThat(t.getSource()).isEqualTo("POS TERM");
        assertThat(t.getAmount()).isEqualByComparingTo(new BigDecimal("504.77"));
        assertThat(t.getMerchantId()).isEqualTo(800000000L);
        assertThat(t.getMerchantName()).isEqualTo("Abshire-Lowe");
        assertThat(t.getCardNum()).isEqualTo("4859452612877065");
    }

    @Test
    void shouldParseDisclosureGroupLine() {
        String line = "A00000000001000100150{0000000000000000000000000000";

        DisclosureGroup g = loader.parseDisclosureGroup(line);

        assertThat(g.getGroupId()).isEqualTo("A000000000");
        assertThat(g.getTranTypeCd()).isEqualTo("01");
        assertThat(g.getTranCatCd()).isEqualTo(1);
        assertThat(g.getIntRate()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void shouldParseTranCatBalanceLine() {
        String line = "000000000010100010000000000{0000000000000000000000";

        TranCatBalance b = loader.parseTranCatBalance(line);

        assertThat(b.getAcctId()).isEqualTo(1L);
        assertThat(b.getTypeCd()).isEqualTo("01");
        assertThat(b.getCatCd()).isEqualTo(1);
        assertThat(b.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldParseTranTypeLine() {
        String line = "01Purchase                                          00000000";

        TranType t = loader.parseTranType(line);

        assertThat(t.getTypeCd()).isEqualTo("01");
        assertThat(t.getTypeDesc()).isEqualTo("Purchase");
    }

    @Test
    void shouldParseTranCategoryLine() {
        String line = "010001Regular Sales Draft                               0000";

        TranCategory c = loader.parseTranCategory(line);

        assertThat(c.getTypeCd()).isEqualTo("01");
        assertThat(c.getCatCd()).isEqualTo(1);
        assertThat(c.getCatDesc()).isEqualTo("Regular Sales Draft");
    }

    // Load method tests

    @Test
    void shouldLoadAccounts() throws IOException {
        String line = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000                                                                                                                                                                                            ";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadAccounts(is);

        assertThat(count).isEqualTo(1);
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadAccountsSkipEmpty() throws IOException {
        String data = "\n   \n";
        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadAccounts(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadCards() throws IOException {
        String line = "050002445376574000000000050747Aniya Von                                         2023-03-09Y                                                           ";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadCards(is);

        assertThat(count).isEqualTo(1);
        verify(cardRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadCardsSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadCards(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadCardXrefs() throws IOException {
        String line = "050002445376574000000005000000000050";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadCardXrefs(is);

        assertThat(count).isEqualTo(1);
        verify(cardXrefRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadCardXrefsSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("  \n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadCardXrefs(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadCustomers() throws IOException {
        String line = "000000001Immanuel                 Madeline                 Kessler                  618 Deshaun Route                                 Apt. 802                                          Altenwerthshire                                   NCUSA12546     (908)119-8310  (373)693-8684  020973888000000000000493684371961-06-080053581756Y274                                                                                                                                                                        ";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadCustomers(is);

        assertThat(count).isEqualTo(1);
        verify(customerRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadCustomersSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadCustomers(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadDailyTransactions() throws IOException {
        String line = "0000000000683580010001POS TERM  Purchase at Abshire-Lowe                                                                            0000005047G800000000Abshire-Lowe                                      North Enoshaven                                   72112     48594526128770652022-06-10 19:27:53.000000                                              ";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadDailyTransactions(is);

        assertThat(count).isEqualTo(1);
        verify(dailyTransactionRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadDailyTransactionsSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("   \n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadDailyTransactions(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadDisclosureGroups() throws IOException {
        String line = "A00000000001000100150{0000000000000000000000000000";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadDisclosureGroups(is);

        assertThat(count).isEqualTo(1);
        verify(disclosureGroupRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadDisclosureGroupsSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadDisclosureGroups(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadTranCatBalances() throws IOException {
        String line = "000000000010100010000000000{0000000000000000000000";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadTranCatBalances(is);

        assertThat(count).isEqualTo(1);
        verify(tranCatBalanceRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadTranCatBalancesSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadTranCatBalances(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadTranTypes() throws IOException {
        String line = "01Purchase                                          00000000";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadTranTypes(is);

        assertThat(count).isEqualTo(1);
        verify(tranTypeRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadTranTypesSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadTranTypes(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadTranCategories() throws IOException {
        String line = "010001Regular Sales Draft                               0000";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadTranCategories(is);

        assertThat(count).isEqualTo(1);
        verify(tranCategoryRepository).saveAll(anyList());
    }

    @Test
    void shouldLoadTranCategoriesSkipEmpty() throws IOException {
        InputStream is = new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8));

        int count = loader.loadTranCategories(is);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldLoadMultipleAccounts() throws IOException {
        String line1 = "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000                                                                                                                                                                                            ";
        String line2 = "00000000002Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000                                                                                                                                                                                            ";
        String data = line1 + "\n" + line2 + "\n";
        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadAccounts(is);

        assertThat(count).isEqualTo(2);
        verify(accountRepository).saveAll(anyList());
    }

    @Test
    void shouldHandleDailyTransactionWithProcTs() throws IOException {
        String line = "0000000000683580010001POS TERM  Purchase at Abshire-Lowe                                                                            0000005047G800000000Abshire-Lowe                                      North Enoshaven                                   72112     48594526128770652022-06-10 19:27:53.0000002022-06-10 20:00:00.000000";
        InputStream is = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));

        int count = loader.loadDailyTransactions(is);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldParseAccountWithInvalidDate() {
        // Use an invalid date format to test parseDate returning null
        String line = "00000000001Y00000001940{00000020200{00000010200{INVALID   2025-05-202025-05-2000000000000{00000000000{A000000000                                                                                                                                                                                            ";

        Account a = loader.parseAccount(line);

        assertThat(a.getAcctId()).isEqualTo(1L);
        assertThat(a.getOpenDate()).isNull();
    }
}
