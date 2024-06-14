package com.demo.project91.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
    @Id
    @QuerySqlField(index = true)
    private Long id;
    @QuerySqlField(index = true)
    private String name;
    private Integer age;
    @QuerySqlField
    private String country;
}
