package com.wsdy.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.wsdy.saasops.api.config.OkHttpConfig;
import com.wsdy.saasops.api.constants.PtConstants;
import com.wsdy.saasops.api.modules.apisys.entity.SsysConfig;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.user.dto.PtEntity;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.exception.RRException;
import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OkHttpService {

    public final static String GET = "GET";
    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";
    public final static String PATCH = "PATCH";

    private final static String UTF8 = "UTF-8";

    public final static String APPLICATION_JSON = "application/json";
    public final static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private final static String DEFAULT_CHARSET = UTF8;
    private final static String DEFAULT_METHOD = GET;
    private final static boolean DEFAULT_LOG = true;

    private final static String PAY_CODE = "groxyPay";


    @Autowired
    OkHttpConfig okHttpConfig;
    @Autowired
    private TGmApiService tGmApiService;
    @Autowired
    private JsonUtil jsonUtil;

    public OkHttpClient getHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(true)
                .connectTimeout(okHttpConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(okHttpConfig.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(okHttpConfig.getWriteTimeout(), TimeUnit.SECONDS);
        return setProxys(builder, okHttpConfig.getProxyGroup()).build();
    }

    public OkHttpClient getHttpNoProxyClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(false)
                .connectTimeout(okHttpConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(okHttpConfig.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(okHttpConfig.getWriteTimeout(), TimeUnit.SECONDS);
        return setNoProxys(builder).build();
    }

    public OkHttpClient getPayHttpsClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(true)
                .connectTimeout(okHttpConfig.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(okHttpConfig.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(okHttpConfig.getWriteTimeout(), TimeUnit.SECONDS);
        return setPayProxys(builder, PAY_CODE).build();
    }

    private OkHttpClient.Builder setPayProxys(OkHttpClient.Builder builder, String groups) {
        List<SsysConfig> proxylist = tGmApiService.queryList(groups);
        log.info("代理列表：" + jsonUtil.toJson(proxylist));
        if (proxylist.size() > 0) {
            builder.proxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    List<Proxy> list = new ArrayList<>();
                    java.net.Authenticator.setDefault(new java.net.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxylist.get(0).getProxyProperty().getUser(),
                                    proxylist.get(0).getProxyProperty().getPassword().toCharArray());
                        }
                    });
                    if ("SOCKS".equals(proxylist.get(0).getProxyProperty().getProxyType().toUpperCase())) {
                        list.add(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxylist.get(0).getProxyProperty().getIp(),
                                proxylist.get(0).getProxyProperty().getPort())));
                    } else {
                        list.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxylist.get(0).getProxyProperty().getIp(),
                                proxylist.get(0).getProxyProperty().getPort())));
                    }
                    return list;
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                    log.error("uri: {} socketAddress: {} IOException: {}", uri, sa, ioe);
                }
            });
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                builder.sslSocketFactory(sslContext.getSocketFactory(), new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }).build();
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
        }
        return builder;
    }

    private OkHttpClient.Builder setNoProxys(OkHttpClient.Builder builder) {
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }).build();
        } catch (Exception e) {
            log.error("post请求参数组装异常" + e.getStackTrace());
            throw new RRException("post请求参数组装异常");
        }
        return builder;
    }


    /**
     * pt专用
     *
     * @return
     */
    public OkHttpClient getHttpsClient() {
        try {
            KeyStore ks = KeyStore.getInstance(PtConstants.SslEntity.KeyStore);
            InputStream stream = OkHttpService.class.getResourceAsStream(PtConstants.SslEntity.keyFilePath);
            ks.load(stream, PtConstants.SslEntity.keyPwd.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(PtConstants.SslEntity.KeyManager);
            kmf.init(ks, PtConstants.SslEntity.keyPwd.toCharArray());
            SSLContext sc = SSLContext.getInstance(PtConstants.SslEntity.tls);
            sc.init(kmf.getKeyManagers(), null, null);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.retryOnConnectionFailure(true)
                    .connectTimeout(okHttpConfig.getConnectTimeout(), TimeUnit.SECONDS)
                    .readTimeout(okHttpConfig.getReadTimeout(), TimeUnit.SECONDS)
                    .writeTimeout(okHttpConfig.getWriteTimeout(), TimeUnit.SECONDS).
                    hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    }).sslSocketFactory(sc.getSocketFactory());
//            builder.proxySelector(getProxySelector());
            return setProxys(builder, okHttpConfig.getProxyGroup()).build();
        } catch (Exception ex) {
            log.debug("PT 建立SSL 失败{}", ex.getMessage());
            throw new RRException("PT建立SSL失败");
        }
    }

    public String send(String url, PtEntity ptEntity) {
        String responseBody = "";

        FormBody.Builder bodybuilder = new FormBody.Builder();
        Request.Builder builder = new Request.Builder();
        builder.url(url).post(bodybuilder.build());
        builder.addHeader(ptEntity.getEntityKey(), ptEntity.getEntityContext());
        Request request = builder.build();
        Response response = null;
        try {
            OkHttpClient okHttpClient = getHttpsClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.debug("post 方法请求url地址{}参数为{},请求头为{}", url, new Gson().toJson(ptEntity));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }

    private OkHttpClient.Builder setProxys(OkHttpClient.Builder builder, String groups) {
        List<SsysConfig> proxylist = tGmApiService.queryList(groups);
        if (proxylist.size() > 0) {
            builder.proxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    List<Proxy> list = new ArrayList<>();
                    java.net.Authenticator.setDefault(new java.net.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxylist.get(0).getProxyProperty().getUser(),
                                    proxylist.get(0).getProxyProperty().getPassword().toCharArray());
                        }
                    });
                    if ("SOCKS".equals(proxylist.get(0).getProxyProperty().getProxyType().toUpperCase())) {
                        list.add(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxylist.get(0).getProxyProperty().getIp(),
                                proxylist.get(0).getProxyProperty().getPort())));
                    } else {
                        list.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxylist.get(0).getProxyProperty().getIp(),
                                proxylist.get(0).getProxyProperty().getPort())));
                    }
                    return list;
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                    log.error("uri: {} socketAddress: {} IOException: {}", uri, sa, ioe);
                }
            });
        }
        return builder;
    }

    /**
     * @param url
     * @param headParams
     * @return
     */
    public String get(String url, Map<String, String> headParams) {
        String responseBody = "";
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (MapUtils.isNotEmpty(headParams)) {
            headParams.forEach(builder::addHeader);
        }
        Request request = builder.build();
        Response response = null;
        try {
            OkHttpClient okHttpClient = getHttpNoProxyClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.debug("Get 方法请求url地址{}地址为{},请求头为{}", url, JSON.toJSONString(headParams));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }

    public String get(String url) {
        return get(url, null);
    }

    public String get(OkHttpClient httpClient, String url, Map<String, String> stringMap) {
        return execute(OkHttp.builder().url(url).method(GET).mediaType(APPLICATION_JSON).headerMap(stringMap).build(), httpClient);
    }

    public String postPayFrom(OkHttpClient httpClient, String url, Map<String, String> stringMap) {
        return execute(OkHttp.builder().url(url).method(POST).data("").mediaType(APPLICATION_FORM_URLENCODED)
                .headerMap(stringMap).build(), httpClient);
    }

    /**
     * @param url
     * @param params
     * @param headParams
     * @return
     */
    public String post(String url, Map<String, String> params, Map<String, String> headParams) {
        String responseBody = "";

        FormBody.Builder bodybuilder = new FormBody.Builder();
        //添加参数
        if (MapUtils.isNotEmpty(params)) {
            params.forEach(bodybuilder::add);
        }
        Request.Builder builder = new Request.Builder();
        builder.url(url).post(bodybuilder.build());
        if (MapUtils.isNotEmpty(headParams)) {
            headParams.forEach(builder::addHeader);
        }
        Request request = builder.build();
        Response response = null;
        try {
            OkHttpClient okHttpClient = getHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                log.error(response.message());
            }
        } catch (Exception e) {
            log.debug("post 方法请求url地址{}参数为{},请求头为{}", url, new Gson().toJson(params), JSON.toJSONString(headParams));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }

    /**
     * @param url
     * @param params
     * @return
     */
    public String post(String url, Map<String, String> params) {
        return post(url, params, null);
    }

    /**
     * @param url
     * @return
     */
    public String post(String url) {
        return post(url, null, null);
    }

    public String postForm(OkHttpClient httpClient, String url, Map<String, String> formMap) {
        String data = "";
        if (MapUtils.isNotEmpty(formMap)) {
            data = formMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors
                    .joining("&"));
        }
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(APPLICATION_FORM_URLENCODED).build(), httpClient);
    }

    public String postFormBTP(OkHttpClient httpClient, String url, Map<String, Object> formMap) {
        String data = "";
        if (MapUtils.isNotEmpty(formMap)) {
            data = formMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors
                    .joining("&"));
        }
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(APPLICATION_FORM_URLENCODED).build(), httpClient);
    }

    /**
     * @param url
     * @param parmsJosn
     * @param headParams
     * @return
     */
    public String postNoProxyJson(String url, String parmsJosn, Map<String, String> headParams, String method) {
        String responseBody = "";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), parmsJosn);
        Request.Builder builder = new Request.Builder();
        builder.url(url).method(StringUtils.isEmpty(method) ? POST : method, requestBody);
        if (MapUtils.isNotEmpty(headParams)) {
            headParams.forEach(builder::addHeader);
        }
        Request request = builder.build();
        Response response = null;
        try {
            OkHttpClient okHttpClient = getHttpNoProxyClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("post 方法请求url地址{}参数为{},请求头为{},错误信息为{}", url, parmsJosn, JSON.toJSONString(headParams), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }

    /**
     * @param url
     * @param parmsJosn
     * @param headParams
     * @return
     */
    public String postJson(String url, String parmsJosn, Map<String, String> headParams, String method) {
        String responseBody = "";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), parmsJosn);
        Request.Builder builder = new Request.Builder();
        builder.url(url).method(StringUtils.isEmpty(method) ? POST : method, requestBody);
        if (MapUtils.isNotEmpty(headParams)) {
            headParams.forEach(builder::addHeader);
        }
        Request request = builder.build();
        Response response = null;
        try {
            OkHttpClient okHttpClient = getHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.debug("post 方法请求url地址{}参数为{},请求头为{},错误信息为{}", url, parmsJosn, JSON.toJSONString(headParams), e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }

    /**
     * @param url 地址
     * @param obj 提交的对像
     * @return
     */
    public String postJson(String url, Object obj) {
        return postJson(url, JSON.toJSONString(obj), null, null);
    }

    /**
     * @param url 地址
     * @param obj 提交的对像
     * @return
     */
    public String postNoProxyJson(String url, Object obj, Map<String, String> stringMap) {
        return postNoProxyJson(url, JSON.toJSONString(obj), stringMap, null);
    }

    public String postJson(OkHttpClient httpClient, String url, Object obj, Map<String, String> stringMap) {
        return execute(OkHttp.builder().url(url).method(POST).data(JSON.toJSONString(obj))
                .mediaType(APPLICATION_JSON).headerMap(stringMap).build(), httpClient);
    }


    public String postGateWayJson(OkHttpClient httpClient, String url, Object obj, Map<String, String> stringMap) {
        return execute(OkHttp.builder().url(url).method(POST).data(JSON.toJSONString(obj)).mediaType(APPLICATION_JSON).headerMap(stringMap).build(), httpClient);
    }

    /**
     * @param url
     * @param xml
     * @param headParams
     * @return
     */
    public String postXmlParams(String url, String xml, Map<String, String> headParams, String mediaType) {
        String responseBody = "";
        RequestBody requestBody = RequestBody.create(MediaType.parse(StringUtils.isEmpty(mediaType) ? "application/xml; charset=utf-8" : mediaType), xml);
        Request.Builder builder = new Request.Builder();
        builder.url(url).post(requestBody);
        if (MapUtils.isNotEmpty(headParams)) {
            headParams.forEach(builder::addHeader);
        }
        Request request = builder.build();
        Response response = null;
        try {
            OkHttpClient okHttpClient = getHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                log.error(response.message());
            }
        } catch (Exception e) {
            log.debug("post方法url地址{}参数为{},请求头为{}", url, xml, JSON.toJSONString(headParams));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }


    /**
     * 通用执行方法
     */
    private String execute(OkHttp okHttp, OkHttpClient httpClient) {
        if (org.apache.commons.lang3.StringUtils.isBlank(okHttp.requestCharset)) {
            okHttp.requestCharset = DEFAULT_CHARSET;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(okHttp.responseCharset)) {
            okHttp.responseCharset = DEFAULT_CHARSET;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(okHttp.method)) {
            okHttp.method = DEFAULT_METHOD;
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(okHttp.mediaType)) {
            okHttp.mediaType = APPLICATION_JSON;
        }
        if (okHttp.requestLog) {//记录请求日志
            log.info(okHttp.toString());
        }

        String url = okHttp.url;

        Request.Builder builder = new Request.Builder();

        if (MapUtils.isNotEmpty(okHttp.queryMap)) {
            String queryParams = okHttp.queryMap.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("&"));
            url = String.format("%s%s%s", url, url.contains("?") ? "&" : "?", queryParams);
        }
        builder.url(url);

        if (MapUtils.isNotEmpty(okHttp.headerMap)) {
            okHttp.headerMap.forEach(builder::addHeader);
        }

        String method = okHttp.method.toUpperCase();
        String mediaType = String.format("%s;charset=%s", okHttp.mediaType, okHttp.requestCharset);

        if (org.apache.commons.lang3.StringUtils.equals(method, GET)) {
            builder.get();
        } else if (ArrayUtils.contains(new String[]{POST, PUT, DELETE, PATCH}, method)) {
            RequestBody requestBody = RequestBody.create(MediaType.parse(mediaType), okHttp.data);
            builder.method(method, requestBody);
        } else {
            throw new RRException(String.format("http method:%s not support!", method));
        }
        String result;
        try {
            Response response = httpClient.newCall(builder.build()).execute();
            result = response.body().string();
            if (okHttp.responseLog) {//记录返回日志
                log.info(String.format("Got response->%s", result));
            }
        } catch (Exception e) {
            log.error(okHttp.toString(), e);
            return null;
        }
        return result;
    }

    @Builder
    @ToString(exclude = {"requestCharset", "responseCharset", "requestLog", "responseLog"})
    static class OkHttp {
        private String url;
        private String method = DEFAULT_METHOD;
        private String data;
        private String mediaType = APPLICATION_JSON;
        private Map<String, String> queryMap;
        private Map<String, String> headerMap;
        private String requestCharset = DEFAULT_CHARSET;
        private boolean requestLog = DEFAULT_LOG;
        private String responseCharset = DEFAULT_CHARSET;
        private boolean responseLog = DEFAULT_LOG;
    }
}
