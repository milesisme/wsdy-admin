package com.wsdy.saasops.mt.service;

import com.wsdy.saasops.api.config.ApiConfig;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.utils.pay.MD5Encoder;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.log.dao.LogMbrregisterMapper;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.log.service.LogMbrloginService;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrWallet;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.IpService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.mt.dto.MTResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
@Transactional
public class MTDataService {

    @Autowired
    private MbrAccountService  mbrAccountService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ApiConfig  apiConfig ;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private  MbrWalletService  mbrWalletService;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private IpService ipService;
    @Autowired
    private LogMbrregisterMapper logMbrregisterMapper;
    @Autowired
    private LogMbrloginService logMbrloginService;


    public MTResponseDto register(String cpNumber, String ip){
        String mtDefaultCagencyName = apiConfig.getMtDefaultCagency();
        if(mtDefaultCagencyName == null ||  mtDefaultCagencyName.length() == 0){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_33, "上级代理为开通");
        }
        AgentAccount aa = new AgentAccount();
        aa.setAgyAccount(mtDefaultCagencyName);
        AgentAccount agentAccount =  agentAccountMapper.selectOne(aa);
        if(agentAccount == null){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_33, "上级代理为开通");
        }

        MbrAccount mbrAccount  = mbrAccountService.getMtAccountInfoByMobileOne(cpNumber,null, agentAccount.getId());

        if(mbrAccount != null){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_55, "账号已存在");
        }
        mbrAccount = mbrAccountService.mtRegister(cpNumber, agentAccount, ip);

        logMbrregisterMapper.insert(getlogRegister(mbrAccount, mbrAccount.getLoginSource()));
        return MTResponseDto.response(MTResponseDto.ERROR_CODE_00 ,"成功" );
    }

    public MTResponseDto login(String cpNumber, String siteCode, String ip){
        String mtDefaultCagencyName = apiConfig.getMtDefaultCagency();
        if(mtDefaultCagencyName == null ||  mtDefaultCagencyName.length() == 0){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_33, "上级代理为开通");
        }
        AgentAccount aa = new AgentAccount();
        aa.setAgyAccount(mtDefaultCagencyName);
        AgentAccount agentAccount =  agentAccountMapper.selectOne(aa);
        if(agentAccount == null){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_33, "上级代理为开通");
        }
        MbrAccount mbrAccount  = mbrAccountService.getMtAccountInfoByMobileOne(cpNumber,null, agentAccount.getId());

        if(mbrAccount == null){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_44, "账号不存在");
        }
        String token = apiUserService.queryLoginTokenCache(siteCode, mbrAccount.getLoginName());
        if(token == null || token.length() ==0){
            token = jwtUtils.generateToken(mbrAccount.getId(), mbrAccount.getLoginName(), 2592000* 12);
            apiUserService.updateLoginTokenCache(siteCode, mbrAccount.getLoginName(),token);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        mbrAccount.setLoginIp(ip);
        logMbrloginService.saveLoginLog(mbrAccount);
        return MTResponseDto.response(MTResponseDto.ERROR_CODE_00, "成功", data);
    }

    public MTResponseDto getBalance(String cpNumber){
        String mtDefaultCagencyName = apiConfig.getMtDefaultCagency();
        if(mtDefaultCagencyName == null ||  mtDefaultCagencyName.length() == 0){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_33, "上级代理为开通");
        }
        AgentAccount aa = new AgentAccount();
        aa.setAgyAccount(mtDefaultCagencyName);
        AgentAccount agentAccount =  agentAccountMapper.selectOne(aa);
        if(agentAccount == null){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_33, "上级代理为开通");
        }
        MbrAccount mbrAccount  = mbrAccountService.getMtAccountInfoByMobileOne(cpNumber,null, agentAccount.getId());

        if(mbrAccount == null){
            return  MTResponseDto.response(MTResponseDto.ERROR_CODE_44, "账号不存在");
        }

        MbrWallet mbrWallet = mbrWalletService.getBalance(mbrAccount.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("balance", mbrWallet.getBalance());
        return MTResponseDto.response(MTResponseDto.ERROR_CODE_00, "成功", data);
    }


    public void updateAgentId(Integer accountId, String spreadCode){
        if(spreadCode != null  && spreadCode.length() > 0){
            String mtDefaultCagencyName = apiConfig.getMtDefaultCagency();
            AgentAccount parentAgent = new AgentAccount();
            parentAgent.setAgyAccount(mtDefaultCagencyName);
            AgentAccount agentAccount =  agentAccountMapper.selectOne(parentAgent);
            MbrAccount mbrAccount  = mbrAccountService.getAccountInfo(accountId);
            if((agentAccount.getId().compareTo(mbrAccount.getTagencyId()) ==0 ) && (mbrAccount.getTagencyId().compareTo(mbrAccount.getCagencyId() ) == 0)){
                AgentAccount childAccount = new AgentAccount();
                childAccount.setAgyAccount(spreadCode);
                AgentAccount agentAccount2 =  agentAccountMapper.selectOne(childAccount);
                if(agentAccount2!= null){
                    Integer count = agentMapper.isParent(agentAccount2.getId(), agentAccount.getId());
                    if(count > 0){
                        mbrAccountService.updateCagencyIdByAccountId(mbrAccount.getId(), agentAccount2.getId());
                    }
                }
            }
        }
    }


    public void mtCallBack(String siteCode, Integer accountId, BigDecimal amount, String spreadCode){

        if(spreadCode==null || spreadCode.length() == 0){
            return;
        }
        MbrAccount mbrAccount =  mbrMapper.findMbrLevelAndAgyInfoById(accountId);
        String token = apiUserService.queryAgentLoginTokenCache(siteCode, mbrAccount.getLoginName());
        String url = apiConfig.getMtNotifyDomain() + "/api/public?service=Charge.SdyNotify";
        Map<String, String> params = new HashMap<>();
        params.put("game_tenant_id", "101");
        params.put("uid", accountId.toString());
        params.put("token", token);
        params.put("amount", String.valueOf(amount.intValue()));
        long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", String.valueOf(timestamp));
        String sign = doSign(apiConfig.getMtNotifyKey(), accountId, timestamp);
        params.put("sign",sign);

        log.info("mtnotify==回调蜜桃请求参数:{}", jsonUtil.toJson(params));
        String data = "";
        if (MapUtils.isNotEmpty(params)) {
            data = params.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors
                    .joining("&"));
        }
        if(data.length()> 0){
            url = url + "&"+ data;
        }
        String result = okHttpService.get(okHttpService.getHttpNoProxyClient(), url, null);
        log.info("mtnotify==回调蜜桃结果:{}", StringEscapeUtils.unescapeJava(result));
    }

    private String doSign(String key, Integer uid, long timestamp){
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append(uid);
        builder.append(timestamp);
       return MD5Encoder.encode(builder.toString());
    }

    private LogMbrRegister getlogRegister(MbrAccount mbrAccount, Byte source) {
        LogMbrRegister logRegister = new LogMbrRegister();
        logRegister.setRegisterIp(mbrAccount.getRegisterIp());
        logRegister.setRegisterSource(source);
        logRegister.setRegisterUrl(mbrAccount.getRegisterUrl());
        logRegister.setLoginName(mbrAccount.getLoginName());
        logRegister.setAccountId(mbrAccount.getId());
        logRegister.setCheckip(mbrAccount.getCheckip()); //添加记录
        logRegister.setRegisterTime(getCurrentDate(FORMAT_18_DATE_TIME));
        logRegister.setRegArea(ipService.getIpArea(logRegister.getRegisterIp()));
        return logRegister;
    }


}
