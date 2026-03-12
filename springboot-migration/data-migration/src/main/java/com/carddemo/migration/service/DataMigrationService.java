package com.carddemo.migration.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.entity.User;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TransactionRepository;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.migration.converter.EbcdicConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Reads EBCDIC data files from app/data/EBCDIC/ and loads into PostgreSQL.
 * 
 * File mappings:
 * - AWS00011.CARDDEMO.ACCTDATA.PS → accounts table
 * - AWS00011.CARDDEMO.CARDDATA.PS → cards table
 * - AWS00011.CARDDEMO.CUSTDATA.PS → customers table
 * - AWS00011.CARDDEMO.TRANSACT.PS → transactions table
 * - AWS00011.CARDDEMO.CARDXREF.PS → card cross-references (updates cards table)
 * - AWS00011.CARDDEMO.USRSEC.PS → users table
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataMigrationService {

    private final EbcdicConverter ebcdicConverter;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public void migrateAccounts(Path filePath) throws IOException {
        log.info("Migrating accounts from: {}", filePath);
        int recordLength = 300;
        int count = 0;

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            byte[] record = new byte[recordLength];
            while (raf.read(record) == recordLength) {
                try {
                    Account account = new Account();
                    account.setAccountId(Long.parseLong(
                            ebcdicConverter.ebcdicToString(record, 0, 11).trim()));
                    account.setAccountStatus(ebcdicConverter.ebcdicToString(record, 11, 1));
                    account.setCurrentBalance(
                            ebcdicConverter.comp3ToDecimal(record, 12, 6, 2));
                    account.setCreditLimit(
                            ebcdicConverter.comp3ToDecimal(record, 18, 6, 2));
                    account.setCashCreditLimit(
                            ebcdicConverter.comp3ToDecimal(record, 24, 6, 2));
                    account.setOpenDate(parseDate(
                            ebcdicConverter.ebcdicToString(record, 30, 10)));
                    account.setExpirationDate(parseDate(
                            ebcdicConverter.ebcdicToString(record, 40, 10)));
                    account.setReissueDate(parseDate(
                            ebcdicConverter.ebcdicToString(record, 50, 10)));
                    account.setCurrCycCredit(
                            ebcdicConverter.comp3ToDecimal(record, 60, 6, 2));
                    account.setCurrCycDebit(
                            ebcdicConverter.comp3ToDecimal(record, 66, 6, 2));
                    account.setGroupId(ebcdicConverter.ebcdicToString(record, 72, 10));

                    accountRepository.save(account);
                    count++;
                } catch (Exception e) {
                    log.warn("Skipping invalid account record at offset {}: {}",
                            raf.getFilePointer() - recordLength, e.getMessage());
                }
            }
        }
        log.info("Migrated {} accounts", count);
    }

    @Transactional
    public void migrateCustomers(Path filePath) throws IOException {
        log.info("Migrating customers from: {}", filePath);
        int recordLength = 500;
        int count = 0;

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            byte[] record = new byte[recordLength];
            while (raf.read(record) == recordLength) {
                try {
                    Customer customer = new Customer();
                    customer.setCustomerId(Long.parseLong(
                            ebcdicConverter.ebcdicToString(record, 0, 9).trim()));
                    customer.setFirstName(ebcdicConverter.ebcdicToString(record, 9, 25));
                    customer.setMiddleName(ebcdicConverter.ebcdicToString(record, 34, 25));
                    customer.setLastName(ebcdicConverter.ebcdicToString(record, 59, 25));
                    customer.setAddressLine1(ebcdicConverter.ebcdicToString(record, 84, 50));
                    customer.setAddressLine2(ebcdicConverter.ebcdicToString(record, 134, 50));
                    customer.setAddressLine3(ebcdicConverter.ebcdicToString(record, 184, 50));
                    customer.setState(ebcdicConverter.ebcdicToString(record, 234, 2));
                    customer.setCountryCode(ebcdicConverter.ebcdicToString(record, 236, 3));
                    customer.setZipCode(ebcdicConverter.ebcdicToString(record, 239, 10));
                    customer.setPhone1(ebcdicConverter.ebcdicToString(record, 249, 15));
                    customer.setPhone2(ebcdicConverter.ebcdicToString(record, 264, 15));
                    customer.setSsn(ebcdicConverter.ebcdicToString(record, 279, 9));
                    customer.setGovtId(ebcdicConverter.ebcdicToString(record, 288, 20));
                    customer.setDateOfBirth(parseDate(
                            ebcdicConverter.ebcdicToString(record, 308, 10)));
                    customer.setFicoCreditScore(Integer.parseInt(
                            ebcdicConverter.ebcdicToString(record, 318, 3).trim()));

                    customerRepository.save(customer);
                    count++;
                } catch (Exception e) {
                    log.warn("Skipping invalid customer record: {}", e.getMessage());
                }
            }
        }
        log.info("Migrated {} customers", count);
    }

    @Transactional
    public void migrateCards(Path filePath) throws IOException {
        log.info("Migrating cards from: {}", filePath);
        int recordLength = 150;
        int count = 0;

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            byte[] record = new byte[recordLength];
            while (raf.read(record) == recordLength) {
                try {
                    Card card = new Card();
                    card.setCardNumber(ebcdicConverter.ebcdicToString(record, 0, 16));
                    card.setAccountId(Long.parseLong(
                            ebcdicConverter.ebcdicToString(record, 16, 11).trim()));
                    card.setCvvCode(ebcdicConverter.ebcdicToString(record, 27, 3));
                    card.setEmbossedName(ebcdicConverter.ebcdicToString(record, 30, 50));
                    card.setExpirationDate(parseDate(
                            ebcdicConverter.ebcdicToString(record, 80, 10)));
                    card.setCardStatus(ebcdicConverter.ebcdicToString(record, 90, 1));
                    // customerId will be set during CARDXREF migration
                    card.setCustomerId(0L);

                    cardRepository.save(card);
                    count++;
                } catch (Exception e) {
                    log.warn("Skipping invalid card record: {}", e.getMessage());
                }
            }
        }
        log.info("Migrated {} cards", count);
    }

    @Transactional
    public void migrateUsers(Path filePath) throws IOException {
        log.info("Migrating users from: {}", filePath);
        int recordLength = 80;
        int count = 0;

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            byte[] record = new byte[recordLength];
            while (raf.read(record) == recordLength) {
                try {
                    User user = new User();
                    user.setUserId(ebcdicConverter.ebcdicToString(record, 0, 8));
                    String rawPassword = ebcdicConverter.ebcdicToString(record, 8, 8).trim();
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setFirstName(ebcdicConverter.ebcdicToString(record, 16, 20));
                    user.setLastName(ebcdicConverter.ebcdicToString(record, 36, 20));
                    user.setUserType(ebcdicConverter.ebcdicToString(record, 56, 1));

                    userRepository.save(user);
                    count++;
                } catch (Exception e) {
                    log.warn("Skipping invalid user record: {}", e.getMessage());
                }
            }
        }
        log.info("Migrated {} users", count);
    }

    @Transactional
    public void migrateTransactions(Path filePath) throws IOException {
        log.info("Migrating transactions from: {}", filePath);
        int recordLength = 350;
        int count = 0;

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            byte[] record = new byte[recordLength];
            while (raf.read(record) == recordLength) {
                try {
                    Transaction transaction = new Transaction();
                    transaction.setTransactionId(
                            ebcdicConverter.ebcdicToString(record, 0, 16));
                    transaction.setTransactionTypeCode(
                            ebcdicConverter.ebcdicToString(record, 16, 2));
                    transaction.setTransactionCategoryCode(
                            ebcdicConverter.ebcdicToString(record, 18, 4));
                    transaction.setTransactionSource(
                            ebcdicConverter.ebcdicToString(record, 22, 10));
                    transaction.setTransactionDescription(
                            ebcdicConverter.ebcdicToString(record, 32, 100));
                    transaction.setTransactionAmount(
                            ebcdicConverter.comp3ToDecimal(record, 132, 6, 2));
                    transaction.setMerchantId(
                            ebcdicConverter.ebcdicToString(record, 138, 9));
                    transaction.setMerchantName(
                            ebcdicConverter.ebcdicToString(record, 147, 50));
                    transaction.setMerchantCity(
                            ebcdicConverter.ebcdicToString(record, 197, 50));
                    transaction.setMerchantZip(
                            ebcdicConverter.ebcdicToString(record, 247, 10));
                    transaction.setCardNumber(
                            ebcdicConverter.ebcdicToString(record, 257, 16));
                    transaction.setTransactionTimestamp(LocalDateTime.now());

                    transactionRepository.save(transaction);
                    count++;
                } catch (Exception e) {
                    log.warn("Skipping invalid transaction record: {}", e.getMessage());
                }
            }
        }
        log.info("Migrated {} transactions", count);
    }

    /**
     * Reads the CARDXREF file and updates existing Card entities with customerId.
     * The CARDXREF VSAM file links card numbers to customer and account IDs.
     * Record layout from CVACT03Y.cpy: CARD-XREF-RECORD (RECLN 50)
     *   XREF-CARD-NUM  PIC X(16)  offset 0
     *   XREF-CUST-ID   PIC 9(09)  offset 16
     *   XREF-ACCT-ID   PIC 9(11)  offset 25
     */
    @Transactional
    public void migrateCardXref(Path filePath) throws IOException {
        log.info("Migrating card cross-references from: {}", filePath);
        int recordLength = 50;
        int count = 0;

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            byte[] record = new byte[recordLength];
            while (raf.read(record) == recordLength) {
                try {
                    String cardNumber = ebcdicConverter.ebcdicToString(record, 0, 16).trim();
                    String custIdStr = ebcdicConverter.ebcdicToString(record, 16, 9).trim();
                    Long customerId = custIdStr.isEmpty() ? 0L : Long.parseLong(custIdStr);

                    cardRepository.findById(cardNumber).ifPresent(card -> {
                        card.setCustomerId(customerId);
                        cardRepository.save(card);
                    });
                    count++;
                } catch (Exception e) {
                    log.warn("Skipping invalid card xref record: {}", e.getMessage());
                }
            }
        }
        log.info("Migrated {} card cross-references", count);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
