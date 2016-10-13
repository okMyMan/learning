package com.utils.mq;

/**
 * @date 2015/12/14 10:59
 * @author Konson.zhao
 * @Description 消息队列的请求处理接口，该接口定义了消息处理器所需实现的接口方法。
 */
public interface IMessageQueueServerHandler extends IMessageQueueHandler {

    /**
     * 获取业务处理对象
     */
    IServiceHandler getHandler();
}
