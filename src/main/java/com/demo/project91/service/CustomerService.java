package com.demo.project91.service;

import java.util.Optional;

import com.demo.project91.pojo.Customer;
import com.demo.project91.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Interact with Ignite via IgniteRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    final CustomerRepository customerRepository;

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer.getId(), customer);
    }

    public Iterable<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

}
