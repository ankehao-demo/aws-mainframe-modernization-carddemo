package com.aws.carddemo.batch;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.DailyTransaction;
import com.aws.carddemo.entity.DailyTransactionReject;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.entity.TransactionCategoryBalance;
import com.aws.carddemo.entity.TransactionCategoryBalanceId;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.DailyTransactionRejectRepository;
import com.aws.carddemo.repository.TransactionCategoryBalanceRepository;
import com.aws.carddemo.repository.TransactionRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class TransactionPostingProcessor implements ItemProcessor<DailyTransaction, Transaction> {

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryBalanceRepository tranCatBalRepository;
    private final DailyTransactionRejectRepository rejectRepository;

    public TransactionPostingProcessor(CardXrefRepository cardXrefRepository,
                                       AccountRepository accountRepository,
                                       TransactionRepository transactionRepository,
                                       TransactionCategoryBalanceRepository tranCatBalRepository,
                                       DailyTransactionRejectRepository rejectRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.tranCatBalRepository = tranCatBalRepository;
        this.rejectRepository = rejectRepository;
    }

    @Override
    public Transaction process(DailyTransaction dailyTran) throws Exception {
        // Validate card exists in card_xref (reject reason 100)
        Optional<CardXref> cardXrefOpt = cardXrefRepository.findById(dailyTran.getDalytranCardNum());
        if (cardXrefOpt.isEmpty()) {
            reject(dailyTran, 100, "Card not found in cross-reference");
            return null;
        }

        CardXref cardXref = cardXrefOpt.get();

        // Validate account exists (reject reason 101)
        Optional<Account> accountOpt = accountRepository.findById(cardXref.getXrefAcctId());
        if (accountOpt.isEmpty()) {
            reject(dailyTran, 101, "Account not found");
            return null;
        }

        Account account = accountOpt.get();

        // Validate credit limit (reject reason 102)
        BigDecimal projectedBalance = account.getAcctCurrCycCredit()
                .subtract(account.getAcctCurrCycDebit())
                .add(dailyTran.getDalytranAmt());
        if (account.getAcctCreditLimit().compareTo(projectedBalance) < 0) {
            reject(dailyTran, 102, "Credit limit exceeded");
            return null;
        }

        // Validate account not expired (reject reason 103)
        if ("N".equals(account.getAcctActiveStatus())) {
            reject(dailyTran, 103, "Account is inactive/expired");
            return null;
        }

        // Generate transaction ID
        String maxId = transactionRepository.findMaxTranId().orElse("0000000000000000");
        long nextId = Long.parseLong(maxId.trim()) + 1;
        String newTranId = String.format("%016d", nextId);

        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .tranId(newTranId)
                .tranTypeCd(dailyTran.getDalytranTypeCd())
                .tranCatCd(dailyTran.getDalytranCatCd())
                .tranSource(dailyTran.getDalytranSource())
                .tranDesc(dailyTran.getDalytranDesc())
                .tranAmt(dailyTran.getDalytranAmt())
                .tranMerchantId(dailyTran.getDalytranMerchantId())
                .tranMerchantName(dailyTran.getDalytranMerchantName())
                .tranMerchantCity(dailyTran.getDalytranMerchantCity())
                .tranMerchantZip(dailyTran.getDalytranMerchantZip())
                .tranCardNum(dailyTran.getDalytranCardNum())
                .tranOrigTs(dailyTran.getDalytranOrigTs())
                .tranProcTs(timestamp)
                .build();

        // Update account balances
        BigDecimal tranAmt = dailyTran.getDalytranAmt();
        account.setAcctCurrCycDebit(account.getAcctCurrCycDebit().add(tranAmt));
        account.setAcctCurrBal(account.getAcctCurrBal().add(tranAmt));
        accountRepository.save(account);

        // Update tran_cat_balance
        TransactionCategoryBalanceId balId = new TransactionCategoryBalanceId(
                cardXref.getXrefAcctId(),
                dailyTran.getDalytranTypeCd(),
                dailyTran.getDalytranCatCd());
        Optional<TransactionCategoryBalance> balOpt = tranCatBalRepository.findById(balId);
        if (balOpt.isPresent()) {
            TransactionCategoryBalance bal = balOpt.get();
            bal.setTranCatBal(bal.getTranCatBal().add(tranAmt));
            tranCatBalRepository.save(bal);
        }

        // Mark daily transaction as posted
        dailyTran.setPosted(true);

        return transaction;
    }

    private void reject(DailyTransaction dailyTran, int reasonCode, String reasonDesc) {
        DailyTransactionReject reject = DailyTransactionReject.builder()
                .dailyTransaction(dailyTran)
                .rejectReasonCode(reasonCode)
                .rejectReasonDesc(reasonDesc)
                .build();
        rejectRepository.save(reject);
        dailyTran.setPosted(true); // Mark as processed even if rejected
    }
}
