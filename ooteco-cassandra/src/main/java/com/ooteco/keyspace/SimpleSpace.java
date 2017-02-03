package com.ooteco.keyspace;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.ooteco.CassandraCluster;

/**
 * Created by xule on 2017/1/10.
 * 创建一个简单应用的space
 */
public class SimpleSpace extends KeySpace {

    private static PreparedStatement getKeyValueStmt;
    private static PreparedStatement setKeyValueStmt;

    private SimpleSpace() {
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
    private SimpleSpace(CassandraCluster cluster, String strategy, Integer replication_factor) {
        connect(cluster);
        createSchema(strategy, replication_factor);
        createTable();

        setKeyValueStmt = session.prepare("INSERT INTO SimpleSpace.keyValues (key, value) VALUES (?, ?)");
        getKeyValueStmt = session.prepare("SELECT value FROM SimpleSpace.keyValues WHERE key = ?");
    }

    private void createSchema(String strategy, Integer replication_factor) {
        session.execute("CREATE KEYSPACE IF NOT EXISTS SimpleSpace WITH replication " +
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
        session.execute(
                "CREATE TABLE IF NOT EXISTS SimpleSpace.users (id uuid PRIMARY KEY, name text);"
        );
    }

    public void setStr(String key, String value) {
        Statement stmt = setKeyValueStmt.bind(key, value);
        session.execute(stmt);
//        session.execute("INSERT INTO SimpleSpace.keyValues (key, value) VALUES (?, ?)",
//                key, value);
    }

    public String getStr(String key) {
        Statement stmt = getKeyValueStmt.bind(key).setFetchSize(1);
        Row row = session.execute(stmt).one();
        return null == row? null :row.getString("value");
    }
}
