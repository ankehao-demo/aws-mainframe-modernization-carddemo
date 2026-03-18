package com.aws.carddemo.service;

import com.aws.carddemo.dto.TransactionTypeDto;
import com.aws.carddemo.entity.TransactionType;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.TransactionTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionTypeDto> listTransactionTypes(Pageable pageable) {
        Page<TransactionType> page = transactionTypeRepository.findAll(pageable);
        return page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionTypeDto getTransactionType(String typeCode) {
        TransactionType type = transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction type not found: " + typeCode));
        return mapToDto(type);
    }

    @Transactional
    public TransactionTypeDto addTransactionType(TransactionTypeDto dto) {
        if (transactionTypeRepository.existsById(dto.getTranType())) {
            throw new ValidationException("Transaction type already exists: " + dto.getTranType());
        }

        TransactionType type = TransactionType.builder()
                .tranType(dto.getTranType())
                .tranTypeDesc(dto.getTranTypeDesc())
                .build();

        TransactionType saved = transactionTypeRepository.save(type);
        return mapToDto(saved);
    }

    @Transactional
    public TransactionTypeDto updateTransactionType(String typeCode, TransactionTypeDto dto) {
        TransactionType type = transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction type not found: " + typeCode));

        if (dto.getTranTypeDesc() != null) {
            type.setTranTypeDesc(dto.getTranTypeDesc());
        }

        TransactionType saved = transactionTypeRepository.save(type);
        return mapToDto(saved);
    }

    private TransactionTypeDto mapToDto(TransactionType type) {
        return TransactionTypeDto.builder()
                .tranType(type.getTranType())
                .tranTypeDesc(type.getTranTypeDesc())
                .build();
    }
}
