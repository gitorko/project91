package com.demo.project91.service;

import java.util.Optional;

import com.demo.project91.pojo.Customer;
import com.demo.project91.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    final CustomerRepository customerRepository;

    @Cacheable(cacheNames = "CustomerCache", key = "#id")
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
}
