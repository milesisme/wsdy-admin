package com.wsdy.saasops.common.utils.tencent;

import com.beust.jcommander.internal.Lists;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vpc.v20170312.VpcClient;
import com.tencentcloudapi.vpc.v20170312.models.DescribeIpGeolocationInfosRequest;
import com.tencentcloudapi.vpc.v20170312.models.DescribeIpGeolocationInfosResponse;
import com.tencentcloudapi.vpc.v20170312.models.IpField;
import com.tencentcloudapi.vpc.v20170312.models.IpGeolocationInfo;
import com.wsdy.saasops.api.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TencentApiUtil {
//    @Value("${tencent.secretId}")
    private String secretId;
//    @Value("${tencent.secretKey}")
    private String secretKey;
//    @Value("${tencent.region}")
    private String region;
    @Autowired
    private JsonUtil jsonUtil;

    public String getIpArea(String[] ips) {
        String ip = null;
        try {
            // 获取产品对象
            VpcClient client = getVpcClient();
            // 实例请求对象创建设置
            DescribeIpGeolocationInfosRequest req = new DescribeIpGeolocationInfosRequest();
            req.setAddressIps(ips);
            IpField ipField = new IpField();
            ipField.setProvince(true);
            ipField.setCountry(true);
            ipField.setCity(true);
            req.setFields(ipField);

            // 请求
            Long startTime = System.currentTimeMillis();
            DescribeIpGeolocationInfosResponse resp = client.DescribeIpGeolocationInfos(req);
            log.info("getIpArea==time==" +(System.currentTimeMillis()-startTime));
            // 处理返回结果
            if(resp.getTotal() > 0){
                IpGeolocationInfo[] address = resp.getAddressInfo();
                IpGeolocationInfo info = address[0];
                ip = getString(info.getCountry())
                        + getString(info.getProvince())
                        + getString(info.getCity());
            }

            log.info(DescribeIpGeolocationInfosResponse.toJsonString(resp));
        }catch (Exception e){
            log.error("getIpArea==ips=" + jsonUtil.toJson(ips) + "==e==" + e);
        }
        return ip;
    }

    private  String getString(String string){
        return StringUtils.isEmpty(string) || "未知".equals(string) ? "": string;
    }

    private VpcClient getVpcClient(){
        // 实例化一个认证对象
        Credential cred = new Credential(secretId, secretKey);

        // 配置产品对象
        ClientProfile clientProfile = getClientProfile();

        // 实例化产品对象,clientProfile是可选的
        VpcClient client = new VpcClient(cred,region,clientProfile);

        return client;
    }

    public static ClientProfile getClientProfile(){
        // 实例化一个http选项，可选的
        HttpProfile httpProfile = new HttpProfile();
        // httpProfile.setProxyHost("真实代理ip");
        // httpProfile.setProxyPort(真实代理端口);
//            httpProfile.setReqMethod("GET"); // get请求(默认为post请求)
//            httpProfile.setProtocol("https://");  // 在外网互通的网络环境下支持http协议(默认是https协议),请选择(https:// or http://)
//            httpProfile.setConnTimeout(30); // 请求连接超时时间，单位为秒(默认60秒)
//            httpProfile.setWriteTimeout(30);  // 设置写入超时时间，单位为秒(默认0秒)
//            httpProfile.setReadTimeout(30);  // 设置读取超时时间，单位为秒(默认0秒)
//            httpProfile.setEndpoint("vpc.tencentcloudapi.com"); // 指定接入地域域名(默认就近接入)
        // 实例化一个client选项，可选的
        ClientProfile clientProfile = new ClientProfile();
//            clientProfile.setSignMethod("HmacSHA256"); // 指定签名算法(默认为HmacSHA256)
        // 自3.1.80版本开始，SDK 支持打印日志。
        clientProfile.setHttpProfile(httpProfile);
        // 在创建 CLientProfile 对象时，设置 debug 模式为真,会打印sdk异常信息和流量信息
        clientProfile.setDebug(true);
//            // 从3.1.16版本开始，支持设置公共参数 Language, 默认不传，选择(ZH_CN or EN_US)
//            clientProfile.setLanguage(Language.EN_US);

        return clientProfile;
    }

    public static void main(String[] args) {
        TencentApiUtil util = new TencentApiUtil();
        List<String> lists = Lists.newArrayList();
//        lists.add("182.18.206.31");
        lists.add("240e:398:18c3:6e10:3454:f0e9:246a:c85b");
        String address = util.getIpArea(lists.toArray(new String[lists.size()]));
        System.out.println(address);
    }
}

