package com.infinity.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.Policies;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CassandraUtils
 *
 * @author Alvin Xu
 * @date 2016/10/31
 * @description
 */
public class CassandraUtils {

    private static Cluster cluster;

    private static Session session;

    private CassandraUtils() {
    }

    private CassandraUtils(String ips, String port) {
        PoolingOptions poolingOptions = new PoolingOptions();

        // 这些都有默认值  可以这么查看 PoolingOptions poolingOptions = cluster.getConfiguration().getPoolingOptions();
        poolingOptions
                .setConnectionsPerHost(HostDistance.LOCAL, 4, 10)
                .setConnectionsPerHost(HostDistance.REMOTE, 2, 4);

        poolingOptions
                .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                .setMaxRequestsPerConnection(HostDistance.REMOTE, 2000);
        // To disable heartbeat, set the interval to 0.
        poolingOptions.setHeartbeatIntervalSeconds(60);

        // determines the threshold that triggers the creation of a new connection when the pool is not at its maximum capacity. In general, you shouldn’t need to change its default value.
        // poolingOptions.setNewConnectionThreshold(HostDistance.LOCAL, 32768);

        String[] contactPoints = ips.split(";");

        cluster = Cluster.builder()
                // 配置数据库连接池
                .withPoolingOptions(poolingOptions)
                .addContactPoints(contactPoints)
                .withPort(Integer.parseInt(port))
//                .withLoadBalancingPolicy(Policies.defaultLoadBalancingPolicy())
                .build();

        session = cluster.connect();
    }

    /**
     * 一个项目可以只有一个session
     * @return
     */
    public static Session getSession() {
        return session;
    }

    /**
     * session是线程安全的，所以一个应用中，你可以只有一个session实例，官方建议一个keyspace一个session。
     *
     * @param keySpace
     * @return
     */
    public static Session getSession(String keySpace) {
        return cluster.connect(keySpace);
    }


    /**
     * print the number of open connections, active requests, and maximum capacity for each host, every 5 seconds
     */
    public static void monitorPool() {
        final LoadBalancingPolicy loadBalancingPolicy =
                cluster.getConfiguration().getPolicies().getLoadBalancingPolicy();
        final PoolingOptions poolingOptions =
                cluster.getConfiguration().getPoolingOptions();

        ScheduledExecutorService scheduled =
                Executors.newScheduledThreadPool(1);
        scheduled.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Session.State state = session.getState();
                for (Host host : state.getConnectedHosts()) {
                    HostDistance distance = loadBalancingPolicy.distance(host);
                    int connections = state.getOpenConnections(host);
                    int inFlightQueries = state.getInFlightQueries(host);
                    System.out.printf("%s connections=%d, current load=%d, max load=%d%n",
                            host, connections, inFlightQueries, connections * poolingOptions.getMaxRequestsPerConnection(distance));
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public static void metadataUsed() {
        Metadata metadata = cluster.getMetadata();
        Set<Host> hosts = metadata.getAllHosts();

        List<KeyspaceMetadata> keyspaceMetadatas = metadata.getKeyspaces();
    }
}
