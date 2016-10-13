package com.utils.mq.impl.active;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @date 2016/4/21 15:27
 * @author Konson.zhao
 */
class ActiveFlowControl {
    static final AtomicInteger timeOutCount = new AtomicInteger();
    static final AtomicLong lastTimeOut = new AtomicLong();
    static final Long idleTime = 300000L;
    static final Long sleepTime = 30000L;
    static final Long watchTime = 60000L;
}
