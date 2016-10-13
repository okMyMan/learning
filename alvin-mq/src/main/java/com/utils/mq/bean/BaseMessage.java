package com.utils.mq.bean;

import java.util.Date;

/**
 * @date 2015/12/15 16:42
 * @author Konson.zhao
 * 消息队列请求信息实体对象，该对象定义了一个消息在传递过程中所需要的基本要素。消息中各字段需要遵循约定优于配置的原则。<br>
 * {@link com.utils.mq.impl.AbstractMQClientHandler} {@link com.utils.mq.impl.AbstractMQServerHandler} 除特殊定制化需求以外，一般建议通过该类进行消息的初始化，该类实现了各消息基本字段的默认最佳实现。
 * @param <T> 消息内部对象类型
 */
public abstract class BaseMessage<T> {
    /**
     * 消息ID，用于各系统中唯一定位消息（包括回应消息处理时用于定位请求消息），因此务必确保该字段全局唯一。
     * 框架赋值，客户端无需赋值。
     */
    private String messageId;
    /**
     * 会话ID，用于提供基本的会话定位。会话ID默认约定：
     * <p><b>若当前消息已经从属于某一个会话，则该消息同样归属于该会话；否则客户端可选择是否开启新会话。</b></p>
     * <b>不建议</b>在已经从属于某一个会话的情况下构造新会话，因为这会造成会话日志定位困难或出现某些会话丢失的情况。
     * 若客户端应用确实需要实现类似于父子会话或构造新会话等功能，也建议在content对象中通过自有字段来构造而非修改sessionId的约定。
     */
    private String sessionId;
    /**
     * 前台流水号，该流水号由客户端生成。前台流水号约定：
     * <p><b>若当前消息衍生于某一个父消息的调用，则当前消息应归属于同一个前台流水号。
     * 若父消息需要一个回应，则应将该前台流水号作为该回应的后台流水号进行返回</b></p>
     * 框架赋值，客户端无需赋值。
     */
    private String foregroundSerialId;
    /**
     * 后台流水号，该流水号由服务端生成。后台流水号约定：
     * <p><b>若请求消息需要一个回应，则应在收到请求时即生成该后台流水号，作为所有子业务请求消息的请求流水号，在子业务处理完毕后将后台流水号进行返回</b></p>
     * 框架赋值，客户端无需赋值。
     */
    private String backgroundSerialId;
    /**
     * 后台（目标端）服务名，服务名约定格式为：
     * <p><b>系统名-模块名-接口名-方法名-版本号</b>，命名段为首字母大写的驼峰式命名，版本号由阿拉伯数字与.组成，默认为0.0</p>
     * 框架赋值，客户端无需赋值。
     */
    private String serviceType;
    /**
     * 发起请求的请求方服务标识
     * 框架赋值，客户端无需赋值。
     */
    private String foregroundName;
    /**
     * 前台发起时间戳，用于记录日志以及超时判断
     * 框架赋值，客户端无需赋值。
     */
    private Date foregroundTimestamp;
    /**
     * 超时时间（毫秒数），默认180s
     */
    private long overTime = 180000L;
    /**
     * 客户端地址，用于记录消息产生地址
     * 框架赋值，客户端无需赋值。
     */
    private String bornHost;
    /**
     * 消息正文对象
     */
    private T content;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getForegroundSerialId() {
        return foregroundSerialId;
    }

    public void setForegroundSerialId(String foregroundSerialId) {
        this.foregroundSerialId = foregroundSerialId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Date getForegroundTimestamp() {
        return foregroundTimestamp;
    }

    public void setForegroundTimestamp(Date foregroundTimestamp) {
        this.foregroundTimestamp = foregroundTimestamp;
    }

    public long getOverTime() {
        return overTime;
    }

    public void setOverTime(long overTime) {
        this.overTime = overTime;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getForegroundName() {
        return foregroundName;
    }

    public void setForegroundName(String foregroundName) {
        this.foregroundName = foregroundName;
    }

    public String getBackgroundSerialId() {
        return backgroundSerialId;
    }

    public void setBackgroundSerialId(String backgroundSerialId) {
        this.backgroundSerialId = backgroundSerialId;
    }

    public String getBornHost() {
        return bornHost;
    }

    public void setBornHost(String bornHost) {
        this.bornHost = bornHost;
    }
}
