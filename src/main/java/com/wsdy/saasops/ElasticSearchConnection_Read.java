package com.wsdy.saasops;

import com.wsdy.saasops.config.amazones.ESService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ElasticSearchConnection_Read extends ESService {

    @Value("${es.amazonElasticsearch}")
    private Boolean amazonElasticsearch;

    @Value("${read_elasticsearch.aesEndpoint}")
    private String aesEndpoint;
    @Value("${read_elasticsearch.serviceName}")
    private String serviceName;
    @Value("${read_elasticsearch.region}")
    private String region;
    @Value("${read_elasticsearch.accessKey}")
    private String accessKey;
    @Value("${read_elasticsearch.secretKey}")
    private String secretKey;
    @Value("${read_elasticsearch.amazonTimeout}")
    private int amazonTimeout;

    @Value("${read_elasticsearch.url}")
    private String url;
    @Value("${read_elasticsearch.port}")
    private int clientport;
    @Value("${read_elasticsearch.rest.port}")
    private int restport;
    @Value("${read_elasticsearch.name}")
    private String name;
    @Value("${read_elasticsearch.password}")
    private String password;
    @Value("${read_elasticsearch.timeout}")
    private int timeout;

    public TransportClient client;

    public RestClient restClient_Read;

    public ElasticSearchConnection_Read() {

    }

    @PostConstruct
    private void init() {
        //         配置信息
        Settings esSetting = Settings.builder()
                //设置ES实例的名称
                .put("cluster.name", "saasops-dbcenter")
                //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
                .put("client.transport.sniff", true)
                .build();
        //初始化client较老版本发生了变化，此方法有几个重载方法，初始化插件等。
        client = new PreBuiltTransportClient(esSetting);
        if (amazonElasticsearch) {
            restClient_Read = initRestClient(aesEndpoint, serviceName, region, accessKey, secretKey,amazonTimeout);
        } else {
            restClient_Read = initRestClient(url, restport, name, password, timeout);
        }

    }

}
