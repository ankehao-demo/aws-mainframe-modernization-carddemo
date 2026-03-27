package com.cardemo.dto;

import java.util.List;

/**
 * Paginated wrapper for card list results.
 * Replaces the COBOL 7-per-page screen pagination (COCRDLIC.cbl)
 * with standard Spring pagination metadata.
 */
public class CardListPageResponse {

    private List<CardListResponse> content;
    private int pageNumber;
    private int totalPages;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    public CardListPageResponse() {
    }

    public CardListPageResponse(List<CardListResponse> content, int pageNumber,
                                int totalPages, boolean hasNextPage, boolean hasPreviousPage) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.totalPages = totalPages;
        this.hasNextPage = hasNextPage;
        this.hasPreviousPage = hasPreviousPage;
    }

    public List<CardListResponse> getContent() {
        return content;
    }

    public void setContent(List<CardListResponse> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
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
}
