package com.utils.mq.impl.active;

import com.alibaba.fastjson.JSON;
import com.infinity.codec.SerializeUtils;
import com.utils.mq.IServiceHandler;
import com.utils.mq.bean.MessageServiceConfig;
import com.utils.mq.bean.RequestMessage;
import com.utils.mq.bean.ResponseMessage;
import com.utils.mq.impl.AbstractMQServerHandler;
import com.utils.mq.tool.CommonUtils;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ActiveMQServerHandler
 *
 * @author Alvin Xu
 * @date 2016/10/10
 */
public class ActiveMQServerHandler extends AbstractMQServerHandler {
    public static Logger logger = LoggerFactory.getLogger(ActiveMQServerHandler.class);
    private final Connection connection;
    private final ConcurrentHashMap<String, BlockingDeque<SendWrapper>> responseWrapper = new ConcurrentHashMap<>();
    //    private final MessageConsumer requestReceiver;
    private final BlockingDeque<SendWrapper> loggerSender = new LinkedBlockingDeque<>();
    private final Lock lock = new ReentrantLock();
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final AtomicLong lastTimeOut = new AtomicLong(0);
    private final AtomicInteger timeOutCount = new AtomicInteger(0);
    private final Session requestSession;
    private final Destination requestDestination;
    private String outAddress = null;
    // 负载均衡
    private final BlockingQueue<RequestMessage> requestMessageQueue = new LinkedBlockingDeque<>();
    private BlockingQueue<RequestMessage> logMessages = new LinkedBlockingQueue<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final AtomicInteger number = new AtomicInteger();
    private final AtomicInteger count = new AtomicInteger();
    private final AtomicInteger receiverNumber = new AtomicInteger();
    private final AtomicInteger receiverCount = new AtomicInteger();
    // 加锁
    private final Lock recvLock = new ReentrantLock();
    private final AtomicInteger quickRecvCount = new AtomicInteger(0);

    /**
     * @param config 消息队列配置
     */
    public ActiveMQServerHandler(MessageServiceConfig config, IServiceHandler handlers) throws JMSException {
        super(config, handlers);
        logger.error("Mq server init start" + config.getServiceType());
        String inAddress = null;
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
        //        factory.setUseAsyncSend(true);
        connection = factory.createConnection();
        ((ActiveMQConnection) connection).setUseAsyncSend(true);
        connection.start();


        // 初始化业务请求的接受队列
        requestSession = connection.createSession(Boolean.FALSE, Session.DUPS_OK_ACKNOWLEDGE);
        if (config.isBroadcastFlag()) {
            requestDestination =
                    requestSession.createTopic(REQUEST_TOPIC_PREFIX + config.getServiceType() + config.getSuffix());
        } else {
            requestDestination =
                    requestSession.createQueue(REQUEST_TOPIC_PREFIX + config.getServiceType() + config.getSuffix());
        }

        // 启动一个线程处理收到的request(存于requestMessageQueue)
        handleRequest();
        // 启动线程获取request
        receiveRequest();
        logger.error("Mq server init complete" + config.getServiceType());

    }

