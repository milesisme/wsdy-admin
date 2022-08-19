package com.wsdy.saasops.api.utils;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.common.exception.RRException;
import lombok.Builder;
import lombok.ToString;
import okhttp3.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class OkHttpUtils {

    private final static Logger log = LoggerFactory.getLogger(OkHttpUtils.class);

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

    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

    private final static OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(120, 10, TimeUnit.MINUTES))
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS).build();

    public OkHttpClient getHttpClientSSL() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);
        return setHttpSSL(builder).build();
    }

    private OkHttpClient.Builder setHttpSSL(OkHttpClient.Builder builder) {
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
        return builder;
    }

   /* public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        execute(OkHttp.builder().url("http://127.0.0.1:8080/api/transfer/TEST").method(POST).data("")
                .requestLog(true).responseLog(true).build());
    }*/

    public static String postGateWayJson(String url, Object obj, Map<String, String> stringMap) {
        return execute(OkHttp.builder().url(url).method(POST).data(JSON.toJSONString(obj)).mediaType(APPLICATION_JSON).headerMap(stringMap).build());
    }

    public static String get(OkHttpClient httpClient, String url) {
        return execute(OkHttp.builder().url(url).build());
    }


    public static InputStream getInputStream(String url) {
        return executeByteStream(OkHttp.builder().url(url).build());
    }

    /**
     * GET请求
     *
     * @param url URL地址
     * @return
     */
    public static String get(String url) {
        return execute(OkHttp.builder().url(url).build());
    }

    /**
     * GET请求
     *
     * @param url URL地址
     * @return
     */
    public static String get(String url, String charset) {
        return execute(OkHttp.builder().url(url).responseCharset(charset).build());
    }

    /**
     * 带查询参数的GET查询
     *
     * @param url      URL地址
     * @param queryMap 查询参数
     * @return
     */
    public static String get(String url, Map<String, String> queryMap) {
        return execute(OkHttp.builder().url(url).queryMap(queryMap).build());
    }

    /**
     * 带查询参数的GET查询
     *
     * @param url      URL地址
     * @param queryMap 查询参数
     * @return
     */
    public static String get(String url, Map<String, String> queryMap, String charset) {
        return execute(OkHttp.builder().url(url).queryMap(queryMap).responseCharset(charset).build());
    }


    /**
     * POST
     * application/x-www-form-urlencoded
     *
     * @param url
     * @param formMap
     * @return
     */
    public static String postForm(String url, Map<String, String> formMap) {
        String data = "";
        if (MapUtils.isNotEmpty(formMap)) {
            data = formMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors
                    .joining("&"));
        }
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(APPLICATION_FORM_URLENCODED).build());
    }

    /**
     * 通用执行方法
     */
    private static String execute(OkHttp okHttp) {
        if (StringUtils.isBlank(okHttp.requestCharset)) {
            okHttp.requestCharset = DEFAULT_CHARSET;
        }
        if (StringUtils.isBlank(okHttp.responseCharset)) {
            okHttp.responseCharset = DEFAULT_CHARSET;
        }
        if (StringUtils.isBlank(okHttp.method)) {
            okHttp.method = DEFAULT_METHOD;
        }
        if (StringUtils.isBlank(okHttp.mediaType)) {
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

        if (StringUtils.equals(method, GET)) {
            builder.get();
        } else if (ArrayUtils.contains(new String[]{POST, PUT, DELETE, PATCH}, method)) {
            RequestBody requestBody = RequestBody.create(MediaType.parse(mediaType), okHttp.data);
            builder.method(method, requestBody);
        } else {
            throw new RRException(String.format("http method:%s not support!", method));
        }
        String result;
        try {
            Response response = client.newCall(builder.build()).execute();
            result = response.body().string();
            if (okHttp.responseLog) {//记录返回日志
                log.info(String.format("Got response->%s", result));
            }
            response.close();
        } catch (Exception e) {
            log.error(okHttp.toString() + "okhttp1", e);
            return null;
        }
        return result;
    }

    /**
     * 通用执行方法 获取流
     */
    private static InputStream executeByteStream(OkHttp okHttp) {
        if (StringUtils.isBlank(okHttp.requestCharset)) {
            okHttp.requestCharset = DEFAULT_CHARSET;
        }
        if (StringUtils.isBlank(okHttp.responseCharset)) {
            okHttp.responseCharset = DEFAULT_CHARSET;
        }
        if (StringUtils.isBlank(okHttp.method)) {
            okHttp.method = DEFAULT_METHOD;
        }
        if (StringUtils.isBlank(okHttp.mediaType)) {
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

        if (StringUtils.equals(method, GET)) {
            builder.get();
        } else if (ArrayUtils.contains(new String[]{POST, PUT, DELETE, PATCH}, method)) {
            RequestBody requestBody = RequestBody.create(MediaType.parse(mediaType), okHttp.data);
            builder.method(method, requestBody);
        } else {
            throw new RRException(String.format("http method:%s not support!", method));
        }
        InputStream result;
        try {
            Response response = client.newCall(builder.build()).execute();
            result = response.body().byteStream();
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
