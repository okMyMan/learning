package com.utils.mq.impl;

import com.alibaba.fastjson.JSON;
import com.infinity.thread.NamedThreadFactory;
import com.utils.mq.IMessageQueueServerHandler;
import com.utils.mq.IServiceHandler;
import com.utils.mq.bean.AsyncExchange;
import com.utils.mq.bean.MessageServiceConfig;
import com.utils.mq.bean.RequestMessage;
import com.utils.mq.bean.ResponseMessage;
import com.utils.mq.tool.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * AbstractMQServerHandler
 *
 * @author Alvin Xu
 * @date 2016/10/10
 * @description 抽象的RPC消息队列回应处理实现类
 */
public abstract class AbstractMQServerHandler implements IMessageQueueServerHandler {
    // 控制器线程数
    public static final int CONTROLLER_NUM = 2;
    // 日志记录
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMQServerHandler.class);
    // 用于业务处理的处理类Map
    private final IServiceHandler handler;
    // 用于接收、调度处理回应消息的控制器线程池
    private final ExecutorService controllerService;
    // 用于接收、调度处理回应消息的控制器线程池
    private final ExecutorService businessService;
    // 消息队列配置
    private final MessageServiceConfig config;
    // 用于运行开关
    protected volatile boolean run = true;
    /**
     * @param config 消息队列配置
     */
    public AbstractMQServerHandler(MessageServiceConfig config, IServiceHandler handlers) {
        this.config = config;
        if (null == config.getServiceName() || config.getServiceName().isEmpty()) {
            config.setServiceName(CommonUtils.uniqueId());
        }
        this.handler = handlers;
        controllerService = Executors.newFixedThreadPool(CONTROLLER_NUM,
                new NamedThreadFactory("CONTROL" + config.getServiceType() + config.getSuffix() + config.getServiceName()));

        businessService = new ThreadPoolExecutor(
                config.getHandleThread(),
                config.getHandleThread(),
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new NamedThreadFactory("BUSINESS" + config.getServiceType() + config.getSuffix() + config.getServiceName()));



    }
    /**
     * 启动消息处理线程
     */
    protected void handleRequest() {
        for (int i = 0; i < CONTROLLER_NUM; i++) {
            controllerService.submit(

                    new Runnable() {
                        @Override
                        public void run() {
                            while (run) {
                                try {
                                    deal();
                                } catch (Throwable t) {
                                    LOGGER.error("Error when handle request", t);
                                }
                            }
                        }
                    }

            );
        }
    }

    private void deal() {
        try {
            final RequestMessage requestMessage = pollMessage(10);
            if (null != requestMessage && null != requestMessage.getForegroundTimestamp() && requestMessage.getOverTime() > (
                    System.currentTimeMillis() - requestMessage.getForegroundTimestamp().getTime())) {
                // 未超出超时时间限制
                businessService.execute(

                        new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    // 在收到请求时即设置后台流水号，保证后续的子请求全使用该后台流水号。
                                    requestMessage.setBackgroundSerialId(CommonUtils.uniqueId());
                                    if (getConfig().isAsyncFLag()) {
                                        // 异步处理
                                        AsyncExchange exchange = new AsyncExchange(requestMessage) {
                                            @Override
                                            public void onSuccess(Object response) {
                                                try {
                                                    if (getRequestMessage().isResponseFlag()) {
                                                        // 需要回应
                                                        ResponseMessage responseMessage =
                                                                CommonUtils.buildResponse(response, null, getRequestMessage());
                                                        responseMessage.setBackgroundName(getConfig().getServiceName());
                                                        sendMessage(responseMessage);
                                                    }
                                                } catch (Throwable throwable) {
                                                    LOGGER.error("Error when send response to client.", throwable);
                                                }
                                            }

                                            @Override
                                            public void onException(Throwable throwable) {
                                                try {
                                                    LOGGER.error("Error when handle business request.", throwable);
                                                    if (getRequestMessage().isResponseFlag()) {
                                                        // 需要回应，将异常扔回客户端进行穿透处理
                                                        ResponseMessage responseMessage =
                                                                CommonUtils.buildResponse(null, throwable, getRequestMessage());
                                                        responseMessage.setBackgroundName(getConfig().getServiceName());
                                                        sendMessage(responseMessage);
                                                    }
                                                } catch (Throwable t) {
                                                    LOGGER.error("Error when send response to client.", t);
                                                }
                                            }
                                        };
                                        getHandler().handleRequestMessage(exchange);
                                    } else {
                                        // 同步处理
                                        try {
                                            Object handleResult = getHandler().handleRequestMessage(requestMessage);
                                            if (requestMessage.isResponseFlag()) {
                                                // 需要回应
                                                ResponseMessage<Object> responseMessage =
                                                        CommonUtils.buildResponse(handleResult, null, requestMessage);
                                                responseMessage.setBackgroundName(getConfig().getServiceName());
                                                sendMessage(responseMessage);
                                            }
                                        } catch (Throwable throwable) {
                                            LOGGER.error("Error when handle business request.", throwable);
                                            if (requestMessage.isResponseFlag()) {
                                                // 需要回应
                                                ResponseMessage<Object> responseMessage = CommonUtils.buildResponse(null, throwable, requestMessage);
                                                responseMessage.setBackgroundName(getConfig().getServiceName());
                                                sendMessage(responseMessage);
                                            }
                                        }
                                    }
                                } catch (Throwable t) {
                                    LOGGER.error("Error when handle requestMessage:" + JSON.toJSONString(requestMessage), t);
                                }
                            }
                        }


                );
            } else {
                if (null != requestMessage) {
                    LOGGER.error("Request for " + requestMessage.getServiceType() + " discard due to out of time." + (
                            System.currentTimeMillis() - requestMessage.getForegroundTimestamp().getTime())
                            + " expired time:" + requestMessage.getOverTime());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IServiceHandler getHandler() {
        return handler;
    }

    public MessageServiceConfig getConfig() {
        return config;
    }

    /**
     * 回应消息的发送方法，子类需要根据具体的底层消息队列实现来完成此方法。
     * @param responseMessage 待发送的回应消息
     */
    protected abstract void sendMessage(ResponseMessage responseMessage);

    /**
     * 请求消息的批量获取方法，若底层队列实现不支持带超时时间的批量获取，可通过自实现BlockingQueue缓存的方式实现此逻辑。
     * @param timeout 超时时间
     * @return 在超时时间内可获取到的所有消息
     */
    protected abstract RequestMessage pollMessage(long timeout) throws InterruptedException;

    public void shutdown() {
        run = false;
        controllerService.shutdown();
        businessService.shutdown();
    }
}
