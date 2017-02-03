package com.ooteco.cassandra;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.ooteco.utils.CacheUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;


/**
 * Created by xule on 2017/1/11.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:beans.xml"})
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

    @Test
    public void testMapper(){
        CacheUtils.setString("haoren", "haorenka");
        String str = CacheUtils.getString("haoren");
        System.currentTimeMillis();
    }
}
