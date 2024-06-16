package com.demo.project91.controller;

import java.util.Objects;
import java.util.Optional;

import com.demo.project91.pojo.Company;
import com.demo.project91.pojo.Customer;
import com.demo.project91.pojo.Employee;
import com.demo.project91.service.AccountService;
import com.demo.project91.service.CompanyService;
import com.demo.project91.service.CountryService;
import com.demo.project91.service.CustomerService;
import com.demo.project91.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    final Ignite ignite;
    final CustomerService customerService;
    final EmployeeService employeeService;
    final CompanyService companyService;
    final AccountService accountService;
    final CountryService countryService;

    @GetMapping("/info")
    public String getInfo() {
        DataRegionConfiguration drc = ignite.configuration().getDataStorageConfiguration().getDataRegionConfigurations()[0];
        StringBuilder sb = new StringBuilder();
        sb.append("IgniteConsistentId: " + ignite.configuration().getConsistentId());
        sb.append("\n");
        sb.append("IgniteInstanceName: " + ignite.configuration().getIgniteInstanceName());
        sb.append("\n");
        sb.append("CommunicationSpi.localPort: " + ((TcpCommunicationSpi) ignite.configuration().getCommunicationSpi()).getLocalPort());
        sb.append("\n");
        sb.append("DefaultDataRegion initial size: " + ignite.configuration().getDataStorageConfiguration().getDefaultDataRegionConfiguration().getInitialSize());
        sb.append("\n");
        sb.append("Size: " + drc.getName() + " initial size: " + drc.getInitialSize());
        sb.append("\n");
        for (String cacheName : ignite.cacheNames()) {
            sb.append("Cache in cluster: " + cacheName);
            sb.append("\n");
        }
        return sb.toString();
    }

    @GetMapping("/country/{key}")
    public String getCountryFromCache(@PathVariable String key) {
        log.info("Fetching data from country-cache!");
        return countryService.getValue(key);
    }

    @PutMapping("/country/{key}/{value}")
    public String saveCountryCode(@PathVariable String key, @PathVariable String value) {
        log.info("Inserting data to country-cache!");
        countryService.insert(key, value);
        return "done!";
    }

    @PostMapping("/customer")
    public Customer saveCustomer(@Valid @RequestBody Customer customer) {
        log.info("Saving customer!");
        return customerService.saveCustomer(customer);
    }

    @GetMapping("/customer")
    public Iterable<Customer> getAllCustomers() {
        log.info("Fetching customers!");
        return customerService.getAllCustomers();
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        log.info("Getting customer!");
        Optional<Customer> customer = customerService.getCustomerById(id);
        if (customer.isPresent() && Objects.isNull(customer.get())) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customer.get());
    }

    @GetMapping("/account/insert")
    public String accountInsert() {
        log.info("Inserting accounts!");
        accountService.insertAccounts();
        return "done!";
    }

    @GetMapping("/account")
    public String getAccounts() {
        log.info("Fetching accounts!");
        return accountService.getAllAccounts();
    }

    @PostMapping("/employee")
    public Employee saveEmployee(@Valid @RequestBody Employee employee) {
        log.info("Saving employee!");
        return employeeService.save(employee);
    }

    @GetMapping("/employee")
    public Iterable<Employee> getAllEmployees() {
        log.info("Fetching employees!");
        return employeeService.getAllEmployees();
    }

    @GetMapping("/company")
    public Iterable<Company> getAllCompany() {
        log.info("Fetching companies!");
        return companyService.getAllCompanies();
    }

    @GetMapping("/company/mock-data")
    public void insertDummyDataToDb() {
        log.info("Insert mock data!");
        companyService.insertMockData();
    }
}
