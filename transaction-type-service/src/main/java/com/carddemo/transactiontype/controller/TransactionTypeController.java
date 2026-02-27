package com.carddemo.transactiontype.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.transactiontype.dto.TransactionTypeRequest;
import com.carddemo.transactiontype.entity.TransactionType;
import com.carddemo.transactiontype.service.TransactionTypeService;

import jakarta.validation.Valid;

/**
 * REST controller replacing both CICS transactions:
 * - CTTU (add/edit via COTRTUPC) — mapped to PUT /{trType}
 * - CTLI (list/update/delete via COTRTLIC) — mapped to GET / and DELETE /{trType}
 *
 * The default page size is 7, matching WS-MAX-SCREEN-LINES PIC S9(4) COMP VALUE 7
 * defined at line 60 of COTRTLIC.cbl.
 *
 * The BMS maps (COTRTUP and COTRTLI) are the old terminal screen definitions
 * and are fully replaced by this REST API.
 */
@RestController
@RequestMapping("/api/transaction-types")
public class TransactionTypeController {

    private final TransactionTypeService service;

    public TransactionTypeController(TransactionTypeService service) {
        this.service = service;
    }

    @GetMapping
    public Page<TransactionType> list(
            @RequestParam(defaultValue = "00") String startKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {
        return service.list(startKey, page, size);
    }

    @PutMapping("/{trType}")
    public TransactionType upsert(
            @PathVariable String trType,
            @Valid @RequestBody TransactionTypeRequest req) {
        return service.upsert(trType, req.getDescription());
    }

    @DeleteMapping("/{trType}")
    public ResponseEntity<Void> delete(@PathVariable String trType) {
        service.delete(trType);
        return ResponseEntity.noContent().build();
    }
}
