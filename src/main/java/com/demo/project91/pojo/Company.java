package com.demo.project91.pojo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company implements Serializable {

    private static final long serialVersionUID = -1L;

    @QuerySqlField(index = true)
    private Long id;

    private String name;
}
