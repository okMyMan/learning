package com.ooteco.keyspace.space2;

import com.ooteco.CassandraCluster;
import com.ooteco.keyspace.KeySpace;

/**
 * Created by xule on 2017/1/13.
 */
public class ProductSpace extends KeySpace {
    private ProductSpace() {
    }

    /**
     * 初始化keyspace
     *
     * @param cluster            具体集群
     * @param strategy           副本配置策略
     *                           SimpleStrategy   副本不考虑机架的因素，按照Token放置在连续的几个节点。假如副本数为3，属于A节点的数据在B.C两个节点中也放置副本。
     *                           OldNetworkTopologyStrategy  考虑机架的因素，除了基本的数据外，先找一个处于不同数据中心的点放置一个副本，其余N-2个副本放置在同一数据中心的不同机架中。
     *                           NetworkTopologyStrategy   将M个副本放置到其他的数据中心，将N-M-1的副本放置在同一数据中心的不同机架中。
     * @param replication_factor 副本因子
     */
    private ProductSpace(CassandraCluster cluster, String strategy, Integer replication_factor) {
        connect(cluster);
        createSchema(strategy, replication_factor);
        createTable();

    }

    private void createSchema(String strategy, Integer replication_factor) {
        session.execute("CREATE KEYSPACE IF NOT EXISTS ProductSpace WITH replication " +
                "= {'class':'" + strategy + "', 'replication_factor':" + replication_factor + " };");
    }

    private void createTable() {
        // 创建表1 keyValues  只有两个字段,  key 和 value
        session.execute(
                "CREATE TABLE IF NOT EXISTS SimpleSpace.keyValues (" +
                        "key text PRIMARY KEY," +
                        "value text," +
                        ");");


        // 创建表2 ...
//        session.execute(
//                "CREATE TABLE IF NOT EXISTS SimpleSpace.users (id uuid PRIMARY KEY, name text);"
//        );
    }
}
