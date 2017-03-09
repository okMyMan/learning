package com.infinity.redis.exception;

/**
 * Created by IntelliJ IDEA.
 * User: xuehuayang
 * Date: 12-3-29
 * To change this template use File | Settings | File Templates.
 */
public class JedisXException extends RuntimeException {
    public JedisXException(String message) {
        super(message);
    }

    public JedisXException(Throwable cause) {
        super(cause);
    }

    public JedisXException(String message, Throwable cause) {
        super(message, cause);
    }
}
