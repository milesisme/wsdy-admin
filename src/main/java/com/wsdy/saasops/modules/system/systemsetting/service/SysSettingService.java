package com.wsdy.saasops.modules.system.systemsetting.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.wsdy.saasops.modules.system.systemsetting.dto.*;
import com.wsdy.saasops.modules.system.systemsetting.vo.BroadcastVo;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.wsdy.saasops.api.modules.user.service.SendMailSevice;
import com.wsdy.saasops.api.modules.user.service.SendSmsSevice;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.QiNiuYunUtil;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.dto.SettingAgentDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.system.systemsetting.dao.SmsConfigMapper;
import com.wsdy.saasops.modules.system.systemsetting.dao.StationSetMapper;
import com.wsdy.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsConfig;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting.SysValueConst;
import tk.mybatis.mapper.entity.Example;

@Service("sysSettingService")
@Transactional
public class SysSettingService {

    @Autowired
    private SysSettingMapper sysSettingMapper;
    @Autowired
    private SendSmsSevice sendSmsSevice;
    @Autowired
    private SendMailSevice sendMailSevice;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private StationSetMapper stationSetMapper;
    @Autowired
    private ApiSysMapper apiSysMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private SmsConfigMapper smsConfigMapper;


    public SysSetting queryObject(String syskey) {
        return sysSettingMapper.selectByPrimaryKey(syskey);
    }

    // 获取会员注册设置
    public List<SysSetting> getRegisterInfoList() {

        List<SysSetting> list = sysSettingMapper.selectAll();
        List<SysSetting> ssList = new ArrayList<>();
        List<String> keys = Lists.newArrayList(SystemConstants.MEMBER_ACCOUNT, SystemConstants.MEMBER_LOGIN_PASSWORD,
                SystemConstants.MEMBER_REPEATED_PASSWORD, SystemConstants.MEMBER_VERIFICATION_CODE,
                SystemConstants.MEMBER_REAL_NAME, SystemConstants.MEMBER_TELPHONE, SystemConstants.MEMBER_TELPHONE_CODE,
                SystemConstants.MEMBER_EMAIL, SystemConstants.MEMBER_QQ, SystemConstants.MEMBER_WECHAT, SystemConstants.MEMBER_ADDRESS,
                SystemConstants.SMS_MOBILE_COMPEL_BIND, SystemConstants.MEMBER_WEB_REGISTER, SystemConstants.MEMBER_WEB_REGISTER_METHOD,
                SystemConstants.MEMBER_IP,SystemConstants.MEMBER_DEVICE, SystemConstants.MEMBER_PROMOTION);
        if (list != null) {
            for (SysSetting ss : list) {
                if (keys.contains(ss.getSyskey())) {
                    ssList.add(ss);
                }
            }
        }
        return ssList;
    }

    public LinkedHashMap<String, Object> getRegisterInfoMap() {
        List<SysSetting> ssList = getRegisterInfoList();
        LinkedHashMap<String, Object> map = getMap(ssList);
        return map;
    }

