package com.aws.carddemo.migration;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.Card;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.Customer;
import com.aws.carddemo.entity.DiscountGroup;
import com.aws.carddemo.entity.DiscountGroupId;
import com.aws.carddemo.entity.TranCatBalance;
import com.aws.carddemo.entity.TranCatBalanceId;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.entity.TransactionId;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.CustomerRepository;
import com.aws.carddemo.repository.DiscountGroupRepository;
import com.aws.carddemo.repository.TranCatBalanceRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationRunner.class);

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final DiscountGroupRepository discountGroupRepository;

    public DataMigrationRunner(AccountRepository accountRepository,
                               CardRepository cardRepository,
                               CardXrefRepository cardXrefRepository,
                               CustomerRepository customerRepository,
                               TransactionRepository transactionRepository,
                               TranCatBalanceRepository tranCatBalanceRepository,
                               DiscountGroupRepository discountGroupRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.discountGroupRepository = discountGroupRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            log.info("No data directory specified. Usage: --data.dir=/path/to/app/data/ASCII");
            return;
        }

        String dataDir = null;
        for (String arg : args) {
            if (arg.startsWith("--data.dir=")) {
                dataDir = arg.substring("--data.dir=".length());
            }
        }

        if (dataDir == null) {
            log.info("No --data.dir argument provided. Skipping migration.");
            return;
        }

        Path basePath = Path.of(dataDir);
        if (!Files.isDirectory(basePath)) {
            log.error("Data directory does not exist: {}", basePath);
            return;
        }

        log.info("Starting data migration from: {}", basePath);

        migrateAccounts(basePath.resolve("acctdata.txt"));
        migrateCustomers(basePath.resolve("custdata.txt"));
        migrateCards(basePath.resolve("carddata.txt"));
        migrateCardXrefs(basePath.resolve("cardxref.txt"));
        migrateTranCatBalances(basePath.resolve("tcatbal.txt"));
        migrateDiscountGroups(basePath.resolve("discgrp.txt"));
        migrateTransactions(basePath.resolve("dailytran.txt"));

        log.info("Data migration complete.");
    }

    private void migrateAccounts(Path file) {
        // CVACT01Y: RECLN 300
        // Pos 0-10:  ACCT-ID           PIC 9(11)
        // Pos 11:    ACCT-ACTIVE-STATUS PIC X(01)
        // Pos 12-23: ACCT-CURR-BAL     PIC S9(10)V99 (12 chars with overpunch)
        // Pos 24-35: ACCT-CREDIT-LIMIT PIC S9(10)V99
        // Pos 36-47: ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
        // Pos 48-57: ACCT-OPEN-DATE    PIC X(10)
        // Pos 58-67: ACCT-EXPIRAION-DATE PIC X(10)
        // Pos 68-77: ACCT-REISSUE-DATE PIC X(10)
        // Pos 78-89: ACCT-CURR-CYC-CREDIT PIC S9(10)V99
        // Pos 90-101: ACCT-CURR-CYC-DEBIT PIC S9(10)V99
        // Pos 102-111: ACCT-ADDR-ZIP   PIC X(10)
        // Pos 112-121: ACCT-GROUP-ID   PIC X(10)
        processFile(file, "account", line -> {
            Account acct = Account.builder()
                    .acctId(FixedWidthParser.parseLong(line, 0, 11))
                    .activeStatus(FixedWidthParser.parseString(line, 11, 1))
                    .currBal(FixedWidthParser.parseSignedDecimal(line, 12, 12, 2))
                    .creditLimit(FixedWidthParser.parseSignedDecimal(line, 24, 12, 2))
                    .cashCreditLimit(FixedWidthParser.parseSignedDecimal(line, 36, 12, 2))
                    .openDate(FixedWidthParser.parseDate(line, 48, 10))
                    .expirationDate(FixedWidthParser.parseDate(line, 58, 10))
                    .reissueDate(FixedWidthParser.parseDate(line, 68, 10))
                    .currCycCredit(FixedWidthParser.parseSignedDecimal(line, 78, 12, 2))
                    .currCycDebit(FixedWidthParser.parseSignedDecimal(line, 90, 12, 2))
                    .addrZip(FixedWidthParser.parseString(line, 102, 10))
                    .groupId(FixedWidthParser.parseString(line, 112, 10))
                    .build();
            if (acct.getAcctId() != null) {
                accountRepository.save(acct);
                return true;
            }
            return false;
        });
    }

    private void migrateCustomers(Path file) {
        // CVCUS01Y: RECLN 500
        // Pos 0-8:   CUST-ID           PIC 9(09)
        // Pos 9-33:  CUST-FIRST-NAME   PIC X(25)
        // Pos 34-58: CUST-MIDDLE-NAME  PIC X(25)
        // Pos 59-83: CUST-LAST-NAME    PIC X(25)
        // Pos 84-133: CUST-ADDR-LINE-1  PIC X(50)
        // Pos 134-183: CUST-ADDR-LINE-2 PIC X(50)
        // Pos 184-233: CUST-ADDR-LINE-3 PIC X(50)
        // Pos 234-235: CUST-ADDR-STATE-CD PIC X(02)
        // Pos 236-238: CUST-ADDR-COUNTRY-CD PIC X(03)
        // Pos 239-248: CUST-ADDR-ZIP    PIC X(10)
        // Pos 249-263: CUST-PHONE-NUM-1 PIC X(15)
        // Pos 264-278: CUST-PHONE-NUM-2 PIC X(15)
        // Pos 279-287: CUST-SSN         PIC 9(09)
        // Pos 288-307: CUST-GOVT-ISSUED-ID PIC X(20)
        // Pos 308-317: CUST-DOB         PIC X(10)
        // Pos 318-327: CUST-EFT-ACCOUNT-ID PIC X(10)
        // Pos 328:    CUST-PRI-CARD-HOLDER-IND PIC X(01)
        // Pos 329-331: CUST-FICO-CREDIT-SCORE PIC 9(03)
        processFile(file, "customer", line -> {
            Customer cust = Customer.builder()
                    .custId(FixedWidthParser.parseLong(line, 0, 9))
                    .firstName(FixedWidthParser.parseString(line, 9, 25))
                    .middleName(FixedWidthParser.parseString(line, 34, 25))
                    .lastName(FixedWidthParser.parseString(line, 59, 25))
                    .addrLine1(FixedWidthParser.parseString(line, 84, 50))
                    .addrLine2(FixedWidthParser.parseString(line, 134, 50))
                    .addrLine3(FixedWidthParser.parseString(line, 184, 50))
                    .stateCd(FixedWidthParser.parseString(line, 234, 2))
                    .countryCd(FixedWidthParser.parseString(line, 236, 3))
                    .zipCode(FixedWidthParser.parseString(line, 239, 10))
                    .phoneNum1(FixedWidthParser.parseString(line, 249, 15))
                    .phoneNum2(FixedWidthParser.parseString(line, 264, 15))
                    .ssn(FixedWidthParser.parseString(line, 279, 9))
                    .govtIssuedId(FixedWidthParser.parseString(line, 288, 20))
                    .dateOfBirth(FixedWidthParser.parseDate(line, 308, 10))
                    .eftAccountId(FixedWidthParser.parseString(line, 318, 10))
                    .priCardHolderInd(FixedWidthParser.parseString(line, 328, 1))
                    .ficoScore(FixedWidthParser.parseInt(line, 329, 3))
                    .build();
            if (cust.getCustId() != null) {
                customerRepository.save(cust);
                return true;
            }
            return false;
        });
    }

    private void migrateCards(Path file) {
        // CVACT02Y: RECLN 150
        // Pos 0-15:  CARD-NUM          PIC X(16)
        // Pos 16-26: CARD-ACCT-ID      PIC 9(11)
        // Pos 27-29: CARD-CVV-CD       PIC 9(03)
        // Pos 30-79: CARD-EMBOSSED-NAME PIC X(50)
        // Pos 80-89: CARD-EXPIRAION-DATE PIC X(10)
        // Pos 90:    CARD-ACTIVE-STATUS PIC X(01)
        processFile(file, "card", line -> {
            String cardNum = FixedWidthParser.parseString(line, 0, 16);
            Long acctId = FixedWidthParser.parseLong(line, 16, 11);
            if (cardNum != null && acctId != null) {
                Account acct = accountRepository.findById(acctId).orElse(null);
                if (acct == null) {
                    log.warn("Card {} references unknown account {}, skipping", cardNum, acctId);
                    return false;
                }
                Card card = Card.builder()
                        .cardNum(cardNum)
                        .account(acct)
                        .cvvCd(FixedWidthParser.parseInt(line, 27, 3))
                        .embossedName(FixedWidthParser.parseString(line, 30, 50))
                        .expirationDate(FixedWidthParser.parseDate(line, 80, 10))
                        .activeStatus(FixedWidthParser.parseString(line, 90, 1))
                        .build();
                cardRepository.save(card);
                return true;
            }
            return false;
        });
    }

    private void migrateCardXrefs(Path file) {
        // CVACT03Y: RECLN 50
        // Pos 0-15:  XREF-CARD-NUM     PIC X(16)
        // Pos 16-24: XREF-CUST-ID      PIC 9(09)
        // Pos 25-35: XREF-ACCT-ID      PIC 9(11)
        processFile(file, "card_xref", line -> {
            String cardNum = FixedWidthParser.parseString(line, 0, 16);
            Long custId = FixedWidthParser.parseLong(line, 16, 9);
            Long acctId = FixedWidthParser.parseLong(line, 25, 11);
            if (cardNum != null && custId != null && acctId != null) {
                Customer cust = customerRepository.findById(custId).orElse(null);
                Account acct = accountRepository.findById(acctId).orElse(null);
                if (cust == null || acct == null) {
                    log.warn("Card xref {} references unknown customer {} or account {}, skipping",
                            cardNum, custId, acctId);
                    return false;
                }
                CardXref xref = CardXref.builder()
                        .cardNum(cardNum)
                        .customer(cust)
                        .account(acct)
                        .build();
                cardXrefRepository.save(xref);
                return true;
            }
            return false;
        });
    }

    private void migrateTranCatBalances(Path file) {
        // CVTRA01Y: RECLN 50
        // Pos 0-10:  TRANCAT-ACCT-ID   PIC 9(11)
        // Pos 11-12: TRANCAT-TYPE-CD   PIC X(02)
        // Pos 13-16: TRANCAT-CD        PIC 9(04)
        // Pos 17-28: TRAN-CAT-BAL      PIC S9(09)V99 (12 chars with overpunch)
        processFile(file, "tran_cat_balance", line -> {
            Long acctId = FixedWidthParser.parseLong(line, 0, 11);
            String typeCd = FixedWidthParser.parseString(line, 11, 2);
            Integer catCd = FixedWidthParser.parseInt(line, 13, 4);
            if (acctId != null && typeCd != null && catCd != null) {
                TranCatBalance tcb = TranCatBalance.builder()
                        .id(new TranCatBalanceId(acctId, typeCd, catCd))
                        .balance(FixedWidthParser.parseSignedDecimal(line, 17, 12, 2))
                        .build();
                tranCatBalanceRepository.save(tcb);
                return true;
            }
            return false;
        });
    }

    private void migrateDiscountGroups(Path file) {
        // CVTRA02Y: RECLN 50
        // Pos 0-9:   DIS-ACCT-GROUP-ID PIC X(10)
        // Pos 10-11: DIS-TRAN-TYPE-CD  PIC X(02)
        // Pos 12-15: DIS-TRAN-CAT-CD   PIC 9(04)
        // Pos 16-21: DIS-INT-RATE      PIC S9(04)V99 (6 chars with overpunch)
        processFile(file, "discount_group", line -> {
            String groupId = FixedWidthParser.parseString(line, 0, 10);
            String typeCd = FixedWidthParser.parseString(line, 10, 2);
            Integer catCd = FixedWidthParser.parseInt(line, 12, 4);
            if (groupId != null && typeCd != null && catCd != null) {
                DiscountGroup dg = DiscountGroup.builder()
                        .id(new DiscountGroupId(groupId, typeCd, catCd))
                        .discountRate(FixedWidthParser.parseSignedDecimal(line, 16, 6, 2))
                        .build();
                discountGroupRepository.save(dg);
                return true;
            }
            return false;
        });
    }

    private void migrateTransactions(Path file) {
        // CVTRA05Y: RECLN 350 — field order: TRAN-ID(16), TRAN-TYPE-CD(2), TRAN-CAT-CD(4),
        // TRAN-SOURCE(10), TRAN-DESC(100), TRAN-AMT(S9(09)V99=12), TRAN-MERCHANT-ID(9),
        // TRAN-MERCHANT-NAME(50), TRAN-MERCHANT-CITY(50), TRAN-MERCHANT-ZIP(10),
        // TRAN-CARD-NUM(16), TRAN-ORIG-TS(26), TRAN-PROC-TS(26), FILLER(20)
        processFile(file, "transaction", line -> {
            String tranId = FixedWidthParser.parseString(line, 0, 16);
            String typeCd = FixedWidthParser.parseString(line, 16, 2);
            Integer catCd = FixedWidthParser.parseInt(line, 18, 4);
            String source = FixedWidthParser.parseString(line, 22, 10);
            String desc = FixedWidthParser.parseString(line, 32, 100);
            var amount = FixedWidthParser.parseSignedDecimal(line, 132, 12, 2);
            Long merchantId = FixedWidthParser.parseLong(line, 144, 9);
            String merchantName = FixedWidthParser.parseString(line, 153, 50);
            String merchantCity = FixedWidthParser.parseString(line, 203, 50);
            String merchantZip = FixedWidthParser.parseString(line, 253, 10);
            String cardNum = FixedWidthParser.parseString(line, 263, 16);
            var origTs = FixedWidthParser.parseTimestamp(line, 279, 26);
            var procTs = FixedWidthParser.parseTimestamp(line, 305, 26);

            if (tranId != null && cardNum != null) {
                Transaction txn = Transaction.builder()
                        .id(new TransactionId(cardNum, tranId))
                        .typeCd(typeCd)
                        .catCd(catCd)
                        .source(source)
                        .description(desc)
                        .amount(amount)
                        .merchantId(merchantId)
                        .merchantName(merchantName)
                        .merchantCity(merchantCity)
                        .merchantZip(merchantZip)
                        .origTs(origTs)
                        .procTs(procTs)
                        .build();
                transactionRepository.save(txn);
                return true;
            }
            return false;
        });
    }

    private void processFile(Path file, String entityName, RecordProcessor processor) {
        if (!Files.exists(file)) {
            log.warn("Data file not found: {}, skipping {} migration", file, entityName);
            return;
        }

        int total = 0;
        int success = 0;
        int errors = 0;

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                total++;
                try {
                    if (processor.process(line)) {
                        success++;
                    } else {
                        errors++;
                        if (errors <= 10) {
                            log.warn("Skipped {} record #{}: parse returned false", entityName, total);
                        }
                    }
                } catch (Exception e) {
                    errors++;
                    if (errors <= 10) {
                        log.warn("Error processing {} record #{}: {}", entityName, total, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to read {}: {}", file, e.getMessage());
            return;
        }

        log.info("Migration {}: {} total, {} success, {} errors", entityName, total, success, errors);
    }

    @FunctionalInterface
    interface RecordProcessor {
        boolean process(String line);
    }
}
