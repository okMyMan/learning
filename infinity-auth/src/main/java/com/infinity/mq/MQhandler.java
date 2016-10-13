package com.infinity.mq;

import com.utils.mq.IServiceHandler;
import com.utils.mq.bean.AsyncExchange;
import com.utils.mq.bean.RequestMessage;

/**
 * MQhandler
 *
 * @author Alvin Xu
 * @date 2016/10/13
 * @description
 */
public class MQhandler implements IServiceHandler {
    @Override
    public <T> Object handleRequestMessage(RequestMessage<T> requestMessage) {
        String ss = (String)requestMessage.getContent();
        System.out.println();
        return ss+" special";
    }

    @Override
    public <T> void handleRequestMessage(AsyncExchange exchange) {

    }

    @Override
    public <T> boolean reject(RequestMessage<T> requestMessage, int waitSize) {
        return false;
    }

    @Override
    public <T> Object rejectResponse(RequestMessage<T> requestMessage, int waitSize) {
        return null;
    }
}
