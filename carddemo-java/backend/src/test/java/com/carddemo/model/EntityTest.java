package com.carddemo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {

    // Transaction entity
    @Test
    void transactionGettersAndSetters() {
        Transaction t = new Transaction();
        t.setTranId("TRAN0001");
        t.setTypeCd("01");
        t.setCatCd(1);
        t.setSource("POS");
        t.setDescription("Test purchase");
        t.setAmount(new BigDecimal("99.99"));
        t.setMerchantId(12345L);
        t.setMerchantName("Test Merchant");
        t.setMerchantCity("Test City");
        t.setMerchantZip("12345");
        t.setCardNum("4111111111111111");
        LocalDateTime now = LocalDateTime.now();
        t.setOrigTs(now);
        t.setProcTs(now);

        assertThat(t.getTranId()).isEqualTo("TRAN0001");
        assertThat(t.getTypeCd()).isEqualTo("01");
        assertThat(t.getCatCd()).isEqualTo(1);
        assertThat(t.getSource()).isEqualTo("POS");
        assertThat(t.getDescription()).isEqualTo("Test purchase");
        assertThat(t.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(t.getMerchantId()).isEqualTo(12345L);
        assertThat(t.getMerchantName()).isEqualTo("Test Merchant");
        assertThat(t.getMerchantCity()).isEqualTo("Test City");
        assertThat(t.getMerchantZip()).isEqualTo("12345");
        assertThat(t.getCardNum()).isEqualTo("4111111111111111");
        assertThat(t.getOrigTs()).isEqualTo(now);
        assertThat(t.getProcTs()).isEqualTo(now);
    }

    // DailyTransaction entity
    @Test
    void dailyTransactionGettersAndSetters() {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("DTRAN001");
        dt.setTypeCd("02");
        dt.setCatCd(2);
        dt.setSource("ATM");
        dt.setDescription("Cash advance");
        dt.setAmount(new BigDecimal("200.00"));
        dt.setMerchantId(67890L);
        dt.setMerchantName("ATM Corp");
        dt.setMerchantCity("ATM City");
        dt.setMerchantZip("54321");
        dt.setCardNum("5500000000000004");
        LocalDateTime now = LocalDateTime.now();
        dt.setOrigTs(now);
        dt.setProcTs(now);

        assertThat(dt.getTranId()).isEqualTo("DTRAN001");
        assertThat(dt.getTypeCd()).isEqualTo("02");
        assertThat(dt.getCatCd()).isEqualTo(2);
        assertThat(dt.getSource()).isEqualTo("ATM");
        assertThat(dt.getDescription()).isEqualTo("Cash advance");
        assertThat(dt.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(dt.getMerchantId()).isEqualTo(67890L);
        assertThat(dt.getMerchantName()).isEqualTo("ATM Corp");
        assertThat(dt.getMerchantCity()).isEqualTo("ATM City");
        assertThat(dt.getMerchantZip()).isEqualTo("54321");
        assertThat(dt.getCardNum()).isEqualTo("5500000000000004");
        assertThat(dt.getOrigTs()).isEqualTo(now);
        assertThat(dt.getProcTs()).isEqualTo(now);
    }

    // Customer entity
    @Test
    void customerGettersAndSetters() {
        Customer c = new Customer();
        c.setCustId(1L);
        c.setFirstName("John");
        c.setMiddleName("M");
        c.setLastName("Doe");
        c.setAddrLine1("123 Main St");
        c.setAddrLine2("Apt 1");
        c.setAddrLine3("Building A");
        c.setAddrStateCd("CA");
        c.setAddrCountryCd("USA");
        c.setAddrZip("90210");
        c.setPhoneNum1("555-1234");
        c.setPhoneNum2("555-5678");
        c.setSsn("123456789");
        c.setGovtIssuedId("DL12345");
        LocalDate dob = LocalDate.of(1990, 1, 1);
        c.setDob(dob);
        c.setEftAccountId("EFT001");
        c.setPriCardHolderInd("Y");
        c.setFicoCreditScore((short) 750);

        assertThat(c.getCustId()).isEqualTo(1L);
        assertThat(c.getFirstName()).isEqualTo("John");
        assertThat(c.getMiddleName()).isEqualTo("M");
        assertThat(c.getLastName()).isEqualTo("Doe");
        assertThat(c.getAddrLine1()).isEqualTo("123 Main St");
        assertThat(c.getAddrLine2()).isEqualTo("Apt 1");
        assertThat(c.getAddrLine3()).isEqualTo("Building A");
        assertThat(c.getAddrStateCd()).isEqualTo("CA");
        assertThat(c.getAddrCountryCd()).isEqualTo("USA");
        assertThat(c.getAddrZip()).isEqualTo("90210");
        assertThat(c.getPhoneNum1()).isEqualTo("555-1234");
        assertThat(c.getPhoneNum2()).isEqualTo("555-5678");
        assertThat(c.getSsn()).isEqualTo("123456789");
        assertThat(c.getGovtIssuedId()).isEqualTo("DL12345");
        assertThat(c.getDob()).isEqualTo(dob);
        assertThat(c.getEftAccountId()).isEqualTo("EFT001");
        assertThat(c.getPriCardHolderInd()).isEqualTo("Y");
        assertThat(c.getFicoCreditScore()).isEqualTo((short) 750);
    }

    // User entity
    @Test
    void userGettersAndSetters() {
        User u = new User();
        u.setUsrId("USR001");
        u.setFirstName("Test");
        u.setLastName("User");
        u.setPassword("PASS1234");
        u.setUserType("U");

        assertThat(u.getUsrId()).isEqualTo("USR001");
        assertThat(u.getFirstName()).isEqualTo("Test");
        assertThat(u.getLastName()).isEqualTo("User");
        assertThat(u.getPassword()).isEqualTo("PASS1234");
        assertThat(u.getUserType()).isEqualTo("U");
    }

    @Test
    void userParameterizedConstructor() {
        User u = new User("USR002", "First", "Last", "PASS5678", "A");
        assertThat(u.getUsrId()).isEqualTo("USR002");
        assertThat(u.getFirstName()).isEqualTo("First");
        assertThat(u.getLastName()).isEqualTo("Last");
        assertThat(u.getPassword()).isEqualTo("PASS5678");
        assertThat(u.getUserType()).isEqualTo("A");
    }

    // TranType entity
    @Test
    void tranTypeGettersAndSetters() {
        TranType tt = new TranType();
        tt.setTypeCd("01");
        tt.setTypeDesc("Purchase");

        assertThat(tt.getTypeCd()).isEqualTo("01");
        assertThat(tt.getTypeDesc()).isEqualTo("Purchase");
    }

    // TranCategory entity
    @Test
    void tranCategoryGettersAndSetters() {
        TranCategory tc = new TranCategory();
        tc.setTypeCd("01");
        tc.setCatCd(1);
        tc.setCatDesc("Regular Sales Draft");

        assertThat(tc.getTypeCd()).isEqualTo("01");
        assertThat(tc.getCatCd()).isEqualTo(1);
        assertThat(tc.getCatDesc()).isEqualTo("Regular Sales Draft");
    }

    // Account entity
    @Test
    void accountGettersAndSetters() {
        Account a = new Account();
        a.setAcctId(1L);
        a.setActiveStatus("Y");
        a.setCurrBal(new BigDecimal("1000.00"));
        a.setCreditLimit(new BigDecimal("5000.00"));
        a.setCashCreditLimit(new BigDecimal("2000.00"));
        LocalDate today = LocalDate.now();
        a.setOpenDate(today);
        a.setExpirationDate(today);
        a.setReissueDate(today);
        a.setCurrCycCredit(new BigDecimal("100.00"));
        a.setCurrCycDebit(new BigDecimal("50.00"));
        a.setAddrZip("12345");
        a.setGroupId("GRP001");

        assertThat(a.getAcctId()).isEqualTo(1L);
        assertThat(a.getActiveStatus()).isEqualTo("Y");
        assertThat(a.getCurrBal()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(a.getCreditLimit()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(a.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(a.getOpenDate()).isEqualTo(today);
        assertThat(a.getExpirationDate()).isEqualTo(today);
        assertThat(a.getReissueDate()).isEqualTo(today);
        assertThat(a.getCurrCycCredit()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(a.getCurrCycDebit()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(a.getAddrZip()).isEqualTo("12345");
        assertThat(a.getGroupId()).isEqualTo("GRP001");
    }

    // Card entity
    @Test
    void cardGettersAndSetters() {
        Card c = new Card();
        c.setCardNum("4111111111111111");
        c.setCustId(1L);
        c.setAcctId(1L);
        c.setActiveStatus("Y");
        c.setExpirationDate(LocalDate.of(2025, 12, 31));

        assertThat(c.getCardNum()).isEqualTo("4111111111111111");
        assertThat(c.getCustId()).isEqualTo(1L);
        assertThat(c.getAcctId()).isEqualTo(1L);
        assertThat(c.getActiveStatus()).isEqualTo("Y");
        assertThat(c.getExpirationDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    // CardXref entity
    @Test
    void cardXrefGettersAndSetters() {
        CardXref x = new CardXref();
        x.setCardNum("4111111111111111");
        x.setCustId(1L);
        x.setAcctId(1L);

        assertThat(x.getCardNum()).isEqualTo("4111111111111111");
        assertThat(x.getCustId()).isEqualTo(1L);
        assertThat(x.getAcctId()).isEqualTo(1L);
    }

    // DisclosureGroup entity
    @Test
    void disclosureGroupGettersAndSetters() {
        DisclosureGroup g = new DisclosureGroup();
        g.setGroupId("GRP001");
        g.setTranTypeCd("01");
        g.setTranCatCd(1);
        g.setIntRate(new BigDecimal("15.000"));

        assertThat(g.getGroupId()).isEqualTo("GRP001");
        assertThat(g.getTranTypeCd()).isEqualTo("01");
        assertThat(g.getTranCatCd()).isEqualTo(1);
        assertThat(g.getIntRate()).isEqualByComparingTo(new BigDecimal("15.000"));
    }

    // TranCatBalance entity
    @Test
    void tranCatBalanceGettersAndSetters() {
        TranCatBalance b = new TranCatBalance();
        b.setAcctId(1L);
        b.setTypeCd("01");
        b.setCatCd(1);
        b.setBalance(new BigDecimal("500.00"));

        assertThat(b.getAcctId()).isEqualTo(1L);
        assertThat(b.getTypeCd()).isEqualTo("01");
        assertThat(b.getCatCd()).isEqualTo(1);
        assertThat(b.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
    }
}
