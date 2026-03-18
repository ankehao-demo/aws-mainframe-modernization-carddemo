package com.aws.carddemo.service;

import com.aws.carddemo.dto.AuthorizationRequest;
import com.aws.carddemo.dto.AuthorizationResponse;
import com.aws.carddemo.entity.Account;
import com.aws.carddemo.entity.AuthFraud;
import com.aws.carddemo.entity.CardXref;
import com.aws.carddemo.entity.PendingAuthDetail;
import com.aws.carddemo.entity.PendingAuthSummary;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.AccountRepository;
import com.aws.carddemo.repository.AuthFraudRepository;
import com.aws.carddemo.repository.CardXrefRepository;
import com.aws.carddemo.repository.PendingAuthDetailRepository;
import com.aws.carddemo.repository.PendingAuthSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AuthorizationService {

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final PendingAuthSummaryRepository pendingAuthSummaryRepository;
    private final PendingAuthDetailRepository pendingAuthDetailRepository;
    private final AuthFraudRepository authFraudRepository;

    public AuthorizationService(CardXrefRepository cardXrefRepository,
                                AccountRepository accountRepository,
                                PendingAuthSummaryRepository pendingAuthSummaryRepository,
                                PendingAuthDetailRepository pendingAuthDetailRepository,
                                AuthFraudRepository authFraudRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.pendingAuthSummaryRepository = pendingAuthSummaryRepository;
        this.pendingAuthDetailRepository = pendingAuthDetailRepository;
        this.authFraudRepository = authFraudRepository;
    }

    @Transactional
    public AuthorizationResponse processAuthorization(AuthorizationRequest request) {
        // Look up card cross-reference
        CardXref cardXref = cardXrefRepository.findById(request.getCardNum())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getCardNum()));

        // Read account data
        Account account = accountRepository.findById(cardXref.getXrefAcctId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for card: " + request.getCardNum()));

        // Get or create pending auth summary
        PendingAuthSummary summary = pendingAuthSummaryRepository
                .findByPaAcctId(cardXref.getXrefAcctId())
                .orElseGet(() -> {
                    PendingAuthSummary newSummary = PendingAuthSummary.builder()
                            .paAcctId(cardXref.getXrefAcctId())
                            .paApprovedCount(0)
                            .paApprovedAmount(BigDecimal.ZERO)
                            .paDeclinedCount(0)
                            .paDeclinedAmount(BigDecimal.ZERO)
                            .paCreditLimit(account.getAcctCreditLimit())
                            .paCreditAvailable(account.getAcctCreditLimit().subtract(account.getAcctCurrBal()))
                            .build();
                    return pendingAuthSummaryRepository.save(newSummary);
                });

        // Make approval/decline decision
        BigDecimal totalAuthAmount = summary.getPaApprovedAmount().add(request.getAmount());
        boolean approved = totalAuthAmount.compareTo(account.getAcctCreditLimit()) <= 0;

        String authId = UUID.randomUUID().toString().substring(0, 16);
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

        // Create pending auth detail
        PendingAuthDetail detail = PendingAuthDetail.builder()
                .summary(summary)
                .paCardNum(request.getCardNum())
                .paTranId(authId)
                .paTranAmt(request.getAmount())
                .paMerchantId(request.getMerchantId())
                .paMerchantName(request.getMerchantName())
                .paAuthDate(dateStr)
                .paAuthTime(timeStr)
                .paRespCode(approved ? "0000" : "0051")
                .paFraudFlag("N")
                .build();

        pendingAuthDetailRepository.save(detail);

        // Update summary
        if (approved) {
            summary.setPaApprovedCount(summary.getPaApprovedCount() + 1);
            summary.setPaApprovedAmount(summary.getPaApprovedAmount().add(request.getAmount()));
            summary.setPaCreditAvailable(summary.getPaCreditAvailable().subtract(request.getAmount()));
        } else {
            summary.setPaDeclinedCount(summary.getPaDeclinedCount() + 1);
            summary.setPaDeclinedAmount(summary.getPaDeclinedAmount().add(request.getAmount()));
        }
        pendingAuthSummaryRepository.save(summary);

        return AuthorizationResponse.builder()
                .cardNum(request.getCardNum())
                .authId(authId)
                .responseCode(approved ? "0000" : "0051")
                .approvedAmount(approved ? request.getAmount() : BigDecimal.ZERO)
                .message(approved ? "Approved" : "Declined - credit limit exceeded")
                .build();
    }

    @Transactional
    public void markFraud(String cardNum, String tranId) {
        AuthFraud fraud = AuthFraud.builder()
                .afCardNum(cardNum)
                .afTranId(tranId)
                .afFraudDate(LocalDateTime.now())
                .afFraudType("MANUAL")
                .afNotes("Marked as fraud by administrator")
                .build();
        authFraudRepository.save(fraud);
    }
}
