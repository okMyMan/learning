package com.ooteco;

import com.datastax.driver.core.Cluster;

/**
 * Created by xule on 2017/1/12.
 */
public class CassandraCluster {
    public Cluster instance;

    private CassandraCluster() {
    }

    /**
     *
     * @param ips       ip列表,  分号隔开
     * @param port      端口  所有节点的端口一致
     */
    private CassandraCluster(String ips, int port) {
        String[] contactPoints = ips.split(";");
        instance = Cluster.builder()
                .addContactPoints(contactPoints).withPort(port)
                .build();
    }
}
