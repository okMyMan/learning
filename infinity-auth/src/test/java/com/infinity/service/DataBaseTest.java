package com.infinity.service;

import com.cassandra.keyspace.SimpleSpace;
import com.infinity.dao.TestDao;
import com.infinity.entity.FundBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * DataBaseTest
 *
 * @author Alvin Xu
 * @date 2016/9/12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:beans.xml"})
public class DataBaseTest {
    @Autowired
    private TestDao testDao;

    @Test
    public void test() {
        List<FundBase> results = testDao.getAllFundBase();
        System.out.println();
    }

    @Test
    public void testKeyValue() {
//        SimpleSpace.setStr("xule", "12");
        String str = SimpleSpace.getStr("xule");
        System.currentTimeMillis();
    }
}
