package com.aws.carddemo.service.auth;

import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.CardCrossRef;
import com.aws.carddemo.entity.auth.PendingAuthDetail;
import com.aws.carddemo.entity.auth.PendingAuthSummary;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.CardCrossRefRepository;
import com.aws.carddemo.repository.auth.PendingAuthSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Authorization processing service — replaces MQ trigger in COPAUA0C.cbl.
 * Processes authorization requests: validates card, checks limits, creates pending auth.
 * In production, this would use @JmsListener for MQ integration.
 */
@Service
public class AuthorizationProcessingService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationProcessingService.class);

    private final PendingAuthSummaryRepository summaryRepository;
    private final CardCrossRefRepository cardCrossRefRepository;
    private final AccountRepository accountRepository;

    public AuthorizationProcessingService(PendingAuthSummaryRepository summaryRepository,
                                           CardCrossRefRepository cardCrossRefRepository,
                                           AccountRepository accountRepository) {
        this.summaryRepository = summaryRepository;
        this.cardCrossRefRepository = cardCrossRefRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Process an authorization request.
     * In production with JMS, this method would be annotated with @JmsListener.
     */
    @Transactional
    public PendingAuthSummary processAuthorization(String cardNum, BigDecimal amount,
                                                     String merchantId, String merchantName) {
        LocalDateTime now = LocalDateTime.now();
        String authId = "AUTH" + now.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        if (authId.length() > 16) authId = authId.substring(0, 16);

        PendingAuthSummary summary = new PendingAuthSummary();
        summary.setAuthId(authId);
        summary.setCardNum(cardNum);
        summary.setAuthAmount(amount);
        summary.setAuthTimestamp(now);
        summary.setMerchantId(merchantId);
        summary.setMerchantName(merchantName);

        // Validate card in cross-ref
        Optional<CardCrossRef> xrefOpt = cardCrossRefRepository.findById(cardNum);
        if (xrefOpt.isEmpty()) {
            summary.setAuthStatus("RJ");
            addDetail(summary, "VALIDATE", "Card not found in cross-reference");
            return summaryRepository.save(summary);
        }

        String acctId = xrefOpt.get().getAcctId();
        summary.setAcctId(acctId);

        // Validate account
        Optional<Account> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            summary.setAuthStatus("RJ");
            addDetail(summary, "VALIDATE", "Account not found: " + acctId);
            return summaryRepository.save(summary);
        }

        Account account = acctOpt.get();

        // Check expiration
        if (account.getExpirationDate() != null && account.getExpirationDate().isBefore(now.toLocalDate())) {
            summary.setAuthStatus("RJ");
            addDetail(summary, "VALIDATE", "Account expired");
            return summaryRepository.save(summary);
        }

        // Check credit limit
        BigDecimal availCredit = account.getCreditLimit().subtract(account.getCurrBal());
        if (amount.compareTo(availCredit) > 0) {
            summary.setAuthStatus("RJ");
            addDetail(summary, "VALIDATE", "Insufficient credit. Available: " + availCredit);
            return summaryRepository.save(summary);
        }

        // Approved
        summary.setAuthStatus("AP");
        addDetail(summary, "APPROVED", "Authorization approved for " + amount);
        log.info("Authorization {} APPROVED for card {} amount {}", authId, cardNum, amount);

        return summaryRepository.save(summary);
    }

    private void addDetail(PendingAuthSummary summary, String type, String message) {
        PendingAuthDetail detail = new PendingAuthDetail();
        detail.setSummary(summary);
        detail.setDetailType(type);
        detail.setDetailMessage(message);
        detail.setDetailTimestamp(LocalDateTime.now());
        summary.getDetails().add(detail);
    }
}
