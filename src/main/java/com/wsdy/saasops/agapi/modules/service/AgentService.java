package com.wsdy.saasops.agapi.modules.service;

import static com.wsdy.saasops.modules.sys.service.SysEncryptionService.PREFIX_SPLICE;
import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beust.jcommander.internal.Lists;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.agapi.modules.dto.AgentAccountDto;
import com.wsdy.saasops.agapi.modules.mapper.AgapiMapper;
import com.wsdy.saasops.agapi.modules.utils.AgentStringUtil;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.api.modules.user.dao.FindPwMapper;
import com.wsdy.saasops.api.modules.user.dto.ModPwdDto;
import com.wsdy.saasops.api.modules.user.dto.PromotionUrlDto;
import com.wsdy.saasops.api.modules.user.entity.FindPwEntity;
import com.wsdy.saasops.api.modules.user.mapper.ApiUserMapper;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.SendSmsSevice;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.EncryptioUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.dao.AgyDomainMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.mapper.AgentMapper;
import com.wsdy.saasops.modules.agent.service.CommissionService;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.SysEncrypt;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.dto.PromotionSet;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AgentService  extends BaseService<AgentMapper, AgentAccount> {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgapiMapper agapiMapper;
    @Autowired
    private CommissionService commissionService;
    @Autowired
    private MbrAccountService accountService;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private AgyDomainMapper agyDomainMapper;
    @Autowired
    private SysSettingMapper sysSettingMapper;
    @Autowired
    ApiSysMapper apiSysMapper;
    @Autowired
    private SendSmsSevice sendSmsSevice;
    @Autowired
    private FindPwMapper findPwMapper;
    
    @Autowired
    private ApiUserService apiUserService;
    
    @Autowired
    private ApiUserMapper apiUserMapper;

    private static final String registerH5 = "/register.html?agentId=";
    private static final String registerPC = "/registerSuc.php?agentId=";

    public Map<String, Object> agentAccountLogin(AgentAccount account) {
        Map<String, Object> map = new HashMap<>(8);
        AgentAccount account1 = checkoutAvailable(null, account.getAgyAccount());
        if (isNull(account1)) {
            throw new R200Exception("??????????????????????????????!");
        }
        if (Constants.EVNumber.two == account1.getStatus()) {
            throw new R200Exception("?????????????????????");
        }
        if (Constants.EVNumber.zero == account1.getAvailable()) {
            throw new R200Exception("???????????????????????????????????????????????????");
        }
        String logAgyPwd = new Sha256Hash(account.getAgyPwd(), account1.getSalt()).toHex();
        if (!account1.getAgyPwd().equals(logAgyPwd)) {
            throw new R200Exception("????????????,???????????????!");
        }
        if (account1.getParentId() != null && account1.getParentId() == 0) {
            map.put("agentType", "?????????");
        } else {
            map.put("agentType", "??????");
        }
        map.put("agyAccount", account1.getAgyAccount());
        map.put("agyAccountId", account1.getId());
        map.put("parentId", account1.getParentId());
        String agentToken = jwtUtils.agentGenerateToken(String.valueOf(account1.getId()));
        map.put("agentToken", agentToken);
        return map;
    }

    public AgentAccount checkoutAvailable(Integer accountId, String agyAccount) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(agyAccount);
        agentAccount.setId(accountId);
        AgentAccount account = agentAccountMapper.selectOne(agentAccount);
        return account;
    }

    public AgentAccountDto agyAccountInfo(AgentAccount account) {
        AgentAccountDto accountDto = agapiMapper.findAccountInfo(account.getId());
        accountDto.setMobile(AgentStringUtil.replacePhone(accountDto.getMobile()));
        accountDto.setEmail(AgentStringUtil.replaceEmail(accountDto.getEmail()));
        accountDto.setQq(AgentStringUtil.replaceQq(accountDto.getQq()));
        accountDto.setWeChat(AgentStringUtil.replaceWeChat(accountDto.getWeChat()));
        accountDto.setRealName(AgentStringUtil.replaceRealName(accountDto.getRealName()));
        return accountDto;
    }

    public int sendMobCode(AgentAccount agentAccount) {
        return agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
    }

    public void agyDomainSave(AgyDomain agyDomain, AgentAccount account) {
        agyDomain.setAccountId(account.getId());
        agyDomain.setAgyAccount(account.getAgyAccount());
        commissionService.domainSave(agyDomain, account.getAgyAccount());
    }

    public PageUtils agyDomainList(Integer pageNo, Integer pageSize, AgentAccount account) {
        AgyDomain agyDomain = new AgyDomain();
        agyDomain.setAccountId(account.getId());
        return commissionService.agentDomainList(agyDomain, pageNo, pageSize);
    }

    public void addMbrAccount(MbrAccount account, AgentAccount agentAccount) {
        accountService.adminSave(account, agentAccount, null, null, Boolean.FALSE, Constants.EVNumber.five);
    }

    public void agyAccountSave(AgentAccount agentAccount, AgentAccount account) {
       /* agentAccount.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentAccount.setCreateUser(account.getAgyAccount());
        agentAccount.setParentId(account.getId());
        agentAccountService.agyAccountSave(agentAccount, Constants.EVNumber.zero);*/
    }

    public void agyAccountUpdate(AgentAccount agentAccount) {
        agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
    }

    public void agyUpdateAvailable(AgentAccount agentAccount) {
        agentAccountMapper.updateByPrimaryKeySelective(agentAccount);
    }

    public List<AgyDomain> queryAllDomains() {
        return agentMapper.queryAllDomains();
    }

    public PageUtils findNoBaseDomains(Integer id, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        AgyDomain agyDomainParam = new AgyDomain();
        agyDomainParam.setAccountId(id);
        agyDomainParam.setStatus(1); //????????????
        agyDomainParam.setIsDel(1);
        List<AgyDomain> list = agyDomainMapper.select(agyDomainParam);
        return BeanUtil.toPagedResult(list);
    }

    public PromotionUrlDto findBaseDomains(Integer id, String siteCode) {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        PromotionSet promotionSet = new PromotionSet();
        if (Collections3.isNotEmpty(settingList)) {
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.AGENT_PROMOTION.equals(sysSetting.getSyskey())) {
                    promotionSet.setSiteUrlId(sysSetting.getSysvalue().isEmpty() ? null : Integer.parseInt(sysSetting.getSysvalue()));
                }
            });
        }

        if (Objects.isNull(promotionSet.getSiteUrlId())) {
            return null;
        }
        ;
        TcpSiteurl siteurl = new TcpSiteurl();
        siteurl.setSiteCode(siteCode);
        List<TcpSiteurl> siteUrlList = apiSysMapper.findCpSiteUrlBySiteCode(siteurl);
        if (Collections3.isEmpty(siteUrlList)) {
            return null;
        }
        ;
        String domain = "";
        for (TcpSiteurl tcpSiteurl : siteUrlList) {
            if (promotionSet.getSiteUrlId().equals(tcpSiteurl.getId())) {
                domain = tcpSiteurl.getSiteUrl();
            }
        }
        if (domain.isEmpty()) {
            return null;
        }
        ;
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(id);
        PromotionUrlDto promotionUrlDto = new PromotionUrlDto();
        promotionUrlDto.setPromotionUrl(domain + registerPC + agentAccount.getSpreadCode());
        promotionUrlDto.setPromotionH5Url(domain + registerH5 + agentAccount.getSpreadCode());
        return promotionUrlDto;
    }

    // ?????????????????????
    public boolean isTagency(AgentAccount account) {
        return agapiMapper.isTagency(account.getId());
    }

    public PageUtils accountAuditDetail(String loginName, String date, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundAudit> list = agentMapper.accountAuditDetail(loginName,date);
        PageUtils p = BeanUtil.toPagedResult(list);
        return BeanUtil.toPagedResult(list);
    }
    
    /**
     * ???????????????????????????????????????Mbr_Retrvpw???
     * @param findPw
     * @return
     */
    @Transactional
    public String saveSmsCode(FindPwEntity findPw) {
    	
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgyAccount(findPw.getLoginName());
        AgentAccount queryObjectCond = this.queryObjectCond(agentAccount);
        
        if (StringUtil.isEmpty(queryObjectCond.getMobile())) {
            throw new RRException("?????????????????????????????????????????????!");
        }
        String code = sendSms(queryObjectCond.getMobile(),null, findPw.getMobileAreaCode(), Constants.EVNumber.three);
        findPw.setVaildCode(code);
        findPw.setAccountType(2);
        findPw.setVaildType(new Byte("1"));
        apiUserMapper.insertFindPwd(findPw);
        return code;
    }
    
    /**
     * 
     * ????????????
     * @param mobile
     * @param oldCode
     * @param mobileAreaCode
     * @return
     */
    public String sendSms(String mobile, String oldCode,String mobileAreaCode, Integer module) {
        if (StringUtil.isEmpty(oldCode)) {
            oldCode = CommonUtil.getRandomCode();
        }
        sendSmsSevice.sendSms(mobile, oldCode,true, mobileAreaCode, module);
        return oldCode;
    }
    

    /**
     * 	??????????????????
     * @param modPwdDto
     * @param siteCode
     * @return
     */
    @Transactional
    public boolean modPwd(ModPwdDto modPwdDto, String siteCode) {
        if (validCode(modPwdDto.getCode(), modPwdDto.getLoginName())) {
        	
    	    AgentAccount agentAccount = new AgentAccount();
            agentAccount.setAgyAccount(modPwdDto.getLoginName());
            AgentAccount info = this.queryObjectCond(agentAccount);
            
            String salt = info.getSalt();
            AgentAccount agentAccountUp = new AgentAccount();
            agentAccountUp.setAgyPwd(new Sha256Hash(modPwdDto.getPassword(), salt).toHex());
            agentAccountUp.setId(info.getId());
            this.update(agentAccountUp);
            
            FindPwEntity entity = new FindPwEntity();
            entity.setLoginName(modPwdDto.getLoginName());
            entity.setAccountType(2);
            findPwMapper.delete(entity);

            // ??????token
            apiUserService.rmAgentLoginTokenCache(CommonUtil.getSiteCode(), modPwdDto.getLoginName());

            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    @Transactional
    public boolean validCode(String code, String loginName) {
        FindPwEntity entity = new FindPwEntity();
        entity.setLoginName(loginName);
        entity.setAccountType(2);
        entity = findPwMapper.selectOne(entity);
        if (entity == null) {
            throw new RRException("?????????????????????!");
        }
        if (entity.getVaildTimes() > 2) {
            throw new RRException("?????????????????????,??????????????????!");
        }
        if (!entity.getVaildCode().equals(code)) {
            entity.setVaildTimes(entity.getVaildTimes() + 1);
            findPwMapper.updateByPrimaryKey(entity);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    /**
     * ??????????????? ????????????
     * 	
     * @param mobile
     * @param isVerifyMoblie
     * @return
     */
    public List<AgentAccount> getAgyInfoByMobile(String mobile) {
    	 AgentAccount agentAccount = new AgentAccount();
         agentAccount.setMobile(mobile);
         return this.queryListCond(agentAccount);
    }
    
}
