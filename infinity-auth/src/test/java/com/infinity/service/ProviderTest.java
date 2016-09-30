package com.infinity.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * ProviderTest
 *
 * @author Alvin Xu
 * @date 2016/9/21
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:beans.xml"})
public class ProviderTest {

    @Test
    public void test111() throws IOException {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"remote-servlet.xml"});
        context.start();

        System.out.println("新浪微博：疯狂的杨中仁.");

        System.in.read(); // 按任意键退出
    }
}
