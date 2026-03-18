package com.aws.carddemo.service;

import com.aws.carddemo.dto.TransactionDto;
import com.aws.carddemo.entity.Transaction;
import com.aws.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionReport(String cardNum, Pageable pageable) {
        Page<Transaction> page = transactionRepository.findByTranCardNum(cardNum, pageable);
        return page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private TransactionDto mapToDto(Transaction transaction) {
        return TransactionDto.builder()
                .tranId(transaction.getTranId())
                .tranTypeCd(transaction.getTranTypeCd())
                .tranCatCd(transaction.getTranCatCd())
                .tranSource(transaction.getTranSource())
                .tranDesc(transaction.getTranDesc())
                .tranAmt(transaction.getTranAmt())
                .tranMerchantId(transaction.getTranMerchantId())
                .tranMerchantName(transaction.getTranMerchantName())
                .tranMerchantCity(transaction.getTranMerchantCity())
                .tranMerchantZip(transaction.getTranMerchantZip())
                .tranCardNum(transaction.getTranCardNum())
                .tranOrigTs(transaction.getTranOrigTs())
                .tranProcTs(transaction.getTranProcTs())
                .build();
    }
}
