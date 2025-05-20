package com.zkcompany.entity;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.*;
import io.jsonwebtoken.security.SecurityException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package entity *
 * @since 1.0
 */
public class JwtUtil {
    //有效期为
    public static final Long JWT_TTL = 3600000L;// 60 * 60 *1000  一个小时

    //Jwt令牌信息
    //public static final String JWT_KEY = "topSource_security";
    //private static final String SECRET_KEY_STRING = "topSource_securityOauth2.0_validate_selectSignAlgorithm";
    private static final String SECRET_KEY = "topSource_securityOauth2.0_validate_selectSignAlgorithm";

    /**
     * 生成令牌
     * @param id
     * @param subject
     * @param ttlMillis
     * @return
     */
    public static String createJWT(String id, String subject, Long ttlMillis) {
        //当前系统时间
        long nowMillis = System.currentTimeMillis();
        //令牌签发时间
        Date now = new Date(nowMillis);

        //如果令牌有效期为null，则默认设置有效期1小时
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }

        //令牌过期时间设置
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);

        //生成秘钥
        SecretKey secretKey = generalKey();

        //封装Jwt令牌信息
        JwtBuilder builder = Jwts.builder()
                .id(id)  //唯一的ID
                .subject(subject) // 主题  可以是JSON数据
                .issuer("topsource")  // 签发者
                .issuedAt(now)   // 签发时间
                .signWith(secretKey)// 签名算法以及密匙
                .expiration(expDate);  // 设置过期时间
        return builder.compact();
    }

    public static String createJWT(String subject, Long ttlMillis) {
        //当前系统时间
        long nowMillis = System.currentTimeMillis();
        //令牌签发时间
        Date now = new Date(nowMillis);

        //如果令牌有效期为null，则默认设置有效期1小时
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }

        //令牌过期时间设置
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);

        //生成秘钥
        SecretKey secretKey = generalKey();

        //封装Jwt令牌信息
        JwtBuilder builder = Jwts.builder()
                .id(UUID.randomUUID().toString())  //唯一的ID
                .subject(subject) // 主题  可以是JSON数据
                .issuer("topsource")  // 签发者
                .issuedAt(now)   // 签发时间
                .signWith(secretKey)// 签名算法以及密匙
                .expiration(expDate);  // 设置过期时间
        return builder.compact();
    }

    /**
     * 生成加密 secretKey
     *
     * @return
     */
    public static SecretKey generalKey() {
    byte[] encodedKey = JwtUtil.SECRET_KEY.getBytes();
    SecretKey key = new SecretKeySpec(encodedKey,"HmacSHA384");
    //SecretKey key = Keys.hmacShaKeyFor(encodedKey);
    return key;
    }


    /**
    * 解析令牌数据
    *
    * @param jwt
    * @return
    * @throws Exception
    */
    public static Claims parseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        /*return Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(jwt)
        .getBody();*/
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public static void main(String[] args) {
    //String jwt = JwtUtil.createJWT("weiyibiaoshi", "aaaaaa", null);
    //System.out.println(jwt);
        String jwt = "eyJhbGciOiJIUzM4NCJ9.eyJqdGkiOiJlYzdkMzEyYS00YjJlLTQyNjYtYmVjMy00NTE1MjUwODg3ZmEiLCJzdWIiOiJ7XCJ1c2VyX3JvbGVcIjpbXCJST0xFX3VzZXJcIl0sXCJ1c2VyXCI6e1wiY3JlYXRlZFwiOlwiMjAyNS0wMS0yMyAyMjoxNDozNFwiLFwiZW1haWxcIjpcIjg4cHZxQHllYWgubmV0XCIsXCJpZFwiOlwiMTg4MTcwNjk1ODMwMjYwOTQwOFwiLFwiaXBcIjpcIjA6MDowOjA6MDowOjA6MVwiLFwibGFzdFVwZGF0ZVwiOlwiMjAyNS0wMS0yMSAyMjoxNDozNFwiLFwibmFtZVwiOlwi546L5pm25q-TXCIsXCJwYXNzd29yZFwiOlwiXCIsXCJwaG9uZVwiOlwiMTU3MDM4OTcyMDRcIixcInNleFwiOlwiMFwiLFwic3RhdHVzXCI6XCIwXCIsXCJ1c2VybmFtZVwiOlwia2VsbHkzXCJ9fSIsImlzcyI6InRvcHNvdXJjZSIsImlhdCI6MTc0NzI1NTg0MCwiZXhwIjoxNzQ1NjI3OTcxfQ.7LABPbPiutOQjoOEBsVgsFIfP7UXMEWR00uLaeG7O1LOkKkKeldMlPQNfMDH7Vl5";
        try {
            Claims claims = JwtUtil.parseJWT(jwt);
            System.out.println(claims);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
