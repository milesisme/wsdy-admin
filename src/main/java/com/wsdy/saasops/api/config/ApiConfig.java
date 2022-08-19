package com.wsdy.saasops.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "api")
@PropertySource("classpath:api.properties")
public class ApiConfig {
	//播音
    private String bbinUrl;
    private String bbinWebsite;
    private String bbinUppername;
    private String bbinTransferKey;
    private String bbinCheckTransfer;
    private String bbinCheckUsrBalanceKey;
    private String bbinTransferRecordKey;
    //登陆专用URL
    private String bbinLogin;
    //登陆成功HTML中标记
    private String bbinloginscmark;
    
    //注册专用
    private Integer regDeaultGroup;//默认会员组ID号
    private Integer regDeaultTagencyId;//默认总代ID号
    private Integer regDeaultCagencyId;//默认区域总代ID号
    private Integer defaultTestId;//默认测试区代理
    private Integer testAgentId;//默认测试总代
    private String  mtDefaultCagency; // 蜜桃默认代理

    //邮件
    private String mailAuth;
    private String mailTimeout;
    
    //登陆找回密码失验证码失效时间
    private Integer kaptchaRedisExpire;
    //登陆
    private Integer kaptchaRedisLoginExpire;
    //密码连续错误5次锁定时间默认180秒
    private Integer passwdLockTime;
    //密码记录错误次数最长时间为1天
    private Integer passwdLockIvnTime;


    private String mtNotifyDomain;
    private String mtNotifyKey;
}
