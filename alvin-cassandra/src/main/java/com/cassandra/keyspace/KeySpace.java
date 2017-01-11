package com.cassandra.keyspace;

import com.cassandra.bean.ClusterCenter;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Created by xule on 2017/1/10.
 */
public class KeySpace {

    public static Session session;

    /**
     * Initiates a connection to the cluster
     * specified by the given contact point.
     *
     * @param cluster the cluster to use.
     */
    public void connect(ClusterCenter cluster) {
        session = cluster.instance.connect();
    }
}