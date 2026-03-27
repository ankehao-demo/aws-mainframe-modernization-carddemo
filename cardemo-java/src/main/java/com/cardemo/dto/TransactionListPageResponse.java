package com.cardemo.dto;

import java.util.List;

/**
 * Paginated wrapper for transaction list results.
 * Replaces the COBOL 10-per-page screen pagination (COTRN00C.cbl)
 * with keyset-style pagination metadata including first/last transaction IDs.
 */
public class TransactionListPageResponse {

    private List<TransactionListResponse> content;
    private int pageNumber;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private String firstTransactionId;
    private String lastTransactionId;

    public TransactionListPageResponse() {
    }

    public TransactionListPageResponse(List<TransactionListResponse> content, int pageNumber,
                                       boolean hasNextPage, boolean hasPreviousPage,
                                       String firstTransactionId, String lastTransactionId) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.hasNextPage = hasNextPage;
        this.hasPreviousPage = hasPreviousPage;
        this.firstTransactionId = firstTransactionId;
        this.lastTransactionId = lastTransactionId;
    }

    public List<TransactionListResponse> getContent() {
        return content;
    }

    public void setContent(List<TransactionListResponse> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public String getFirstTransactionId() {
        return firstTransactionId;
    }

    public void setFirstTransactionId(String firstTransactionId) {
        this.firstTransactionId = firstTransactionId;
    }

    public String getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(String lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }
}
