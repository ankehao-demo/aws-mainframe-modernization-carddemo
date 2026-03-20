package com.aws.carddemo.service.auth;

import com.aws.carddemo.entity.auth.FraudRecord;
import com.aws.carddemo.entity.auth.PendingAuthSummary;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.auth.FraudRecordRepository;
import com.aws.carddemo.repository.auth.PendingAuthSummaryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Fraud service — replaces COPAUS2C.cbl.
 * Marks authorization records as fraudulent.
 */
@Service
public class FraudService {

    private final FraudRecordRepository fraudRecordRepository;
    private final PendingAuthSummaryRepository summaryRepository;

    public FraudService(FraudRecordRepository fraudRecordRepository,
                         PendingAuthSummaryRepository summaryRepository) {
        this.fraudRecordRepository = fraudRecordRepository;
        this.summaryRepository = summaryRepository;
    }

    @Transactional
    public FraudRecord markAsFraud(String authId, String fraudType, String description) {
        PendingAuthSummary summary = summaryRepository.findById(authId)
                .orElseThrow(() -> new ResourceNotFoundException("Authorization not found: " + authId));

        summary.setAuthStatus("FR");
        summaryRepository.save(summary);

        FraudRecord fraud = new FraudRecord();
        fraud.setAuthId(authId);
        fraud.setCardNum(summary.getCardNum());
        fraud.setAcctId(summary.getAcctId());
        fraud.setFraudType(fraudType);
        fraud.setFraudAmount(summary.getAuthAmount());
        fraud.setFraudDescription(description);
        fraud.setReportedDate(LocalDateTime.now());
        fraud.setStatus("OPEN");

        return fraudRecordRepository.save(fraud);
    }

    public Page<FraudRecord> listFraudRecords(Pageable pageable) {
        return fraudRecordRepository.findAll(pageable);
    }

    public Page<FraudRecord> listFraudByCard(String cardNum, Pageable pageable) {
        return fraudRecordRepository.findByCardNum(cardNum, pageable);
    }
}
