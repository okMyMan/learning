package com.utils.mq.impl;

import com.alibaba.fastjson.JSON;
import com.infinity.thread.NamedThreadFactory;
import com.utils.mq.IMessageQueueClientHandler;
import com.utils.mq.bean.AsyncCaller;
import com.utils.mq.bean.MessageServiceConfig;
import com.utils.mq.bean.RequestMessage;
import com.utils.mq.bean.ResponseMessage;
import com.utils.mq.tool.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.*;

/**
 * @author Konson.zhao
 * @date 2015/12/14 17:23
 * @Description 抽象的RPC消息队列请求处理实现类
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractMQClientHandler implements IMessageQueueClientHandler {
    // 控制器线程数
    public static final int CONTROLLER_NUM = 5;
    // 日志记录
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMQClientHandler.class);
    // 用于存储回调交换队列的Map
    private final ConcurrentHashMap<String, ArrayBlockingQueue<ResponseMessage>> responseMessageMap = new ConcurrentHashMap<>();
    // 用于存储异步调用对象的Map
//    private final ConcurrentHashMap<String, AsyncCaller> callerMap = new ConcurrentHashMap<>();
    // 用于存储超时处理执行Future的Map
    private final ConcurrentHashMap<String, ScheduledFuture> futureMap = new ConcurrentHashMap<>();
    // 用于存储请求消息对象的Map
    private final ConcurrentHashMap<String, RequestMessage> requestMap = new ConcurrentHashMap<>();
    // 用于定期清理Map的调度线程池
    private final ScheduledExecutorService scheduledExecutorService;
    // 用于接收、调度处理回应消息的控制器线程池
    private final ExecutorService controllerService =
            Executors.newFixedThreadPool(CONTROLLER_NUM, new NamedThreadFactory("MQ-Controller"));
    // 消息队列配置
    private final MessageServiceConfig config;
    // 用于运行开关
    private volatile boolean run = true;

    /**
     * @param config 消息队列配置
     */
    public AbstractMQClientHandler(MessageServiceConfig config) {
        this.config = config;
        if (null == config.getServiceName() || config.getServiceName().isEmpty()) {
            config.setServiceName(CommonUtils.uniqueId());
        }
        scheduledExecutorService =
                new ScheduledThreadPoolExecutor(config.getHandleThread(), new NamedThreadFactory("MQ-Schedule"));
    }

    /**
     * 启动消息处理线程,
     * 只是把receiveResponse()方法接收到的responseMessageDeque队列中的responseMessage一个一个取出来,放到responseMessageMap中
     */
    protected void handleResponse() {
        for (int i = 0; i < CONTROLLER_NUM; i++) {
            controllerService.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            while (run) {



                                try {
                                    ResponseMessage responseMessage = pollResponseMessage(10);
                                    if (null != responseMessage && null != responseMessage.getForegroundTimestamp() && responseMessage.getOverTime() > (
                                            System.currentTimeMillis() - responseMessage.getForegroundTimestamp().getTime())) {
                                        try {
                                            ArrayBlockingQueue<ResponseMessage> responseQueue = responseMessageMap.get(responseMessage.getRequestMessageId());
//                                            AsyncCaller caller = callerMdap.get(responseMessage.getRequestMessageId());
                                            if (null != responseQueue) {
                                                // 同步调用，加锁防止在发送完毕之前就开始offer
                                                boolean offer = responseQueue.offer(responseMessage);
                                                if (!offer) {
                                                    LOGGER.error("Consumer waiting for response responseMessage has been canceled.Cost" + (
                                                            System.currentTimeMillis() - responseMessage.getForegroundTimestamp().getTime()));
                                                }
                                            }
//                                            else if (null != caller) {
//                                                if (null != responseMessage.getThrowable()) {
//                                                    scheduledExecutorService.execute(
//                                                            new ExceptionCaller(requestMap.get(responseMessage.getRequestMessageId()),
//                                                                    responseMessage.getThrowable(), caller));
//                                                } else {
//                                                    // 异步成功调用
//                                                    scheduledExecutorService.execute(new SuccessCaller(responseMessage, caller));
//                                                }
//                                            }
                                            else {
                                                LOGGER.error("No consumer waiting for response responseMessage:" + JSON.toJSONString(responseMessage));
                                            }
                                        } catch (Throwable t) {
                                            LOGGER.error("Error when handle response responseMessage:" + JSON.toJSONString(responseMessage), t);
                                        }
                                    } else {
                                        if (null != responseMessage) {
                                            LOGGER.error("Response for " + responseMessage.getServiceType() + " discard due to out of time.");
                                        }
                                    }
                                } catch (Throwable t) {
                                    LOGGER.error("Error when handle response message", t);
                                }


                            }
                        }
                    }
            );
        }
    }


    protected void handleException(RequestMessage requestMessage, Throwable t) {
        // 异步调用
        ArrayBlockingQueue<ResponseMessage> queue = responseMessageMap.get(requestMessage.getMessageId());
//        AsyncCaller caller = callerMap.get(requestMessage.getMessageId());
        if (null != queue) {
            // 同步调用
            boolean offer = queue.offer(CommonUtils.buildResponse(null, t, requestMessage));
            if (!offer) {
                LOGGER.error("Error when handle send message exception:" + JSON.toJSONString(requestMessage), t);
            }
        }
//        else if (null != caller) {
//            // 异步调用
//            scheduledExecutorService.execute(new ExceptionCaller(requestMessage, t, caller));
//        }
    }

    /**
     * 请求消息的发送方法，子类需要根据具体的底层消息队列实现来完成此方法。
     *
     * @param requestMessage 待发送的请求消息
     * @param waitingSend
     */
    protected abstract void sendMessage(RequestMessage requestMessage, boolean waitingSend);

    /**
     * 回应消息的批量获取方法，若底层队列实现不支持带超时时间的批量获取，可通过自实现BlockingQueue缓存的方式实现此逻辑。
     *
     * @param timeout 超时时间
     * @return 在超时时间内可获取到的所有消息
     */
    protected abstract ResponseMessage pollResponseMessage(long timeout) throws InterruptedException;

    /**
     * 根据父请求及其他要素构造请求消息
     *
     * @param <T>           正文对象类型
     * @param content       消息的业务正文对象
     * @param timeout       超时时间
     * @param parentRequest 所属父请求，可为null，子消息的会话ID及前台流水号依赖此对象。
     * @param isUnique
     * @return 构造好的请求消息
     */
    protected <T> RequestMessage<T> buildRequest(T content, long timeout, RequestMessage parentRequest, boolean isUnique) {
        RequestMessage<T> requestMessage = new RequestMessage<>();
        requestMessage.setMessageId(CommonUtils.uniqueId());
        if (null != parentRequest) {
            requestMessage.setSessionId(parentRequest.getSessionId());
            requestMessage.setForegroundSerialId(parentRequest.getBackgroundSerialId());
        } else {
            requestMessage.setSessionId(CommonUtils.uniqueId());
            requestMessage.setForegroundSerialId(CommonUtils.uniqueId());
        }
        requestMessage.setServiceType(config.getServiceType() + config.getSuffix());
        requestMessage.setForegroundName(config.getServiceName());
        requestMessage.setBroadcastFlag(config.isBroadcastFlag());
        requestMessage.setResponseFlag(config.isResponseFlag());
        requestMessage.setOverTime(timeout);
        requestMessage.setForegroundTimestamp(new Date());
        requestMessage.setContent(content);
        requestMessage.setUniqueFlag(isUnique);
        return requestMessage;
    }

    @Override
    public <T> ResponseMessage<?> request(T content, long timeout)throws TimeoutException, InterruptedException {
        return request(content, timeout, null, false);
    }

    @Override
    public <T> ResponseMessage<?> request(T content, long timeout, RequestMessage<?> parentMessage, boolean uniqueFlag)
            throws TimeoutException, InterruptedException {
        RequestMessage<T> requestMessage = buildRequest(content, timeout, parentMessage, uniqueFlag);
        return request(requestMessage);
    }

    @Override
    public <T> ResponseMessage<?> request(RequestMessage<T> requestMessage) throws TimeoutException, InterruptedException {
        // TODO 为什么没看到queue的赋值,如果不复制,null == responseMessage,则后面会抛错呀?
        ArrayBlockingQueue<ResponseMessage> responseMessageQueue = new ArrayBlockingQueue<>(1);
        // 注册回调事件对象
        ArrayBlockingQueue<ResponseMessage> previousValue = responseMessageMap.putIfAbsent(requestMessage.getMessageId(), responseMessageQueue);
        if (null != previousValue) {
            // 重复发送
            throw new RuntimeException("Can not send same request message, Message id [" + requestMessage + "] exists!");
        }
        try {
            sendMessage(requestMessage, requestMessage.isResponseFlag());
            if (!requestMessage.isResponseFlag()) {
                return buildEmptyResponse(requestMessage);
            }
            ResponseMessage responseMessage = responseMessageQueue.poll(
                    requestMessage.getOverTime() - (System.currentTimeMillis() - requestMessage.getForegroundTimestamp()
                            .getTime()), TimeUnit.MILLISECONDS);
            if (null == responseMessage) {
                throw new TimeoutException(requestMessage.getServiceType() + " Execute time out!");
            } else if (null != responseMessage.getThrowable()) {
                throw new RuntimeException("Unexpected server exception.", responseMessage.getThrowable());
            }
            return responseMessage;
        } finally {
            // 无论什么执行结果，在执行结束后清理事件对象等内存遗留对象
            clean(requestMessage.getMessageId(), true);
        }
    }

    /**
     * 清理目标消息ID遗留在内存中的数据，防止内存溢出
     *
     * @param messageId 待清理的消息ID
     * @param cancel    是否取消定时任务
     */
    protected void clean(String messageId, boolean cancel) {
        // 清除清理任务
        ScheduledFuture remove = futureMap.remove(messageId);
        if (cancel && null != remove) {
            // 取消清理任务
            remove.cancel(false);
        }
        // 清理回调任务
        responseMessageMap.remove(messageId);
        // 清理异步任务
//        callerMap.remove(messageId);
        // 清理回调请求
        requestMap.remove(messageId);
    }

    public void shutdown() {
        run = false;
        scheduledExecutorService.shutdown();
        controllerService.shutdown();
    }

    public MessageServiceConfig getConfig() {
        return config;
    }


    protected <T> ResponseMessage<T> buildEmptyResponse(RequestMessage requestMessage) {
        ResponseMessage<T> responseMessage = new ResponseMessage<>();
        responseMessage.setMessageId(CommonUtils.uniqueId());
        responseMessage.setRequestMessageId(requestMessage.getMessageId());
        responseMessage.setForegroundName(requestMessage.getForegroundName());
        responseMessage.setBackgroundName(getConfig().getServiceName());
        responseMessage.setBackgroundSerialId(requestMessage.getBackgroundSerialId());
        responseMessage.setSessionId(requestMessage.getSessionId());
        responseMessage.setServiceType(requestMessage.getServiceType());
        responseMessage.setOverTime(requestMessage.getOverTime());
        responseMessage.setForegroundTimestamp(requestMessage.getForegroundTimestamp());
        responseMessage.setBackgroundTimestamp(new Date());
        return responseMessage;
    }

    /**
     * 异步调用的异常处理逻辑
     */
    private class ExceptionCaller implements Runnable {

        private final RequestMessage requestMessage;
        private final Throwable throwable;
        private final AsyncCaller requestCaller;

        public ExceptionCaller(RequestMessage requestMessage, Throwable throwable, AsyncCaller requestCaller) {
            this.requestMessage = requestMessage;
            this.throwable = throwable;
            this.requestCaller = requestCaller;
        }

        @Override
        public void run() {
            boolean getLock = requestCaller.getLock().tryLock();
            if (getLock) {
                // 成功取锁，执行超时动作
                try {
                    requestCaller.onException(requestMessage, throwable);
                } catch (Throwable t) {
                    // 没有自定义的异常处理逻辑，记录日志
                    LOGGER.error("Receive an unexpected exception!", t);
                } finally {
                    requestCaller.getLock().unlock();
                    clean(requestMessage.getMessageId(), false);
                }
            }
        }
    }


    /**
     * 异步调用的超时处理逻辑
     */
    private class OvertimeCaller implements Runnable {

        private final RequestMessage requestMessage;
        private final AsyncCaller requestCaller;

        public OvertimeCaller(RequestMessage requestMessage, AsyncCaller requestCaller) {
            this.requestMessage = requestMessage;
            this.requestCaller = requestCaller;
        }

        @Override
        public void run() {
            boolean getLock = requestCaller.getLock().tryLock();
            if (getLock) {
                // 成功取锁，执行超时动作
                try {
                    //                    System.out.println("超时" + requestMessage.getMessageId());
                    requestCaller.onOvertime(requestMessage);
                } catch (Throwable t) {
                    LOGGER.error("Error when execute overtime logic!", t);
                } finally {
                    requestCaller.getLock().unlock();
                    clean(requestMessage.getMessageId(), false);
                }
            }
        }
    }


    /**
     * 异步调用的成功处理逻辑
     */
    private class SuccessCaller implements Runnable {

        private final ResponseMessage responseMessage;
        private final AsyncCaller requestCaller;

        public SuccessCaller(ResponseMessage responseMessage, AsyncCaller requestCaller) {
            this.responseMessage = responseMessage;
            this.requestCaller = requestCaller;
        }

        @Override
        public void run() {
            boolean getLock = requestCaller.getLock().tryLock();
            if (getLock) {
                // 成功取锁，执行
                try {
                    requestCaller.onSuccess(requestMap.get(responseMessage.getRequestMessageId()), responseMessage);
                } catch (Throwable t) {
                    LOGGER.error("Error when execute success logic!", t);
                } finally {
                    requestCaller.getLock().unlock();
                    clean(responseMessage.getRequestMessageId(), true);
                }
            }
        }
    }
}
