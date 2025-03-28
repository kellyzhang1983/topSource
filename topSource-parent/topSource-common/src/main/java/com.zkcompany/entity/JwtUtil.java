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
String jwt = "eyJhbGciOiJIUzM4NCJ9.eyJqdGkiOiIzZTZlOTYzMi0wMjNmLTRmNmMtODc0My0wZWViYzVkZWJjZjAiLCJzdWIiOiJ7XCJ1c2VyX3JvbGVcIjpbXCJST0xFX2FkbWluXCIsXCJST0xFX3VzZXJcIl0sXCJzY29wZXNcIjpbXCJhcHBcIixcInRvcHNvdXJjZVwiXSxcInVzZXJcIjp7XCJjcmVhdGVkXCI6XCIyMDI1LTAxLTIxIDIyOjE0OjI2XCIsXCJlbWFpbFwiOlwieWV0QG1zbi5jb21cIixcImlkXCI6XCIxODgxNzA2OTI0NjU1OTAyNzIwXCIsXCJpcFwiOlwiMDowOjA6MDowOjA6MDoxXCIsXCJsYXN0VXBkYXRlXCI6XCIyMDI1LTAxLTIxIDIyOjE0OjI2XCIsXCJsYXN0X2xvZ2luX3RpbWVcIjpcIjIwMjUtMDEtMjEgMjI6MTQ6MjZcIixcIm5hbWVcIjpcIuWvh-mdmeeQvFwiLFwibmlja19uYW1lXCI6XCLog5zlpJrotJ_lsJFcIixcInBhc3N3b3JkXCI6XCIkMmEkMTAkSEpjM1dLam82dEM0NnpTanQ3Q0g4LjUzUXZXd1pLeExWdXI2Mm5iRnhmRUpXNHRIbGVoSUNcIixcInBob25lXCI6XCIxMzYwNDk3NzM1NVwiLFwic2V4XCI6XCIwXCIsXCJzb3VyY2VfdHlwZVwiOlwiMlwiLFwic3RhdHVzXCI6XCIxXCIsXCJ1c2VybmFtZVwiOlwia2VsbHkxXCJ9fSIsImlzcyI6InRvcHNvdXJjZSIsImlhdCI6MTc0MjI5NDg2NywiZXhwIjoxNzczODMwODY3fQ.veRSBwdnykkXquiYK_WsVbE10UZ7fJJjq2-bQeMj3nqr1TZIu6vi7TcwGnAqzuJS";
try {
Claims claims = JwtUtil.parseJWT(jwt);
System.out.println(claims);
} catch (Exception e) {
e.printStackTrace();
}


}
}
