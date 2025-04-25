package com.zkcompany.clientStart;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

@Component
public class ElasticSearchInit {

    @Value("${elasticsearch.client.host}")
    private String host;

    @Value("${elasticsearch.client.port}")
    private int port;

    @Value("${elasticsearch.client.username}") // 从配置读取 Token
    private String username;

    @Value("${elasticsearch.client.password}") // 从配置读取 Token
    private String password;

    @Value("${elasticsearch.client.certs}") // 从配置读取 Token
    private String certs;


    @Bean
    public ElasticsearchClient loadElasticsearchClient() throws Exception{
        //1.获取证书
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = Files.newInputStream(Path.of(certs))) {
            trustStore.load(is, "13LlD9OTSny-WkprXlxyJA".toCharArray()); // 替换为实际密码
        }

        // 2. 创建 SSL 上下文
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStore, null)
                .build();

        // 3. 创建 REST 客户端
        RestClient restClient = RestClient.builder(
                        new HttpHost(host, port, "https"))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        // 获取连接等待时间改为60秒
                        .setConnectionRequestTimeout(60000)
                        // 服务器响应超时改为60秒
                        .setSocketTimeout(600000))
                .setHttpClientConfigCallback(httpConfigBuilder -> httpConfigBuilder
                        //连接在连接池中的最大存活时间（空闲连接超过此时间会被回收）
                        .setConnectionTimeToLive(60, TimeUnit.SECONDS)
                        .setSSLContext(sslContext)
                        .setDefaultCredentialsProvider(
                                new BasicCredentialsProvider() {{
                                    setCredentials(
                                            AuthScope.ANY,
                                            new UsernamePasswordCredentials(username, password) // ES 用户名密码
                                    );
                                }}
                        )
                )
                .build();

        // 4. 创建 Transport 层（处理 JSON 转换）
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper() // 使用 Jackson 处理 java对象
        );

        return new ElasticsearchClient(transport);
    }
}
