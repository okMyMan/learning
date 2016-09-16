package com.infinity.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * AuthCheckTest
 *
 * @author Alvin Xu
 * @date 2016/9/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:beans.xml"})
public class AuthCheckTest {
    @Autowired
    AuthCheckService authCheckService;

    @Test
    public void testRebuildToken() {
        authCheckService.generateToken("123456");
    }

}
