package com.wsdy.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RegisterSet {
    @ApiModelProperty("会员账号")
    private Integer loginName;
    @ApiModelProperty("会员登录密码")
    private Integer loginPwd;
    @ApiModelProperty("会员重复密码")
    private Integer reLoginPwd;
    @ApiModelProperty("会员验证码")
    private Integer captchareg;
    @ApiModelProperty("会员真实姓名")
    private Integer realName;
    @ApiModelProperty("会员手机")
    private Integer mobile;
    @ApiModelProperty("会员手机验证码")
    private Integer mobileCaptchareg;
    @ApiModelProperty("会员邮箱")
    private Integer email;
    @ApiModelProperty("会员qq")
    private Integer qq;
    @ApiModelProperty("会员微信")
    private Integer weChat;
    @ApiModelProperty("会员地址")
    private Integer address;

    @ApiModelProperty("代理账号")
    private Integer agentLoginName;
    @ApiModelProperty("代理登录密码")
    private Integer agentLoginPwd;
    @ApiModelProperty("代理重复密码")
    private Integer agentReLoginPwd;
    @ApiModelProperty("代理验证码")
    private Integer agentCaptchareg;
    @ApiModelProperty("代理真实姓名")
    private Integer agentRealName;
    @ApiModelProperty("代理手机")
    private Integer agentMobile;
    @ApiModelProperty("代理手机验证码")
    private Integer agentMobileCaptchareg;
    @ApiModelProperty("代理邮箱")
    private Integer agentEmail;
    @ApiModelProperty("代理qq")
    private Integer agentQQ;
    @ApiModelProperty("代理微信")
    private Integer agentWechat;
    @ApiModelProperty("代理地址")
    private Integer agentAddress;

    @ApiModelProperty("同ip数量")
    private Integer loginIp;
    @ApiModelProperty("同设备数量")
    private Integer deviceUuid;

    @ApiModelProperty("是否允许前台注册 0不允许 1允许")
    private Integer accWebRegister;

    @ApiModelProperty("选择开通注册方式 0 普通注册 1 普通注册+快捷模式 2 快捷模式")
    private Integer registerMethod;
    @ApiModelProperty("是否允许重复名注册 0不开启 1开启")
    private Integer realNameRepeat;
    @ApiModelProperty("是否显示推广码 0不开启 1开启")
    private Integer promotion;
}
