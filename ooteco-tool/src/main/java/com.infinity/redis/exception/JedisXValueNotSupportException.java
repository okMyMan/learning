package com.infinity.redis.exception;

import redis.clients.jedis.exceptions.JedisDataException;

/**
 * Created by IntelliJ IDEA.
 * User: xuehuayang
 * Date: 12-4-14
 * To change this template use File | Settings | File Templates.
 */
public class JedisXValueNotSupportException extends JedisDataException {
    public JedisXValueNotSupportException(String message) {
        super(message);
    }

    public JedisXValueNotSupportException(Throwable cause) {
        super(cause);
    }

    public JedisXValueNotSupportException(String message, Throwable cause) {
        super(message, cause);
    }
}
