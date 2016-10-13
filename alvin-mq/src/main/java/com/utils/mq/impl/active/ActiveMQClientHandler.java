package com.utils.mq.impl.active;

import com.alibaba.fastjson.JSON;
import com.infinity.codec.SerializeUtils;
import com.utils.mq.bean.MessageServiceConfig;
import com.utils.mq.bean.RequestMessage;
import com.utils.mq.bean.ResponseMessage;
import com.utils.mq.impl.AbstractMQClientHandler;
import com.utils.mq.tool.CommonUtils;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ActiveMQClientHandler
 *
 * @author Alvin Xu
 * @date 2016/10/9
 */
public class ActiveMQClientHandler extends AbstractMQClientHandler {
    public static Logger logger = LoggerFactory.getLogger(ActiveMQClientHandler.class);

    private final BlockingDeque<SendWrapper> requestSenderWrapperDeque = new LinkedBlockingDeque<>();
    //    private final Session requestSession;
    //    private final MessageConsumer responseReceiver;
    //    private final Session responseSession;
    private final BlockingDeque<SendWrapper> loggerSender = new LinkedBlockingDeque<>();
    //    private final Session loggerSession;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final BlockingQueue<ResponseMessage> responseMessageDeque = new LinkedBlockingDeque<>();
    private final Session responseSession;
    private final Destination responseDestination;
    private BlockingQueue<RequestMessage> logMessages = new LinkedBlockingQueue<>();
    private final Connection connection;
    private String outAddress = null;
    private String inAddress = null;

    private final AtomicInteger number = new AtomicInteger();
    private final AtomicInteger senderWrapperCount = new AtomicInteger();
    private final AtomicInteger receiverNumber = new AtomicInteger();
    private final AtomicInteger quickRecvCount = new AtomicInteger(0);

    private ExecutorService executorService = Executors.newFixedThreadPool(50);

