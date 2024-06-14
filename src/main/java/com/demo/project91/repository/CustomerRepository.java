package com.demo.project91.repository;

import com.demo.project91.pojo.Customer;
import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;

@RepositoryConfig(cacheName = "CustomerCache")
public interface CustomerRepository extends IgniteRepository<Customer, Long> {
}
