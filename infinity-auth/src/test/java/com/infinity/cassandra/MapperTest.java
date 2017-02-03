package com.infinity.cassandra;

import com.cassandra.keyspace.SimpleSpace;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;


/**
 * Created by xule on 2017/1/11.
 */
public class MapperTest {
    @Table(keyspace = "SimpleSpace", name = "users",
            readConsistency = "QUORUM",
            writeConsistency = "QUORUM",
            caseSensitiveKeyspace = false,
            caseSensitiveTable = false)
    public class User {
        @PartitionKey
        @Column(name = "id")
        private UUID id;

        @Column(name = "name")
        private String name;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Autowired
    SimpleSpace simpleSpace;
    @Test
    public void testMapper(){
        MappingManager manager = new MappingManager(simpleSpace.session);
    }
}
