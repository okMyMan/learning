package com.infinity.redis;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by xule on 2017/2/26.
 */
public class JedisX {
    private final Jedis jedis;


    private final JedisPool jedisPool;

//    private JedisX() {
//    }

    public JedisX(final GenericObjectPoolConfig poolConfig, final String host, int port,
                  int timeout, final String password) {
        jedisPool = new JedisPool(poolConfig, host, port, timeout, password);
        jedis = jedisPool.getResource();
//        jedis = jedisPool.getResource();
//        jedis.auth(PropertiesUtils.getValueFormProperties("redis.pwd"));
    }

    public String setString(String key, String value) {
        return jedis.set(Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(value.getBytes()));
    }

    public String getString(String key) {
        byte[] value = jedis.get(Base64.encodeBase64(key.getBytes()));
        return value != null ? new String(Base64.decodeBase64(value)) : null;
    }
}
