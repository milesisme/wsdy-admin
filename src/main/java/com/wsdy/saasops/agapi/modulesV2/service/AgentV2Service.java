package com.wsdy.saasops.agapi.modulesV2.service;


import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2ListDto;
import com.wsdy.saasops.agapi.modulesV2.mapper.AgentV2Mapper;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Slf4j
@Service
@Transactional
public class AgentV2Service {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AgentV2Mapper agentAccountMapper;
    @Autowired
    private AgentV2LoginLogService agentV2LoginLogService;
    @Autowired
    private AgentV2AccountLogService agentV2AccountLogService;

    public Map<String, Object> agentAccountLogin(AgentAccount account, String siteCode, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>(8);
        AgentAccount agyAccount = checkoutAvailable(null, account.getAgyAccount());
        if (isNull(agyAccount)) {
            throw new R200Exception("账号不存在或异常!");
        }
        if (Constants.EVNumber.zero == agyAccount.getAvailable()) {
            throw new R200Exception("对不起，帐号已禁用，请联系在线客服");
        }
        String logAgyPwd = new Sha256Hash(account.getAgyPwd(), agyAccount.getSalt()).toHex();
        if (!agyAccount.getAgyPwd().equals(logAgyPwd)) {
            throw new R200Exception("密码有误,请重新输入!");
        }

        // 异步保存登录记录
        agyAccount.setLoginIp(CommonUtil.getIpAddress(request));    // 登入IP
        agyAccount.setRegisterUrl(IpUtils.getUrl(request));         // 登入url
        asyncLogoInfo(agyAccount,siteCode);

        // 账户信息
        map.put("agyAccountId", agyAccount.getId());      // 账户id
        map.put("parentId", agyAccount.getParentId());    // 父节点
        map.put("agentType", agyAccount.getAgentType());  // 代理类别
        map.put("agyAccount", agyAccount.getAgyAccount());// 代理名
        map.put("available", agyAccount.getAvailable());  // 账号状态
        map.put("balance", agyAccount.getBalance());      // 代理点数
        map.put("bettingStatus", agyAccount.getBettingStatus());// 投注状态 1开启，0关闭
        map.put("realpeople", agyAccount.getRealpeople());// 真人分成
        map.put("electronic", agyAccount.getElectronic());// 电子分成
        map.put("realpeoplewash", agyAccount.getRealpeoplewash());// 真人洗码佣金比例
        map.put("electronicwash", agyAccount.getElectronicwash());// 电子洗码佣金比例
        String agentToken = jwtUtils.agentGenerateToken(String.valueOf(agyAccount.getId()));
        map.put("agentToken", agentToken);

        return map;
    }

    public void asyncLogoInfo(AgentAccount agyAccount, String siteCode) {
        CompletableFuture future = CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            //设置之前未有登出的登出时间
            agentV2LoginLogService.setLoginOffTime(agyAccount.getAgyAccount());
            // 保存登入记录
            agentV2LoginLogService.saveLoginLog(agyAccount);
        });
        try {
            future.get();
        } catch (Exception e) {
            log.error("AgentV2Service==asyncLogoInfo==error==", e);
        }
    }

    public AgentAccount checkoutAvailable(Integer accountId, String agyAccount) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(agyAccount);
        agentAccount.setId(accountId);
        AgentAccount account = agentAccountMapper.getAgentInfo(agentAccount);
        return account;
    }

    public AgentAccount agyAccountInfo(AgentAccount account) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setId(account.getId());
        account = agentAccountMapper.getAgentInfo(agentAccount);
//        accountDto.setMobile(AgentStringUtil.replacePhone(accountDto.getMobile()));
//        accountDto.setEmail(AgentStringUtil.replaceEmail(accountDto.getEmail()));
//        accountDto.setQq(AgentStringUtil.replaceQq(accountDto.getQq()));
//        accountDto.setWeChat(AgentStringUtil.replaceWeChat(accountDto.getWeChat()));
//        accountDto.setRealName(AgentStringUtil.replaceRealName(accountDto.getRealName()));
        return account;
    }

    public int bindAgentMobile(AgentAccount agentAccount) {
        int ret = agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
        // 插入账户记录
        agentV2AccountLogService.bindAgentMobile(agentAccount);
        return ret;
    }

    public void updateAgentPassword(AgentAccount agentAccount, AgentAccount loginAccount) {
        AgentAccount account = agentAccountMapper.selectByPrimaryKey(agentAccount.getId());
        if (nonNull(account)) {
            String salt = account.getSalt();
            String oldAgyPwd = new Sha256Hash(agentAccount.getOldAgyPwd(), salt).toHex();
            String agyPwd = new Sha256Hash(agentAccount.getAgyPwd(), salt).toHex();
            if (!account.getAgyPwd().equals(oldAgyPwd)) {
                throw new R200Exception("旧密码错误");
            }
            if (oldAgyPwd.equals(agyPwd)) {
                throw new R200Exception("新密码不能跟旧密码相同");
            }
            account.setAgyPwd(agyPwd);
            account.setModifyUser(loginAccount.getAgyAccount() );
            account.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            agentAccountMapper.updateByPrimaryKeySelective(account);

            // 插入账户记录
            agentV2AccountLogService.updateAgentPassword(account,loginAccount);
        }
    }

    public List<AgentV2ListDto> search(AgentAccount agentAccount) {
        // 判断是会员还是代理
        List<AgentV2ListDto>  judgeList = agentAccountMapper.judgeMbrOrAgent(agentAccount);
        if(Objects.isNull(judgeList)){  // 查无改账号或不属于登录账户线下的
            return new ArrayList<AgentV2ListDto>();
        }
        boolean isMbr = false;
        for(AgentV2ListDto dto : judgeList){
            if("ACCOUNT".equals(dto.getUserCode())){
                isMbr = true;
            }
        }
        if(isMbr){  // 会员
            return agentAccountMapper.getSearchUserMbr(agentAccount);
        }else{      // 代理
            return agentAccountMapper.getSearchUserAgent(agentAccount);
        }
    }
}
