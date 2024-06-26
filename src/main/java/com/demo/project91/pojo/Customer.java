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
public class Customer implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     *  @QuerySqlField(index = true) field will be group indexed.
     */
    @QuerySqlField(index = true)
    private Long id;

    /**
     *  QuerySqlField.Group field will be group indexed.
     */
    @QuerySqlField.Group(name = "idx1", order = 0)
    @QuerySqlField(index = true)
    private String firstName;
    @QuerySqlField(index = true)
    @QuerySqlField.Group(name = "idx1", order = 1)
    private String lastName;

    private Integer age;
    /**
     *  @QuerySqlField field will be part of query but not indexed.
     */
    @QuerySqlField
    private String country;

}
