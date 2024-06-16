package com.demo.project91.service;

import java.util.ArrayList;
import java.util.List;
import javax.cache.Cache;

import com.demo.project91.pojo.Employee;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;

/**
 * Interact with Ignite as key-value store (persistent store)
 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    final Ignite ignite;
    IgniteCache<Long, Employee> cache;

    @PostConstruct
    public void postInit() {
        cache = ignite.cache("employee-cache");
    }

    public Employee save(Employee employee) {
        cache.put(employee.getId(), employee);
        return employee;
    }

    public Iterable<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        for (Cache.Entry<Long, Employee> e : cache) {
            employees.add(e.getValue());
        }
        return employees;
    }
}
