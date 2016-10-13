package com.utils.mq.bean;

/**
 * @date 2015/12/16 16:44
 * @author Konson.zhao
 * 消息服务的配置对象，包括队列服务器地址及服务类型等基本配置。
 */
public class MessageServiceConfig {

    // ES日志存储索引名
    public static final String ES_LOG_INDEX = "mq-log";
    // ES日志存储服务类型
    public static final String ES_LOG_SERVICE_TYPE = "ES-LOG";
    /**
     * 消息队列的地址
     */
    private String mqUrl;
    /**
     * 服务类型
     */
    private String serviceType;
    /**
     * 服务名，用于定位逻辑服务器
     */
    private String serviceName;
    /**
     * 广播标识，约定：
     * <p><b>true</b>：该消息进行广播，所有目标服务的监听者都将收到该请求<br><b>false</b>：该消息不广播，将会随机选择一个目标服务监听者进行服务</p>
     * 默认不进行广播
     */
    private boolean broadcastFlag = false;
    /**
     * 是否需要回应标识，约定：
     * <p><b>true</b>：该消息需要一个对方回应信息<b><br>false</b>：该消息属于通知式调用，不需要对方回应信息，只要确认消息队列收到信息了即可，一般用于广播信息。也用于某些一次性的通知消息</p>
     * 默认需要回应信息。
     */
    private boolean responseFlag = true;
    /**
     * 业务处理线程池线程数，默认50
     */
    private int handleThread = 50;
    /**
     * 是否记录ES日志，默认记录。
     */
    private boolean recordEsLog = true;
    /**
     * 是否异步处理，默认同步。
     */
    private boolean asyncFLag = false;
    /**
     * 是否需要通过redis做唯一性控制，默认不做
     */
    private boolean uniqueFlag = false;

    /**
     * service type的后缀，用于测试
     */
    private String suffix = "";

    /**
     * 预取数量，目前仅activeMQ模式支持此配置
     */
    private int prefetchSize = 1024;

    /**
     * 批量获取，目前仅activeMQ模式支持此配置
     */
    private boolean batchFlag = false;

    /**
     * 流量控制，是否需要监控全局的消息发送及接受情况进行流量控制
     */
    private boolean flowControlFlag = false;

    public String getMqUrl() {
        return mqUrl;
    }

    public void setMqUrl(String mqUrl) {
        this.mqUrl = mqUrl;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

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

    public int getHandleThread() {
        return handleThread;
    }

    public void setHandleThread(int handleThread) {
        this.handleThread = handleThread;
    }

    public boolean isRecordEsLog() {
        return recordEsLog;
    }

    public void setRecordEsLog(boolean recordEsLog) {
        this.recordEsLog = recordEsLog;
    }

    public boolean isAsyncFLag() {
        return asyncFLag;
    }

    public void setAsyncFLag(boolean asyncFLag) {
        this.asyncFLag = asyncFLag;
    }

    public boolean isUniqueFlag() {
        return uniqueFlag;
    }

    public void setUniqueFlag(boolean uniqueFlag) {
        this.uniqueFlag = uniqueFlag;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getPrefetchSize() {
        return prefetchSize;
    }

    public void setPrefetchSize(int prefetchSize) {
        this.prefetchSize = prefetchSize;
    }

    public boolean isBatchFlag() {
        return batchFlag;
    }

    public void setBatchFlag(boolean batchFlag) {
        this.batchFlag = batchFlag;
    }

    public boolean isFlowControlFlag() {
        return flowControlFlag;
    }

    public void setFlowControlFlag(boolean flowControlFlag) {
        this.flowControlFlag = flowControlFlag;
    }

    public enum MessageAction {
        SEND, RECEIVE
    }
}
