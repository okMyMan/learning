package com.infinity;

import com.infinity.dubbo.DemoService;
import com.infinity.dubbo.HiGirl;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * DubboTest
 *
 * @author Alvin Xu
 * @date 2016/9/20
 */
public class DubboTest {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "remote-servlet.xml" });
        context.start();

        DemoService demoService = (DemoService) context.getBean("demoService"); //
        String hello = demoService.sayHello("tom"); // ִ
        System.out.println(hello); //


        HiGirl hiGirl = (HiGirl) context.getBean("hiService");
        String hi = hiGirl.hiGirl("yuki");
        System.out.println(hi); //ll

        //
        // System.out.println(demoService.hehe());
        System.in.read();
    }
}
