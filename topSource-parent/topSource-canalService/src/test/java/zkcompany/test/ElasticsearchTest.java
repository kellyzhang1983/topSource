package zkcompany.test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.zkcompany.dao.impl.ElasticSearchInit;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

@SpringBootTest(classes = ElasticSearchInit.class)
public class ElasticsearchTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;


    public ElasticsearchClient loadElasticsearchClient() throws Exception{

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = Files.newInputStream(Path.of("D:/ES_8.17.4/config/certs/http.p12"))) {
            trustStore.load(is, "13LlD9OTSny-WkprXlxyJA".toCharArray()); // 替换为实际密码
        }

        // 2. 创建 SSL 上下文
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStore, null)
                .build();

        // 3. 创建 REST 客户端
        RestClient restClient = RestClient.builder(
                        new HttpHost("localhost", 9200, "https"))
                .setHttpClientConfigCallback(b -> b
                        .setSSLContext(sslContext)
                        .setDefaultCredentialsProvider(
                                new BasicCredentialsProvider() {{
                                    setCredentials(
                                            AuthScope.ANY,
                                            new UsernamePasswordCredentials("elastic", "kelly19831017") // ES 用户名密码
                                    );
                                }}
                        )
                )
                .build();

        // 2. 创建 Transport 层（处理 JSON 转换）
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper() // 使用 Jackson 处理 JSON
        );

        return new ElasticsearchClient(transport);
    }

    @Test
    public void  connElasticsearch(){
        String name = null;
        try {
            name = elasticsearchClient.info().clusterName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(name);
    }
}
