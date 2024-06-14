package com.demo.project91.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.demo.project91.pojo.Customer;
import com.demo.project91.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    final CustomerRepository customerRepository;
    final Ignite ignite;

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer.getId(), customer);
    }

    public Iterable<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        log.info("Getting from customer-cache");
        IgniteCache<Long, Customer> cache = ignite.cache("customer-cache");
        Optional<Customer> customer = Optional.ofNullable(cache.get(id));
        if (customer.isPresent()) {
            log.info("Getting from ignite db");
            customer = customerRepository.findById(id);
        }
        return customer;
    }

    /**
     * Insert data into a ignite table using SqlFieldsQuery
     */
    public void insertAccounts() {
        IgniteCache<Long, Object> accounts = ignite.cache("account-cache");
        String qry = "INSERT INTO ACCOUNTS(ID, AMOUNT, UPDATEDATE) VALUES(?, ?, ?)";
        accounts.query(new SqlFieldsQuery(qry).setArgs(1, 250.05, new Date())).getAll();
        accounts.query(new SqlFieldsQuery(qry).setArgs(2, 255.05, new Date())).getAll();
        accounts.query(new SqlFieldsQuery(qry).setArgs(3, .05, new Date())).getAll();
    }

    /**
     * Get data from ignite table using SqlFieldsQuery
     */
    public String getAllAccounts() {
        IgniteCache<Long, Object> accounts = ignite.cache("account-cache");
        String qry = "SELECT * FROM ACCOUNTS";
        StringBuilder sb = new StringBuilder();
        List<List<?>> res = accounts.query(new SqlFieldsQuery(qry)).getAll();
        for (List<?> row : res) {
            sb.append("(" + row.get(0) + ", " + row.get(1) + ", " + row.get(2) + ")");
            sb.append("\n");
        }
        return sb.toString();
    }

//    @Cacheable(cacheNames = "CustomerCache", key = "#id")
//    public Optional<Customer> findById(Long id) {
//        log.info("Fetching from ignite db");
//        return customerRepository.findById(id);
//    }
}
