package com.wsdy.saasops.common.utils.alicloud;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.geoip.model.v20200101.DescribeIpv4LocationRequest;
import com.aliyuncs.geoip.model.v20200101.DescribeIpv4LocationResponse;
import com.aliyuncs.geoip.model.v20200101.DescribeIpv6LocationRequest;
import com.aliyuncs.geoip.model.v20200101.DescribeIpv6LocationResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.wsdy.saasops.api.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class AliCloudApiUtil {
    @Value("${aliCloud.accesskeyId}")
    private String accesskeyId;     // RAM账号的AccessKey ID
    @Value("${aliCloud.accesskeySecret}")
    private String accesskeySecret; // RAM账号Access Key Secret
    @Value("${aliCloud.regionId}")
    private String regionId;        // 地域ID: ip位置信息只有一个节点
    @Autowired
    private JsonUtil jsonUtil;

    public String getIpArea(String ip) {
        if(StringUtils.isEmpty(ip)){
            return null;
        }
        if(ip.replaceAll("\\d", "").length() == 3){ // ipv4
            return getIpV4Area(ip);
        }else{
            return getIpV6Area(ip);
        }
    }

    public String getIpV4Area(String ip) {
        String result = null;
        try {
            // 获取AcsClient
            IAcsClient client = getDefaultAcsClient();

            // 创建API请求并设置参数
            DescribeIpv4LocationRequest request = new DescribeIpv4LocationRequest();
            request.setIp(ip);

            // 发起请求并处理应答或异常
            Long startTime = System.currentTimeMillis();
            DescribeIpv4LocationResponse resp = client.getAcsResponse(request);
            log.info("getIpArea==ip==" + ip + "time==" +(System.currentTimeMillis()-startTime) +"==resp==" + jsonUtil.toJson(resp));
            if(Objects.nonNull(resp)){
                result = resp.getCountry() + resp.getProvince() + resp.getCity() ;
            }

        } catch (ClientException e) {
            log.error("getIpArea==ip=" + ip + "==ErrCode==" + e.getErrCode() + "==ErrMsg==" + e.getErrMsg() + "==RequestId==" + e.getRequestId() );
        } catch (Exception e){
            log.error("getIpArea==ip=" + ip + "==e==" + e);
        }
        return result;
    }

    public String getIpV6Area(String ip) {
        String result = null;
        try {
            // 获取AcsClient
            IAcsClient client = getDefaultAcsClient();

            // 创建API请求并设置参数
            DescribeIpv6LocationRequest request = new DescribeIpv6LocationRequest();
            request.setIp(ip);

            // 发起请求并处理应答或异常
            Long startTime = System.currentTimeMillis();
            DescribeIpv6LocationResponse resp = client.getAcsResponse(request);
            log.info("getIpArea==ip==" + ip + "time==" +(System.currentTimeMillis()-startTime) +"==resp==" + jsonUtil.toJson(resp));
            if(Objects.nonNull(resp)){
                result = resp.getCountry() + resp.getProvince() + resp.getCity() ;
            }
        } catch (ClientException e) {
            log.error("getIpArea==ip=" + ip + "==ErrCode==" + e.getErrCode() + "==ErrMsg==" + e.getErrMsg() + "==RequestId==" + e.getRequestId() );
        } catch (Exception e){
            log.error("getIpArea==ip=" + ip + "==e==" + e);
        }
        return result;
    }

    private IAcsClient getDefaultAcsClient(){
        DefaultProfile profile = DefaultProfile.getProfile( regionId,accesskeyId, accesskeySecret);
        // 启动日志功能,传入接口对象
        profile.setLogger(log);
        IAcsClient client = new DefaultAcsClient(profile);
        return client;
    }

    public static void main(String[] args) {
        AliCloudApiUtil util = new AliCloudApiUtil();
        String address = util.getIpArea("240e:398:18c3:6e10:3454:f0e9:246a:c85b");
        System.out.println(address);
    }
}

