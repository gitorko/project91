package com.demo.project91.service;

import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.springframework.stereotype.Service;

/**
 * Interact with Ignite directly via SqlFieldsQuery
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    final Ignite ignite;
    IgniteCache<Long, Object> accountCache;

    @PostConstruct
    public void postInit() {
        accountCache = ignite.cache("account-cache");
    }

    /**
     * Insert data into a ignite table using SqlFieldsQuery
     */
    public void insertAccounts() {
        String qry = "INSERT INTO ACCOUNTS(ID, AMOUNT, UPDATEDATE) VALUES(?, ?, ?)";
        accountCache.query(new SqlFieldsQuery(qry).setArgs(1, 250.05, new Date())).getAll();
        accountCache.query(new SqlFieldsQuery(qry).setArgs(2, 255.05, new Date())).getAll();
        accountCache.query(new SqlFieldsQuery(qry).setArgs(3, .05, new Date())).getAll();
    }

    /**
     * Get data from ignite table using SqlFieldsQuery
     */
    public String getAllAccounts() {
        String qry = "SELECT * FROM ACCOUNTS";
        StringBuilder sb = new StringBuilder();
        List<List<?>> res = accountCache.query(new SqlFieldsQuery(qry)).getAll();
        for (List<?> row : res) {
            sb.append("(" + row.get(0) + ", " + row.get(1) + ", " + row.get(2) + ")");
            sb.append("\n");
        }
        return sb.toString();
    }
}
