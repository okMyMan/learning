package com.utils.mq.impl.active;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * @date 2016/4/21 16:12
 * @author Konson.zhao
 */
public class SendWrapper {
    private MessageProducer sender;
    private Session session;
    private Connection connection;
    private int number;
    private long lastUsed = System.currentTimeMillis();

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public MessageProducer getSender() {
        return sender;
    }

    public void setSender(MessageProducer sender) {
        this.sender = sender;
    }
}
