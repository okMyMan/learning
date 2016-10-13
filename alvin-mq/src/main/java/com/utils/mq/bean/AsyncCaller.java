package com.utils.mq.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @date 2015/12/25 12:01
 * @author Konson.zhao
 * 异步调用对象，用于存储请求对象及回调方法
 */
public abstract class AsyncCaller {

    private static Logger logger = LoggerFactory.getLogger(AsyncCaller.class);
    private final Object request;
    private final RequestMessage parentMessage;
    private final long timeOut;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean uniqueFlag = false;

    /**
     * 通过业务对象由框架构造请求消息
     * @param request 请求业务对象
     * @param timeOut 超时时间
     */
    protected AsyncCaller(Object request, long timeOut) {
        this(request, timeOut, null);
    }

    /**
     * 通过业务对象由框架构造请求消息
     * @param request 请求业务对象
     * @param timeOut 超时时间
     * @param parentMessage 所属父消息
     */
    protected AsyncCaller(Object request, long timeOut, RequestMessage parentMessage) {
        this.request = request;
        this.timeOut = timeOut;
        this.parentMessage = parentMessage;
    }

    /**
     * 成功收到回应报文后的处理逻辑
     */
    public abstract void onSuccess(RequestMessage requestMessage, ResponseMessage responseMessage);

    /**
     * 超时后的处理逻辑，<b>不建议在该方法中加入太多耗时处理，以防止大量超时操作影响整体性能。</b>
     */
    public abstract void onOvertime(RequestMessage requestMessage);

    /**
     * 出现异常后的处理逻辑，默认同超时处理
     */
    public void onException(RequestMessage requestMessage, Throwable t) {
        logger.error(t.getMessage(), t);
        onOvertime(requestMessage);
    }

    public Object getRequest() {
        return request;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public RequestMessage getParentMessage() {
        return parentMessage;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public boolean isUniqueFlag() {
        return uniqueFlag;
    }

    public void setUniqueFlag(boolean uniqueFlag) {
        this.uniqueFlag = uniqueFlag;
    }

}
