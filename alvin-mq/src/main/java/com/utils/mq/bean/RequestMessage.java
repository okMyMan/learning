package com.utils.mq.bean;

/**
 * @date 2015/12/15 16:42
 * @author Konson.zhao
 * 请求消息对象
 */
public class RequestMessage<T> extends BaseMessage<T> {
    /**
     * 广播标识，约定：
     * <p><b>true</b>：该消息进行广播，所有目标服务的监听者都将收到该请求<br><b>false</b>：该消息不广播，将会随机选择一个目标服务监听者进行服务</p>
     * 默认不进行广播
     */
    private boolean broadcastFlag = false;
    /**
     * 是否需要回应标识，约定：
     * <p><b>true</b>：该消息需要一个对方回应信息<b><br>false</b>：该消息属于通知式调用，不需要对方回应信息，只要确认消息队列收到信息了即可，一般用于广播信息</p>
     * 默认需要回应信息。
     */
    private boolean responseFlag = true;

    /**
     * 是否需要通过redis做唯一性控制，默认不做
     */
    private boolean uniqueFlag = false;

    public boolean isBroadcastFlag() {
        return broadcastFlag;
    }

    public void setBroadcastFlag(boolean broadcastFlag) {
        this.broadcastFlag = broadcastFlag;
    }

    public boolean isResponseFlag() {
        return responseFlag;
    }

    public void setResponseFlag(boolean responseFlag) {
        this.responseFlag = responseFlag;
    }

    public boolean isUniqueFlag() {
        return uniqueFlag;
    }

    public void setUniqueFlag(boolean uniqueFlag) {
        this.uniqueFlag = uniqueFlag;
    }
}
