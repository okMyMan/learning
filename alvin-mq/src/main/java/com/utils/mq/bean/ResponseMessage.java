package com.utils.mq.bean;

import java.util.Date;

/**
 * @date 2015/12/15 16:42
 * @author Konson.zhao
 * 回应消息对象
 */
public class ResponseMessage<T> extends BaseMessage<T> {
    /**
     * 请求消息的ID号，用于定位请求消息
     * 框架赋值，客户端无需赋值。
     */
    private String requestMessageId;
    /**
     * 后台发起时间戳，用于记录日志以及超时判断
     * 框架赋值，客户端无需赋值。
     */
    private Date backgroundTimestamp;
    /**
     * 提供服务的服务方服务标识
     * 框架赋值，客户端无需赋值。
     */
    private String backgroundName;

    /**
     * 服务方抛出的运行时异常，客户端可自行选择抛出或记录日志等操作
     */
    private Throwable throwable;

    public String getRequestMessageId() {
        return requestMessageId;
    }

    public void setRequestMessageId(String requestMessageId) {
        this.requestMessageId = requestMessageId;
    }

    public Date getBackgroundTimestamp() {
        return backgroundTimestamp;
    }

    public void setBackgroundTimestamp(Date backgroundTimestamp) {
        this.backgroundTimestamp = backgroundTimestamp;
    }

    public String getBackgroundName() {
        return backgroundName;
    }

    public void setBackgroundName(String backgroundName) {
        this.backgroundName = backgroundName;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
