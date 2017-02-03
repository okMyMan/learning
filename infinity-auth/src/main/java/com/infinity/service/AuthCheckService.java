package com.infinity.service;

import com.infinity.constants.AuthStatus;
import com.infinity.entity.response.AuthResponse;
import io.jsonwebtoken.Claims;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuthCheckService
 *
 * @author Alvin Xu
 * @date 2016/9/12
 */
@Service
@Scope("prototype")
//1.Singleton:表示该Bean是单例模式，在Spring容器中共享一个Bean的实例
//2.Prototype:每次调用都会新创建一个Bean的实例
//3.Request:这个是使用在Web中，给每一个http request新建一个Bean实例
//4.Session:这个同样是使用在Web中，表示给每一个http session新建一个Bean实例
public class AuthCheckService {
    private static Logger logger = LoggerFactory.getLogger(AuthCheckService.class);
    @Autowired
    private JwtTokenService jwtTokenService;

    // 用户注销操作,则把token放到这个map, key是token字符串,value是存活时间
    // token校验操作,首先查看token是否存在这个map中,如果存在,则表示用户已经注销了,校验失败并提示
    private final ConcurrentHashMap<String, Date> invalidTokenMap = new ConcurrentHashMap<>();

    /**
     * 注销token池,定期清理掉过期的token
     */
    @Scheduled(fixedDelay = 60000 * 5)
    private void removeInvalidToken() {
        if (MapUtils.isEmpty(invalidTokenMap)) {
            return;
        }
        for (Map.Entry<String, Date> entry : invalidTokenMap.entrySet()) {
            Date value = entry.getValue();
            if (value.before(new Date())) {
                // token超时,则删除
                invalidTokenMap.remove(entry.getKey());
            }
        }
    }


    /**
     * generate a token
     *
     * @param id
     * @return
     */
    public AuthResponse generateToken(String id) {
        // 存活时间设为两分钟
        String token = jwtTokenService.createJWT(id, 60000 * 2);
        AuthResponse response = AuthResponse.build(AuthStatus.SUCCESS);
        response.setData(token);
        return response;
    }

    /**
     * check a token if it's valid or over time
     *
     * @param id
     * @param token
     * @return
     */
    public AuthResponse checkToken(String id, String token) {
        try {
            if (invalidTokenMap.containsKey(token)) {
                return AuthResponse.build(AuthStatus.CHECK_TOKEN_LOGDOUT);
            }

            Claims claims = jwtTokenService.parseJWT(token);

            String tokenId = claims.getId();
            if (StringUtils.isEmpty(id) || !id.equals(tokenId)) {
                logger.info("token is not belong the id, the id is " + id + " ,the token id is " + tokenId);
                return AuthResponse.build(AuthStatus.CHECK_TOKEN_FAILURE);
            }

            Date expireDate = claims.getExpiration();
            if (expireDate.before(new Date())) {
                return AuthResponse.build(AuthStatus.CHECK_TOKEN_EXPIRED);
            }
        } catch (Exception e) {
            // 如果超出有效期,则报错 JWT expired at 2016-09-12T21:56:26+0800. Current time: 2016-09-12T21:56:47+0800
            logger.error("token maybe expired when check it, the id is " + id, e);
            return AuthResponse.build(AuthStatus.RUNTIME_EXCEPTION);
        }
        return AuthResponse.makeSuccess(null);
    }

    /**
     * 用户注销,则把token放到失效token池
     * @param token
     * @return
     */
    public AuthResponse invalidToken(String token) {
        try {
            Claims claims = jwtTokenService.parseJWT(token);
            // 暂时存到内存, invalidTokenMap的value值=token的失效时间
            // 后期建议存到redis
            invalidTokenMap.put(token, claims.getExpiration());
            return AuthResponse.build(AuthStatus.SUCCESS);
        } catch (Exception e) {
            logger.error("token maybe expired when invalid it, ", e);
            return AuthResponse.build(AuthStatus.RUNTIME_EXCEPTION);
        }
    }

}
