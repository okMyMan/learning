package com.infinity.service;

import com.infinity.redis.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

/**
 * RedisTest
 *
 * @author Alvin Xu
 * @date 2016/9/19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:beans.xml"})
public class RedisTest {

    @Test
    /**
     * 最简单的调用形式
     */
    public void testSimple() {

        Jedis jedis = new Jedis("192.168.118.4", 6379);
        jedis.auth("123");
        String keys = "name";
        // 删数据
        jedis.del(keys);
        // 存数据
        jedis.set(keys, "snowolf");
        // 取数据
        String value = jedis.get(keys);
        System.out.println(value);


//        String saveResult = RedisUtil.getJedisX().setString("123", 60000 * 2, "eertert");
//        String result = RedisUtil.getJedisX().getString("");
        System.currentTimeMillis();
    }

    @Test
    /**
     * 使用pool,在beans.xml配置池
     */
    public void testPool() {
        // 从池中获取一个Jedis对象
        Jedis jedis = RedisUtil.jedisPool.getResource();
        String keys = "name";

// 删数据
        jedis.del(keys);
// 存数据
        jedis.set(keys, "snowolf");
// 取数据
        String value = jedis.get(keys);

        System.out.println(value);

// 释放对象池
//        RedisUtil.jedisPool.returnResource(jedis);
        jedis.close();
    }
}
