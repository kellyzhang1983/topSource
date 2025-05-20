package com.zkcompany.config;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class InnerCallSignManager {
    private String secretKey = "topsource-innerMethod-call";

    // 生成服务端签名（每分钟变化）
    public String generateSign(String uri) {
        long timeWindow = System.currentTimeMillis() / 60000;
        return DigestUtils.sha256Hex(secretKey + uri + timeWindow);
    }

}
