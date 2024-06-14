package com.demo.project91.pojo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ignite.cache.query.annotations.QueryGroupIndex;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@QueryGroupIndex.List(@QueryGroupIndex(name = "idx1"))
public class Employee implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     *  @QuerySqlField(index = true) field will be group indexed.
     */
    @QuerySqlField(index = true)
    private Long id;

    /**
     *  QuerySqlField.Group field will be group indexed.
     */
    @QuerySqlField(index = true)
    private String name;
    private String email;
}
