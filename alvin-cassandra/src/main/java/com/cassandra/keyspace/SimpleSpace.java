package com.cassandra.keyspace;

import com.cassandra.bean.ClusterCenter;
import com.cassandra.bean.HostAndPort;
import com.datastax.driver.core.*;
import jnr.ffi.annotations.In;
import org.omg.CORBA.Object;

import java.security.KeyStore;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by xule on 2017/1/10.
 * 创建一个简单应用的space
 */
public class SimpleSpace extends KeySpace {

    private static PreparedStatement getKeyValueStmt;

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
    private SimpleSpace(ClusterCenter cluster, String strategy, Integer replication_factor) {
        connect(cluster);
        createSchema(strategy, replication_factor);
        createTable();

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
    }

    public static void setStr(String key, String value) {
        session.execute("INSERT INTO SimpleSpace.keyValues (key, value) VALUES (?, ?)",
                key, value);
    }

    public static String getStr(String key) {
        Statement stmt = getKeyValueStmt.bind(key).setFetchSize(1);
        Row row = session.execute(stmt).one();
        return null == row? null :row.getString("value");
    }
}
