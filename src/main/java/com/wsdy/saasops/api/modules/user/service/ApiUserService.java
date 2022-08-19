package com.wsdy.saasops.api.modules.user.service;

import com.wsdy.saasops.api.annotation.CacheDuration;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.api.modules.user.dao.FindPwMapper;
import com.wsdy.saasops.api.modules.user.dto.*;
import com.wsdy.saasops.api.modules.user.entity.FindPwEntity;
import com.wsdy.saasops.api.modules.user.mapper.ApiUserMapper;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.config.MessagesConfig;
import com.wsdy.saasops.modules.agent.dao.AgentAccountMapper;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.system.systemsetting.dto.MailSet;
import com.wsdy.saasops.modules.system.systemsetting.dto.StationSet;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class ApiUserService {

    @Autowired
    ApiSysMapper ApiSysMapper;
    @Autowired
    FindPwMapper findPwMapper;
    @Autowired
    private SendMailSevice sendMailSevice;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private SendSmsSevice sendSmsSevice;
    @Autowired
    private ApiUserMapper apiUserMapper;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private JsonUtil jsonUtil;

    /**
     * 特别注意 如果加缓存 如果在同一个类中调用缓存的方法，缓存是不会起做用
     *
     * @param siteCode
     * @param loginName
     * @return
     */
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_PASS_KEY, key = "#siteCode+'_'+#loginName")
    public LoginVerifyDto queryPassLtdNoCache(String siteCode, String loginName) {
        LoginVerifyDto loginVerifyDto = new LoginVerifyDto();
        loginVerifyDto.setNo(0);
        return loginVerifyDto;
    }

    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_PASS_KEY, key = "#siteCode+'_'+#mobile")
    public LoginVerifyDto queryPassMptdNoCache(String siteCode, String mobile) {
        LoginVerifyDto loginVerifyDto = new LoginVerifyDto();
        loginVerifyDto.setNo(0);
        return loginVerifyDto;
    }

    @CacheEvict(cacheNames = ApiConstants.REIDS_LOGIN_PASS_KEY, key = "#siteCode+'_'+#loginName")
    public void rmPassLtdNoCache(String siteCode, String loginName) {
    }

    public String getKaptcha(HttpSession session, String key) {
        String kaptcha = "";
        if (StringUtils.isEmpty(session.getAttribute(key))) {
            throw new R200Exception("图形验证码已失效!");
        } else {
            kaptcha = session.getAttribute(key).toString();
            //session.removeAttribute(key);
        }
        return kaptcha;
    }

    public String getKaptchaMb(UserDto userDto) {
        Object kaptchaLog = redisService.getRedisValus(RedisConstants.REDIS_MOBILE_REGISTE_CODE + CommonUtil.getSiteCode() + userDto.getMobile());
        if (isNull(kaptchaLog)) {
            throw new R200Exception("短信验证码已经失效！");
        }
        String kaptchaMb = kaptchaLog.toString();
        if (StringUtil.isEmpty(userDto.getMobileCaptchareg())) {
            throw new R200Exception("请重新获取短信验证码！");
        }
        if (!userDto.getMobileCaptchareg().equalsIgnoreCase(kaptchaMb)) {
            throw new R200Exception("短信验证码不正确!");
        }
        return kaptchaMb;
    }


    //账号OFFLINE
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_OFFLINE_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 300)
    public Integer queryLoginOfflineCache(String siteCode, String loginName) {
        return 1;
    }

    //账号OFFLINE更新
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_OFFLINE_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 300)
    public Integer updateLoginOfflineCache(String siteCode, String loginName, Integer value) {
        return value;
    }


    //pt2 token 查询 1个小时(59分钟)
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_PT2TOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 3540)
    public String queryPt2LoginTokenCache(String siteCode, String username) {
        return "";
    }

    //pt2 token 保存(59分钟)
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_PT2TOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 3540)
    public String updatePt2LoginTokenCache(String siteCode, String username, String token) {
        return token;
    }

    //nt token 查询 半个小时(29分钟)
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_NTTOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 1740)
    public NtLoginRes queryNtLoginTokenCache(String siteCode, String username) {
        return null;
    }

    //nt token 保存 半个小时(29分钟)
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_NTTOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 3540)
    public NtLoginRes updateNtLoginTokenCache(String siteCode, String username, NtLoginRes loginRes) {
        return loginRes;
    }

    //Png token 查询 半个小时(59分钟)
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_PNGTOKEN_KEY, key = "#prefix+'_'+#username")
    @CacheDuration(duration = 1740)
    public String queryPngLoginTokenCache(String prefix, String username) {
        return null;
    }

    //nt token 保存 半个小时(59分钟)
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_PNGTOKEN_KEY, key = "#prefix+'_'+#username")
    @CacheDuration(duration = 1740)
    public String updatePngLoginTokenCache(String prefix, String username, String token) {
        return token;
    }

    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    public String queryLoginTokenCache(String siteCode, String loginName) {
        return "";
    }
    
    @Cacheable(cacheNames = ApiConstants.REIDS_AGENT_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    public String queryAgentLoginTokenCache(String siteCode, String loginName) {
    	return "";
    }

    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 30 * 24 * 60 * 60)
    //@CacheDuration(duration = 180)
    public String updateLoginTokenCache(String siteCode, String loginName, String token) {
        return token;
    }
    
    @CachePut(cacheNames = ApiConstants.REIDS_AGENT_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 30 * 24 * 60 * 60)
    public String updateAgentLoginTokenCache(String siteCode, String loginName, String token) {
        return token;
    }

    public void updateLoginTokenCacheListener(String siteCode, String loginName) {
        /*redisService.setRedisExpiredTime(ApiConstants.REIDS_LOGIN_TOKEN_LISTENER_KEY + "_"
                + siteCode + "_" + loginName, loginName, 180, TimeUnit.SECONDS);*/
        redisService.setRedisExpiredTime(ApiConstants.REIDS_LOGIN_TOKEN_LISTENER_KEY + "_"
                + siteCode + "_" + loginName, loginName, 43200 * 2 * 30, TimeUnit.SECONDS);
    }
    
    //代理TOKEN 删除
    @CacheEvict(cacheNames = ApiConstants.REIDS_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    public void rmAgentLoginTokenCache(String siteCode, String loginName) {
    }

    //TOKEN 删除
    @CacheEvict(cacheNames = ApiConstants.REIDS_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    public void rmLoginTokenCache(String siteCode, String loginName) {
    }


    //邮箱或手机验证CODE
    @Cacheable(cacheNames = ApiConstants.REIDS_VFYMAILORMOB_CODE_KEY, key = "#siteCode+'_'+#loginName")
    //@CacheDuration(duration = 1800)//半个小时有效
    public VfyMailOrMobDto queryVfyMailOrMobCodeCache(String siteCode, String loginName) {
        return new VfyMailOrMobDto();
    }

    //邮箱或手机验证CODE 保存
    @CachePut(cacheNames = ApiConstants.REIDS_VFYMAILORMOB_CODE_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 1800)//半个小时有效
    public VfyMailOrMobDto updateVfyMailOrMobCodeCache(String siteCode, String loginName, VfyMailOrMobDto vfyCode) {
        return vfyCode;
    }

    //安全校验短信验证码
    @Cacheable(cacheNames = ApiConstants.REIDS_SECURITY_MOB_CODE_KEY, key = "#siteCode+'_'+#loginName")
    public VfyMailOrMobDto querySecurityMobCodeCache(String siteCode, String loginName) {
        return null;
    }
    //安全校验短信验证码 保存
    @CachePut(cacheNames = ApiConstants.REIDS_SECURITY_MOB_CODE_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 900)//15分钟有效
    public VfyMailOrMobDto updateSecurityMobCode(String siteCode, String loginName, VfyMailOrMobDto vfyCode) {
        return vfyCode;
    }

    @Transactional
    public void saveMailCode(FindPwEntity findPw, String siteCode) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(findPw.getLoginName());
        if (StringUtil.isEmpty(mbrAccount.getEmail()) || mbrAccount.getIsVerifyEmail().equals(Available.disable)) {
            throw new RRException("此账号不能使用邮箱找回密码!");
        }
        String code = sendMail(siteCode, mbrAccount.getEmail(), messagesConfig.getValue("api.fp.email.subject"), messagesConfig.getValue("api.fp.email.content"));
        if (StringUtils.isEmpty(code)) {
            throw new RRException("发送邮件失败!");
        }
        findPw.setVaildCode(code);
        apiUserMapper.insertFindPwd(findPw);
    }

    /**
     * 找回密码发送短信，并保存到Mbr_Retrvpw表
     * @param findPw
     * @return
     */
    @Transactional
    public String saveSmsCode(FindPwEntity findPw, String language) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(findPw.getLoginName());
        if (StringUtil.isEmpty(mbrAccount.getMobile()) || mbrAccount.getIsVerifyMoblie().equals(Available.disable)) {
            throw new RRException("此账号不能使用手机号码找回密码!");
        }
        String code = sendSms(mbrAccount.getMobile(),null, findPw.getMobileAreaCode(), Constants.EVNumber.three, language);
        findPw.setVaildCode(code);
        findPw.setAccountType(1);
        apiUserMapper.insertFindPwd(findPw);
        return code;
    }

    @Transactional
    public boolean validCode(String code, String loginName) {
        FindPwEntity entity = new FindPwEntity();
        entity.setLoginName(loginName);
        entity.setAccountType(1);
        entity = findPwMapper.selectOne(entity);
        if (entity == null) {
            throw new RRException("没有下发验证码!");
        }
        if (entity.getVaildTimes() > 2) {
            throw new RRException("请不要重复尝试,错误的验证码!");
        }
        if (!entity.getVaildCode().equals(code)) {
            entity.setVaildTimes(entity.getVaildTimes() + 1);
            findPwMapper.updateByPrimaryKey(entity);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Transactional
    public boolean modPwd(ModPwdDto modPwdDto, String siteCode) {
        if (validCode(modPwdDto.getCode(), modPwdDto.getLoginName())) {
            MbrAccount info = mbrAccountService.getAccountInfo(modPwdDto.getLoginName());
            String salt = info.getSalt();
            MbrAccount mbrAccount = new MbrAccount();
            mbrAccount.setLoginPwd(new Sha256Hash(modPwdDto.getPassword(), salt).toHex());
            mbrAccount.setId(info.getId());
            mbrAccountService.update(mbrAccount);
            FindPwEntity entity = new FindPwEntity();
            entity.setAccountType(1);
            entity.setLoginName(modPwdDto.getLoginName());
            findPwMapper.delete(entity);

            redisService.del(RedisConstants.REDIS_USER_LOGIN + info.getLoginName().toLowerCase() + "_" + siteCode);
            redisService.del(RedisConstants.REDIS_MOBILE_LOGIN + info.getMobile() + "_" + siteCode);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Transactional
    public String sendVfyMailCode(VfyMailOrMobDto vfyDto, String siteCode, Integer userId) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        if (!StringUtils.isEmpty(mbrAccount.getIsVerifyEmail()) && mbrAccount.getIsVerifyEmail() == Available.enable) {
            throw new RRException("邮箱已验证,请不要重复验证!");
        }
        MbrAccount accountParam = new MbrAccount();
        accountParam.setEmail(vfyDto.getEmail());
        List<MbrAccount> mbrAccount1 = mbrAccountMapper.select(accountParam);
        if (Collections3.isNotEmpty(mbrAccount1)) {
            throw new RRException("此邮箱已绑定，请更换邮箱!");
        }
        return sendMail(siteCode, vfyDto.getEmail(), messagesConfig.getValue("api.fp.vfyemail.subject"), messagesConfig.getValue("api.fp.vfyemail.content"));
    }

    @Transactional
    public String sendVfySmsCode(VfyMailOrMobDto vfyDto, Integer userId, String language) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        // 仅允许前端进行一次绑定
        if (!StringUtils.isEmpty(mbrAccount.getIsVerifyMoblie()) && mbrAccount.getIsVerifyMoblie() == Available.enable) {
            throw new RRException("该会员已经绑定手机号!");
        }
        return sendSms(vfyDto.getMobile(), null, vfyDto.getMobileAreaCode(), Constants.EVNumber.five, language);

    }

    /**
     * 	获取安全码
     * @param vfyDto
     * @param userId
     * @return
     */
    @Transactional
    public String sendSecurityMobCode(VfyMailOrMobDto vfyDto, Integer userId, Integer module, String language) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        if (StringUtils.isEmpty(mbrAccount.getMobile())) {
            throw new RRException("请先绑定手机号！");
        }
        if(!vfyDto.getMobile().equals(mbrAccount.getMobile())){
            throw new RRException("请确认手机号是否正确！");
        }
        return sendSms(vfyDto.getMobile(), null, mbrAccount.getMobileAreaCode(), module, language);

    }

    /**
     * 代理绑定手机号
     * @param agentAccount
     * @param agyId
     * @return
     */
    @Transactional
    public String sendAgentSmsCode(AgentAccount agentAccount, Integer agyId, String language) {
        // 手机号码是否重复校验
        int count = agentAccountMapper.selectCount(agentAccount);
        if (count != 0) {
            throw new RRException("该手机号已被使用，请核对您的手机号!");
        }
        return sendSms(agentAccount.getMobile(), null, null, Constants.EVNumber.five, language);
    }

    /**
     * 
     * 发送校验短信，
     * @param mobile
     * @param oldCode
     * @param mobileAreaCode
     * @param module ： 模块代码 详见 com.wsdy.saasops.modules.system.systemsetting.entity.SmsLog.module
     * @return
     */
    @Transactional
    public String sendVfySmsOneCode(String mobile, String oldCode, String mobileAreaCode, Integer module, String language) {
        List<MbrAccount> mbrAccountList = mbrAccountService.getAccountInfoByMobile(mobile, Constants.EVNumber.one);
        if(Objects.isNull(mbrAccountList) || mbrAccountList.size() == 0){
            throw new RRException("该手机暂未进行绑定校验，请核对您的手机号或使用会员账号+密码登录!");
        }
        // 提示手机存在多个绑定账号(生产存量问题提示)
        if (mbrAccountList.size() > 1) {
            throw new RRException("手机号同时被两个账号绑定，请联系管理员处理！");
        }

        return sendSms(mobile, oldCode, mobileAreaCode, module, language);

    }

    /**
     * 
     * 会员登陆验证码
     * @param mobile
     * @param oldCode
     * @param mobileAreaCode
     * @return
     */
    @Transactional
    public String sendVfySmsOneCodForLbpn(String mobile, String oldCode, String mobileAreaCode, String language) {
        List<MbrAccount> mbrAccountList = mbrAccountService.getAccountInfoByMobile(mobile, Constants.EVNumber.one);

        // 提示手机存在多个绑定账号(生产存量问题提示)
        if (mbrAccountList.size() > 1) {
            throw new RRException("手机号同时被两个账号绑定，请联系管理员处理！");
        }

        return sendSms(mobile, oldCode, mobileAreaCode, Constants.EVNumber.two, language);

    }

    /**
     * 会员手机验证(注册)
     * @param mobile
     * @param mobileAreaCode
     * @return
     */
    @Transactional
    public String sendVfySmsRegCode(String mobile, String mobileAreaCode, String language) {
        MbrAccount mbrAccountInfo = mbrAccountService.getAccountInfoByMobileOne(mobile,null);
        if (Objects.nonNull(mbrAccountInfo) && !StringUtils.isEmpty(mbrAccountInfo.getIsVerifyMoblie()) && mbrAccountInfo.getIsVerifyMoblie() == Available.enable) {
            throw new RRException("该手机号已被使用，请核对您的手机号!");
        }
        return sendSms(mobile, null	, mobileAreaCode, Constants.EVNumber.one, language);

    }

    /**
     * @param mobile
     * @param mobileAreaCode
     * @param  module ： 模块代码 详见 com.wsdy.saasops.modules.system.systemsetting.entity.SmsLog.module
     * @return
     */
    @Transactional
    public String sendAgentSmsRegCode(String mobile, String mobileAreaCode, Integer module, String language) {
        return sendSms(mobile, null	, mobileAreaCode, module, language);
    }

    /**
     * 
     * 发送短信
     * @param mobile
     * @param oldCode
     * @param mobileAreaCode
     * @return
     */
    public String sendSms(String mobile, String oldCode,String mobileAreaCode, Integer module, String language) {
        if (StringUtil.isEmpty(oldCode)) {
            oldCode = CommonUtil.getRandomCode();
        }

        if (Constants.LANGUAGE_VI.equals(language)) {
            // 越南站短信
            sendSmsSevice.vietnameSendSms(mobile, oldCode,false, mobileAreaCode, module);
        } else {
            // 默认中文短信
            sendSmsSevice.sendSms(mobile, oldCode,true, mobileAreaCode, module);
        }
        return oldCode;
    }

    private String sendMail(String siteCode, String mail, String subject, String content) {
        MailSet mailSet = sysSettingService.getMailSet(siteCode);
        StationSet stationSet = sysSettingService.getStation(siteCode);
        String code = CommonUtil.getRandomCode();
        subject = subject.replace("#gameName", stationSet.getWebsiteTitle());
        content = content.replace("#gameName", stationSet.getWebsiteTitle()).replace("#code", code);
        boolean flag = sendMailSevice.sendMail(mailSet, mail, subject, content,null);
        if (flag) {
            return code;
        }
        return null;
    }

    public Double checkRecaptcha(HttpServletRequest request, String token) {
        Map<String,String> data = new HashMap<>(3);
        data.put("secret","6Lee37wUAAAAAJQ0kv-_dZQaE8bsedouJwuSOrWU");
        data.put("response",token);
        data.put("remoteip",CommonUtil.getIpAddress(request));
        String url = "https://www.google.com/recaptcha/api/siteverify";
        String result = okHttpService.postForm(okHttpService.getHttpNoProxyClient(),url,data);
        log.info("谷歌验证返回信息："+result);
        Map<String,Object> respData = jsonUtil.toMap(result);
        return (Double)respData.get("score");
    }

    public int updateOfflineById(Integer accountId){
        return mbrAccountService.updateOfflineById(accountId);
    }

    public MbrAccount getById(Integer accountId){
        return mbrAccountService.getAccountInfo(accountId);
    }
}
