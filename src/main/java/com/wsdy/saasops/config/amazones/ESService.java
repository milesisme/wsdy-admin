package com.wsdy.saasops.config.amazones;

import com.amazonaws.auth.AWS4Signer;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public class ESService {

    public RestClient initRestClient(String aesEndpoint, String serviceName, String region, String accessKey, String secretKey,int amazonTimeout) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, new BatchDefaultAWSCredentialsProviderChain(accessKey, secretKey));
        return RestClient.builder(HttpHost.create(aesEndpoint)).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)).
                setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                        requestConfigBuilder.setConnectTimeout(amazonTimeout);
                        requestConfigBuilder.setSocketTimeout(amazonTimeout);
                        requestConfigBuilder.setConnectionRequestTimeout(amazonTimeout);
                        return requestConfigBuilder;
                    }
                }).setMaxRetryTimeoutMillis(amazonTimeout).build();
    }

    public RestClient initRestClient(String url, int restport, String name, String password, int timeout) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(name, password));
        RestClientBuilder builder = RestClient.builder(new HttpHost(url, restport, "http"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                })
                .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                    @Override
                    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                        requestConfigBuilder.setConnectTimeout(timeout);
                        requestConfigBuilder.setSocketTimeout(timeout);
                        requestConfigBuilder.setConnectionRequestTimeout(timeout);
                        return requestConfigBuilder;
                    }
                })/***超时时间设为2分钟**/
                .setMaxRetryTimeoutMillis(timeout);
        return builder.build();
    }
}
