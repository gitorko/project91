package com.demo.project91.repository;

import javax.cache.Cache;

import com.demo.project91.pojo.Customer;
import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;

@RepositoryConfig(cacheName = "customer-cache")
public interface CustomerRepository extends IgniteRepository<Customer, Long> {
    Cache.Entry<Long, Customer> findByFirstName(String firstName);
}
