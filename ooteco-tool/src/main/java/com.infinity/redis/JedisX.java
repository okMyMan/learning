package com.infinity.redis;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.util.SafeEncoder;

/**
 * Created by xule on 2017/2/26.
 */
public class JedisX {
    protected static final Logger log = LoggerFactory.getLogger(JedisX.class);
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

    public Long expire(String key, int expireSeconds) {
        return expire(key, SafeEncoder.encode(key), expireSeconds);
    }

    private Long expire(String key, byte[] bytekey, int expireSeconds) {
        Long ret = jedis.expire(bytekey, expireSeconds);
        return ret;
    }

    public byte[] getByteArr(String key) {
        byte[] ret = get(SafeEncoder.encode(key));
        return ret;
    }

    public byte[] getSetByteArr(String key, byte[] value, int expireSecond) {
        return getSet(SafeEncoder.encode(key), value, expireSecond);

    }

    private byte[] getSet(byte[] bytekey, byte[] value, int expireSecond) {
        byte[] ret = jedis.getSet(bytekey, value);
        if (expireSecond > 0) {
            jedis.expire(bytekey, expireSecond);
        }
        return ret;
    }

    public String setByteArr(final String key, byte[] value, int expireSecond) {
        return set(SafeEncoder.encode(key), value, expireSecond);
    }

    private byte[] get(byte[] bytekey) {
        byte[] ret = jedis.get(bytekey);
        return ret;
    }

    private String set(final byte[] bytekey, byte[] value, int expireSecond) {
        String ret = null;
        if (expireSecond > 0) {
            ret = jedis.setex(bytekey, expireSecond, value);
        } else {
            ret = jedis.set(bytekey, value);
        }
        return ret;
    }


    public long delete(String key) {
        return delete(key, SafeEncoder.encode(key));
    }


    private long delete(String key, byte[]... bytekey) {
        long ret = jedis.del(bytekey);
        return ret;
    }
}
