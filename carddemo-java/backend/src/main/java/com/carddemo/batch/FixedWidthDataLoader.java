package com.carddemo.batch;

import com.carddemo.model.*;
import com.carddemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads data from fixed-width ASCII files exported from COBOL/VSAM into the database.
 * Handles COBOL zoned decimal encoding for numeric fields.
 */
@Service
public class FixedWidthDataLoader {

    private static final Logger log = LoggerFactory.getLogger(FixedWidthDataLoader.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final TranTypeRepository tranTypeRepository;
    private final TranCategoryRepository tranCategoryRepository;

    public FixedWidthDataLoader(
            AccountRepository accountRepository,
            CardRepository cardRepository,
            CardXrefRepository cardXrefRepository,
            CustomerRepository customerRepository,
            DailyTransactionRepository dailyTransactionRepository,
            DisclosureGroupRepository disclosureGroupRepository,
            TranCatBalanceRepository tranCatBalanceRepository,
            TranTypeRepository tranTypeRepository,
            TranCategoryRepository tranCategoryRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.tranTypeRepository = tranTypeRepository;
        this.tranCategoryRepository = tranCategoryRepository;
    }

    @Transactional
    public int loadAccounts(InputStream inputStream) throws IOException {
        List<Account> accounts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Account a = parseAccount(line);
                accounts.add(a);
            }
        }
        accountRepository.saveAll(accounts);
        log.info("Loaded {} accounts", accounts.size());
        return accounts.size();
    }

    Account parseAccount(String line) {
        // CVACT01Y.cpy: RECLN 300
        // ACCT-ID PIC 9(11) = pos 0-10
        // ACCT-ACTIVE-STATUS PIC X(01) = pos 11
        // ACCT-CURR-BAL PIC S9(10)V99 = pos 12-23 (12 chars zoned decimal)
        // ACCT-CREDIT-LIMIT PIC S9(10)V99 = pos 24-35
        // ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99 = pos 36-47
        // ACCT-OPEN-DATE PIC X(10) = pos 48-57
        // ACCT-EXPIRAION-DATE PIC X(10) = pos 58-67
        // ACCT-REISSUE-DATE PIC X(10) = pos 68-77
        // ACCT-CURR-CYC-CREDIT PIC S9(10)V99 = pos 78-89
        // ACCT-CURR-CYC-DEBIT PIC S9(10)V99 = pos 90-101
        // ACCT-ADDR-ZIP PIC X(10) = pos 102-111
        // ACCT-GROUP-ID PIC X(10) = pos 112-121
        Account a = new Account();
        a.setAcctId(Long.parseLong(line.substring(0, 11).trim()));
        a.setActiveStatus(line.substring(11, 12));
        a.setCurrBal(CobolZonedDecimalParser.parseMoney(line.substring(12, 24)));
        a.setCreditLimit(CobolZonedDecimalParser.parseMoney(line.substring(24, 36)));
        a.setCashCreditLimit(CobolZonedDecimalParser.parseMoney(line.substring(36, 48)));
        a.setOpenDate(parseDate(line.substring(48, 58).trim()));
        a.setExpirationDate(parseDate(line.substring(58, 68).trim()));
        a.setReissueDate(parseDate(line.substring(68, 78).trim()));
        a.setCurrCycCredit(CobolZonedDecimalParser.parseMoney(line.substring(78, 90)));
        a.setCurrCycDebit(CobolZonedDecimalParser.parseMoney(line.substring(90, 102)));
        a.setAddrZip(line.substring(102, 112).trim());
        a.setGroupId(line.substring(112, 122).trim());
        return a;
    }

    @Transactional
    public int loadCards(InputStream inputStream) throws IOException {
        List<Card> cards = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Card c = parseCard(line);
                cards.add(c);
            }
        }
        cardRepository.saveAll(cards);
        log.info("Loaded {} cards", cards.size());
        return cards.size();
    }

    Card parseCard(String line) {
        // CVACT02Y.cpy: RECLN 150
        // CARD-NUM PIC X(16) = pos 0-15
        // CARD-ACCT-ID PIC 9(11) = pos 16-26
        // CARD-CVV-CD PIC 9(03) = pos 27-29
        // CARD-EMBOSSED-NAME PIC X(50) = pos 30-79
        // CARD-EXPIRAION-DATE PIC X(10) = pos 80-89
        // CARD-ACTIVE-STATUS PIC X(01) = pos 90
        Card c = new Card();
        c.setCardNum(line.substring(0, 16).trim());
        c.setAcctId(Long.parseLong(line.substring(16, 27).trim()));
        // skip CVV (pos 27-29) and embossed name (pos 30-79) - not in our schema
        c.setExpirationDate(parseDate(line.substring(80, 90).trim()));
        c.setActiveStatus(line.substring(90, 91));
        // custId will be resolved from cardxref
        return c;
    }

    @Transactional
    public int loadCardXrefs(InputStream inputStream) throws IOException {
        List<CardXref> xrefs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                CardXref x = parseCardXref(line);
                xrefs.add(x);
            }
        }
        cardXrefRepository.saveAll(xrefs);
        log.info("Loaded {} card xrefs", xrefs.size());
        return xrefs.size();
    }

    CardXref parseCardXref(String line) {
        // CVACT03Y.cpy: RECLN 50
        // XREF-CARD-NUM PIC X(16) = pos 0-15
        // XREF-CUST-ID PIC 9(09) = pos 16-24
        // XREF-ACCT-ID PIC 9(11) = pos 25-35
        CardXref x = new CardXref();
        x.setCardNum(line.substring(0, 16).trim());
        x.setCustId(Long.parseLong(line.substring(16, 25).trim()));
        x.setAcctId(Long.parseLong(line.substring(25, 36).trim()));
        return x;
    }

    @Transactional
    public int loadCustomers(InputStream inputStream) throws IOException {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Customer c = parseCustomer(line);
                customers.add(c);
            }
        }
        customerRepository.saveAll(customers);
        log.info("Loaded {} customers", customers.size());
        return customers.size();
    }

    Customer parseCustomer(String line) {
        // CVCUS01Y.cpy: RECLN 500
        // CUST-ID PIC 9(09) = pos 0-8
        // CUST-FIRST-NAME PIC X(25) = pos 9-33
        // CUST-MIDDLE-NAME PIC X(25) = pos 34-58
        // CUST-LAST-NAME PIC X(25) = pos 59-83
        // CUST-ADDR-LINE-1 PIC X(50) = pos 84-133
        // CUST-ADDR-LINE-2 PIC X(50) = pos 134-183
        // CUST-ADDR-LINE-3 PIC X(50) = pos 184-233
        // CUST-ADDR-STATE-CD PIC X(02) = pos 234-235
        // CUST-ADDR-COUNTRY-CD PIC X(03) = pos 236-238
        // CUST-ADDR-ZIP PIC X(10) = pos 239-248
        // CUST-PHONE-NUM-1 PIC X(15) = pos 249-263
        // CUST-PHONE-NUM-2 PIC X(15) = pos 264-278
        // CUST-SSN PIC 9(09) = pos 279-287
        // CUST-GOVT-ISSUED-ID PIC X(20) = pos 288-307
        // CUST-DOB-YYYY-MM-DD PIC X(10) = pos 308-317
        // CUST-EFT-ACCOUNT-ID PIC X(10) = pos 318-327
        // CUST-PRI-CARD-HOLDER-IND PIC X(01) = pos 328
        // CUST-FICO-CREDIT-SCORE PIC 9(03) = pos 329-331
        Customer c = new Customer();
        c.setCustId(Long.parseLong(line.substring(0, 9).trim()));
        c.setFirstName(line.substring(9, 34).trim());
        c.setMiddleName(line.substring(34, 59).trim());
        c.setLastName(line.substring(59, 84).trim());
        c.setAddrLine1(line.substring(84, 134).trim());
        c.setAddrLine2(line.substring(134, 184).trim());
        c.setAddrLine3(line.substring(184, 234).trim());
        c.setAddrStateCd(line.substring(234, 236).trim());
        c.setAddrCountryCd(line.substring(236, 239).trim());
        c.setAddrZip(line.substring(239, 249).trim());
        c.setPhoneNum1(line.substring(249, 264).trim());
        c.setPhoneNum2(line.substring(264, 279).trim());
        c.setSsn(line.substring(279, 288).trim());
        c.setGovtIssuedId(line.substring(288, 308).trim());
        c.setDob(parseDate(line.substring(308, 318).trim()));
        c.setEftAccountId(line.substring(318, 328).trim());
        c.setPriCardHolderInd(line.substring(328, 329));
        c.setFicoCreditScore(Short.parseShort(line.substring(329, 332).trim()));
        return c;
    }

    @Transactional
    public int loadDailyTransactions(InputStream inputStream) throws IOException {
        List<DailyTransaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                DailyTransaction t = parseDailyTransaction(line);
                transactions.add(t);
            }
        }
        dailyTransactionRepository.saveAll(transactions);
        log.info("Loaded {} daily transactions", transactions.size());
        return transactions.size();
    }

    DailyTransaction parseDailyTransaction(String line) {
        // CVTRA06Y.cpy: RECLN 350
        // DALYTRAN-ID PIC X(16) = pos 0-15
        // DALYTRAN-TYPE-CD PIC X(02) = pos 16-17
        // DALYTRAN-CAT-CD PIC 9(04) = pos 18-21
        // DALYTRAN-SOURCE PIC X(10) = pos 22-31
        // DALYTRAN-DESC PIC X(100) = pos 32-131
        // DALYTRAN-AMT PIC S9(09)V99 = pos 132-142 (11 chars zoned decimal)
        // DALYTRAN-MERCHANT-ID PIC 9(09) = pos 143-151
        // DALYTRAN-MERCHANT-NAME PIC X(50) = pos 152-201
        // DALYTRAN-MERCHANT-CITY PIC X(50) = pos 202-251
        // DALYTRAN-MERCHANT-ZIP PIC X(10) = pos 252-261
        // DALYTRAN-CARD-NUM PIC X(16) = pos 262-277
        // DALYTRAN-ORIG-TS PIC X(26) = pos 278-303
        // DALYTRAN-PROC-TS PIC X(26) = pos 304-329
        DailyTransaction t = new DailyTransaction();
        t.setTranId(line.substring(0, 16).trim());
        t.setTypeCd(line.substring(16, 18).trim());
        t.setCatCd(Integer.parseInt(line.substring(18, 22).trim()));
        t.setSource(line.substring(22, 32).trim());
        t.setDescription(line.substring(32, 132).trim());
        t.setAmount(CobolZonedDecimalParser.parseBigDecimal(line.substring(132, 143), 2));
        t.setMerchantId(Long.parseLong(line.substring(143, 152).trim()));
        t.setMerchantName(line.substring(152, 202).trim());
        t.setMerchantCity(line.substring(202, 252).trim());
        t.setMerchantZip(line.substring(252, 262).trim());
        t.setCardNum(line.substring(262, 278).trim());
        t.setOrigTs(parseTimestamp(line.substring(278, 304).trim()));
        // proc_ts may be blank
        if (line.length() > 304) {
            String procTsStr = line.substring(304, Math.min(330, line.length())).trim();
            if (!procTsStr.isEmpty()) {
                t.setProcTs(parseTimestamp(procTsStr));
            }
        }
        return t;
    }

    @Transactional
    public int loadDisclosureGroups(InputStream inputStream) throws IOException {
        List<DisclosureGroup> groups = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                DisclosureGroup g = parseDisclosureGroup(line);
                groups.add(g);
            }
        }
        disclosureGroupRepository.saveAll(groups);
        log.info("Loaded {} disclosure groups", groups.size());
        return groups.size();
    }

    DisclosureGroup parseDisclosureGroup(String line) {
        // CVTRA02Y.cpy: RECLN 50
        // DIS-ACCT-GROUP-ID PIC X(10) = pos 0-9
        // DIS-TRAN-TYPE-CD PIC X(02) = pos 10-11
        // DIS-TRAN-CAT-CD PIC 9(04) = pos 12-15
        // DIS-INT-RATE PIC S9(04)V99 = pos 16-21 (6 chars zoned decimal)
        DisclosureGroup g = new DisclosureGroup();
        g.setGroupId(line.substring(0, 10).trim());
        g.setTranTypeCd(line.substring(10, 12).trim());
        g.setTranCatCd(Integer.parseInt(line.substring(12, 16).trim()));
        g.setIntRate(CobolZonedDecimalParser.parseBigDecimal(line.substring(16, 22), 2));
        return g;
    }

    @Transactional
    public int loadTranCatBalances(InputStream inputStream) throws IOException {
        List<TranCatBalance> balances = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                TranCatBalance b = parseTranCatBalance(line);
                balances.add(b);
            }
        }
        tranCatBalanceRepository.saveAll(balances);
        log.info("Loaded {} tran cat balances", balances.size());
        return balances.size();
    }

    TranCatBalance parseTranCatBalance(String line) {
        // CVTRA01Y.cpy: RECLN 50
        // TRANCAT-ACCT-ID PIC 9(11) = pos 0-10
        // TRANCAT-TYPE-CD PIC X(02) = pos 11-12
        // TRANCAT-CD PIC 9(04) = pos 13-16
        // TRAN-CAT-BAL PIC S9(09)V99 = pos 17-27 (11 chars zoned decimal)
        TranCatBalance b = new TranCatBalance();
        b.setAcctId(Long.parseLong(line.substring(0, 11).trim()));
        b.setTypeCd(line.substring(11, 13).trim());
        b.setCatCd(Integer.parseInt(line.substring(13, 17).trim()));
        b.setBalance(CobolZonedDecimalParser.parseBigDecimal(line.substring(17, 28), 2));
        return b;
    }

    @Transactional
    public int loadTranTypes(InputStream inputStream) throws IOException {
        List<TranType> types = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                TranType t = parseTranType(line);
                types.add(t);
            }
        }
        tranTypeRepository.saveAll(types);
        log.info("Loaded {} tran types", types.size());
        return types.size();
    }

    TranType parseTranType(String line) {
        // CVTRA03Y.cpy: RECLN 60
        // TRAN-TYPE PIC X(02) = pos 0-1
        // TRAN-TYPE-DESC PIC X(50) = pos 2-51
        TranType t = new TranType();
        t.setTypeCd(line.substring(0, 2).trim());
        t.setTypeDesc(line.substring(2, 52).trim());
        return t;
    }

    @Transactional
    public int loadTranCategories(InputStream inputStream) throws IOException {
        List<TranCategory> categories = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                TranCategory c = parseTranCategory(line);
                categories.add(c);
            }
        }
        tranCategoryRepository.saveAll(categories);
        log.info("Loaded {} tran categories", categories.size());
        return categories.size();
    }

    TranCategory parseTranCategory(String line) {
        // CVTRA04Y.cpy: RECLN 60
        // TRAN-TYPE-CD PIC X(02) = pos 0-1
        // TRAN-CAT-CD PIC 9(04) = pos 2-5
        // TRAN-CAT-TYPE-DESC PIC X(50) = pos 6-55
        TranCategory c = new TranCategory();
        c.setTypeCd(line.substring(0, 2).trim());
        c.setCatCd(Integer.parseInt(line.substring(2, 6).trim()));
        c.setCatDesc(line.substring(6, 56).trim());
        return c;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private LocalDateTime parseTimestamp(String tsStr) {
        if (tsStr == null || tsStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(tsStr.trim(), TS_FMT);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse timestamp: {}", tsStr);
            return null;
        }
    }
}
