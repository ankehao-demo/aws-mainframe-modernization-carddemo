package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardCrossRef;
import com.aws.carddemo.entity.DisclosureGroup;
import com.aws.carddemo.entity.DisclosureGroupId;
import com.aws.carddemo.entity.TranCategoryBalance;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardCrossRefRepository;
import com.aws.carddemo.repository.DisclosureGroupRepository;
import com.aws.carddemo.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * InterestCalculationJob — mirrors CBACT04C.cbl batch program.
 * Reads tran_category_balance records, looks up disclosure_group for interest rate,
 * computes monthly interest = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200,
 * accumulates per account, updates account balances.
 * See CBACT04C.cbl lines 180-232 and 462-515.
 */
@Configuration
public class InterestCalculationJobConfig {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJobConfig.class);
    private static final BigDecimal TWELVE_HUNDRED = new BigDecimal("1200");

    private final AccountRepository accountRepository;
    private final CardCrossRefRepository cardCrossRefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalculationJobConfig(AccountRepository accountRepository,
                                         CardCrossRefRepository cardCrossRefRepository,
                                         DisclosureGroupRepository disclosureGroupRepository,
                                         TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardCrossRefRepository = cardCrossRefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public JpaPagingItemReader<TranCategoryBalance> tcatbalReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<TranCategoryBalance>()
                .name("tcatbalReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM TranCategoryBalance t ORDER BY t.acctId, t.typeCd, t.catCd")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemWriter<TranCategoryBalance> interestCalculationWriter() {
        // Accumulate interest per account, then update
        Map<String, BigDecimal> accountInterest = new HashMap<>();
        AtomicInteger tranSuffix = new AtomicInteger(0);

        return items -> {
            for (TranCategoryBalance tcb : items) {
                Optional<Account> acctOpt = accountRepository.findById(tcb.getAcctId());
                if (acctOpt.isEmpty()) continue;
                Account account = acctOpt.get();

                String groupId = account.getGroupId();
                if (groupId == null || groupId.isBlank()) {
                    groupId = "DEFAULT";
                }

                // 1200-GET-INTEREST-RATE
                DisclosureGroupId dgId = new DisclosureGroupId(groupId, tcb.getTypeCd(), tcb.getCatCd());
                Optional<DisclosureGroup> dgOpt = disclosureGroupRepository.findById(dgId);

                if (dgOpt.isEmpty() && !"DEFAULT".equals(groupId)) {
                    dgId = new DisclosureGroupId("DEFAULT", tcb.getTypeCd(), tcb.getCatCd());
                    dgOpt = disclosureGroupRepository.findById(dgId);
                }

                if (dgOpt.isEmpty() || dgOpt.get().getIntRate().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                // 1300-COMPUTE-INTEREST: monthly_int = (balance * rate) / 1200
                BigDecimal monthlyInt = tcb.getBalance()
                        .multiply(dgOpt.get().getIntRate())
                        .divide(TWELVE_HUNDRED, 2, RoundingMode.HALF_UP);

                accountInterest.merge(tcb.getAcctId(), monthlyInt, BigDecimal::add);

                // 1300-B-WRITE-TX: write interest transaction
                int suffix = tranSuffix.incrementAndGet();
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
                String tranId = String.format("%-10s%06d", now, suffix);
                if (tranId.length() > 16) tranId = tranId.substring(0, 16);

                Transaction intTran = new Transaction();
                intTran.setTranId(tranId);
                intTran.setTypeCd("01");
                intTran.setCatCd(5);
                intTran.setSource("System");
                intTran.setDescription("Int. for a/c " + tcb.getAcctId());
                intTran.setAmount(monthlyInt);
                intTran.setMerchantId("000000000");
                intTran.setCardNum(getCardForAccount(tcb.getAcctId()));
                intTran.setOrigTs(LocalDateTime.now());
                intTran.setProcTs(LocalDateTime.now());

                transactionRepository.save(intTran);
            }

            // 1050-UPDATE-ACCOUNT: update account balances with accumulated interest
            for (Map.Entry<String, BigDecimal> entry : accountInterest.entrySet()) {
                Optional<Account> acctOpt = accountRepository.findById(entry.getKey());
                if (acctOpt.isPresent()) {
                    Account account = acctOpt.get();
                    account.setCurrBal(account.getCurrBal().add(entry.getValue()));
                    account.setCurrCycCredit(BigDecimal.ZERO);
                    account.setCurrCycDebit(BigDecimal.ZERO);
                    accountRepository.save(account);
                }
            }
            accountInterest.clear();
        };
    }

    private String getCardForAccount(String acctId) {
        List<CardCrossRef> xrefs = cardCrossRefRepository.findByAcctId(acctId);
        return xrefs.isEmpty() ? "0000000000000000" : xrefs.get(0).getCardNum();
    }

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         JpaPagingItemReader<TranCategoryBalance> tcatbalReader,
                                         ItemWriter<TranCategoryBalance> interestCalculationWriter) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .<TranCategoryBalance, TranCategoryBalance>chunk(100, transactionManager)
                .reader(tcatbalReader)
                .writer(interestCalculationWriter)
                .build();
    }

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }
}
