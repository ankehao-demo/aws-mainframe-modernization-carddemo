package com.carddemo.account.batch;

import com.carddemo.account.entity.Account;
import com.carddemo.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Interest calculation batch job - replaces CBACT04C.cbl / INTCALC JCL job.
 * Calculates monthly interest on account balances.
 */
@Component
public class InterestCalculationJob {
    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJob.class);
    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.015");

    private final AccountRepository accountRepository;

    public InterestCalculationJob(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Scheduled(cron = "0 0 2 1 * ?")
    public void calculateInterest() {
        log.info("Starting interest calculation batch job");
        List<Account> accounts = accountRepository.findAll();
        int processed = 0;
        for (Account account : accounts) {
            if ("Y".equals(account.getActiveStatus()) && account.getCurrentBalance() != null
                    && account.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal interest = account.getCurrentBalance().multiply(MONTHLY_RATE).setScale(2, RoundingMode.HALF_UP);
                account.setCurrentBalance(account.getCurrentBalance().add(interest));
                accountRepository.save(account);
                processed++;
            }
        }
        log.info("Interest calculation complete. Processed {} accounts", processed);
    }
}
