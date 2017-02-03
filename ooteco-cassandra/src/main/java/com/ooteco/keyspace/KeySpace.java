package com.ooteco.keyspace;

import com.datastax.driver.core.Session;
import com.ooteco.CassandraCluster;

/**
 * Created by xule on 2017/1/10.
 */
public class KeySpace {

    public Session session;

    /**
     * Initiates a connection to the cluster
     * specified by the given contact point.
     *
     * @param cluster the cluster to use.
     */
    public void connect(CassandraCluster cluster) {
        session = cluster.instance.connect();
    }
}