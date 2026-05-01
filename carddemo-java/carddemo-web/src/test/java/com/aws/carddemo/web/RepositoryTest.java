package com.aws.carddemo.web;

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
import com.aws.carddemo.entity.UserSecurity;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.CustomerRepository;
import com.aws.carddemo.repository.DiscountGroupRepository;
import com.aws.carddemo.repository.TranCatBalanceRepository;
import com.aws.carddemo.repository.TransactionRepository;
import com.aws.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("dev")
class RepositoryTest {

    @Autowired private AccountRepository accountRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private CardXrefRepository cardXrefRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TranCatBalanceRepository tranCatBalanceRepository;
    @Autowired private UserSecurityRepository userSecurityRepository;
    @Autowired private DiscountGroupRepository discountGroupRepository;

    @Test
    void accountPersistAndRetrieve() {
        Account acct = Account.builder()
                .acctId(1L)
                .activeStatus("Y")
                .currBal(new BigDecimal("1000.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .build();
        accountRepository.save(acct);

        var found = accountRepository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("Y", found.get().getActiveStatus());
        assertEquals(0, new BigDecimal("1000.00").compareTo(found.get().getCurrBal()));
    }

    @Test
    void customerPersistAndRetrieve() {
        Customer cust = Customer.builder()
                .custId(1L)
                .firstName("John")
                .lastName("Doe")
                .stateCd("NY")
                .countryCd("USA")
                .build();
        customerRepository.save(cust);

        var found = customerRepository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void cardPersistAndRetrieve() {
        Account acct = Account.builder().acctId(100L).activeStatus("Y").build();
        accountRepository.save(acct);

        Card card = Card.builder()
                .cardNum("1234567890123456")
                .account(acct)
                .embossedName("JOHN DOE")
                .activeStatus("Y")
                .build();
        cardRepository.save(card);

        var found = cardRepository.findById("1234567890123456");
        assertTrue(found.isPresent());
        assertEquals("JOHN DOE", found.get().getEmbossedName());
    }

    @Test
    void cardXrefPersistAndRetrieve() {
        Account acct = Account.builder().acctId(200L).activeStatus("Y").build();
        accountRepository.save(acct);
        Customer cust = Customer.builder().custId(200L).build();
        customerRepository.save(cust);

        CardXref xref = CardXref.builder()
                .cardNum("9876543210123456")
                .account(acct)
                .customer(cust)
                .build();
        cardXrefRepository.save(xref);

        var found = cardXrefRepository.findById("9876543210123456");
        assertTrue(found.isPresent());
    }

    @Test
    void transactionPersistAndRetrieve() {
        TransactionId id = new TransactionId("1234567890123456", "0000000000000001");
        Transaction txn = Transaction.builder()
                .id(id)
                .typeCd("01")
                .catCd(1)
                .amount(new BigDecimal("50.47"))
                .origTs(LocalDateTime.of(2022, 6, 10, 19, 27, 53))
                .build();
        transactionRepository.save(txn);

        var found = transactionRepository.findById(id);
        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("50.47").compareTo(found.get().getAmount()));
    }

    @Test
    void tranCatBalancePersistAndRetrieve() {
        TranCatBalanceId id = new TranCatBalanceId(1L, "01", 1);
        TranCatBalance tcb = TranCatBalance.builder()
                .id(id)
                .balance(new BigDecimal("500.00"))
                .build();
        tranCatBalanceRepository.save(tcb);

        var found = tranCatBalanceRepository.findById(id);
        assertTrue(found.isPresent());
    }

    @Test
    void userSecurityPersistAndRetrieve() {
        UserSecurity user = UserSecurity.builder()
                .userId("admin01")
                .password("$2a$10$hash")
                .userType("A")
                .firstName("Admin")
                .lastName("User")
                .build();
        userSecurityRepository.save(user);

        var found = userSecurityRepository.findById("admin01");
        assertTrue(found.isPresent());
        assertEquals("A", found.get().getUserType());
    }

    @Test
    void discountGroupPersistAndRetrieve() {
        DiscountGroupId id = new DiscountGroupId("GRP001", "01", 1);
        DiscountGroup dg = DiscountGroup.builder()
                .id(id)
                .discountRate(new BigDecimal("1.50"))
                .build();
        discountGroupRepository.save(dg);

        var found = discountGroupRepository.findById(id);
        assertTrue(found.isPresent());
    }
}
