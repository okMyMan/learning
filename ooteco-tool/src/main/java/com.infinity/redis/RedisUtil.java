package com.infinity.redis;

/***************************************************************************************************
 *   @Title: RedisUtil.java                                                                             *
 *   @Date:  2015年7月27日 下午8:42:59                                                                 *
 *   @Since: JDK 1.7                                                                               *
 *   @Copyright: All material herein ©2015 Marco Polo Technology Co., Ltd, all rights reserved     * 
 * *********************************************************************************************** *
 *    注意： 本内容仅限于深圳马可孛罗科技有限公司内部使用，禁止转发                                                      *
 ***************************************************************************************************/

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;


/** 
 * @ClassName: RedisUtil 
 * @Description: 
 * @author Jango 
 * @date 2015年7月27日 下午8:42:59 
 * @since JDK 1.7 
 */
public class RedisUtil {
    
//    public static int expireSecond = 600;
    private static JedisClusterX clusterX;
    
    private RedisUtil() {}

    /**构造注入*/
    private RedisUtil(String hosts, int timeout, int maxRedirection) {
        String[] hostArray = hosts.split(";");
        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
        for(String host : hostArray){
            String[] hap = host.split(":");
            if(2 == hap.length){
                HostAndPort hostAndPort = new HostAndPort(hap[0], Integer.valueOf(hap[1]));
                hostAndPorts.add(hostAndPort);
            }
        }
        clusterX = new JedisClusterX(hostAndPorts, timeout, maxRedirection);
    }

    private RedisUtil(String hosts, int timeout, int maxRedirection, String password) {
        String[] hostArray = hosts.split(";");
        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
        for(String host : hostArray){
            String[] hap = host.split(":");
            if(2 == hap.length){
                HostAndPort hostAndPort = new HostAndPort(hap[0], Integer.valueOf(hap[1]));
                hostAndPorts.add(hostAndPort);
            }
        }
        clusterX = new JedisClusterX(hostAndPorts, timeout, maxRedirection, password);
    }

    public static JedisClusterX getJedisX() {
        return clusterX;
    }


    public static JedisPool jedisPool;
    static{
        // 在web中,还可以用
        ResourceBundle bundle = ResourceBundle.getBundle("redis-config");
        if (bundle == null) {
            throw new IllegalArgumentException(
                    "[redis.properties] is not found!");
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.valueOf(bundle
                .getString("redis.pool.maxTotal")));
        config.setMaxIdle(Integer.valueOf(bundle
                .getString("redis.pool.maxIdle")));
        config.setTestOnBorrow(Boolean.valueOf(bundle
                .getString("redis.pool.testOnBorrow")));
        config.setTestOnReturn(Boolean.valueOf(bundle
                .getString("redis.pool.testOnReturn")));
        jedisPool = new JedisPool(config, bundle.getString("redis.ip"),
                Integer.valueOf(bundle.getString("redis.port")), 2000, bundle.getString("redis.password"));
    }

}
