package com.carddemo.transactiontype.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.transactiontype.entity.TransactionType;
import com.carddemo.transactiontype.repository.TransactionTypeRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Service layer for Transaction Type management.
 *
 * Replaces COBOL programs:
 * - COTRTUPC.cbl (CICS transaction CTTU) — add/edit operations
 * - COTRTLIC.cbl (CICS transaction CTLI) — list/update/delete operations
 *
 * All mutating operations are @Transactional, replacing EXEC CICS SYNCPOINT
 * used after each successful DB2 operation in the COBOL programs.
 */
@Service
@Transactional
public class TransactionTypeService {

    private final TransactionTypeRepository repo;

    public TransactionTypeService(TransactionTypeRepository repo) {
        this.repo = repo;
    }

    /**
     * Mirrors COTRTUPC 9600-WRITE-PROCESSING: upsert (update or insert).
     *
     * The COBOL program attempts an UPDATE first; if SQLCODE = +100 (not found),
     * it falls back to INSERT. This method replicates that logic using findById + save.
     *
     * @param trType      the 2-character transaction type code
     * @param description the transaction type description (up to 50 chars)
     * @return the saved or updated TransactionType entity
     */
    public TransactionType upsert(String trType, String description) {
        return repo.findById(trType)
                .map(existing -> {
                    existing.setTrDescription(description);
                    return repo.save(existing);
                })
                .orElseGet(() -> repo.save(new TransactionType(trType, description)));
    }

    /**
     * Mirrors COTRTLIC 9200-UPDATE-RECORD.
     *
     * Updates TR_DESCRIPTION for a given TR_TYPE. If the record is not found,
     * throws EntityNotFoundException (equivalent to SQLCODE +100 in COBOL).
     *
     * @param trType      the 2-character transaction type code
     * @param description the new description
     */
    public void update(String trType, String description) {
        TransactionType tt = repo.findById(trType)
                .orElseThrow(() -> new EntityNotFoundException(trType));
        tt.setTrDescription(description);
        repo.save(tt);
    }

    /**
     * Mirrors COTRTLIC 9300-DELETE-RECORD.
     *
     * Deletes by TR_TYPE. The COBOL handles SQLCODE = -532 (FK violation — child
     * records exist). In Spring/JPA, this surfaces as DataIntegrityViolationException
     * automatically due to the DELETE RESTRICT FK constraint on TRANSACTION_TYPE_CATEGORY.
     *
     * @param trType the 2-character transaction type code to delete
     */
    public void delete(String trType) {
        if (!repo.existsById(trType)) {
            throw new EntityNotFoundException(trType);
        }
        repo.deleteById(trType);
    }

    /**
     * Mirrors 8000-READ-FORWARD cursor paging from COTRTLIC.cbl.
     *
     * Pages through records ordered by TR_TYPE starting from a given key.
     * The default page size of 7 matches WS-MAX-SCREEN-LINES PIC S9(4) COMP VALUE 7
     * defined at line 60 of COTRTLIC.cbl.
     *
     * @param startKey the starting transaction type code for paging
     * @param page     zero-based page number
     * @param size     page size (default 7 in controller)
     * @return a page of TransactionType entities
     */
    @Transactional(readOnly = true)
    public Page<TransactionType> list(String startKey, int page, int size) {
        return repo.findByTrTypeGreaterThanEqualOrderByTrType(
                startKey, PageRequest.of(page, size));
    }
}
