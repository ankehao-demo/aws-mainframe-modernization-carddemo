package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardCrossRef;
import com.aws.carddemo.entity.DailyTransaction;
import com.aws.carddemo.entity.TranCategoryBalance;
import com.aws.carddemo.entity.TranCategoryBalanceId;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardCrossRefRepository;
import com.aws.carddemo.repository.TranCategoryBalanceRepository;
import com.aws.carddemo.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PostTransactionJob — mirrors CBTRN02C.cbl batch program.
 * Reads daily_transaction, validates, posts to transaction table,
 * updates account balances and tran_category_balance.
 *
 * Validation reasons (from CBTRN02C lines 370-421):
 *   100 = invalid card number (not in card_cross_ref)
 *   101 = account record not found
 *   102 = over credit limit
 *   103 = account expired
 */
@Configuration
public class PostTransactionJobConfig {

    private static final Logger log = LoggerFactory.getLogger(PostTransactionJobConfig.class);

    private final AccountRepository accountRepository;
    private final CardCrossRefRepository cardCrossRefRepository;
    private final TranCategoryBalanceRepository tranCategoryBalanceRepository;
    private final TransactionRepository transactionRepository;

    public PostTransactionJobConfig(AccountRepository accountRepository,
                                     CardCrossRefRepository cardCrossRefRepository,
                                     TranCategoryBalanceRepository tranCategoryBalanceRepository,
                                     TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardCrossRefRepository = cardCrossRefRepository;
        this.tranCategoryBalanceRepository = tranCategoryBalanceRepository;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public JpaPagingItemReader<DailyTransaction> dailyTransactionReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(emf)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.tranId")
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<DailyTransaction, Transaction> postTransactionProcessor() {
        return dailyTran -> {
            // 1500-A-LOOKUP-XREF: validate card exists in cross-ref (reason 100)
            Optional<CardCrossRef> xrefOpt = cardCrossRefRepository.findById(dailyTran.getCardNum());
            if (xrefOpt.isEmpty()) {
                log.warn("REJECTED reason 100: INVALID CARD NUMBER {} for tran {}",
                        dailyTran.getCardNum(), dailyTran.getTranId());
                return null;
            }
            CardCrossRef xref = xrefOpt.get();

            // 1500-B-LOOKUP-ACCT: validate account exists (reason 101)
            Optional<Account> acctOpt = accountRepository.findById(xref.getAcctId());
            if (acctOpt.isEmpty()) {
                log.warn("REJECTED reason 101: ACCOUNT NOT FOUND for acct {} tran {}",
                        xref.getAcctId(), dailyTran.getTranId());
                return null;
            }
            Account account = acctOpt.get();

            // Check credit limit (reason 102)
            BigDecimal tempBal = account.getCurrCycCredit()
                    .subtract(account.getCurrCycDebit())
                    .add(dailyTran.getAmount());
            if (account.getCreditLimit().compareTo(tempBal) < 0) {
                log.warn("REJECTED reason 102: OVERLIMIT for acct {} tran {}",
                        xref.getAcctId(), dailyTran.getTranId());
                return null;
            }

            // Check expiration (reason 103)
            if (account.getExpirationDate() != null && dailyTran.getOrigTs() != null
                    && account.getExpirationDate().isBefore(dailyTran.getOrigTs().toLocalDate())) {
                log.warn("REJECTED reason 103: ACCOUNT EXPIRED for acct {} tran {}",
                        xref.getAcctId(), dailyTran.getTranId());
                return null;
            }

            // 2000-POST-TRANSACTION: map daily tran to transaction record
            Transaction tran = new Transaction();
            tran.setTranId(dailyTran.getTranId());
            tran.setTypeCd(dailyTran.getTypeCd());
            tran.setCatCd(dailyTran.getCatCd());
            tran.setSource(dailyTran.getSource());
            tran.setDescription(dailyTran.getDescription());
            tran.setAmount(dailyTran.getAmount());
            tran.setMerchantId(dailyTran.getMerchantId());
            tran.setMerchantName(dailyTran.getMerchantName());
            tran.setMerchantCity(dailyTran.getMerchantCity());
            tran.setMerchantZip(dailyTran.getMerchantZip());
            tran.setCardNum(dailyTran.getCardNum());
            tran.setOrigTs(dailyTran.getOrigTs());
            tran.setProcTs(LocalDateTime.now());

            return tran;
        };
    }

    @Bean
    public ItemWriter<Transaction> postTransactionWriter() {
        return transactions -> {
            for (Transaction tran : transactions) {
                // 2900-WRITE-TRANSACTION-FILE
                transactionRepository.save(tran);

                // Look up cross-ref for account update
                Optional<CardCrossRef> xrefOpt = cardCrossRefRepository.findById(tran.getCardNum());
                if (xrefOpt.isEmpty()) continue;
                String acctId = xrefOpt.get().getAcctId();

                Optional<Account> acctOpt = accountRepository.findById(acctId);
                if (acctOpt.isEmpty()) continue;
                Account account = acctOpt.get();

                // 2800-UPDATE-ACCOUNT-REC (lines 545-560)
                account.setCurrBal(account.getCurrBal().add(tran.getAmount()));
                if (tran.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
                    account.setCurrCycCredit(account.getCurrCycCredit().add(tran.getAmount()));
                } else {
                    account.setCurrCycDebit(account.getCurrCycDebit().add(tran.getAmount().negate()));
                }
                accountRepository.save(account);

                // 2700-UPDATE-TCATBAL (lines 467-542)
                TranCategoryBalanceId tcbId = new TranCategoryBalanceId(
                        acctId, tran.getTypeCd(), tran.getCatCd());
                Optional<TranCategoryBalance> tcbOpt = tranCategoryBalanceRepository.findById(tcbId);
                if (tcbOpt.isPresent()) {
                    TranCategoryBalance tcb = tcbOpt.get();
                    tcb.setBalance(tcb.getBalance().add(tran.getAmount()));
                    tranCategoryBalanceRepository.save(tcb);
                } else {
                    TranCategoryBalance tcb = new TranCategoryBalance();
                    tcb.setAcctId(acctId);
                    tcb.setTypeCd(tran.getTypeCd());
                    tcb.setCatCd(tran.getCatCd());
                    tcb.setBalance(tran.getAmount());
                    tranCategoryBalanceRepository.save(tcb);
                }
            }
        };
    }

    @Bean
    public Step postTransactionStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     JpaPagingItemReader<DailyTransaction> dailyTransactionReader,
                                     ItemProcessor<DailyTransaction, Transaction> postTransactionProcessor,
                                     ItemWriter<Transaction> postTransactionWriter) {
        return new StepBuilder("postTransactionStep", jobRepository)
                .<DailyTransaction, Transaction>chunk(10, transactionManager)
                .reader(dailyTransactionReader)
                .processor(postTransactionProcessor)
                .writer(postTransactionWriter)
                .build();
    }

    @Bean
    public Job postTransactionJob(JobRepository jobRepository, Step postTransactionStep) {
        return new JobBuilder("postTransactionJob", jobRepository)
                .start(postTransactionStep)
                .build();
    }
}
