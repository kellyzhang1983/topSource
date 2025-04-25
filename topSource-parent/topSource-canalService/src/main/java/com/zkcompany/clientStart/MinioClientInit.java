package com.zkcompany.clientStart;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConfigurationProperties(prefix = "minioclient")
@Data
public class MinioClientInit {

    private String url;
    private String accessKey;
    private String secreKey;
    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey,secreKey)
                .build();
    }
}
