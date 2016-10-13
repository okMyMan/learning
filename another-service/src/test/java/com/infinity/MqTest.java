package com.infinity;

import com.utils.mq.bean.ResponseMessage;
import com.utils.mq.impl.active.ActiveMQClientHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MqTest
 *
 * @author Alvin Xu
 * @date 2016/10/13
 * @description
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:beans.xml"})
public class MqTest {

    @Autowired
    private ActiveMQClientHandler testMQHandler;

    @Test
    public void testMQ(){
        try {
            ResponseMessage message = testMQHandler.request("ddd", TimeUnit.SECONDS.toMillis(30));
            if (null == message || null == message.getContent()) {
                System.out.println();
            }
            String isAutoTicketForbid = (String) message.getContent();
            System.out.println();

        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
