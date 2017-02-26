package com.infinity.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * @author Alvin
 * @ClassName: RedisUtil
 * @Description:
 * @date 2015年7月27日 下午8:42:59
 * @since JDK 1.7
 */
public class RedisUtil {

    //    public static int expireSecond = 600;
    private static JedisClusterX clusterX;

    public static JedisPool jedisPool;

    private static JedisX jedisX;

    private static JedisPoolConfig config;

    private RedisUtil() {
    }

    /**
     * 构造注入
     */
    private RedisUtil(String hosts, int timeout, int maxRedirection) {
        String[] hostArray = hosts.split(";");
        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
        for (String host : hostArray) {
            String[] hap = host.split(":");
            if (2 == hap.length) {
                HostAndPort hostAndPort = new HostAndPort(hap[0], Integer.valueOf(hap[1]));
                hostAndPorts.add(hostAndPort);
            }
        }
        clusterX = new JedisClusterX(hostAndPorts, timeout, maxRedirection);
    }

    private RedisUtil(String hosts, int timeout, int maxRedirection, String password) {
        String[] hostArray = hosts.split(";");
        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
        for (String host : hostArray) {
            String[] hap = host.split(":");
            if (2 == hap.length) {
                HostAndPort hostAndPort = new HostAndPort(hap[0], Integer.valueOf(hap[1]));
                hostAndPorts.add(hostAndPort);
            }
        }
        clusterX = new JedisClusterX(hostAndPorts, timeout, maxRedirection, password);
    }

    public static JedisClusterX getJedisClusterX() {
        return clusterX;
    }


    static {
        // 在web中,还可以用
//        ResourceBundle bundle = ResourceBundle.getBundle("redis-config");
//        if (bundle == null) {
//            throw new IllegalArgumentException(
//                    "[redis.properties] is not found!");
//        }
//        config = new JedisPoolConfig();
//        config.setMaxTotal(Integer.valueOf(bundle
//                .getString("redis.pool.maxTotal")));
//        config.setMaxIdle(Integer.valueOf(bundle
//                .getString("redis.pool.maxIdle")));
//        config.setTestOnBorrow(Boolean.valueOf(bundle
//                .getString("redis.pool.testOnBorrow")));
//        config.setTestOnReturn(Boolean.valueOf(bundle
//                .getString("redis.pool.testOnReturn")));
//        jedisPool = new JedisPool(config, bundle.getString("redis.ip"),
//                Integer.valueOf(bundle.getString("redis.port")), 2000, bundle.getString("redis.password"));
    }

    private RedisUtil(String ip, String port, String password, String timeOut, String maxTotal, String maxIdle, String testOnBorrow, String testOnReturn) {
        config = new JedisPoolConfig();
        config.setMaxTotal(Integer.valueOf(maxTotal));
        config.setMaxIdle(Integer.valueOf(maxIdle));
        config.setTestOnBorrow(Boolean.valueOf(testOnBorrow));
        config.setTestOnReturn(Boolean.valueOf(testOnReturn));
//        jedisPool = new JedisPool(config, ip, Integer.valueOf(port), Integer.valueOf(timeOut), password);

        jedisX = new JedisX(config, ip, Integer.valueOf(port), Integer.valueOf(timeOut), password);
    }

    public static JedisX getJedisX() {
        return jedisX;
    }

}