    /**
     * @throws JMSException
     */
    protected void receiveRequest() throws JMSException {
        // 通过多个consumer来加快消费速度
        logger.error("Create receiver " + getConfig().getServiceType() + "[" + receiverNumber.incrementAndGet()
                + "],current count is [" + receiverCount.incrementAndGet() + "]");
        final MessageConsumer requestReceiver = requestSession.createConsumer(requestDestination);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            boolean needLock = false;
                            try {
                                AtomicLong useLastTimeOut = null;
                                AtomicInteger useTimeOutCount = null;
                                if (getConfig().isFlowControlFlag()) {
                                    useLastTimeOut = ActiveFlowControl.lastTimeOut;
                                    useTimeOutCount = ActiveFlowControl.timeOutCount;
                                } else {
                                    useLastTimeOut = lastTimeOut;
                                    useTimeOutCount = timeOutCount;

//                                    useLastTimeOut = this.lastTimeOut;
//                                    useTimeOutCount = this.timeOutCount;
                                }
                                needLock = useLastTimeOut.get() > (System.currentTimeMillis() - ActiveFlowControl.watchTime);
                                if (needLock) {
                                    recvLock.lock();
                                }
                                if (useLastTimeOut.get() > (System.currentTimeMillis() - ActiveFlowControl.sleepTime)) {
                                    // 出现发送失败则进入极限限流模式，暂停半分钟后拉取请求
                                    logger.error(
                                            getConfig().getServiceType() + " Waiting for timeout, watch [" + ActiveFlowControl.sleepTime
                                                    + "ms]");
                                    Thread.sleep(ActiveFlowControl.sleepTime);
                                } else if (useLastTimeOut.get() > (System.currentTimeMillis() - ActiveFlowControl.watchTime)) {
                                    // 一分钟观察期，根据失败次数动态调整拉取请求次数
                                    int waitingTime = 1000 * Math.min(Math.max(useTimeOutCount.get(), 1), 10);
                                    logger.error(
                                            getConfig().getServiceType() + " Waiting for timeout count, watch [" + waitingTime + "ms]");
                                    Thread.sleep(waitingTime);
                                } else if (useTimeOutCount.get() > 0) {
                                    // 恢复正常
                                    logger.error(getConfig().getServiceType() + " Timeout count clean.");
                                    useTimeOutCount.set(0);
                                }
                                long cur = System.currentTimeMillis();
                                final Message message = requestReceiver.receive();
                                if (System.currentTimeMillis() - cur < 500) {
                                    quickRecvCount.incrementAndGet();
                                } else {
                                    quickRecvCount.set(0);
                                }

                                if (message instanceof BytesMessage) {
                                    executorService.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                byte[] body = CommonUtils.readAllData((BytesMessage) message);
                                                //  反序列化并加入待处理队列
                                                RequestMessage requestMessage =
                                                        SerializeUtils.unGZIPAndSerialObjInBean(body, RequestMessage.class);
                                                //                                logger.error("Receive response message" + requestMessage.getMessageId() + " cost" + (
                                                //                                        new Date().getTime() - requestMessage.getForegroundTimestamp().getTime()));
                                                //                                requestMessage.setBornHost(message.get);
                                                boolean reject = false;
                                                int curSize = requestMessageQueue.size();
                                                try {
                                                    // reject策略不影响业务逻辑，抛出异常视作不拒绝
                                                    reject = getHandler().reject(requestMessage, curSize);
                                                } catch (Throwable t) {
                                                    logger.error("Error when run reject judgement,ignore it!", t);
                                                }
                                                if (!reject) {
                                                    // 通过了reject判断，不需要拒绝
                                                    // 负载均衡限流，不积存任务
                                                    requestMessageQueue.offer(requestMessage);
                                                } else {
                                                    try {
                                                        Object rejectResponse = getHandler().rejectResponse(requestMessage, curSize);
                                                        if (null != rejectResponse) {
                                                            ResponseMessage<Object> responseMessage =
                                                                    CommonUtils.buildResponse(rejectResponse, null, requestMessage);
                                                            responseMessage.setBackgroundName(getConfig().getServiceName());
                                                            sendMessage(responseMessage);
                                                        }
                                                    } catch (Throwable t) {
                                                        logger.error("Error when send reject response!", t);
                                                    } finally {
                                                        logger.error(
                                                                "Message " + JSON.toJSONString(requestMessage) + " has been rejected!");
                                                    }
                                                }
                                            } catch (Throwable e) {
                                                logger.error(getConfig().getServiceType() + getConfig().getSuffix()
                                                        + " Error when receive response :", e);
                                            }
                                        }
                                    });
                                } else if (null != message) {
                                    logger.error("Error when receive message for" + JSON.toJSONString(getConfig().getServiceType())
                                            + ",target message type is " + message.getClass());
                                }
                                if (quickRecvCount.get() > receiverNumber.get()) {
                                    // 连续N次消费都能直接获取到消息，创建新receiver
                                    quickRecvCount.set(0);
                                    receiveRequest();
                                }
                            } catch (Throwable t) {
                                logger.error("Error when receive response message.", t);
                            } finally {
                                if (needLock) {
                                    recvLock.unlock();
                                }
                            }
                        }

                    }
                }).start();
    }



    @Override
    protected void sendMessage(ResponseMessage responseMessage) {
        BytesMessage bytesMessage = new ActiveMQBytesMessage();
        try {
            responseMessage.setBornHost(outAddress);
            bytesMessage.writeBytes(SerializeUtils.SerialAndGzipObjInBean(responseMessage));
            //                        wrapper.responseSender.send(bytesMessage, DeliveryMode.NON_PERSISTENT, 4, responseMessage.getOverTime());
            //            logger.error("Send response message" + responseMessage.getRequestMessageId() + " cost" + (new Date().getTime()
            //                    - responseMessage.getForegroundTimestamp().getTime()));
            send(responseMessage.getForegroundName(), bytesMessage);
            //            logger.error("Send response message complete" + responseMessage.getRequestMessageId() + " cost" + (
            //                    new Date().getTime() - responseMessage.getForegroundTimestamp().getTime()));
        } catch (Throwable e) {
            lastTimeOut.set(System.currentTimeMillis());
            timeOutCount.incrementAndGet();
            ActiveFlowControl.lastTimeOut.set(System.currentTimeMillis());
            ActiveFlowControl.timeOutCount.incrementAndGet();
            logger.error("Send timeout,increment the timeout count to [" + timeOutCount + "]");
            throw new RuntimeException("Error when send response message" + JSON.toJSONString(responseMessage), e);
        }
    }


    private void send(String serviceName, BytesMessage bytesMessage) throws JMSException, InterruptedException {
        BlockingDeque<SendWrapper> wrappers = responseWrapper.get(serviceName);
        if (null == wrappers) {
            lock.lock();
            try {
                wrappers = responseWrapper.get(serviceName);
                if (null == wrappers) {
                    wrappers = new LinkedBlockingDeque<>();
                    responseWrapper.put(serviceName, wrappers);
                    wrappers.offer(createWrapper(serviceName));
                    final BlockingDeque<SendWrapper> finalWrappers = wrappers;
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    while (true) {
                                        try {
                                            // 关闭闲置session
                                            shutdownWrapper(finalWrappers);
                                            Thread.sleep(1000);
                                        } catch (Throwable t) {
                                            logger.error("Error when shutdown idle session.", t);
                                        }
                                    }
                                }
                            }).start();
                }
            } finally {
                lock.unlock();
            }
        }
        // 负载均衡
        SendWrapper poll = null;
        try {
            poll = wrappers.pollLast(100, TimeUnit.MILLISECONDS);
            if (null == poll) {
                poll = createWrapper(serviceName);
            }
            poll.setLastUsed(System.currentTimeMillis());
            poll.getSender().send(bytesMessage);
        } finally {
            if (null != poll) {
                wrappers.offerLast(poll);
            }
        }
    }

    private SendWrapper createWrapper(String serviceName) throws JMSException {
        SendWrapper wrapper = new SendWrapper();
        wrapper.setConnection(connection);
        wrapper.setSession(wrapper.getConnection().createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE));
        Destination destination1 = wrapper.getSession()
                .createQueue(RESPONSE_TOPIC_PREFIX + getConfig().getServiceType() + getConfig().getSuffix() + serviceName);
        wrapper.setSender(wrapper.getSession().createProducer(destination1));
        wrapper.getSender().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        wrapper.setNumber(number.incrementAndGet());
        count.incrementAndGet();
        logger.warn(
                "Create sender " + getConfig().getServiceType() + "[" + wrapper.getNumber() + "],current count is [" + count
                        .get() + "]");
        return wrapper;
    }

    private void shutdownWrapper(BlockingDeque<SendWrapper> wrappers) throws JMSException {
        while (true) {
            SendWrapper poll = wrappers.pollFirst();
            if (null != poll) {
                if (System.currentTimeMillis() - poll.getLastUsed() > ActiveFlowControl.idleTime) {
                    poll.getSender().close();
                    poll.getSession().close();
                    count.decrementAndGet();
                    logger.warn(
                            "Close sender " + getConfig().getServiceType() + "[" + poll.getNumber() + "],current count is ["
                                    + count.get() + "]");
                    continue;
                } else {
                    wrappers.offerLast(poll);
                }
            }
            break;
        }
    }

    @Override
    protected RequestMessage pollMessage(long timeout) throws InterruptedException {
        return requestMessageQueue.poll(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

}
