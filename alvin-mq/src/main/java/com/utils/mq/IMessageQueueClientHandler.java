package com.utils.mq;


import com.utils.mq.bean.RequestMessage;
import com.utils.mq.bean.ResponseMessage;

import java.util.concurrent.TimeoutException;

/**
 * @date 2015/12/14 10:59
 * @author Konson.zhao
 * @Description 基于消息队列的RPC请求处理接口，该接口定义了消息处理器所需实现的接口方法，包括同步及异步处理。
 * <b>推荐<b/>通过异步请求的方式来使用该处理器，提高客户端的吞吐量及并发量同时减小线程数。
 */
public interface IMessageQueueClientHandler extends IMessageQueueHandler {

    /**
     * 表现为类似Hessian调用的同步调用逻辑，需要实现最大灵活度的调用时调用此方法。
     * @param requestMessage 入参参数，包括目标服务及超时时间等参数。若调用无需回应，会在消息投递成功后直接返回一个成功信息。
     * @param <T> 入参正文的对象类型
     * @return 回应报文
     * @deprecated 由于消息框架的回调及服务类型等参数极大影响RPC交互逻辑，及其不推荐通过此方法实现RPC调用。
     * 如确定需要使用此方法进行调用，请务必参照<br>
     *     {@link com.utils.mq.impl.AbstractMQClientHandler#buildRequest(Object, long, RequestMessage, boolean)}<br>
     *         的请求消息构造方式。
     */
    @Deprecated
    <T> ResponseMessage<?> request(RequestMessage<T> requestMessage) throws TimeoutException, InterruptedException;

    <T> ResponseMessage<?> request(T request, long timeout) throws TimeoutException, InterruptedException;
    /**
     * 表现为类似Hessian调用的同步调用逻辑，若请求消息需要做唯一性控制，调用此方法
     * @param request 请求正文对象
     * @param parentMessage 所属会话的请求消息
     * @param <T> 入参正文的对象类型
     * @return 回应消息对象
     * @throws TimeoutException
     * @throws InterruptedException
     */
    <T> ResponseMessage<?> request(T request, long timeout, RequestMessage<?> parentMessage, boolean uniqueFlag)
            throws TimeoutException, InterruptedException;

}
