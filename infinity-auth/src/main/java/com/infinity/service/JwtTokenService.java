package com.infinity.service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.*;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JwtTokenService
 *
 * @author Alvin Xu
 * @date 2016/9/12
 */
@Component
public class JwtTokenService {
    @Value("${jwt.key}")
    private String jwtKey;

    /**
     * Sample method to construct a JWT
     * @param id
     * @param ttlMillis  存活时间
     * @return
     */
    public String createJWT (String id, long ttlMillis) {
        return createJWT(id, null, null, ttlMillis);
    }

    /**
     * Sample method to construct a JWT
     *
     * @param id
     * @param issuer
     * @param subject
     * @param ttlMillis
     * @return
     */
    public String createJWT(String id, String issuer, String subject, long ttlMillis) {
//The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

//We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
//        iss：Issuer，发行者
//        sub：Subject，主题
//        aud：Audience，观众
//        exp：Expiration time，过期时间
//        nbf：Not before
//        iat：Issued at，发行时间
//        jti：JWT ID
        //Let's set the JWT Claims

        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);

//if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

//Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }


    /**
     * Sample method to validate and read the JWT
     *
     * @param jwt
     */
    public Claims parseJWT(String jwt) {
//This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(jwtKey))
                .parseClaimsJws(jwt).getBody();
        return claims;
    }
}
