package com.aws.carddemo.service;

import com.aws.carddemo.dto.CustomerDto;
import com.aws.carddemo.entity.Customer;
import com.aws.carddemo.exception.ResourceNotFoundException;
import com.aws.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomer(String custId) {
        Customer customer = customerRepository.findById(custId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + custId));
        return mapToDto(customer);
    }

    private CustomerDto mapToDto(Customer customer) {
        return CustomerDto.builder()
                .custId(customer.getCustId())
                .custFirstName(customer.getCustFirstName())
                .custMiddleName(customer.getCustMiddleName())
                .custLastName(customer.getCustLastName())
                .custAddrLine1(customer.getCustAddrLine1())
                .custAddrLine2(customer.getCustAddrLine2())
                .custAddrLine3(customer.getCustAddrLine3())
                .custAddrStateCd(customer.getCustAddrStateCd())
                .custAddrCountryCd(customer.getCustAddrCountryCd())
                .custAddrZip(customer.getCustAddrZip())
                .custPhoneNum1(customer.getCustPhoneNum1())
                .custPhoneNum2(customer.getCustPhoneNum2())
                .custSsn(customer.getCustSsn())
                .custGovtIssuedId(customer.getCustGovtIssuedId())
                .custDobYyyyMmDd(customer.getCustDobYyyyMmDd())
                .custEftAccountId(customer.getCustEftAccountId())
                .custPriCardHolderInd(customer.getCustPriCardHolderInd())
                .custFicoCreditScore(customer.getCustFicoCreditScore())
                .build();
    }
}
