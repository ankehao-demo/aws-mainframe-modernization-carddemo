package com.cardemo.config;

import com.cardemo.entity.Account;
import com.cardemo.entity.Card;
import com.cardemo.entity.CardXref;
import com.cardemo.entity.Customer;
import com.cardemo.entity.UserSecurity;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
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
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserSecurityRepository userSecurityRepository,
                      AccountRepository accountRepository,
                      CardRepository cardRepository,
                      CardXrefRepository cardXrefRepository,
                      CustomerRepository customerRepository,
                      PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
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
}
