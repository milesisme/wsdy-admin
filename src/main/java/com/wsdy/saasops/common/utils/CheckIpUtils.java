package com.wsdy.saasops.common.utils;

import com.google.gson.Gson;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.modules.member.dto.CheckIpDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CheckIpUtils {
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private RedisService redisService;
    @Value("${check.iplink}")
    private String checkIPLink;
    @Value("${check.xkey}")
    private String xKey;

    /**
     * 获取ip的风控等级
     * 先从redis中查询，不存在则从三方接口查询
     * @param ip            待校验ip
     * @param siteCode      站点code
     * @return 返回风险 0低风险 1高风险 2中风险 10 自定义的异常未查询到等
     */
    public String getCheckIp(String ip,String siteCode){
        log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==start");
        // 入参校验
        if(StringUtils.isEmpty(ip) || StringUtils.isEmpty(siteCode)){
            return Constants.CHECK_IP_FAIL;
        }

        try{
            // 1. redis中有，获取redis中的数据
            String key = RedisConstants.MEMBER_CHECK_IP + siteCode + "_" + ip;
            Object value = redisService.getRedisValus(key);
            if(!Objects.isNull(value)){
                log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==获取redis中的checkIp" );
                String checkIp = value.toString();
                log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==获取redis中的checkIp==" + checkIp);
                return checkIp;
            }

            // 2. 否则查询接口，并存入redis
            Map<String, String> headParams = new HashMap<String, String>(2);
            headParams.put("X-Key", xKey);
            String url = checkIPLink + ip;
            log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==查询" );
            String result = okHttpService.get(okHttpService.getHttpNoProxyClient(), url, headParams);     // 不支持代理服务器查询
            log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==查询==result==" + result);
            if (StringUtil.isEmpty(result)) {
                log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==result返回为空!" );
                return Constants.CHECK_IP_FAIL;
            }

            CheckIpDto checkIpDto = new Gson().fromJson(result, CheckIpDto.class);
            if(Objects.isNull(checkIpDto)){
                log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==checkIpDto返回为空!" );
                return Constants.CHECK_IP_FAIL;
            }
            if(StringUtil.isEmpty(checkIpDto.getBlock())){
                log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==block返回为空!" );
                return Constants.CHECK_IP_FAIL;
            }

            // 3. 存入redis
            String block = checkIpDto.getBlock();
            log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==block==" + block );
            redisService.setRedisExpiredTime(key, checkIpDto.getBlock(), 3, TimeUnit.DAYS);
            log.info("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==block==" + block + "==存入redis" );
            return block;
        }catch (Exception e){
            log.error("getCheckIp==ip==" + ip + "==siteCode==" + siteCode + "==发生异常==" + e );
            return Constants.CHECK_IP_FAIL;
        }
    }
}
