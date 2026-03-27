package com.cardemo.config;

import com.cardemo.entity.Account;
import com.cardemo.entity.Card;
import com.cardemo.entity.CardXref;
import com.cardemo.entity.Customer;
import com.cardemo.entity.Transaction;
import com.cardemo.entity.UserSecurity;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import com.cardemo.repository.TransactionRepository;
import com.cardemo.repository.UserSecurityRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds initial data for development/testing.
 * User records match the default credentials from the COBOL app:
 *   ADMIN001/PASSWORD (admin)
 *   USER0001/PASSWORD (regular user)
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserSecurityRepository userSecurityRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserSecurityRepository userSecurityRepository,
                      AccountRepository accountRepository,
                      CardRepository cardRepository,
                      CardXrefRepository cardXrefRepository,
                      CustomerRepository customerRepository,
                      TransactionRepository transactionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userSecurityRepository.count() > 0) {
            return;
        }

        seedUsers();
        seedCustomers();
        seedAccounts();
        seedCards();
        seedCardXrefs();
        seedTransactions();
    }

    private void seedUsers() {
        createUser("ADMIN001", "Admin", "User", "PASSWORD", "A");
        createUser("ADMIN002", "Admin", "Two", "PASSWORD", "A");
        createUser("USER0001", "Regular", "User", "PASSWORD", "U");
        createUser("USER0002", "John", "Doe", "PASSWORD", "U");
        createUser("USER0003", "Jane", "Smith", "PASSWORD", "U");
    }

    private void createUser(String userId, String firstName, String lastName,
                            String password, String userType) {
        UserSecurity user = new UserSecurity();
        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserType(userType);
        userSecurityRepository.save(user);
    }

    private void seedCustomers() {
        Customer c1 = new Customer();
        c1.setCustId(1000000001L);
        c1.setFirstName("John");
        c1.setMiddleName("A");
        c1.setLastName("Smith");
        c1.setAddrLine1("123 Main Street");
        c1.setAddrLine2("Apt 4B");
        c1.setAddrStateCd("NY");
        c1.setAddrCountryCd("US");
        c1.setAddrZip("10001");
        c1.setPhoneNum1("212-555-0101");
        c1.setSsn(123456789L);
        c1.setDateOfBirth("1985-03-15");
        c1.setPrimaryCardHolderInd("Y");
        c1.setFicoCreditScore(750);
        customerRepository.save(c1);

        Customer c2 = new Customer();
        c2.setCustId(1000000002L);
        c2.setFirstName("Jane");
        c2.setMiddleName("B");
        c2.setLastName("Doe");
        c2.setAddrLine1("456 Oak Avenue");
        c2.setAddrStateCd("CA");
        c2.setAddrCountryCd("US");
        c2.setAddrZip("90210");
        c2.setPhoneNum1("310-555-0202");
        c2.setSsn(987654321L);
        c2.setDateOfBirth("1990-07-22");
        c2.setPrimaryCardHolderInd("Y");
        c2.setFicoCreditScore(680);
        customerRepository.save(c2);
    }

    private void seedAccounts() {
        Account a1 = new Account();
        a1.setAcctId(10000000001L);
        a1.setActiveStatus("Y");
        a1.setCurrentBalance(new BigDecimal("1500.00"));
        a1.setCreditLimit(new BigDecimal("5000.00"));
        a1.setCashCreditLimit(new BigDecimal("1500.00"));
        a1.setOpenDate("2020-01-15");
        a1.setExpirationDate("2025-01-15");
        a1.setCurrentCycleCredit(new BigDecimal("200.00"));
        a1.setCurrentCycleDebit(new BigDecimal("350.00"));
        a1.setAddressZip("10001");
        a1.setGroupId("GROUP001");
        accountRepository.save(a1);

        Account a2 = new Account();
        a2.setAcctId(10000000002L);
        a2.setActiveStatus("Y");
        a2.setCurrentBalance(new BigDecimal("3200.50"));
        a2.setCreditLimit(new BigDecimal("10000.00"));
        a2.setCashCreditLimit(new BigDecimal("3000.00"));
        a2.setOpenDate("2019-06-20");
        a2.setExpirationDate("2024-06-20");
        a2.setCurrentCycleCredit(new BigDecimal("500.00"));
        a2.setCurrentCycleDebit(new BigDecimal("750.25"));
        a2.setAddressZip("90210");
        a2.setGroupId("GROUP002");
        accountRepository.save(a2);
    }

    private void seedCards() {
        Card card1 = new Card();
        card1.setCardNum("4111111111111111");
        card1.setAcctId(10000000001L);
        card1.setCvvCode(123);
        card1.setEmbossedName("JOHN A SMITH");
        card1.setExpirationDate("2025-01-15");
        card1.setActiveStatus("Y");
        cardRepository.save(card1);

        Card card2 = new Card();
        card2.setCardNum("4222222222222222");
        card2.setAcctId(10000000002L);
        card2.setCvvCode(456);
        card2.setEmbossedName("JANE B DOE");
        card2.setExpirationDate("2024-06-20");
        card2.setActiveStatus("Y");
        cardRepository.save(card2);
    }

    private void seedCardXrefs() {
        CardXref xref1 = new CardXref();
        xref1.setCardNum("4111111111111111");
        xref1.setCustId(1000000001L);
        xref1.setAcctId(10000000001L);
        cardXrefRepository.save(xref1);

        CardXref xref2 = new CardXref();
        xref2.setCardNum("4222222222222222");
        xref2.setCustId(1000000002L);
        xref2.setAcctId(10000000002L);
        cardXrefRepository.save(xref2);
    }

    private void seedTransactions() {
        Transaction t1 = new Transaction();
        t1.setTranId("0000000000000001");
        t1.setTypeCd("SA");
        t1.setCategoryCd(5001);
        t1.setSource("ONLINE");
        t1.setDescription("Amazon Purchase");
        t1.setAmount(new BigDecimal("125.50"));
        t1.setMerchantId(100000001L);
        t1.setMerchantName("Amazon.com");
        t1.setMerchantCity("Seattle");
        t1.setMerchantZip("98101");
        t1.setCardNum("4111111111111111");
        t1.setOriginTimestamp("2024-01-15-10.30.00.000000");
        t1.setProcessedTimestamp("2024-01-15-10.30.05.000000");
        transactionRepository.save(t1);

        Transaction t2 = new Transaction();
        t2.setTranId("0000000000000002");
        t2.setTypeCd("SA");
        t2.setCategoryCd(5411);
        t2.setSource("POS");
        t2.setDescription("Grocery Store Purchase");
        t2.setAmount(new BigDecimal("67.89"));
        t2.setMerchantId(100000002L);
        t2.setMerchantName("Whole Foods Market");
        t2.setMerchantCity("New York");
        t2.setMerchantZip("10001");
        t2.setCardNum("4111111111111111");
        t2.setOriginTimestamp("2024-01-16-14.15.00.000000");
        t2.setProcessedTimestamp("2024-01-16-14.15.03.000000");
        transactionRepository.save(t2);

        Transaction t3 = new Transaction();
        t3.setTranId("0000000000000003");
        t3.setTypeCd("CR");
        t3.setCategoryCd(6011);
        t3.setSource("ATM");
        t3.setDescription("ATM Cash Withdrawal");
        t3.setAmount(new BigDecimal("-200.00"));
        t3.setMerchantId(100000003L);
        t3.setMerchantName("Chase Bank ATM");
        t3.setMerchantCity("Los Angeles");
        t3.setMerchantZip("90210");
        t3.setCardNum("4222222222222222");
        t3.setOriginTimestamp("2024-02-01-09.00.00.000000");
        t3.setProcessedTimestamp("2024-02-01-09.00.02.000000");
        transactionRepository.save(t3);
    }
}
