package com.aws.carddemo.service;

import com.aws.carddemo.dto.TransactionTypeDto;
import com.aws.carddemo.entity.TransactionType;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.exception.ValidationException;
import com.aws.carddemo.repository.TransactionTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionTypeServiceTest {

    @Mock
    private TransactionTypeRepository transactionTypeRepository;

    @InjectMocks
    private TransactionTypeService transactionTypeService;

    @Test
    void listTransactionTypes_returnsList() {
        TransactionType type = TransactionType.builder().tranType("01").tranTypeDesc("PURCHASE").build();
        Page<TransactionType> page = new PageImpl<>(List.of(type));
        when(transactionTypeRepository.findAll(any(PageRequest.class))).thenReturn(page);

        List<TransactionTypeDto> result = transactionTypeService.listTransactionTypes(PageRequest.of(0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTranType()).isEqualTo("01");
    }

    @Test
    void addTransactionType_duplicate_throwsException() {
        when(transactionTypeRepository.existsById("01")).thenReturn(true);

        TransactionTypeDto dto = TransactionTypeDto.builder().tranType("01").tranTypeDesc("PURCHASE").build();

        assertThatThrownBy(() -> transactionTypeService.addTransactionType(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateTransactionType_notFound_throwsException() {
        when(transactionTypeRepository.findById("99")).thenReturn(Optional.empty());

        TransactionTypeDto dto = TransactionTypeDto.builder().tranTypeDesc("NEW DESC").build();

        assertThatThrownBy(() -> transactionTypeService.updateTransactionType("99", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
