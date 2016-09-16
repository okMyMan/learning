package com.infinity.entity;

/**
 * TokenEntity
 *
 * @author Alvin Xu
 * @date 2016/9/9
 */
public class TokenEntity {
    private long createTime;
    private long expireTime = 60000 * 5;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
