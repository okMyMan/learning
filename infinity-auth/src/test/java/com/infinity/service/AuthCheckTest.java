package com.infinity.service;

import com.infinity.MyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
        AuthCheckService bean = context.getBean(AuthCheckService.class);
        bean.generateToken("123456");

        authCheckService.generateToken("123456");
    }

}
