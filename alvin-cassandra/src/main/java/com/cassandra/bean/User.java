package com.cassandra.bean;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.UUID;

/**
 * Created by xule on 2017/1/9.
 */
@Table(name = "users")
public class User {

    // annotation on a field
    @PartitionKey
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
//@Table(name = "users")
//public class User {
//
//    private UUID id;
//
//    // annotation on a getter method
//    @PartitionKey
//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }
//
//}