    public LinkedHashMap<String, Object> getMap(List<SysSetting> ssList) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        if (ssList != null) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.SMS_MOBILE_COMPEL_BIND.equals(ss.getSyskey())) {
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISREQUIRE, SysValueConst.none.equals(ss.getSysvalue()) ? false : true);
                } else if (SystemConstants.MEMBER_WEB_REGISTER.equals(ss.getSyskey())) {  // 是否允许前台注册 0不允许 1允许
                    map.put(ss.getSyskey(), SysValueConst.none.equals(ss.getSysvalue()) ? false : true);
                } else if (SystemConstants.MEMBER_WEB_REGISTER_METHOD.equals(ss.getSyskey())) {  // 注册方式 0 普通注册(false) 1 普通注册+快捷模式(true) 2 快捷模式
                    map.put(ss.getSyskey(), ss.getSysvalue());
                } else if (SysValueConst.none.equals(ss.getSysvalue())) {
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISVISIBLE, false);
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISREQUIRE, false);
                } else if (SysValueConst.visible.equals(ss.getSysvalue())) {
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISVISIBLE, true);
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISREQUIRE, false);
                } else if (SysValueConst.require.equals(ss.getSysvalue())) {
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISVISIBLE, true);
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISREQUIRE, true);
                }
            }
        }
        return map;
    }

    public void save(SysSetting sysSetting) {
        sysSettingMapper.insert(sysSetting);
    }

    public void update(SysSetting sysSetting) {
        sysSettingMapper.updateByPrimaryKeySelective(sysSetting);
    }

    public void delete(String syskey) {
        sysSettingMapper.deleteByPrimaryKey(syskey);
    }

    // 保存或更新用户条款
    public void modifyOrUpdate(List<SysSetting> ssList) {
        if (ssList != null) {
            for (SysSetting ss : ssList) {
                if (null != ss.getSyskey() && null != ss.getSysvalue()) {
                    SysSetting sys = new SysSetting();
                    sys.setSyskey(ss.getSyskey());
                    SysSetting s01 = sysSettingMapper.selectOne(sys);
                    if (null == s01) {
                        sysSettingMapper.insert(ss);
                    } else {
                        sysSettingMapper.updateByPrimaryKeySelective(ss);
                    }
                }
                if (null != ss.getWebsiteTerms() && null != ss.getSyskey()) {
                    SysSetting sys = new SysSetting();
                    sys.setSyskey(ss.getSyskey());
                    SysSetting s01 = sysSettingMapper.selectOne(sys);
                    if (null == s01) {
                        sysSettingMapper.insert(ss);
                    } else {
                        sysSettingMapper.updateByPrimaryKeySelective(ss);
                    }
                }
            }
        }
    }

    // 获取用户协议条款
    @Cacheable(cacheNames = ApiConstants.REDIS_PROTOCOL_USER_KEY, key = "#siteCode")
    public SysWebTerms getMbrSysWebTerms(String siteCode) {
        Map<String, String> map = getWebsiteTerms();
        SysWebTerms swt = new SysWebTerms();
        Set<String> set = map.keySet();
        if (null != set) {
            for (String key : set) {
                if (key.equals(SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE)) {
                    swt.setDisplay(map.get(key));
                }
                if (key.equals(SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE)) {
                    swt.setServiceTerms(map.get(key));
                }
            }
        }
        return swt;
    }
    
    /**
     * 	根据key查询单个
     * 
     * @param syskey
     * @return
     */
    @Cacheable(value= ApiConstants.REIDS_SYS_SETTING_KEY, key="'syskey_'+#syskey", unless = "#result == null")
    public SysSetting getBySyskey(String syskey) {
        SysSetting record = new SysSetting();
        record.setSyskey(syskey);
        return sysSettingMapper.selectOne(record);
    }

    public Map<String, String> getWebsiteTerms() {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        Map<String, String> map = new HashMap<>();
        List<String> tKeys = Lists.newArrayList(SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE,
                SystemConstants.AGENT_SERVICE_TERMS_OF_WEBSITE);

        List<String> dKeys = Lists.newArrayList(SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE,
                SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE);

        if (ssList != null) {
            for (SysSetting ss : ssList) {
                if (tKeys.contains(ss.getSyskey())) {
                    map.put(ss.getSyskey(), ss.getWebsiteTerms());
                }
                if (dKeys.contains(ss.getSyskey())) {
                    map.put(ss.getSyskey(), ss.getSysvalue());
                }
            }
        }
        return map;
    }

    // 获取站点设置信息
    @Cacheable(cacheNames = ApiConstants.REIDS_STATION_SET_KEY, key = "#siteCode")
    public StationSet getStation(String siteCode) {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        StationSet stationSet = new StationSet();
        if (null != ssList) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.DEFAULT_QUERY_DAYS.equals(ss.getSyskey())) {
                    stationSet.setDefaultQueryDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_QUERY_DAYS.equals(ss.getSyskey())) {
                    stationSet.setMemberQueryDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.PASSWORD_EXPIRE_DAYS.equals(ss.getSyskey())) {
                    stationSet.setPasswordExpireDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.LOGO_PATH.equals(ss.getSyskey())) {
                    stationSet.setLogoPath(ss.getSysvalue());
                }
                if (SystemConstants.TITLE_PATH.equals(ss.getSyskey())) {
                    stationSet.setTitlePath(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_TITLE.equals(ss.getSyskey())) {
                    stationSet.setWebsiteTitle(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_KEYWORDS.equals(ss.getSyskey())) {
                    stationSet.setWebsiteKeywords(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_DESCRIPTION.equals(ss.getSyskey())) {
                    stationSet.setWebsiteDescription(ss.getSysvalue());
                }
                if (SystemConstants.LOGO_PATH.equals(ss.getSyskey())) {
                    stationSet.setLogoPath(ss.getSysvalue());
                }
                if (SystemConstants.TITLE_PATH.equals(ss.getSyskey())) {
                    stationSet.setTitlePath(ss.getSysvalue());
                }
                if (SystemConstants.CONFIG_CODE_PC.equals(ss.getSyskey())) {
                    stationSet.setConfigCodePc(ss.getSysvalue());
                }
                if (SystemConstants.CONFIG_CODE_MB.equals(ss.getSyskey())) {
                    stationSet.setConfigCodeMb(ss.getSysvalue());
                }
                if (SystemConstants.AUTO_DELETE_DAYS.equals(ss.getSyskey())) {
                    stationSet.setAutoDeleteDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.WEBSITE_CODE_MB.equals(ss.getSyskey())) {
                    stationSet.setWebsiteCodeMb(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_CODE_PC.equals(ss.getSyskey())) {
                    stationSet.setWebsiteCodePc(ss.getSysvalue());
                }
            }
        }
        return stationSet;
    }

    // 获取邮件设置信息
    @Cacheable(cacheNames = ApiConstants.REIDS_MAIL_SET_KEY, key = "#siteCode")
    public MailSet getMailSet(String siteCode) {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        MailSet ms = new MailSet();
        if (null != ssList) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.MAIL_SEND_SERVER.equals(ss.getSyskey())) {
                    ms.setMailSendServer(ss.getSysvalue());
                }
                if (SystemConstants.MAIL_SEND_PORT.equals(ss.getSyskey())) {
                    ms.setMailSendPort(ss.getSysvalue());
                }
                if (SystemConstants.MAIL_SEND_ACCOUNT.equals(ss.getSyskey())) {
                    ms.setMailSendAccount(ss.getSysvalue());
                }
                if (SystemConstants.MAIL_PASSWORD.equals(ss.getSyskey())) {
                    ms.setMailPassword(ss.getSysvalue());
                }
                if (SystemConstants.WETHER_SSL.equals(ss.getSyskey())) {
                    ms.setWetherSsl(ss.getSysvalue());
                }
                if (SystemConstants.CHARACTER_SET.equals(ss.getSyskey())) {
                    ms.setCharacterSet(ss.getSysvalue());
                }
            }
        }
        return ms;
    }

    public RegisterSet queryRegisterSet() {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        RegisterSet registerSet = new RegisterSet();
        if (null != ssList) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.MEMBER_ACCOUNT.equals(ss.getSyskey())) {
                    registerSet.setLoginName(Integer.parseInt(ss.getSysvalue()));
                }

                if (SystemConstants.MEMBER_LOGIN_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_REPEATED_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setReLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_VERIFICATION_CODE.equals(ss.getSyskey())) {
                    registerSet.setCaptchareg(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_REAL_NAME.equals(ss.getSyskey())) {
                    registerSet.setRealName(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_TELPHONE.equals(ss.getSyskey())) {
                    registerSet.setMobile(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_TELPHONE_CODE.equals(ss.getSyskey())) {
                    registerSet.setMobileCaptchareg(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_EMAIL.equals(ss.getSyskey())) {
                    registerSet.setEmail(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_QQ.equals(ss.getSyskey())) {
                    registerSet.setQq(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_WECHAT.equals(ss.getSyskey())) {
                    registerSet.setWeChat(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_ADDRESS.equals(ss.getSyskey())) {
                    registerSet.setAddress(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_ACCOUNT.equals(ss.getSyskey())) {
                    registerSet.setAgentLoginName(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_LOGIN_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setAgentLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_REPEATED_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setAgentReLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_VERIFICATION_CODE.equals(ss.getSyskey())) {
                    registerSet.setAgentCaptchareg(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_REAL_NAME.equals(ss.getSyskey())) {
                    registerSet.setAgentRealName(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_TELPHONE.equals(ss.getSyskey())) {
                    registerSet.setAgentMobile(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_TELPHONE_CODE.equals(ss.getSyskey())) {
                    registerSet.setAgentMobileCaptchareg(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_EMAIL.equals(ss.getSyskey())) {
                    registerSet.setAgentEmail(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_QQ.equals(ss.getSyskey())) {
                    registerSet.setAgentQQ(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_WECHAT.equals(ss.getSyskey())) {
                    registerSet.setAgentWechat(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_ADDRESS.equals(ss.getSyskey())) {
                    registerSet.setAgentAddress(Integer.parseInt(ss.getSysvalue()));
                }
                // 是否允许前台注册
                if (SystemConstants.MEMBER_WEB_REGISTER.equals(ss.getSyskey())) {
                    registerSet.setAccWebRegister(Integer.parseInt(ss.getSysvalue()));
                }
                // 注册方式
                if (SystemConstants.MEMBER_WEB_REGISTER_METHOD.equals(ss.getSyskey())) {
                    registerSet.setRegisterMethod(Integer.parseInt(ss.getSysvalue()));
                }
                // 注册ip限制
                if (SystemConstants.MEMBER_IP.equals(ss.getSyskey())) {
                    registerSet.setLoginIp(Integer.parseInt(ss.getSysvalue()));
                }
                // 注册设备限制
                if (SystemConstants.MEMBER_DEVICE.equals(ss.getSyskey())) {
                    registerSet.setDeviceUuid(Integer.parseInt(ss.getSysvalue()));
                }
                // 真实姓名重复 是否开启
                if (SystemConstants.MEMBER_REAL_NAME_REPEAT.equals(ss.getSyskey())) {
                    registerSet.setRealNameRepeat(ss.getSysvalue() == null? 0 :Integer.parseInt(ss.getSysvalue()));
                }

                // 真实姓名重复 是否开启
                if (SystemConstants.MEMBER_PROMOTION.equals(ss.getSyskey())) {
                    registerSet.setPromotion(ss.getSysvalue() == null? 0 :Integer.parseInt(ss.getSysvalue()));
                }
            }
        }
        return registerSet;
    }

    /**
     * 	保存站点配置
     * 
     * @param str
     * @param userName
     * @param ip
     */
    @CacheEvict(value = ApiConstants.REIDS_SYS_SETTING_KEY, allEntries = true)
    public void saveStationSet(String str, String userName, String ip) {
        Gson gson = new Gson();
        StationSet stationSet = gson.fromJson(str, StationSet.class);
        if (null != stationSet) {
            if (null != stationSet.getDefaultQueryDays()) {
                String key = SystemConstants.DEFAULT_QUERY_DAYS;
                String value = Integer.toString(stationSet.getDefaultQueryDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getMemberQueryDays()) {
                String key = SystemConstants.MEMBER_QUERY_DAYS;
                String value = Integer.toString(stationSet.getMemberQueryDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getAutoDeleteDays()) {
                String key = SystemConstants.AUTO_DELETE_DAYS;
                String value = Integer.toString(stationSet.getAutoDeleteDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getPasswordExpireDays()) {
                String key = SystemConstants.PASSWORD_EXPIRE_DAYS;
                String value = Integer.toString(stationSet.getPasswordExpireDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteCodeMb()) {
                String key = SystemConstants.WEBSITE_CODE_MB;
                String value = stationSet.getWebsiteCodeMb();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteCodePc()) {
                String key = SystemConstants.WEBSITE_CODE_PC;
                String value = stationSet.getWebsiteCodePc();
                modifyOrUpdate(key, value);
            }

            if (null != stationSet.getWebsiteTitle()) {
                String key = SystemConstants.WEBSITE_TITLE;
                String value = stationSet.getWebsiteTitle();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteKeywords()) {
                String key = SystemConstants.WEBSITE_KEYWORDS;
                String value = stationSet.getWebsiteKeywords();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteDescription()) {
                String key = SystemConstants.WEBSITE_DESCRIPTION;
                String value = stationSet.getWebsiteDescription();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigCodeMb()) {
                String key = SystemConstants.CONFIG_CODE_MB;
                String value = stationSet.getConfigCodeMb();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigCodePc()) {
                String key = SystemConstants.CONFIG_CODE_PC;
                String value = stationSet.getConfigCodePc();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigCodeMb1()) {    // 客服配置代码2 移动
                String key = SystemConstants.CONFIG_CODE_MB1;
                String value = stationSet.getConfigCodeMb1();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigCodePc1()) {    // 客服配置代码2 PC
                String key = SystemConstants.CONFIG_CODE_PC1;
                String value = stationSet.getConfigCodePc1();
                modifyOrUpdate(key, value);
            }
                if (null != stationSet.getConfigTelegram()) {   // 合营部telegeram
                String key = SystemConstants.CONFIG_TELEGRAM;
                String value = stationSet.getConfigTelegram();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigSkype()) {      // 合营部Skype
                String key = SystemConstants.CONFIG_SKYPE;
                String value = stationSet.getConfigSkype();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigFlygram()) {      // 合营部Flygram
                String key = SystemConstants.CONFIG_FLYGRAM;
                String value = stationSet.getConfigFlygram();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getShowWebSite()) {      // 展示域名
                String key = SystemConstants.SHOW_WEBSITE;
                String value = stationSet.getShowWebSite();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getUsdtBuyUrl()) {      // USDT购买超链接
            	String key = SystemConstants.USDT_BUY_URL;
            	String value = stationSet.getUsdtBuyUrl();
            	modifyOrUpdate(key, value);
            }

            //添加操作日志
            mbrAccountLogService.updateSysSiteSetLog(stationSet, userName, ip);
        }
    }

    public void modifyPic(MultipartFile titlePicFile, MultipartFile logoPicFile) {
        if (Objects.nonNull(titlePicFile)) {
            String fileName = null;
            try {
                String prefix = titlePicFile.getOriginalFilename()
                        .substring(titlePicFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(titlePicFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }

            modifyPicFile(SystemConstants.TITLE_PATH, fileName);
        }
        if (Objects.nonNull(logoPicFile)) {
            String fileName = null;
            try {
                String prefix = logoPicFile.getOriginalFilename()
                        .substring(logoPicFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(logoPicFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
            modifyPicFile(SystemConstants.LOGO_PATH, fileName);
        }
    }

    private void modifyPicFile(String key, String fileName) {
        SysSetting ss = sysSettingMapper.selectByPrimaryKey(key);
        SysSetting record = new SysSetting();
        record.setSyskey(key);
        record.setSysvalue(fileName);

        if (null == ss) {
            sysSettingMapper.insertSelective(record);
        } else {
            sysSettingMapper.updateByPrimaryKey(record);
        }
    }

    public void saveMailSet(MailSet mailSet, String userName, String ip) {
        if (mailSet != null) {
            if (null != mailSet.getMailSendServer()) {
                String key = SystemConstants.MAIL_SEND_SERVER;
                String value = mailSet.getMailSendServer();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getMailSendPort()) {
                String key = SystemConstants.MAIL_SEND_PORT;
                String value = mailSet.getMailSendPort();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getMailSendAccount()) {
                String key = SystemConstants.MAIL_SEND_ACCOUNT;
                String value = mailSet.getMailSendAccount();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getMailPassword()) {
                String key = SystemConstants.MAIL_PASSWORD;
                String value = mailSet.getMailPassword();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getWetherSsl()) {
                String key = SystemConstants.WETHER_SSL;
                String value = mailSet.getWetherSsl();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getCharacterSet()) {
                String key = SystemConstants.CHARACTER_SET;
                String value = mailSet.getWetherSsl();
                modifyOrUpdate(key, value);
            }

            //添加操作日志
            mbrAccountLogService.updateSysMailSetLog(mailSet, userName, ip);
        }
    }

    public void saveRegSet(RegisterSet registerSet, String userName, String ip) {
        if (null != registerSet) {
            if (null != registerSet.getLoginName()) {
                String key = SystemConstants.MEMBER_ACCOUNT;
                String value = Integer.toString(registerSet.getLoginName());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getLoginPwd()) {
                String key = SystemConstants.MEMBER_LOGIN_PASSWORD;
                String value = Integer.toString(registerSet.getLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getReLoginPwd()) {
                String key = SystemConstants.MEMBER_REPEATED_PASSWORD;
                String value = Integer.toString(registerSet.getReLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getCaptchareg()) {
                String key = SystemConstants.MEMBER_VERIFICATION_CODE;
                String value = Integer.toString(registerSet.getCaptchareg());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getRealName()) {
                String key = SystemConstants.MEMBER_REAL_NAME;
                String value = Integer.toString(registerSet.getRealName());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getMobile()) {
                String key = SystemConstants.MEMBER_TELPHONE;
                String value = Integer.toString(registerSet.getMobile());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getMobileCaptchareg()) {
                String key = SystemConstants.MEMBER_TELPHONE_CODE;
                String value = Integer.toString(registerSet.getMobileCaptchareg());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getEmail()) {
                String key = SystemConstants.MEMBER_EMAIL;
                String value = Integer.toString(registerSet.getEmail());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getQq()) {
                String key = SystemConstants.MEMBER_QQ;
                String value = Integer.toString(registerSet.getQq());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getWeChat()) {
                String key = SystemConstants.MEMBER_WECHAT;
                String value = Integer.toString(registerSet.getWeChat());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAddress()) {
                String key = SystemConstants.MEMBER_ADDRESS;
                String value = Integer.toString(registerSet.getAddress());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getLoginName()) {
                String key = SystemConstants.AGENT_ACCOUNT;
                String value = Integer.toString(registerSet.getLoginName());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentLoginPwd()) {
                String key = SystemConstants.AGENT_LOGIN_PASSWORD;
                String value = Integer.toString(registerSet.getAgentLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentReLoginPwd()) {
                String key = SystemConstants.AGENT_REPEATED_PASSWORD;
                String value = Integer.toString(registerSet.getAgentReLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentCaptchareg()) {
                String key = SystemConstants.AGENT_VERIFICATION_CODE;
                String value = Integer.toString(registerSet.getAgentCaptchareg());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentRealName()) {
                String key = SystemConstants.AGENT_REAL_NAME;
                String value = Integer.toString(registerSet.getAgentRealName());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentMobile()) {
                String key = SystemConstants.AGENT_TELPHONE;
                String value = Integer.toString(registerSet.getAgentMobile());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getAgentMobileCaptchareg()) {
                String key = SystemConstants.AGENT_TELPHONE_CODE;
                String value = Integer.toString(registerSet.getAgentMobileCaptchareg());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentEmail()) {
                String key = SystemConstants.AGENT_EMAIL;
                String value = Integer.toString(registerSet.getAgentEmail());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentQQ()) {
                String key = SystemConstants.AGENT_QQ;
                String value = Integer.toString(registerSet.getAgentQQ());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentWechat()) {
                String key = SystemConstants.AGENT_WECHAT;
                String value = Integer.toString(registerSet.getAgentWechat());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentAddress()) {
                String key = SystemConstants.AGENT_ADDRESS;
                String value = Integer.toString(registerSet.getAgentAddress());
                modifyOrUpdate(key, value);
            }
            // 注册方式
            if (null != registerSet.getRegisterMethod()) {
                String key = SystemConstants.MEMBER_WEB_REGISTER_METHOD;
                String value = Integer.toString(registerSet.getRegisterMethod());
                modifyOrUpdate(key, value);
            } // 注册ip次数限制
            if (null != registerSet.getLoginIp()) {
                String key = SystemConstants.MEMBER_IP;
                String value = Integer.toString(registerSet.getLoginIp());
                modifyOrUpdate(key, value);
            } // 注册同设备数量限制
            if (null != registerSet.getDeviceUuid()) {
                String key = SystemConstants.MEMBER_DEVICE;
                String value = Integer.toString(registerSet.getDeviceUuid());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getRealNameRepeat()) {
                String key = SystemConstants.MEMBER_REAL_NAME_REPEAT;
                String value = Integer.toString(registerSet.getRealNameRepeat());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getPromotion()) {
                String key = SystemConstants.MEMBER_PROMOTION;
                String value = Integer.toString(registerSet.getPromotion());
                modifyOrUpdate(key, value);
            }
            //添加操作日志
            mbrAccountLogService.updateSysRegisterSetLog(registerSet, userName, ip);
        }
    }
    public void saveAiRecommendSet(AiRecommend aiRecommend, String userName, String ip) {
        if (null != aiRecommend.getIsEnble()) {
            String key = SystemConstants.AI_RECOMMEND;
            String value = Integer.toString(aiRecommend.getIsEnble());
            modifyOrUpdate(key, value);
        }
        mbrAccountLogService.updateAiRecommendSetLog(aiRecommend, userName, ip);
    }
    public void saveAccWebRegSet(RegisterSet registerSet, String userName, String ip) {
        if (null != registerSet) {
            // 是否允许前台注册
            if (null != registerSet.getAccWebRegister()) {
                String key = SystemConstants.MEMBER_WEB_REGISTER;
                String value = Integer.toString(registerSet.getAccWebRegister());

                // 判断是否有做变更
                SysSetting record = new SysSetting();
                record.setSyskey(key);
                SysSetting re = sysSettingMapper.selectOne(record);

                if ((null != re && !re.getSysvalue().equals(value)) || re == null) {    // 有变更或者不存在
                    modifyOrUpdate(key, value);

                    //添加操作日志
                    mbrAccountLogService.saveAccWebRegSetLog(registerSet, userName, ip);
                }
            }
        }
    }

    public void modifyOrUpdate(String key, String value) {
        SysSetting ss = new SysSetting();
        ss.setSyskey(key);
        ss.setSysvalue(value);
        SysSetting record = new SysSetting();
        record.setSyskey(key);
        SysSetting re = sysSettingMapper.selectOne(record);
        if (null != re) {
            sysSettingMapper.updateByPrimaryKeySelective(ss);
        } else {
            sysSettingMapper.insertSelective(ss);
        }
    }

    public void modifyOrUpdate01(String key, String value) {
        SysSetting ss = new SysSetting();
        ss.setSyskey(key);
        ss.setWebsiteTerms(value);
        SysSetting record = new SysSetting();
        record.setSyskey(key);
        SysSetting re = sysSettingMapper.selectOne(record);
        if (null != re) {
            sysSettingMapper.updateByPrimaryKeySelective(ss);
        } else {
            sysSettingMapper.insertSelective(ss);
        }
    }

    public StationSet queryConfigDaysAndScope() {
        StationSet stationSet = stationSetMapper.queryConfigDaysAndScope();
        return stationSet;
    }

    public StationSet queryStationSet() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        StationSet stationSet = new StationSet();
        for (SysSetting ss : list) {
            if (SystemConstants.DEFAULT_QUERY_DAYS.equals(ss.getSyskey())) {
                stationSet.setDefaultQueryDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.MEMBER_QUERY_DAYS.equals(ss.getSyskey())) {
                stationSet.setMemberQueryDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.PASSWORD_EXPIRE_DAYS.equals(ss.getSyskey())) {
                stationSet.setPasswordExpireDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.WEBSITE_CODE_PC.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodePc(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_CODE_MB.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodeMb(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_TITLE.equals(ss.getSyskey())) {
                stationSet.setWebsiteTitle(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_KEYWORDS.equals(ss.getSyskey())) {
                stationSet.setWebsiteKeywords(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_DESCRIPTION.equals(ss.getSyskey())) {
                stationSet.setWebsiteDescription(ss.getSysvalue());
            }
            if (SystemConstants.MEMBER_WEB_REGISTER.equals(ss.getSyskey())) {
                stationSet.setAccWebRegister("0".equals(ss.getSysvalue()) ? Boolean.FALSE : Boolean.TRUE);
            }
            if (SystemConstants.CONFIG_CODE_PC.equals(ss.getSyskey())) {
                stationSet.setConfigCodePc(ss.getSysvalue());
            }
            if (SystemConstants.CONFIG_CODE_MB.equals(ss.getSyskey())) {
                stationSet.setConfigCodeMb(ss.getSysvalue());
            }
            if (SystemConstants.CONFIG_CODE_PC1.equals(ss.getSyskey())) {   // 客服配置代码2 PC
                stationSet.setConfigCodePc1(ss.getSysvalue());
            }
            if (SystemConstants.CONFIG_CODE_MB1.equals(ss.getSyskey())) {   // 客服配置代码2 移动
                stationSet.setConfigCodeMb1(ss.getSysvalue());
            }
            if (SystemConstants.AGENT_DOMAIN_ANALYSIS_SITE.equals(ss.getSyskey())) {
                stationSet.setAgentDomainAnalysisSite(ss.getSysvalue());
            }
            if (SystemConstants.AUTO_DELETE_DAYS.equals(ss.getSyskey())) {
                stationSet.setAutoDeleteDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.WEBSITE_CODE_PC.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodePc(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_CODE_MB.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodeMb(ss.getSysvalue());
            }
            if (SystemConstants.TITLE_PATH.equals(ss.getSyskey())) {
                String path = ss.getSysvalue();
                stationSet.setTitlePath(path);
            }
            if (SystemConstants.LOGO_PATH.equals(ss.getSyskey())) {
                String path = ss.getSysvalue();
                stationSet.setLogoPath(path);
            }
            if (SystemConstants.CONFIG_TELEGRAM.equals(ss.getSyskey())) {   // 合营部telegeram
                stationSet.setConfigTelegram(ss.getSysvalue());
            }
            if (SystemConstants.CONFIG_SKYPE.equals(ss.getSyskey())) {   // 合营部Skype
                stationSet.setConfigSkype(ss.getSysvalue());
            }
            if (SystemConstants.CONFIG_FLYGRAM.equals(ss.getSyskey())) {   // 合营部Flygram
                stationSet.setConfigFlygram(ss.getSysvalue());
            }
            if (SystemConstants.APP_DOWNLOAD_ANDORID_URL.equals(ss.getSyskey())) {   // android下载链接
                stationSet.setAndroidDownloadUrl(ss.getSysvalue());
            }
            if (SystemConstants.APP_DOWNLOAD_IOS_URL.equals(ss.getSyskey())) {      // aios下载链接
                stationSet.setIosDownloadUrl(ss.getSysvalue());
            }
            if (SystemConstants.SHOW_WEBSITE.equals(ss.getSyskey())) {      // 展示域名
                stationSet.setShowWebSite(ss.getSysvalue());
            }
            if (SystemConstants.USDT_BUY_URL.equals(ss.getSyskey())) {      // USDT购买超链接
            	stationSet.setUsdtBuyUrl(ss.getSysvalue());
            }
        }
        return stationSet;
    }

    public String getCustomerSerUrl(Byte terminal) {
        if (!StringUtils.isEmpty(terminal) && terminal.equals(ApiConstants.Terminal.mobile)) {
            return queryStationSet().getConfigCodeMb();
        } else {
            return queryStationSet().getConfigCodePc();
        }
    }

    public MailSet queryMaliSet() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        MailSet mailSet = new MailSet();
        for (SysSetting ss : list) {
            if (SystemConstants.MAIL_SEND_SERVER.equals(ss.getSyskey())) {
                mailSet.setMailSendServer(ss.getSysvalue());
            }
            if (SystemConstants.MAIL_SEND_PORT.equals(ss.getSyskey())) {
                mailSet.setMailSendPort(ss.getSysvalue());
            }
            if (SystemConstants.MAIL_SEND_ACCOUNT.equals(ss.getSyskey())) {
                mailSet.setMailSendAccount(ss.getSysvalue());
            }
            if (SystemConstants.MAIL_PASSWORD.equals(ss.getSyskey())) {
                mailSet.setMailPassword(ss.getSysvalue());
            }
            if (SystemConstants.WETHER_SSL.equals(ss.getSyskey())) {
                mailSet.setWetherSsl(ss.getSysvalue());
            }
            if (SystemConstants.CHARACTER_SET.equals(ss.getSyskey())) {
                mailSet.setCharacterSet(ss.getSysvalue());
            }
        }
        return mailSet;
    }
    
    
    /**
     * 	查询合营计划设置
     * 
     * @return
     */
    public VenturePlanSet queryVenturePlanSet() {
    	List<SysSetting> list = sysSettingMapper.selectAll();
    	VenturePlanSet venturePlanSet = new VenturePlanSet();
    	for (SysSetting ss : list) {
    		if (SystemConstants.VENTURE_PLAN_PIC.equals(ss.getSyskey())) {
    			venturePlanSet.setVenturePlanPic(ss.getSysvalue());
    		}
    		if (SystemConstants.DEFINE_INDUSTRY_PIC.equals(ss.getSyskey())) {
    			venturePlanSet.setDefineIndustryPic(ss.getSysvalue());
    		}
    	}
    	return venturePlanSet;
    }


    @CacheEvict(cacheNames = ApiConstants.REDIS_PROTOCOL_USER_KEY, key = "#siteCode")
    public void saveWebTerms(WebTerms webTerms, String siteCode) {
        String mDisplay = Integer.toString(webTerms.getMemberDisplayTermsOfWebsite());
        String mTerms = webTerms.getMemberServiceTermsOfWebsite();
        String aDisplay = Integer.toString(webTerms.getAgentDisplayTermsOfWebsite());
        String aTerms = webTerms.getAgentServiceTermsOfWebsite();

        if (null != mDisplay) {
            String key = SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE;
            modifyOrUpdate(key, mDisplay);
        }
        if (null != mTerms) {
            String key = SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE;
            modifyOrUpdate01(key, mTerms);
        }

        if (null != aDisplay) {
            String key = SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE;
            modifyOrUpdate(key, aDisplay);
        }
        if (null != aTerms) {
            String key = SystemConstants.AGENT_SERVICE_TERMS_OF_WEBSITE;
            modifyOrUpdate01(key, aTerms);
        }
    }

    public WebTerms queryWebTerms() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        WebTerms webTerms = new WebTerms();
        if (null != list) {
            for (SysSetting ss : list) {
                if (SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setMemberDisplayTermsOfWebsite(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setMemberServiceTermsOfWebsite(ss.getWebsiteTerms());
                }
                if (SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setAgentDisplayTermsOfWebsite(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_SERVICE_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setAgentServiceTermsOfWebsite(ss.getWebsiteTerms());
                }
            }
        }
        return webTerms;
    }


    public SmsResultDto testReceiveSms(SmsConfig smsConfig) {
        String code = CommonUtil.getRandomCode();
        String content = smsConfig.getTemplate().replace("{0}", code).toString();
        return sendSmsSevice.testReceiveSms(smsConfig.getMobile(), content, smsConfig);
    }

    public String testReceiveMail(MailSet mailSet) {
        String code = CommonUtil.getRandomCode();
        String subject = "测试标题";
        String content = "测试内容:" + code;
        boolean flag = sendMailSevice.sendMail(mailSet, mailSet.getMailReceiver(), subject, content, null);
        if (flag) {
            return content;
        } else {
            return null;
        }
    }

    public void modifyPaySet(PaySet paySet, String userName, String ip) {
        List<String> syskeys = Lists.newArrayList(SystemConstants.PAY_AUTOMATIC, SystemConstants.PAY_MONEY,SystemConstants.isMultipleOpen,
                SystemConstants.DEPOSIT_CONDITION, SystemConstants.WITHDRAW_CONDITION, SystemConstants.ALIPAY_ENABLE,
                SystemConstants.FAST_WITHDRAW_ENABLE, SystemConstants.IS_WITHDRAW_LIMIT_TIME_OPEN, SystemConstants.WITHDRAW_LIMIT_TIME_LIST);
        deleteSysSetting(syskeys);
        Map<String, Object> map = jsonUtil.Entity2Map(paySet);
        List<SysSetting> sysSettingList = syskeys.stream().map(key -> {
            SysSetting sysSetting = new SysSetting();
            sysSetting.setSyskey(key);
            sysSetting.setSysvalue(map.get(key).toString());
            return sysSetting;
        }).collect(Collectors.toList());
        stationSetMapper.batchInsertSysSetting(sysSettingList);

        mbrAccountLogService.updateSysPayAutomaticSetLog(paySet, userName, ip);

    }
    
    /**
     * 	合营计划更新
     */
    public void venturePlanSet(VenturePlanSet venturePlanSet, String userName, String ip) {
    	List<String> syskeys = Lists.newArrayList(SystemConstants.VENTURE_PLAN_PIC, SystemConstants.DEFINE_INDUSTRY_PIC);
    	deleteSysSetting(syskeys);
    	Map<String, Object> map = jsonUtil.Entity2Map(venturePlanSet);
    	List<SysSetting> sysSettingList = syskeys.stream().map(key -> {
    		SysSetting sysSetting = new SysSetting();
    		sysSetting.setSyskey(key);
    		sysSetting.setSysvalue(map.get(key).toString());
    		return sysSetting;
    	}).collect(Collectors.toList());
    	stationSetMapper.batchInsertSysSetting(sysSettingList);
    	
    	mbrAccountLogService.updateSysVenturePlanSetSetLog(userName, ip);
    }

    public void modifyFriendTransSet(PaySet paySet, String userName, String ip) {
        List<String> syskeys = Lists.newArrayList(SystemConstants.FRIEND_TRANS_AUTOMATIC, SystemConstants.FRIEND_TRANS_MAX_AMOUNT);
        deleteSysSetting(syskeys);
        stationSetMapper.insertSysSetting(setSysSetting(SystemConstants.FRIEND_TRANS_AUTOMATIC,
                Constants.EVNumber.one == paySet.getFriendsTransAntomatic() ? paySet.getFriendsTransAntomatic().toString() : "0"));
        stationSetMapper.insertSysSetting(setSysSetting(SystemConstants.FRIEND_TRANS_MAX_AMOUNT,
                Objects.nonNull(paySet.getFriensTransMaxAmount()) ? paySet.getFriensTransMaxAmount().toString() : null));
        mbrAccountLogService.updateSysFriendTransSetLog(paySet, userName, ip);

    }

    /**
     * 	查询出款设置
     * 
     * @return
     */
    public PaySet queryPaySet() {
    	// 要查询的key列表
        List<String> syskeys = Lists.newArrayList(SystemConstants.PAY_AUTOMATIC, SystemConstants.PAY_MONEY,
                SystemConstants.DEPOSIT_CONDITION, SystemConstants.WITHDRAW_CONDITION, SystemConstants.ALIPAY_ENABLE, SystemConstants.isMultipleOpen,
                SystemConstants.FAST_WITHDRAW_ENABLE, SystemConstants.IS_WITHDRAW_LIMIT_TIME_OPEN,SystemConstants.WITHDRAW_LIMIT_TIME_LIST);
        // 根据key查询参数
        List<SysSetting> sysSettingList = getSysSettingList(syskeys);
        Map<String, Object> data = new HashMap<>();
        if (Collections3.isNotEmpty(sysSettingList)) {

            sysSettingList.stream().forEach(sysSetting -> {
                if (sysSetting.getSyskey().equals(SystemConstants.DEPOSIT_CONDITION) || sysSetting.getSyskey().equals(SystemConstants.WITHDRAW_CONDITION)) {
                    Type jsonType = new TypeToken<List<Integer>>() {
                    }.getType();
                    List<Integer> con = jsonUtil.fromJson(sysSetting.getSysvalue(), jsonType);
                    data.put(sysSetting.getSyskey(), con);
                } else if(sysSetting.getSyskey().equals(SystemConstants.WITHDRAW_LIMIT_TIME_LIST)){
                    Type jsonType = new TypeToken<List<WithdrawLimitTimeDto>>() {
                    }.getType();
                    List<WithdrawLimitTimeDto> con = jsonUtil.fromJson(sysSetting.getSysvalue(), jsonType);
                    data.put(sysSetting.getSyskey(), con);
                }
                else {
                        data.put(sysSetting.getSyskey(), sysSetting.getSysvalue());
                    }
            });
            PaySet paySet = jsonUtil.fromJson(jsonUtil.toJson(data), PaySet.class);
            return paySet;
        }
        return null;
    }

    public PaySet queryFriendTransSet() {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (Collections3.isNotEmpty(settingList)) {
            PaySet paySet = new PaySet();
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.FRIEND_TRANS_AUTOMATIC.equals(sysSetting.getSyskey())) {
                    paySet.setFriendsTransAntomatic(Integer.parseInt(sysSetting.getSysvalue()));
                }
                if (SystemConstants.FRIEND_TRANS_MAX_AMOUNT.equals(sysSetting.getSyskey())) {
                    if (Objects.nonNull(sysSetting.getSysvalue())) {  // 禁用状态允许为null
                        paySet.setFriensTransMaxAmount(new BigDecimal(sysSetting.getSysvalue()));
                    }
                }
            });
            return paySet;
        }
        return null;
    }

    public AiRecommend queryAiRecommendSet() {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (Collections3.isNotEmpty(settingList)) {
            AiRecommend aiRecommend = new AiRecommend();
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.AI_RECOMMEND.equals(sysSetting.getSyskey())) {
                    aiRecommend.setIsEnble(Integer.parseInt(sysSetting.getSysvalue()));
                }
            });
            return aiRecommend;
        }
        return null;
    }

    public SysSetting getSysSetting(String syskey) {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(syskey);
        return sysSettingMapper.selectOne(sysSetting);
    }

    private SysSetting setSysSetting(String syskey, String sysvalue) {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(syskey);
        sysSetting.setSysvalue(sysvalue);
        return sysSetting;
    }

    private void deleteSysSetting(List<String> syskeys) {
        syskeys.stream().forEach(sy -> {
            SysSetting sysSetting = new SysSetting();
            sysSetting.setSyskey(sy);
            sysSettingMapper.delete(sysSetting);
        });
    }

    public SettingAgentDto agentInfo() {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (Collections3.isNotEmpty(settingList)) {
            SettingAgentDto settingDto = new SettingAgentDto();
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.AGENT_REGISTER.equals(sysSetting.getSyskey())) {
                    settingDto.setAgentRgister(Integer.parseInt(sysSetting.getSysvalue()));
                }
                if (SystemConstants.AGENT_SYS_REGISTER.equals(sysSetting.getSyskey())) {
                    settingDto.setAgentSysRgister(Integer.parseInt(sysSetting.getSysvalue()));
                }
                if (SystemConstants.AGENT_ADD_ACCOUNT.equals(sysSetting.getSyskey())) {
                    settingDto.setAgentAddAccount(Integer.parseInt(sysSetting.getSysvalue()));
                }
                if (SystemConstants.AGENT_ADD_SUB.equals(sysSetting.getSyskey())) {
                    settingDto.setAgentAddSub(Integer.parseInt(sysSetting.getSysvalue()));
                }
            });
            return settingDto;
        }
        return null;
    }

    public void agentRegister(SettingAgentDto agentDto) {
        List<String> syskeys = Lists.newArrayList(SystemConstants.AGENT_REGISTER, SystemConstants.AGENT_SYS_REGISTER,
                SystemConstants.AGENT_ADD_ACCOUNT, SystemConstants.AGENT_ADD_SUB);
        deleteSysSetting(syskeys);
        stationSetMapper.insertSysSetting(setSysSetting(SystemConstants.AGENT_REGISTER, agentDto.getAgentRgister().toString()));
        stationSetMapper.insertSysSetting(setSysSetting(SystemConstants.AGENT_SYS_REGISTER, agentDto.getAgentSysRgister().toString()));
        stationSetMapper.insertSysSetting(setSysSetting(SystemConstants.AGENT_ADD_ACCOUNT, agentDto.getAgentAddAccount().toString()));
        stationSetMapper.insertSysSetting(setSysSetting(SystemConstants.AGENT_ADD_SUB, agentDto.getAgentAddSub().toString()));
    }

    /**
     * 	 根据type 修改会员，代理，渠道的推广域名
     * 
     * @param promotionSet
     * @param userName
     * @param ip
     */
    public void promotionSet(PromotionSet promotionSet, String userName, String ip) {
        if (promotionSet.getType() == Constants.EVNumber.zero) {
            List<String> syskeys = Lists.newArrayList(SystemConstants.ACCOUNT_PROMOTION);
            deleteSysSetting(syskeys);
            SysSetting sysSetting = new SysSetting();
            sysSetting.setSyskey(SystemConstants.ACCOUNT_PROMOTION);
            sysSetting.setSysvalue(String.valueOf(promotionSet.getSiteUrlId()));
            sysSetting.setWebsiteTerms(null);
            stationSetMapper.insertSysSetting(sysSetting);
            mbrMapper.updatePromotion();
        } else if (promotionSet.getType() == Constants.EVNumber.one) {
            List<String> syskeys = Lists.newArrayList(SystemConstants.AGENT_PROMOTION);
            deleteSysSetting(syskeys);
            SysSetting sysSetting = new SysSetting();
            sysSetting.setSyskey(SystemConstants.AGENT_PROMOTION);
            sysSetting.setSysvalue(String.valueOf(promotionSet.getSiteUrlId()));
            sysSetting.setWebsiteTerms(null);
            stationSetMapper.insertSysSetting(sysSetting);
        } else if (promotionSet.getType() == Constants.EVNumber.two) {
	    	List<String> syskeys = Lists.newArrayList(SystemConstants.CHANNEL_PROMOTION);
	    	deleteSysSetting(syskeys);
	    	SysSetting sysSetting = new SysSetting();
	    	sysSetting.setSyskey(SystemConstants.CHANNEL_PROMOTION);
	    	sysSetting.setSysvalue(String.valueOf(promotionSet.getSiteUrlId()));
	    	sysSetting.setWebsiteTerms(null);
	    	stationSetMapper.insertSysSetting(sysSetting);
        }

        //添加操作日志
        mbrAccountLogService.updateSysPromotionSetLog(promotionSet, userName, ip);
    }

    public PromotionSet queryPromotionSet() {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (Collections3.isNotEmpty(settingList)) {
            PromotionSet promotionSet = new PromotionSet();
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.ACCOUNT_PROMOTION.equals(sysSetting.getSyskey())) {
                    promotionSet.setSiteUrlId(StringUtil.isEmpty(sysSetting.getSysvalue()) ? null : Integer.parseInt(sysSetting.getSysvalue()));
                    promotionSet.setMemo(sysSetting.getWebsiteTerms());
                    promotionSet.setType(Constants.EVNumber.zero);
                }
            });
            return promotionSet;
        }
        return null;
    }

    public PromotionSet queryAgentPromotionSet() {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (Collections3.isNotEmpty(settingList)) {
            PromotionSet promotionSet = new PromotionSet();
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.AGENT_PROMOTION.equals(sysSetting.getSyskey())) {
                    promotionSet.setSiteUrlId(StringUtil.isEmpty(sysSetting.getSysvalue()) ? null : Integer.parseInt(sysSetting.getSysvalue()));
                    promotionSet.setMemo(sysSetting.getWebsiteTerms());
                    promotionSet.setType(Constants.EVNumber.one);
                }
            });
            return promotionSet;
        }
        return null;
    }
    

	/**
	 * 	当前站点的渠道的推广域名
	 * @return
	 */
	public PromotionSet queryChannelPromotionSet() {
		// 返回的对象
		PromotionSet promotionSet = new PromotionSet();
		// 设置的渠道的推广域名
		SysSetting sysSetting = new SysSetting();
		sysSetting.setSyskey(SystemConstants.CHANNEL_PROMOTION);
		SysSetting selectOne = sysSettingMapper.selectOne(sysSetting);
		
		if (selectOne != null) {
			promotionSet.setSiteUrlId(StringUtil.isEmpty(selectOne.getSysvalue()) ? null : Integer.parseInt(selectOne.getSysvalue()));
			promotionSet.setMemo(selectOne.getWebsiteTerms());
			promotionSet.setType(Constants.EVNumber.two);
		}
        return promotionSet;
	}

    public String getPromotionUrl(String siteCode) {
        PromotionSet promotionSet = queryPromotionSet();
        if (isNull(promotionSet)) {
            throw new R200Exception("系统未设置推广域名");
        }
        TcpSiteurl tcpSiteurl = new TcpSiteurl();
        tcpSiteurl.setSiteCode(siteCode);
        tcpSiteurl.setId(promotionSet.getSiteUrlId());
        List<TcpSiteurl> siteurlList = apiSysMapper.findCpSiteUrlBySiteCode(tcpSiteurl);
        if (siteurlList.size() == 0) {
            return null;
        }
        return siteurlList.get(0).getSiteUrl();
    }

    public int findAutomatic(String sysKey) {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(sysKey);
        SysSetting setting = sysSettingMapper.selectOne(sysSetting);
        if (nonNull(setting) && nonNull(setting.getSysvalue())) {
            return Integer.parseInt(setting.getSysvalue());
        }
        return Constants.EVNumber.zero;
    }

    public void setAutomatic(Integer sysvalue, String sysKey) {
        List<String> syskeys = Lists.newArrayList(sysKey);
        // 删除之前获得原来的数据
        SysSetting sysSettingOld = sysSettingMapper.selectByPrimaryKey(sysKey);

        deleteSysSetting(syskeys);
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(sysKey);
        sysSetting.setSysvalue(sysvalue.toString());
        sysSettingMapper.insert(sysSetting);


        mbrAccountLogService.setAutomatic(sysvalue.toString(), isNull(sysSettingOld) ? "0" : sysSettingOld.getSysvalue());
    }

    /**
     * 	用户端查询系统配置
     * @return
     */
    public StationSet queryApiStationSet() {
    	// 查询系统配置
        StationSet stationSet = queryStationSet();
        // 返回的传输对象
        StationSet stationSet1 = new StationSet();
        stationSet1.setLogoPath(stationSet.getLogoPath());
        stationSet1.setTitlePath(stationSet.getTitlePath());
        stationSet1.setAgentDomainAnalysisSite(stationSet.getAgentDomainAnalysisSite());
        stationSet1.setWebsiteCodeMb(stationSet.getWebsiteCodeMb());
        stationSet1.setWebsiteCodePc(stationSet.getWebsiteCodePc());
        stationSet1.setAccWebRegister(stationSet.getAccWebRegister());
        stationSet1.setConfigCodeMb(stationSet.getConfigCodeMb());      // 客服配置代码1 移动
        stationSet1.setConfigCodePc(stationSet.getConfigCodePc());      // 客服配置代码1 PC
        stationSet1.setConfigCodeMb1(stationSet.getConfigCodeMb1());    // 客服配置代码2 移动
        stationSet1.setConfigCodePc1(stationSet.getConfigCodePc1());    // 客服配置代码2 PC
        stationSet1.setConfigTelegram(stationSet.getConfigTelegram());  // 合营部telegeram
        stationSet1.setConfigSkype(stationSet.getConfigSkype());        // 合营部Skype
        stationSet1.setConfigFlygram(stationSet.getConfigFlygram());    // 合营部Flygram
        stationSet1.setAndroidDownloadUrl(stationSet.getAndroidDownloadUrl());  // android下载链接
        stationSet1.setIosDownloadUrl(stationSet.getIosDownloadUrl());          // iios下载链接
        stationSet1.setShowWebSite(stationSet.getShowWebSite());          // 展示域名链接
        stationSet1.setUsdtBuyUrl(stationSet.getUsdtBuyUrl());
        return stationSet1;
    }

    public int findActLevelStaticsRule() {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.ACT_LEVEL_STATICS_RULE);
        SysSetting setting = sysSettingMapper.selectOne(sysSetting);
        if (nonNull(setting) && nonNull(setting.getSysvalue())) {
            return Integer.parseInt(setting.getSysvalue());
        }
        return Constants.EVNumber.zero;
    }

    public Map<String, String> findActLevelStaticsRuleAndDescript() {
        Map<String, String> syskeys = new HashMap<String, String>(8);

        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.ACT_LEVEL_STATICS_RULE);
        SysSetting setting = sysSettingMapper.selectOne(sysSetting);
        if (nonNull(setting) && nonNull(setting.getSysvalue())) {
            syskeys.put(SystemConstants.ACT_LEVEL_STATICS_RULE, setting.getSysvalue());
        }
        SysSetting sysSetting2 = new SysSetting();
        sysSetting2.setSyskey(SystemConstants.ACT_LEVEL_STATICS_RULE_DESCRIPTION);
        SysSetting setting2 = sysSettingMapper.selectOne(sysSetting2);
        if (nonNull(setting2) && nonNull(setting2.getSysvalue())) {
            syskeys.put(SystemConstants.ACT_LEVEL_STATICS_RULE_DESCRIPTION, setting2.getSysvalue());
        }

        SysSetting sysSetting1 = new SysSetting();
        sysSetting1.setSyskey(SystemConstants.DOWNGRADA_DAY);
        SysSetting setting3 = sysSettingMapper.selectOne(sysSetting1);
        if (nonNull(setting3) && nonNull(setting3.getSysvalue())) {
            syskeys.put(SystemConstants.DOWNGRADA_DAY, setting3.getSysvalue());
        }
        
        // 降级计算周期
        SysSetting recoverSetting = new SysSetting();
        recoverSetting.setSyskey(SystemConstants.RECOVER_PROMOTION_DAY);
        SysSetting recoverSysSetting = sysSettingMapper.selectOne(recoverSetting);
        if (nonNull(recoverSysSetting) && nonNull(recoverSysSetting.getSysvalue())) {
        	syskeys.put(SystemConstants.RECOVER_PROMOTION_DAY, recoverSysSetting.getSysvalue());
        }
        return syskeys;
    }

    public void setActLevelStaticsRule(Integer sysvalue, String description, String downgradePromotionDay, String recoverPromotionDay) {
        List<String> syskeys = Lists.newArrayList(
                SystemConstants.ACT_LEVEL_STATICS_RULE,
                SystemConstants.ACT_LEVEL_STATICS_RULE_DESCRIPTION,
                SystemConstants.DOWNGRADA_DAY, SystemConstants.RECOVER_PROMOTION_DAY);
        // 获得原来的数据
        SysSetting sysSettingOld = sysSettingMapper.selectByPrimaryKey(SystemConstants.ACT_LEVEL_STATICS_RULE);
        SysSetting sysSettingOld2 = sysSettingMapper.selectByPrimaryKey(SystemConstants.ACT_LEVEL_STATICS_RULE_DESCRIPTION);
        // 删除原数据
        deleteSysSetting(syskeys);
        // 插入新数据
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.ACT_LEVEL_STATICS_RULE);
        sysSetting.setSysvalue(sysvalue.toString());
        sysSettingMapper.insert(sysSetting);
        sysSetting.setSyskey(SystemConstants.ACT_LEVEL_STATICS_RULE_DESCRIPTION);
        sysSetting.setSysvalue(description);
        sysSettingMapper.insert(sysSetting);
        sysSetting.setSyskey(SystemConstants.DOWNGRADA_DAY);
        sysSetting.setSysvalue(downgradePromotionDay);
        sysSettingMapper.insert(sysSetting);
        // 等级恢复计算周期
        sysSetting.setSyskey(SystemConstants.RECOVER_PROMOTION_DAY);
        sysSetting.setSysvalue(recoverPromotionDay);
        sysSettingMapper.insert(sysSetting);

        // 记录日志
        mbrAccountLogService.setActLevelStaticsRule(sysvalue.toString(), sysSettingOld.getSysvalue(), description, sysSettingOld2.getSysvalue());
    }

    // 查询语音线路设置
    public String queryOutCallset() {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        if (null != ssList) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.OUTCALL_PLATFORM.equals(ss.getSyskey())) {
                    return ss.getSysvalue();
                }
            }
        }
        return String.valueOf(Constants.EVNumber.one);  // 默认blink  1 blink  2 rowave
    }

    // 设置语音线路
    public void outCallSet(String outCallPlatform) {
        List<String> syskeys = Lists.newArrayList(SystemConstants.OUTCALL_PLATFORM);
        deleteSysSetting(syskeys);
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.OUTCALL_PLATFORM);
        sysSetting.setSysvalue(outCallPlatform);
        sysSetting.setWebsiteTerms(null);
        stationSetMapper.insertSysSetting(sysSetting);

        //添加操作日志
        mbrAccountLogService.outCallSetLog(outCallPlatform);
    }

    /**
     * 	根据keys查询参数表
     * 
     * @param keys
     * @return
     */
    public List<SysSetting> getSysSettingList(List<String> keys) {
        return stationSetMapper.getSysSettingList(keys);
    }

    /**
     * 校验出入款完整设置
     *
     * @param account
     */
    public void checkPayCondition(MbrAccount account, String key) {
        SysSetting sysSetting = getSysSetting(key);
        String conStr = sysSetting.getSysvalue();
        if (StringUtil.isNotEmpty(conStr)) {
            Type jsonType = new TypeToken<List<Integer>>() {
            }.getType();
            List<Integer> cons = jsonUtil.fromJson(conStr, jsonType);
            for (Integer con : cons) {
                if (con == Constants.EVNumber.one) {//实名
                    if (StringUtil.isEmpty(account.getRealName())) {
                        throw new R200Exception("请先完善真实姓名");
                    }
                } else if (con == Constants.EVNumber.two) {//手机号
                    if (isNull(account.getIsVerifyMoblie()) || !new Byte("1").equals(account.getIsVerifyMoblie())) {
                        throw new R200Exception("请先完善手机号码");
                    }
                }
            }
        }
    }

    public List<SmsConfig> querySmsConfig() {
        SmsConfig smsConfig = new SmsConfig();
        List<SmsConfig> list = smsConfigMapper.querySmsConfig(smsConfig);
        return list;
    }

    public void modifySmsConfig(SmsConfigDto smsConfigDto, String userName) {
        String modifyTime = getCurrentDate(FORMAT_18_DATE_TIME);
        // 循环更新短信配置数据
        for (SmsConfig smsConfig : smsConfigDto.getSmsConfigs()) {
            smsConfig.setModifyUser(userName);
            smsConfig.setModifyTime(modifyTime);
            smsConfig.setMobileAreaCode(null);      // 前端送这个，不更新
            smsConfigMapper.updateByPrimaryKeySelective(smsConfig);

            //添加操作日志
            mbrAccountLogService.updateSysSmsConfigLog(smsConfig);
        }
    }

    // 获取APP下载设置
    public AppDownloadSet queryAppDownloadSet() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        AppDownloadSet appDownloadSet = new AppDownloadSet();
        for (SysSetting ss : list) {
            if (SystemConstants.APP_DOWNLOAD_ANDORID_URL.equals(ss.getSyskey())) {
                appDownloadSet.setAndroidDownloadUrl(ss.getSysvalue());
            }
            if (SystemConstants.APP_DOWNLOAD_IOS_URL.equals(ss.getSyskey())) {
                appDownloadSet.setIosDownloadUrl(ss.getSysvalue());
            }
        }
        return appDownloadSet;
    }

    // APP下载设置
    public void appDownloadSet(AppDownloadSet appDownloadSet) {
        List<String> syskeys = Lists.newArrayList(
                SystemConstants.APP_DOWNLOAD_ANDORID_URL,
                SystemConstants.APP_DOWNLOAD_IOS_URL);
        // 获得原来的数据
//        SysSetting sysSettingOld = sysSettingMapper.selectByPrimaryKey(SystemConstants.APP_DOWNLOAD_ANDORID_URL);
//        SysSetting sysSettingOld2 = sysSettingMapper.selectByPrimaryKey(SystemConstants.APP_DOWNLOAD_IOS_URL);
        // 删除原数据
        deleteSysSetting(syskeys);
        // 插入新数据
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.APP_DOWNLOAD_ANDORID_URL);
        sysSetting.setSysvalue(appDownloadSet.getAndroidDownloadUrl());
        sysSettingMapper.insert(sysSetting);
        sysSetting.setSyskey(SystemConstants.APP_DOWNLOAD_IOS_URL);
        sysSetting.setSysvalue(appDownloadSet.getIosDownloadUrl());
        sysSettingMapper.insert(sysSetting);

        // 记录日志
        mbrAccountLogService.appDownloadSet(appDownloadSet);
    }

	/**
	 * 	原生投注设置
	 * 
	 * @return
	 */
	public NativeSports queryNativeSports() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        NativeSports nativeSports = new NativeSports();
        for (SysSetting ss : list) {
            if (SystemConstants.IS_OPEN_CAROUSEL.equals(ss.getSyskey())) {
            	nativeSports.setIsOpenCarousel(ss.getSysvalue());
            }
            if (SystemConstants.IS_OPEN_NOTICE.equals(ss.getSyskey())) {
            	nativeSports.setIsOpenNotice(ss.getSysvalue());
            }
        }
        return nativeSports;
	}

	public void updateNativeSports(NativeSports nativeSports) {
		List<String> syskeys = Lists.newArrayList(
                SystemConstants.IS_OPEN_CAROUSEL,
                SystemConstants.IS_OPEN_NOTICE);
        // 删除原数据
        deleteSysSetting(syskeys);
        // 插入新数据
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.IS_OPEN_CAROUSEL);
        sysSetting.setSysvalue(nativeSports.getIsOpenCarousel());
        sysSettingMapper.insert(sysSetting);
        sysSetting.setSyskey(SystemConstants.IS_OPEN_NOTICE);
        sysSetting.setSysvalue(nativeSports.getIsOpenNotice());
        sysSettingMapper.insert(sysSetting);

        // 记录日志
        mbrAccountLogService.updateNativeSports(nativeSports);
	}


    public void  updateCuiDanSet(CuiDanSet cuiDanSet){
        List<String> syskeys = Lists.newArrayList(
                SystemConstants.SYS_CUIDAN);
        deleteSysSetting(syskeys);
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.SYS_CUIDAN);
        sysSetting.setSysvalue(cuiDanSet.getCuiDan().toString());
        sysSettingMapper.insert(sysSetting);
    }


    public Integer  queryCuiDanSet(){
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(SystemConstants.SYS_CUIDAN);
        SysSetting setting = sysSettingMapper.selectOne(sysSetting);
        return setting == null ? 0 : Integer.valueOf(setting.getSysvalue());
    }

    /**
     *
     * @return
     */
    public List<SysSetting> queryBroadcastSwitchSetting() {
        Example example = new Example(SysSetting.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("syskey",
                Arrays.asList(SystemConstants.IS_BROADCASTED_NEW_BONUS_GENERATION,
                        SystemConstants.IS_BROADCASTED_NEW_DEPOSIT_ORDER_GENERATION,
                        SystemConstants.IS_BROADCASTED_NEW_WITHDRAW_ORDER_GENERATION));

        return sysSettingMapper.selectByExample(example);

    }

    public void updateBroadcastSwitchSetting(BroadcastVo vo) {
        String key = vo.getKey();
        Assert.isTrue(vo.getStatus() >= 0 && vo.getStatus() <= 1, "status取值在0到1之间");
        Assert.hasText(key, "关键字不能为空");
        Assert.isTrue(SystemConstants.IS_BROADCASTED_NEW_BONUS_GENERATION.equals(key)
                || SystemConstants.IS_BROADCASTED_NEW_DEPOSIT_ORDER_GENERATION.equals(key)
                || SystemConstants.IS_BROADCASTED_NEW_WITHDRAW_ORDER_GENERATION.equals(key),
                "关键字不合法");
        Example example = new Example(SysSetting.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("syskey", key);
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSysvalue(vo.getStatus() + "");
        // 说明：根据Example条件更新实体record包含的不是null的属性值
        sysSettingMapper.updateByExampleSelective(sysSetting, example);
    }
}
