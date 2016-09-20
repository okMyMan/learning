package com.infinity.entity.response;/**
 * Created by Administrator on 2016/9/9.
 */

import com.infinity.constants.AuthStatus;

/**
 * AuthResponse
 *
 * @author Alvin Xu
 * @date 2016/9/9
 */
public class AuthResponse {

    /**
     * 用户验证成功与否的状态
     */
    private int status;

    /**
     * 当用户校验失败,可以通过这个字段说明原因
     */
    private String message;

    /**
     * 验证成功,可以把token放到这个字段
     */
    private Object data;

    public static <T> AuthResponse makeSuccess(T t) {
        AuthResponse response = new AuthResponse(AuthStatus.SUCCESS.status, AuthStatus.SUCCESS.message);
        response.setData(t);
        return response;
    }

    public static AuthResponse makeFailure(String msg) {
        return new AuthResponse(AuthStatus.CHECK_TOKEN_FAILURE.status, AuthStatus.CHECK_TOKEN_FAILURE.message);
    }

    /**
     * 通过枚举类生成AuthResponse
     *
     * @param status
     * @return
     */
    public static AuthResponse build(AuthStatus status) {
        return new AuthResponse(status.status, status.message);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private AuthResponse() {
    }

    private AuthResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
