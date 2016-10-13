package com.utils.mq.bean;

/**
 * @date 2015/12/26 15:45
 * @author Konson.zhao
 *
 */
public abstract class AsyncExchange {

    private final RequestMessage requestMessage;

    protected AsyncExchange(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    public abstract void onSuccess(Object response);

    public abstract void onException(Throwable throwable);

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }
}
