package com.cassandra.bean;

import com.datastax.driver.core.Cluster;

import java.util.List;

/**
 * Created by xule on 2017/1/11.
 * 每个集群只生成一个cluster属性
 */
public class ClusterCenter {

    public Cluster instance;

    private ClusterCenter() {
    }

    /**
     *
     * @param ips       ip列表,  分号隔开
     * @param port      端口  所有节点的端口一致
     */
    private ClusterCenter(String ips, int port) {
        String[] contactPoints = ips.split(";");
        instance = com.datastax.driver.core.Cluster.builder()
                .addContactPoints(contactPoints).withPort(port)
                .build();
    }
}
