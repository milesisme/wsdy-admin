package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.utils.alicloud.AliCloudApiUtil;
import com.wsdy.saasops.common.utils.tencent.TencentApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
public class IpService {
    @Autowired
    private TencentApiUtil tencentApiUtil;
    @Autowired
    private AliCloudApiUtil aliCloudApiUtil;


    /**
     *  ip获取位置信息_备用
     * @param ip
     * @return
     */
    public String getIpAreaEx(String ip) {
        String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN";
        HttpGet httpGet = new HttpGet(url);
        RequestConfig config = RequestConfig.custom().setConnectTimeout(3000).build();
        httpGet.setConfig(config);
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Host", "ip-api.com");
        httpGet.setHeader("Upgrade-Insecure-Requests", "1");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
        try {
            HttpResponse httpResponse1 = HttpClients.createDefault().execute(httpGet);
            if (httpResponse1.getStatusLine().getStatusCode() == 200) {
                String result = new String(EntityUtils.toString(httpResponse1.getEntity()).getBytes(), StandardCharsets.UTF_8);
                Map<String, Object> objectMap = new JsonUtil().toMap(result);
                String name = objectMap.get("country").toString()
                        + objectMap.get("regionName") + objectMap.get("city");
                return name;
            }
        } catch (Exception e) {
            log.error("getIpArea==ip=" + ip + "==error==",e );
        }
        return null;
    }

    /**
     *  获取ip的位置信息--腾讯云： 不支持ipv6
     * @param ip
     * @return
     */
//    public String getIpArea(String ip) {
//        List<String> lists = Lists.newArrayList();
//        lists.add(ip);
//        String address = tencentApiUtil.getIpArea(lists.toArray(new String[lists.size()]));
//        return address;
//    }

    /**
     *  获取ip的位置信息--阿里云
     * @param ip
     * @return
     */
    public String getIpArea(String ip) {
        if(StringUtils.isEmpty(ip)){
            return null;
        }
        // 先用阿里云接口
        String address = aliCloudApiUtil.getIpArea(ip);
        if(StringUtils.isEmpty(address)){
            // 查无再用另一个接口
            address = getIpAreaEx(ip);
        }
        return address;
    }
}
