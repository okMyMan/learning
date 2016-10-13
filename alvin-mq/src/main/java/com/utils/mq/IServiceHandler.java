package com.utils.mq;


import com.utils.mq.bean.AsyncExchange;
import com.utils.mq.bean.RequestMessage;

/**
 * @date 2015/12/15 19:51
 * @author Konson.zhao
 * @Description 服务定义接口
 */
public interface IServiceHandler {

    String ASYNC_SUFFIX = "Async";

    /**
     * 消息处理方法
     * @param requestMessage 请求消息报文
     * @param <T> 请求消息的正文对象类型{@link com.utils.mq.bean.BaseMessage#content}
     * @return 回应结果对象，消息队列通过该对象及请求消息自动组装回应消息。
     */
    <T> Object handleRequestMessage(RequestMessage<T> requestMessage);

    /**
     * 异步消息处理方法，其默认实现基于同步调用，并不真正异步，仅起API兼容及示例作用。
     * @see com.utils.mq.bean.MessageServiceConfig#asyncFLag
     * @return 回应结果对象，消息队列通过该对象及请求消息自动组装回应消息。
     */
    <T> void handleRequestMessage(AsyncExchange exchange);

    /**
     * 消息限流方法，根据当前队列大小及请求消息判断是否需要拒绝消息。
     * 默认接受所有请求，不做任何限流。若需要限流实现此方法，请同时实现{@link IServiceHandler#rejectResponse(com.utils.mq.bean.RequestMessage, int)}方法。
     * 防止客户端出现无法识别回应消息对象的情况。
     * @param requestMessage 请求消息
     * @param waitSize 当前等待队列长度
     * @param <T> 请求消息的正文对象类型{@link com.utils.mq.bean.BaseMessage#content}
     * @return true:拒绝该消息；false:处理该消息
     */
    <T> boolean reject(RequestMessage<T> requestMessage, int waitSize);

    /**
     * 限流回应消息对象，默认实现返回一个提示字符串。
     *
     * @param requestMessage 请求消息
     * @param waitSize 当前等待队列长度
     * @param <T> 请求消息的正文对象类型{@link com.utils.mq.bean.BaseMessage#content}
     * @return 回应消息对象，当该对象为null时不组装回应消息，将会导致客户端等待直到超时退出。
     */
    <T> Object rejectResponse(RequestMessage<T> requestMessage, int waitSize);
}