    /**
     * @param config 消息队列配置
     */
    public ActiveMQClientHandler(MessageServiceConfig config) throws JMSException {
        super(config);
        logger.error("Mq client init start" + config.getServiceType());
        for (String s : CommonUtils.getAddresses()) {
            if (s.contains(":")) {
                // IPV6地址，暂时屏蔽
                continue;
            }
            if (s.startsWith("10.") || s.startsWith("192.")) {
                inAddress = s;
            } else {
                outAddress = s;
            }
        }
        if (null == outAddress) {
            outAddress = inAddress;
        }
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("ooteco", "ooteco", config.getMqUrl());
        factory.getPrefetchPolicy().setQueuePrefetch(config.getPrefetchSize());
        factory.setSendTimeout(10000);
        factory.setUseCompression(true);
        connection = factory.createConnection();
        ((ActiveMQConnection) connection).setUseAsyncSend(true);
        connection.start();

        // 业务回应接受队列
        if (config.isResponseFlag()) {
            responseSession = connection.createSession(Boolean.FALSE, Session.DUPS_OK_ACKNOWLEDGE);
            responseDestination = responseSession.createQueue(
                    RESPONSE_TOPIC_PREFIX + config.getServiceType() + config.getSuffix() + config.getServiceName());

            // 起一个线程接收consumer端返回的响应,此时自己也是consumer角色了
            receiveResponse();

            // 起一个线程处理接收到的response
            handleResponse();
            //            }
        } else {
            responseSession = null;
            responseDestination = null;
        }

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                // 关闭闲置session
                                shutdownWrapper(requestSenderWrapperDeque);
                                Thread.sleep(1000);
                            } catch (Throwable t) {
                                logger.error("Error when shutdown idle session.", t);
                            }
                        }
                    }
                }
        ).start();

        logger.error("Mq client init complete" + config.getServiceType());
    }

    @Override
    protected void sendMessage(RequestMessage requestMessage, boolean waitingSend) {
        BytesMessage bytesMessage = new ActiveMQBytesMessage();
        //        MessageProducer senderSingle = null;
        SendWrapper wrapper = null;
        try {
            requestMessage.setBornHost(outAddress);
            bytesMessage.writeBytes(SerializeUtils.SerialAndGzipObjInBean(requestMessage));
            wrapper = requestSenderWrapperDeque.pollLast(100, TimeUnit.MILLISECONDS);
            if (null == wrapper) {
                wrapper = createWrapper();
            }
            wrapper.setLastUsed(System.currentTimeMillis());
            wrapper.getSender().send(bytesMessage);


        } catch (Throwable e) {
            ActiveFlowControl.lastTimeOut.set(System.currentTimeMillis());
            ActiveFlowControl.timeOutCount.incrementAndGet();
            throw new RuntimeException("Error when send request message" + JSON.toJSONString(requestMessage), e);
        } finally {
            if (null != wrapper) {
                requestSenderWrapperDeque.offerLast(wrapper);
            }
        }
    }

    private SendWrapper createWrapper() throws JMSException {
        // 业务请求发送队列
        Session requestSession = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
        Destination requestDestination;
        if (getConfig().isBroadcastFlag()) {
            // 广播，订阅模式
            requestDestination = requestSession
                    .createTopic(REQUEST_TOPIC_PREFIX + getConfig().getServiceType() + getConfig().getSuffix());
        } else {
            // 点对点，队列模式
            requestDestination = requestSession
                    .createQueue(REQUEST_TOPIC_PREFIX + getConfig().getServiceType() + getConfig().getSuffix());
        }
        MessageProducer producer = requestSession.createProducer(requestDestination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        SendWrapper wrapper = new SendWrapper();
        wrapper.setConnection(connection);
        wrapper.setSession(requestSession);
        wrapper.setSender(producer);
        wrapper.setNumber(number.incrementAndGet());
        senderWrapperCount.incrementAndGet();
        logger.warn(
                "Create sender " + getConfig().getServiceType() + "[" + wrapper.getNumber() + "],current count is [" + senderWrapperCount
                        .get() + "]");
        return wrapper;
    }

    /**
     * 把responseMessage放到responseMessageDeque队列
     *
     * @throws JMSException
     */
    private void receiveResponse() throws JMSException {
        // 通过多个consumer来加快消费速度
        logger.error("Create receiver " + getConfig().getServiceType() + "[" + receiverNumber.incrementAndGet() + "]");
        final MessageConsumer responseConsumer = responseSession.createConsumer(responseDestination);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        while (true) {
                            try {
                                long cur = System.currentTimeMillis();
                                final Message message = responseConsumer.receive();
                                if (System.currentTimeMillis() - cur < 500) {
                                    quickRecvCount.incrementAndGet();
                                } else {
                                    quickRecvCount.set(0);
                                }
                                if (message instanceof BytesMessage) {
                                    executorService.execute(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        try {
                                                            byte[] bytes = CommonUtils.readAllData((BytesMessage) message);
                                                            ResponseMessage responseMessage =
                                                                    SerializeUtils.unGZIPAndSerialObjInBean(bytes, ResponseMessage.class);
                                                            logger.error("收到回应" + responseMessage.getRequestMessageId() + " cost " + (
                                                                    new Date().getTime() - responseMessage.getForegroundTimestamp()
                                                                            .getTime()));
                                                            responseMessageDeque.offer(responseMessage);
                                                        } catch (Throwable t) {
                                                            logger.error("Error when receive message for" + JSON
                                                                    .toJSONString(getConfig().getServiceType()), t);
                                                        }
                                                    } catch (Throwable t) {
                                                        logger.error("Error when receive response message.", t);
                                                    }
                                                }
                                            }

                                    );
                                } else if (null != message) {
                                    logger.error("Error when receive message for" + JSON.toJSONString(getConfig().getServiceType())
                                            + ",target message type is " + message.getClass());
                                }
                                if (quickRecvCount.get() > receiverNumber.get()) {
                                    // 连续N次消费都能直接获取到消息，创建新receiver
                                    quickRecvCount.set(0);
                                    receiveResponse();
                                }
                            } catch (Throwable t) {
                                logger.error("Error when receive response message.", t);
                            }
                        }
                    }
                }


        ).start();
    }


    private void shutdownWrapper(BlockingDeque<SendWrapper> wrappers) throws JMSException {
        while (true) {
            SendWrapper poll = wrappers.pollFirst();
            if (null != poll) {
                if (System.currentTimeMillis() - poll.getLastUsed() > ActiveFlowControl.idleTime) {
                    poll.getSender().close();
                    poll.getSession().close();
                    senderWrapperCount.decrementAndGet();
                    logger.warn(
                            "Close sender " + getConfig().getServiceType() + "[" + poll.getNumber() + "],current count is ["
                                    + senderWrapperCount.get() + "]");
                    continue;
                } else {
                    wrappers.offerLast(poll);
                }
            }
            break;
        }
    }

    @Override
    protected ResponseMessage pollResponseMessage(long timeout) throws InterruptedException {
        return responseMessageDeque.poll(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
