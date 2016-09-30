package com.infinity.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 消息的生产者（发送者）
 *
 * @author Alvin Xu
 * @date 2016/9/27
 */
public class JMSProducer {

    //默认连接用户名
    private static final String USERNAME = ActiveMQConnection.DEFAULT_USER;
    //默认连接密码
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    //默认连接地址
//    private static final String BROKEURL = ActiveMQConnection.DEFAULT_BROKER_URL;
    private static final String BROKEURL = "tcp://192.168.118.4:61616";
    //发送的消息数量
    private static final int SENDNUM = 10;

    public static void main(String[] args) {
        Session session = null;
        try {
            //创建session
            session = createSession();
            //发送消息
            sendP2PMessage(session);

            session.commit();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * P2P示例 发送消息
     *
     * @param session
     * @throws Exception
     */
    public static void sendP2PMessage(Session session) throws Exception {

        //创建一个名称为 HelloWorld_Queue 的消息队列
        Destination destination = session.createQueue("HelloWorld_Queue");
        //创建消息生产者
        MessageProducer messageProducer = session.createProducer(destination);
        for (int i = 0; i < JMSProducer.SENDNUM; i++) {
            //创建一条文本消息
            TextMessage message = session.createTextMessage("ActiveMQ 发送消息" + i + i);

            // 定制属性
            message.setStringProperty("company","apple");
            message.setDoubleProperty("price", 1200);

            System.out.println("发送消息：Activemq 发送消息" + i);
            //通过消息生产者发出消息
            messageProducer.send(message);
        }

    }

    /**
     * Topic示例 发送消息
     *
     * @param session
     */
    public static void sendTopicMessage(Session session) throws JMSException {
        // 创建一个topic为 HelloWorld_Topic 的发布者/订阅者
        Destination destination = session.createTopic("HelloWorld_Topic");

        // 创建消息生产者
        MessageProducer producer = session.createProducer(destination);

        for (int i = 0; i < JMSProducer.SENDNUM; i++) {
            TextMessage message = session.createTextMessage("ActiveMQ 发送topic消息" + i);
            System.out.println("发送topic消息：Activemq 发送消息" + i);
            //通过消息生产者发出消息
            producer.send(message);
        }
    }

    public static Session createSession() throws JMSException {
        //连接工厂
        ConnectionFactory connectionFactory;
        //连接
        Connection connection = null;
        //会话 接受或者发送消息的线程
        Session session;
        //消息的目的地
        Destination destination;
        //消息生产者
        MessageProducer messageProducer;
        //实例化连接工厂
        connectionFactory = new ActiveMQConnectionFactory(JMSProducer.USERNAME, JMSProducer.PASSWORD, JMSProducer.BROKEURL);

        //通过连接工厂获取连接
        connection = connectionFactory.createConnection();
        //启动连接
        connection.start();
        //创建session
        session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        return session;
    }

}