package com.cardemo.service;

import com.cardemo.dto.TransactionDetailResponse;
import com.cardemo.dto.TransactionListPageResponse;
import com.cardemo.dto.TransactionListResponse;
import com.cardemo.entity.Transaction;
import com.cardemo.exception.ResourceNotFoundException;
import com.cardemo.repository.TransactionRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Transaction service converting COTRN00C.cbl (list) and COTRN01C.cbl (detail) logic.
 *
 * COTRN00C transaction list:
 *   - VSAM STARTBR/READNEXT/READPREV/ENDBR on TRANSACT file
 *   - 10 per page in COBOL, configurable in Java
 *   - Ordered by transaction ID ascending
 *   - STARTBR GTEQ behavior: fetch transactions with ID >= startTransactionId
 *
 * COTRN01C transaction detail:
 *   - READ-ONLY (no UPDATE lock, unlike the COBOL program at line 275)
 *   - Returns all fields from CVTRA05Y.cpy
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionListPageResponse listTransactions(String startTransactionId,
                                                         Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.ASC, "tranId");
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Transaction> page;
        if (startTransactionId != null && !startTransactionId.isEmpty()) {
            page = transactionRepository.findByTranIdGreaterThanEqual(
                    startTransactionId, sortedPageable);
        } else {
            page = transactionRepository.findAll(sortedPageable);
        }

        List<TransactionListResponse> content = page.getContent().stream()
                .map(this::toListResponse)
                .toList();

        String firstTranId = content.isEmpty() ? null : content.get(0).getTransactionId();
        String lastTranId = content.isEmpty() ? null :
                content.get(content.size() - 1).getTransactionId();

        return new TransactionListPageResponse(
                content,
                page.getNumber(),
                page.hasNext(),
                page.hasPrevious(),
                firstTranId,
                lastTranId
        );
    }

    public TransactionDetailResponse getTransactionDetail(String transactionId) {
        Transaction tran = transactionRepository.findByTranId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction ID NOT found: " + transactionId));

        return buildDetailResponse(tran);
    }

    private TransactionListResponse toListResponse(Transaction tran) {
        String formattedDate = formatDate(tran.getOriginTimestamp());
        return new TransactionListResponse(
                tran.getTranId(),
                formattedDate,
                tran.getDescription(),
                tran.getAmount()
        );
    }

    /**
     * Formats date from timestamp to MM/DD/YY per COTRN00C.cbl lines 384-388.
     * Input format from TRAN-ORIG-TS: "YYYY-MM-DD-HH.MM.SS.FFFFFF" or ISO format.
     */
    private String formatDate(String timestamp) {
        if (timestamp == null || timestamp.length() < 10) {
            return timestamp;
        }
        // Extract YYYY-MM-DD portion
        String datePart = timestamp.substring(0, 10);
        if (datePart.contains("-")) {
            String[] parts = datePart.split("-");
            if (parts.length >= 3) {
                String year = parts[0].length() >= 4 ? parts[0].substring(2) : parts[0];
                return parts[1] + "/" + parts[2] + "/" + year;
            }
        }
        return timestamp;
    }

    private TransactionDetailResponse buildDetailResponse(Transaction tran) {
        TransactionDetailResponse response = new TransactionDetailResponse();
        response.setTransactionId(tran.getTranId());
        response.setCardNumber(tran.getCardNum());
        response.setTypeCode(tran.getTypeCd());
        response.setCategoryCode(tran.getCategoryCd());
        response.setSource(tran.getSource());
        response.setAmount(tran.getAmount());
        response.setDescription(tran.getDescription());
        response.setOriginTimestamp(tran.getOriginTimestamp());
        response.setProcessedTimestamp(tran.getProcessedTimestamp());
        response.setMerchantId(tran.getMerchantId());
        response.setMerchantName(tran.getMerchantName());
        response.setMerchantCity(tran.getMerchantCity());
        response.setMerchantZip(tran.getMerchantZip());
        return response;
    }
}
