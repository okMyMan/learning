package com.infinity.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.infinity.Constant;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Tuple;
import redis.clients.util.SafeEncoder;

import com.infinity.redis.exception.JedisXValueNotSupportException;
import com.infinity.redis.util.JedisXSerializeUtil;
import com.infinity.thread.NamedThreadFactory;

public class JedisClusterX {
    protected static final Logger log = LoggerFactory.getLogger(JedisClusterX.class);

    private final JedisCluster cluster;

    private final ExecutorService executorService = Executors.newFixedThreadPool(500, new NamedThreadFactory("jedisx"));

    public JedisClusterX(Set<HostAndPort> jedisClusterNodes) {
        cluster = new JedisCluster(jedisClusterNodes);
    }

    public JedisClusterX(Set<HostAndPort> jedisClusterNodes, int timeout, int maxRedirections) {
        cluster = new JedisCluster(jedisClusterNodes, timeout, maxRedirections);
    }

    public JedisClusterX(Set<HostAndPort> hostAndPorts, int timeout, int maxRedirection, String password) {
        cluster = new JedisCluster(hostAndPorts, timeout, timeout, maxRedirection, password, new GenericObjectPoolConfig());
    }

    public Long expire(String key, int expireSeconds) {
        return expire(key, Base64.encodeBase64(key.getBytes()), expireSeconds);
    }

