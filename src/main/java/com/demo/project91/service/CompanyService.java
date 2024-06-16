package com.demo.project91.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.demo.project91.pojo.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Interact with Ignite via Spring @Cacheable abstraction
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyService {

    final JdbcTemplate jdbcTemplate;

    @Cacheable(value = "company-cache")
    public List<Company> getAllCompanies() {
        log.info("Fetching company from database!");
        return jdbcTemplate.query("select * from company", new CompanyRowMapper());
    }

    public void insertMockData() {
        log.info("Starting to insert mock data!");
        for (int i = 0; i < 10000; i++) {
            jdbcTemplate.update("INSERT INTO company (id, name) " + "VALUES (?, ?)",
                    i, "company_" + i);
        }
        log.info("Completed insert of mock data!");
    }

    class CompanyRowMapper implements RowMapper<Company> {
        @Override
        public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Company(rs.getLong("id"), rs.getString("name"));
        }
    }
}
