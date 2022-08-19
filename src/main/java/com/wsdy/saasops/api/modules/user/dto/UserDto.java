package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;
import java.io.Serializable;



/**
 * 用户
 */
@Setter
@Getter
@ApiModel(value = "会员注册参数,参数必传与不传请参照接口", description = "")
public class UserDto implements Serializable {
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "会员账号,长度为6~10位! 游戏平台方长度限制")
	private String loginName;
	@ApiModelProperty(value = "会员密码,长度为6~18位!")
	private String loginPwd;
    @ApiModelProperty(value="图形验证码 可选")
	private String captchareg;
    @ApiModelProperty(value="行为验证码 可选")
    private String captchaVerification;
    @ApiModelProperty(value="验证码 可选")
	private String mobileCaptchareg;
    @ApiModelProperty(value="真实姓名 可选.")
    private String realName;
    @ApiModelProperty(value="手机区号：86中国，886台湾")
    private String mobileAreaCode;
    @ApiModelProperty(value="手机最大长度最大为11位,可选.")
    private String mobile;
    @ApiModelProperty(value="邮箱最长为30位,可选.")
    private String email;
    @ApiModelProperty(value="手机最大长度最大为11位,可选.")
    private String qq;
    @ApiModelProperty(value="微信最大长度最大为20位,可选.")
    private String weChat;
    @ApiModelProperty(value="地址最大长度最大为50位,可选.")
    private String address;

    @ApiModelProperty(value="代理推广代码长度最大为6位,可选.")
    private String spreadCode;
    @ApiModelProperty(value="推荐人ID")
    private Integer codeId;
    @Transient
    @ApiModelProperty(value="推广代理id")
    private String agentId;
    @Transient
    @ApiModelProperty(value="注册域名")
    private String mainDomain;
    @ApiModelProperty(value = "验证码标志")
    private String codeSign;
    @ApiModelProperty(value="注册方式 0 普通注册 1 普通注册+快捷模式 2 快捷模式")
    private String registerMethod;
    @ApiModelProperty(value="注册设备号")
    private String registerDevice;
    @ApiParam(value = "登录类型:0 PC、1 wap、2 移动端-IOS、3移动端-Android")
    private String loginType;


    /**
     * 推广类型 0好友，1呼朋
     */
    private Integer  promoteType;

}