    private Long expire(String key, byte[] bytekey, int expireSeconds) {

        try {
            Long ret = cluster.expire(bytekey, expireSeconds);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    public String setString(final String key, int expireSecond, String value) {
        return setByteArr(key, expireSecond, Base64.encodeBase64(value.getBytes()));
    }

    public void asyncSetString(final String key, final int expireSecond, final String value) {

        FutureTask<String> future = new FutureTask<String>(new Runnable() {
            @Override
            public void run() {
                setString(key, expireSecond, value);
            }
        }, null);
        executorService.execute(future);
    }

    public void asyncSetObject(final String key, final int expireSecond, final Object value) {
        FutureTask<Object> future = new FutureTask<Object>(new Runnable() {
            @Override
            public void run() {
                setObject(key, expireSecond, value);
            }
        }, null);
        executorService.execute(future);
    }

    public String setObject(final String key, int expireSecond, Object value) {
        return setByteArr(key, expireSecond, serialize(value));
    }

    public String setByteArr(final String key, int expireSecond, byte[] value) {
        valueTypeAssert(value);
        return set(key, Base64.encodeBase64(key.getBytes()), expireSecond, value);
    }

    private String set(String key, final byte[] bytekey, int expireSecond, byte[] value) {

        try {
            String ret = null;
            if (expireSecond > 0) {
                ret = cluster.setex(bytekey, expireSecond, value);
            } else {
                ret = cluster.set(bytekey, value);
            }
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
        }

        return "failed";
    }

    public String multiSetString(final Map<String, String> keyValue, int expireSecond) {
        Map<String, byte[]> keyValuebMap = new HashMap<String, byte[]>();
        for (String key : keyValue.keySet()) {
            String value = keyValue.get(key);
            if (value != null) {
                keyValuebMap.put(key, Base64.encodeBase64(value.getBytes()));
            }
        }
        return multiSet(keyValuebMap, expireSecond);
    }

    public String multiSetObject(Map<String, Object> keyValue, int expireSecond) {
        Map<String, byte[]> keyValuebMap = new HashMap<String, byte[]>();
        for (String key : keyValue.keySet()) {
            Object value = keyValue.get(key);
            if (value != null) {
                keyValuebMap.put(key, serialize(value));
            }
        }
        return multiSet(keyValuebMap, expireSecond);
    }

    public String multiSetByteArr(Map<String, byte[]> keyValue, int expireSecond) {
        Map<String, byte[]> keyValuebMap = new HashMap<String, byte[]>();
        for (String key : keyValue.keySet()) {
            byte[] value = keyValue.get(key);
            if (value != null) {
                keyValuebMap.put(key, value);
            }
        }
        return multiSet(keyValuebMap, expireSecond);
    }

    private String multiSet(Map<String, byte[]> keyValuebMap, int expireSecond) {
        String ret = multiSet(expireSecond, getKeyValueBArrArr(keyValuebMap));
        return ret;
    }

    private String multiSet(int expireSecond, final byte[]... keyValues) {
        try {
            String ret = null;
            ret = cluster.mset(keyValues);
            if (expireSecond > 0) { // it's
                for (int i = 0; i < keyValues.length; i += 2) {
                    cluster.expire(keyValues[i], expireSecond);
                }
            }
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "failed";
    }

    public long setStringIfNotExist(final String key, int expireSecond, String value) {
        return setByteArrIfNotExist(key, expireSecond, Base64.encodeBase64(value.getBytes()));
    }

    public long setObjectIfNotExist(final String key, int expireSecond, Object value) {
        return setByteArrIfNotExist(key, expireSecond, serialize(value));
    }

    public long setByteArrIfNotExist(final String key, int expireSecond, byte[] value) {
        valueTypeAssert(value);
        return setIfNotExist(key, Base64.encodeBase64(key.getBytes()), expireSecond, value);

    }

    private long setIfNotExist(String key, final byte[] bytekey, int expireSecond, byte[] value) {
        try {
            Long ret = cluster.setnx(bytekey, value);
            if (expireSecond > 0) {
                cluster.expire(bytekey, expireSecond);
            }
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1L;
    }

    public long multiSetStringIfNotExist(final Map<String, String> keyValue, int expireSecond) {
        Map<String, byte[]> keyValuebMap = new HashMap<String, byte[]>();
        for (String key : keyValue.keySet()) {
            String value = keyValue.get(key);
            if (value != null) {
                keyValuebMap.put(key, Base64.encodeBase64(value.getBytes()));
            }
        }
        return multiSetIfNotExist(keyValuebMap, expireSecond);
    }

    public long multiSetObjectIfNotExist(Map<String, Object> keyValue, int expireSecond) {
        Map<String, byte[]> keyValuebMap = new HashMap<String, byte[]>();
        for (String key : keyValue.keySet()) {
            Object value = keyValue.get(key);
            if (value != null) {
                keyValuebMap.put(key, serialize(value));
            }
        }
        return multiSetIfNotExist(keyValuebMap, expireSecond);
    }

    public long multiSetByteArrIfNotExist(Map<String, byte[]> keyValue, int expireSecond) {
        Map<String, byte[]> keyValuebMap = new HashMap<String, byte[]>();
        for (String key : keyValue.keySet()) {
            byte[] value = keyValue.get(key);
            if (value != null) {
                keyValuebMap.put(key, value);
            }
        }
        return multiSetIfNotExist(keyValuebMap, expireSecond);
    }

    private long multiSetIfNotExist(Map<String, byte[]> keyValuebMap, int expireSecond) {
        long ret = 0;
        ret += multiSetIfNotExist(expireSecond, getKeyValueBArrArr(keyValuebMap));
        return ret;
    }

    private long multiSetIfNotExist(int expireSecond, final byte[]... keyValues) {
        try {
            long ret = cluster.msetnx(keyValues);
            if (expireSecond > 0) { // it's
                for (int i = 0; i < keyValues.length; i += 2) {
                    cluster.expire(keyValues[i], expireSecond);
                }
            }
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1L;
    }

    public String getString(String key) {
        byte[] ret = get(key, Base64.encodeBase64(key.getBytes()));
        if (ret != null) {
            return Base64.decodeBase64(ret).toString();
        }
        return null;

    }

    public Object getObject(String key) {
        byte[] ret = get(key, Base64.encodeBase64(key.getBytes()));
        if (ret != null) {
            return deserialize(ret);
        }
        return null;
    }

    public Object getObject(final String key, long time) {
        FutureTask<Object> future = new FutureTask<Object>(new Callable<Object>() {
            public Object call() throws Exception {
                return getObject(key);
            }
        });
        executorService.execute(future);
        try {
            return future.get(time, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    public byte[] getByteArr(String key) {
        byte[] ret = get(key, Base64.encodeBase64(key.getBytes()));
        return ret;
    }

    private byte[] get(String key, byte[] bytekey) {
        try {
            byte[] ret = cluster.get(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Map<String, String> multiGetString(List<String> keys) {
        Map<String, String> ret = new HashMap<String, String>();
        Map<String, byte[]> temp = multiGetByteArr(keys);
        for (String key : temp.keySet()) {
            byte[] value = temp.get(key);
            if (value != null) {
                ret.put(key, new String(value));
            } else {
                ret.put(key, null);
            }
        }
        return ret;
    }

    public Map<String, Object> multiGetObject(List<String> keys) {
        Map<String, Object> ret = new HashMap<String, Object>();
        Map<String, byte[]> temp = multiGetByteArr(keys);
        for (String key : temp.keySet()) {
            byte[] value = temp.get(key);
            if (value != null) {
                ret.put(key, deserialize(value));
            } else {
                ret.put(key, null);
            }
        }
        return ret;
    }

    public Map<String, Object> multiGetObject(final List<String> keys, long time) {
        FutureTask<Map<String, Object>> future = new FutureTask<Map<String, Object>>(new Callable<Map<String, Object>>() {
            public Map<String, Object> call() throws Exception {
                return multiGetObject(keys);
            }
        });
        executorService.execute(future);
        try {
            return future.get(time, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("multiGetObject error", e);
        }
        return new HashMap<String, Object>();
    }

    public Map<String, byte[]> multiGetByteArr(List<String> keys) {
        Map<String, byte[]> ret = new HashMap<String, byte[]>();
        try {
            Map<String, byte[]> thisret = multiGet(getBArrArr(keys));
            if (thisret != null) {
                ret.putAll(thisret);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    /**
     * 
     * @param keys
     * @return
     */
    // private Map<String, List<String>> devideKeys(Collection<String> keys) {
    // Map<JedisShardInfo, List<String>> map = new HashMap<JedisShardInfo, List<String>>();
    // Map<String, List<String>> result = new HashMap<String, List<String>>();
    // try {
    // cluster.
    // for (String key : keys) {
    //
    // if (keysList == null) {
    // keysList = new ArrayList<String>();
    // map.put(jedisShardInfo, keysList);
    // }
    // keysList.add(key);
    // }
    // } catch (Exception e) {
    // log.error(e.getMessage(), e);
    // }
    // for (List<String> list : map.values()) {
    // result.put(list.iterator().next(), list);
    // }
    // return result;
    // }

    private Map<String, byte[]> multiGet(byte[]... keys) {
        Map<String, byte[]> map = new HashMap<String, byte[]>();
        try {
            List<byte[]> ret = cluster.mget(keys);
            for (int i = ret.size() - 1; i >= 0; i--) {
                if (ret.get(i) != null)
                    map.put(Base64.encodeBase64String(keys[i]), ret.get(i));
            }
            return map;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return map;
    }


    public Boolean exists(String key) {
        boolean result = exists(key, Base64.encodeBase64(key.getBytes()));
        return result;
    }

    private Boolean exists(String key, byte[] bytekey) {
        try {
            Boolean ret = cluster.exists(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public long delete(String key) {
        return delete(Base64.encodeBase64(key.getBytes()));
    }

    public long deleteWithSharding(String shardingKey, String key) {

        return delete(shardingKey, key, Base64.encodeBase64(key.getBytes()));

    }

    public long multiDelete(List<String> keys) {
        long ret = 0;
        if (keys != null && keys.size() > 0) {
            ret += delete(getBArrArr(keys));
        }
        return ret;
    }

    private long delete(String shardingKey, String key, byte[]... bytekey) {
        try {
            long ret = cluster.del(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    private long delete(byte[]... bytekey) {
        try {
            long ret = cluster.del(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }


    public Long incr(String key) {
        return incrBy(key, 1);
    }

    public Long incrBy(String key, long step) {

        return incrBy(key, Base64.encodeBase64(key.getBytes()), step);

    }

    private Long incrBy(String key, byte[] bytekey, long step) {
        try {
            Long ret = cluster.incrBy(bytekey, step);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    public Long getNumberRecordIncrOrDesr(String key) {
        String ret = getString(key);
        if (ret != null) {
            try {
                long num = Long.parseLong(ret);
                return num;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public Long decr(String key) {
        return decrBy(key, 1);
    }

    public Long decrBy(String key, long step) {

        return decrBy(key, Base64.encodeBase64(key.getBytes()), step);

    }

    private Long decrBy(String key, byte[] bytekey, long step) {
        try {
            Long ret = cluster.decrBy(bytekey, step);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private Long append(String key, byte[] bytekey, byte[] appendBytes) {
        try {
            Long ret = cluster.append(bytekey, appendBytes);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    public String getSetString(String key, int expireSecond, String value) {
        valueTypeAssert(value);

        byte[] ret = getSet(key, Base64.encodeBase64(key.getBytes()), expireSecond, Base64.encodeBase64(value.getBytes()));
        if (ret != null) {
            return new String(Base64.decodeBase64(ret));
        }
        return null;
    }


    public Object getSetObject(String key, int expireSecond, Object value) {
        valueTypeAssert(value);

        byte[] ret = getSet(key, Base64.encodeBase64(key.getBytes()), expireSecond, serialize(value));
        if (ret != null) {
            return deserialize(ret);
        }

        return null;
    }

    public byte[] getSetByteArr(String key, int expireSecond, byte[] value) {
        valueTypeAssert(value);
        return getSet(key, Base64.encodeBase64(key.getBytes()), expireSecond, value);

    }

    private byte[] getSet(String key, byte[] bytekey, int expireSecond, byte[] value) {
        try {
            byte[] ret = cluster.getSet(bytekey, value);
            if (expireSecond > 0) {
                cluster.expire(bytekey, expireSecond);
            }
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    public long hSetString(String key, String field, String value) {
        valueTypeAssert(value);

        return hSet(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()), Base64.encodeBase64(value.getBytes()));

    }

    public long hSetObject(String key, String field, Object value) {
        valueTypeAssert(value);

        return hSet(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()), serialize(value));

    }

    public long hSetByteArr(String key, String field, byte[] value) {
        valueTypeAssert(value);

        return hSet(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()), value);

    }

    private long hSet(String key, byte[] bytekey, byte[] field, byte[] value) {
        try {
            Long ret = cluster.hset(bytekey, field, value);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1L;
    }


    public String hMultiSetString(String key, Map<String, String> fieldValues) {
        Map<byte[], byte[]> fieldbValuebMap = new HashMap<byte[], byte[]>();
        for (String field : fieldValues.keySet()) {
            String value = fieldValues.get(field);
            if (value != null) {
                fieldbValuebMap.put(Base64.encodeBase64(field.getBytes()), Base64.encodeBase64(value.getBytes()));
            }
        }
        return hMultiSet(key, fieldbValuebMap);
    }


    public String hMultiSetObject(String key, Map<String, Object> fieldValues) {
        Map<byte[], byte[]> fieldbValuebMap = new HashMap<byte[], byte[]>();
        for (String field : fieldValues.keySet()) {
            Object value = fieldValues.get(field);
            if (value != null) {
                fieldbValuebMap.put(Base64.encodeBase64(field.getBytes()), serialize(value));
            }
        }
        return hMultiSet(key, fieldbValuebMap);
    }

    public String hMultiSetByteArr(String key, Map<String, byte[]> fieldValues) {
        Map<byte[], byte[]> fieldbValuebMap = new HashMap<byte[], byte[]>();
        for (String field : fieldValues.keySet()) {
            byte[] value = fieldValues.get(field);
            if (value != null) {
                fieldbValuebMap.put(Base64.encodeBase64(field.getBytes()), value);
            }
        }
        return hMultiSet(key, fieldbValuebMap);
    }

    private String hMultiSet(String key, Map<byte[], byte[]> fieldbValuebMap) {
        return hMultiSet(key, Base64.encodeBase64(key.getBytes()), fieldbValuebMap);
    }

    private String hMultiSet(String key, byte[] bytekey, Map<byte[], byte[]> hash) {
        try {
            String ret = cluster.hmset(bytekey, hash);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "failed";
    }


    public String hGetString(String key, String field) {
        byte[] ret = hGetByteArr(key, field);
        if (ret != null) {
            return new String(Base64.decodeBase64(ret));
        }
        return null;
    }


    public Object hGetObject(String key, String field) {
        byte[] ret = hGetByteArr(key, field);
        if (ret != null) {
            return deserialize(ret);
        }
        return null;
    }

    public byte[] hGetByteArr(String key, String field) {
        return hGet(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()));
    }

    private byte[] hGet(String key, byte[] bytekey, byte[] field) {
        try {
            byte[] ret = cluster.hget(bytekey, field);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Map<String, String> hGetAllString(String key) {
        Map<String, byte[]> temp = hGetAllByteArr(key);
        if (temp != null) {
            Map<String, String> ret = new HashMap<String, String>();
            for (String field : temp.keySet()) {
                byte[] value = temp.get(field);
                if (value != null) {
                    ret.put(field, Base64.encodeBase64String(temp.get(field)));
                }
            }
            return ret;
        }
        return null;
    }


    public Map<String, Object> hGetAllObject(String key) {
        Map<String, byte[]> temp = hGetAllByteArr(key);
        if (temp != null) {
            Map<String, Object> ret = new HashMap<String, Object>();
            for (String field : temp.keySet()) {
                byte[] value = temp.get(field);
                if (value != null) {
                    ret.put(field, deserialize(temp.get(field)));
                }
            }
            return ret;
        }
        return null;
    }


    public Map<String, byte[]> hGetAllByteArr(String key) {
        return hGetAll(key, Base64.encodeBase64(key.getBytes()));
    }

    private Map<String, byte[]> hGetAll(String key, byte[] bytekey) {
        Map<String, byte[]> map = new HashMap<String, byte[]>();
        try {
            Map<byte[], byte[]> ret = cluster.hgetAll(bytekey);
            for (Entry<byte[], byte[]> item : ret.entrySet()) {
                map.put(Base64.encodeBase64String(item.getKey()), item.getValue());
            }
            return map;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Map<String, String> hMultiGetString(String key, List<String> fields) {
        Map<String, byte[]> temp = hMultiGetByteArr(key, fields);
        if (temp != null) {
            Map<String, String> ret = new HashMap<String, String>();
            for (String field : temp.keySet()) {
                byte[] value = temp.get(field);
                if (value != null) {
                    ret.put(field, Base64.encodeBase64String((temp.get(field))));
                }
            }
            return ret;
        }
        return null;
    }


    public Map<String, Object> hMultiGetObject(String key, List<String> fields) {
        Map<String, byte[]> temp = hMultiGetByteArr(key, fields);
        if (temp != null) {
            Map<String, Object> ret = new HashMap<String, Object>();
            for (String field : temp.keySet()) {
                byte[] value = temp.get(field);
                if (value != null) {
                    ret.put(field, deserialize(temp.get(field)));
                }
            }
            return ret;
        }
        return null;
    }

    public Map<String, byte[]> hMultiGetByteArr(String key, List<String> fields) {
        return hMultiGet(key, Base64.encodeBase64(key.getBytes()), getBArrArr(fields));
    }

    private Map<String, byte[]> hMultiGet(String key, byte[] bytekey, byte[]... fields) {
        Map<String, byte[]> map = new HashMap<String, byte[]>();
        try {
            List<byte[]> ret = cluster.hmget(bytekey, fields);
            for (int i = ret.size() - 1; i >= 0; i--) {
                map.put(Base64.encodeBase64String(fields[i]), ret.get(i));
            }
            return map;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return map;
    }

    public long hSetStringIfNotExist(String key, String field, String value) {
        return hSetByteArrIfNotExist(key, field, Base64.encodeBase64(value.getBytes()));
    }

    public long hSetObjectIfNotExist(String key, String field, Object value) {
        return hSetByteArrIfNotExist(key, field, serialize(value));
    }


    public long hSetByteArrIfNotExist(String key, String field, byte[] value) {
        valueTypeAssert(value);
        return hSetIfNotExist(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()), value);

    }

    private long hSetIfNotExist(String key, byte[] bytekey, byte[] field, byte[] value) {
        try {
            Long ret = cluster.hsetnx(bytekey, field, value);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1L;
    }

    public long hDelete(String key, String field) {
        return hDelete(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()));
    }

    private long hDelete(String key, byte[] bytekey, byte[] field) {
        try {
            long ret = cluster.hdel(bytekey, field);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1;
    }

    public long hLen(String key) {

        return hLen(key, Base64.encodeBase64(key.getBytes()));

    }

    private long hLen(String key, byte[] bytekey) {
        try {
            long ret = cluster.hlen(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    public Set<String> hKeys(String key) {
        try {
            Set<String> ret = cluster.hkeys(key);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Long hIncrBy(String key, String field, long step) {

        return hIncrBy(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()), step);

    }

    private Long hIncrBy(String key, byte[] bytekey, byte[] field, long step) {
        try {
            Long ret = cluster.hincrBy(bytekey, field, step);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public Boolean hExists(String key, String field) {

        return hExists(key, Base64.encodeBase64(key.getBytes()), Base64.encodeBase64(field.getBytes()));

    }

    private Boolean hExists(String key, byte[] bytekey, byte[] field) {
        try {
            Boolean ret = cluster.hexists(bytekey, field);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public Long lpushString(String key, String item) {
        return lpushByteArr(key, Base64.encodeBase64(item.getBytes()));
    }


    public Long lpushObject(String key, Object item) {
        return lpushByteArr(key, serialize(item));
    }

    public Long lpushByteArr(String key, byte[] item) {
        valueTypeAssert(item);
        return lpush(key, Base64.encodeBase64(key.getBytes()), item);
    }

    private Long lpush(String key, byte[] bytekey, byte[] value) {
        try {
            Long ret = cluster.lpush(bytekey, value);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    public Long rpushString(String key, String item) {
        return rpushByteArr(key, Base64.encodeBase64(item.getBytes()));
    }


    public Long rpushObject(String key, Object item) {
        return rpushByteArr(key, serialize(item));
    }


    public Long rpushByteArr(String key, byte[] item) {
        valueTypeAssert(item);

        return rpush(key, Base64.encodeBase64(key.getBytes()), item);

    }

    private Long rpush(String key, byte[] bytekey, byte[] value) {
        try {
            Long ret = cluster.rpush(bytekey, value);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    public Long lpushStringIfListExist(String key, String item) {
        return lpushByteArrIfListExist(key, Base64.encodeBase64(item.getBytes()));
    }

    public Long lpushObjectIfListExist(String key, Object item) {
        return lpushByteArrIfListExist(key, serialize(item));
    }

    public Long lpushByteArrIfListExist(String key, byte[] item) {
        valueTypeAssert(item);

        return lpushIfListExist(key, Base64.encodeBase64(key.getBytes()), item);

    }

    private Long lpushIfListExist(String key, byte[] bytekey, byte[] value) {
        try {
            Long ret = cluster.lpushx(bytekey, value);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    public Long rpushStringIfListExist(String key, String item) {
        return rpushByteArrIfListExist(key, Base64.encodeBase64(item.getBytes()));
    }

    public Long rpushObjectIfListExist(String key, Object item) {
        return rpushByteArrIfListExist(key, serialize(item));
    }

    public Long rpushByteArrIfListExist(String key, byte[] item) {
        valueTypeAssert(item);
        return rpushIfListExist(key, Base64.encodeBase64(key.getBytes()), item);
    }

    private Long rpushIfListExist(String key, byte[] bytekey, byte[] value) {
        try {
            Long ret = cluster.rpushx(bytekey, value);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    public byte[] lpopByteArr(String key) {
        return lpop(key, Base64.encodeBase64(key.getBytes()));
    }

    private byte[] lpop(String key, byte[] bytekey) {
        try {
            byte[] ret = cluster.lpop(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * ��list�����Ҷ���Object�ķ�ʽ��ȡ����ɾ��һ��item
     *
     * @param key
     * @return
     */

    public Object rpopObject(String key) {
        byte[] ret = rpopByteArr(key);
        if (ret != null) {
            return deserialize(ret);
        }
        return null;
    }

    /**
     * ��list�����Ҷ���byte[]�ķ�ʽ��ȡ����ɾ��һ��item
     *
     * @param key
     * @return
     */

    public byte[] rpopByteArr(String key) {

        return rpop(key, Base64.encodeBase64(key.getBytes()));

    }

    private byte[] rpop(String key, byte[] bytekey) {
        try {
            byte[] ret = cluster.rpop(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        }
        return null;
    }


    /**
     * ��byte[]�ķ�ʽȡ��list��index�������Ҵ�0��ʼ������λ��item
     *
     * @param key
     * @return
     */

    public byte[] lindexByteArr(String key, int index) {

        return lindex(key, Base64.encodeBase64(key.getBytes()), index);

    }

    private byte[] lindex(String key, byte[] bytekey, int index) {
        try {
            byte[] ret = cluster.lindex(bytekey, index);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * ��Object�ķ�ʽȡ��list��ĳλ�������ϵ�items
     *
     * @param key
     * @param start
     * @param end
     * @return
     */

    public List<Object> lrangeObject(String key, int start, int end) {
        List<byte[]> ret = lrangeByteArr(key, start, end);
        if (ret != null) {
            List<Object> trueRet = new ArrayList<Object>();
            for (byte[] item : ret) {
                if (item != null) {
                    trueRet.add(deserialize(item));
                }
            }
            return trueRet;
        }
        return null;
    }

    /**
     * ��byte[]�ķ�ʽȡ��list��ĳλ�������ϵ�items
     *
     * @param key
     * @param start
     * @param end
     * @return
     */

    public List<byte[]> lrangeByteArr(String key, int start, int end) {
        return lrange(key, Base64.encodeBase64(key.getBytes()), start, end);
    }



    private List<byte[]> lrange(String key, byte[] bytekey, int start, int end) {
        try {
            List<byte[]> ret = cluster.lrange(bytekey, start, end);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @Title: lremString
     * @Description: 删除元素
     * @param key
     * @param count
     * count 的值可以是以下几种：
     * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
     * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
     * count = 0 : 移除表中所有与 value 相等的值。
     * @param value
     * @return
     */
    public Long lremString(String key, int count, String value) {
        return lrem(key, Base64.encodeBase64(key.getBytes()), count, Base64.encodeBase64(value.getBytes()));

    }

    private Long lrem(String key, byte[] bytekey, int count, byte[] val) {
        try {
            Long ret = cluster.lrem(bytekey, count, val);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /******************************** above:list lindex,lrange ***************************************/

    /******************************* below:list lset,ltrim ***************************************/
    /**
     * ����list��indexλ���ϵ�ֵ��String���ͣ�
     *
     * @param key
     * @param index
     * @param value
     * @return ���index����list���ȷ���failed;�������óɹ�����OK
     */
    public String lsetString(String key, int index, String value) {
        return lsetByteArr(key, index, Base64.encodeBase64(value.getBytes()));
    }

    public String lsetObject(String key, int index, Object value) {
        return lsetByteArr(key, index, serialize(value));
    }

    public String lsetByteArr(String key, int index, byte[] value) {
        valueTypeAssert(value);
        return lset(key, Base64.encodeBase64(key.getBytes()), index, value);
    }

    private String lset(String key, byte[] bytekey, int index, byte[] value) {
        try {
            String ret = cluster.lset(bytekey, index, value);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "failed";
    }

    /**
     * ��list�ڷ���˽��н�ȡ����Χ֮�ⲿ�ֽ�����������ö���
     *
     * @param key
     * @param start
     * @param end
     * @return ��ȡ�ɹ�����OK�����ָ����Χ����list��ʵ�ʷ�Χ������failed
     */

    public String ltrim(String key, int start, int end) {
        return ltrim(key, Base64.encodeBase64(key.getBytes()), start, end);
    }

    private String ltrim(String key, byte[] bytekey, int start, int end) {
        try {
            String ret = cluster.ltrim(bytekey, start, end);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "failed";
    }

    /******************************** above:list lset,ltrim ***************************************/

    /******************************* below:list len ***************************************/
    /**
     * ��ȡĳlist�ĳ���
     *
     * @param key
     * @return
     */

    public Long llen(String key) {
        return llen(key, Base64.encodeBase64(key.getBytes()));
    }

    private Long llen(String key, byte[] bytekey) {
        try {
            Long ret = cluster.llen(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    /******************************** above:list len ***************************************/

    /******************************* below:set sadd,srem,spop,smember **********************/
    /**
     * �������в���member
     *
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if the element was already a member of the
     *         set
     */

    public Long saddString(String key, String member) {
        return saddByteArr(key, Base64.encodeBase64(member.getBytes()));
    }

    /**
     * �������в���member(���Ӷ����Ƿ���Ӧ�ڴ˷����д�������֤)
     *
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if the element was already a member of the
     *         set
     */

    public Long saddObject(String key, Object member) {
        return saddByteArr(key, serialize(member));
    }

    /**
     * �������в���member
     *
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if the element was already a member of the
     *         set
     */

    public Long saddByteArr(String key, byte[] member) {
        valueTypeAssert(member);
        return sadd(key, Base64.encodeBase64(key.getBytes()), member);
    }

    private Long sadd(String key, byte[] bytekey, byte[] member) {
        try {
            Long ret = cluster.sadd(bytekey, member);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    /**
     * ɾ�������е�member��Ա
     *
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0 if the new element was not a member of
     *         the set
     */

    public Long sremString(String key, String member) {
        return sremByteArr(key, Base64.encodeBase64(member.getBytes()));
    }

    /**
     * ɾ�������е�member��Ա�����Ӷ����Ƿ��ʺϴ˷����д����ԣ�
     *
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0 if the new element was not a member of
     *         the set
     */

    public Long sremObject(String key, Object member) {
        return sremByteArr(key, serialize(member));
    }

    /**
     * ɾ�������е�member��Ա
     *
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0 if the new element was not a member of
     *         the set
     */

    public Long sremByteArr(String key, byte[] member) {
        valueTypeAssert(member);
        return srem(key, Base64.encodeBase64(key.getBytes()), member);

    }

    private Long srem(String key, byte[] bytekey, byte[] member) {
        try {
            Long ret = cluster.srem(bytekey, member);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }


    /**
     * Remove and return a random member from a set
     *
     * @param key
     * @return
     */

    public byte[] spopByteArr(String key) {

        return spop(key, Base64.encodeBase64(key.getBytes()));

    }

    private byte[] spop(String key, byte[] bytekey) {
        try {
            byte[] ret = cluster.spop(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get all the members in a set
     *
     * @param key
     * @return
     */

    public Set<Object> smemberObject(String key) {
        Set<byte[]> ret = smemberByteArr(key);
        if (ret != null) {
            Set<Object> trueRet = new HashSet<Object>();
            for (byte[] member : ret) {
                if (member != null) {
                    trueRet.add(deserialize(member));
                }
            }
            return trueRet;
        }
        return null;
    }

    /**
     * Get all the members in a set
     *
     * @param key
     * @return
     */

    public Set<byte[]> smemberByteArr(String key) {
        return smember(key, Base64.encodeBase64(key.getBytes()));
    }

    private Set<byte[]> smember(String key, byte[] bytekey) {
        try {
            Set<byte[]> ret = cluster.smembers(bytekey);
            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /****************************** above:sorted set zrangeByScore��zrevrangeByScore *************************/

    /****************************** below:sorted set *************************/
    /****************************** above:sorted set *************************/

    /****************************** below:sorted set *************************/
    /****************************** above:sorted set *************************/

    /****************************** below:sorted set *************************/
    /****************************** above:sorted set *************************/

    /**
     * �Զ���������л�
     *
     * @param o ����ʵ��Serializable�ӿ�
     * @return
     */

    public byte[] serialize(Object o) {
        // return HessianSerializeUtil.encode(o);
        return JedisXSerializeUtil.encode(o);
    }

    public byte[][] serialize(Object... os) {
        byte[][] result = new byte[os.length][];
        for (int i = 0; i < os.length; i++) {
            result[i] = JedisXSerializeUtil.encode(os[i]);
        }
        return result;
    }

    /**
     * ��serialize(Object o)�ӿ����ʹ�ã����з����л�
     *
     * @param bytes
     * @return
     */

    public Object deserialize(byte[] bytes) {
        // return HessianSerializeUtil.decode(bytes);
        return JedisXSerializeUtil.decode(bytes);
    }

    private byte[][] getBArrArr(List<String> thisKeys) {
        byte[][] bkeys = new byte[thisKeys.size()][];
        for (int i = thisKeys.size() - 1; i >= 0; i--) {
            bkeys[i] = Base64.encodeBase64(thisKeys.get(i).getBytes());
        }
        return bkeys;
    }

    private byte[][] getKeyValueBArrArr(Map<String, byte[]> keyValueMap) {
        byte[][] bKeyValues = new byte[keyValueMap.size() * 2][];
        int i = 0;
        for (Entry<String, byte[]> entry : keyValueMap.entrySet()) {
            String key = entry.getKey();
            bKeyValues[i + i] = Base64.encodeBase64(key.getBytes());
            bKeyValues[i + i + 1] = keyValueMap.get(key);
            i++;
        }
        return bKeyValues;
    }

    private void valueTypeAssert(Object value) {
        if (value == null) {
            throw new JedisXValueNotSupportException("nut support the Object-type of Null");
        }

    }


}
