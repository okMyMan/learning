package com.utils.mq;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2015/12/29 11:45
 * @author Konson.zhao
 *
 */
public interface IMessageQueueHandler {

    // 请求topic前缀
    String REQUEST_TOPIC_PREFIX = "%rpc-request%";
    // 回应（回调）topic前缀
    String RESPONSE_TOPIC_PREFIX = "%rpc-response%";
    List<String> ADDRESS = new ArrayList<>();


    /**
     * 优雅关闭正在监听的消息队列生产者及消费者客户端和处理线程池，防止积存未处理任务或者重复处理任务。
     */
    void shutdown();

    /**
     * 暂停处理，释放消息队列资源占用，可重复调用。
     */
    void pause();

    /**
     * 恢复处理，可重复调用。
     */
    void resume();


}
