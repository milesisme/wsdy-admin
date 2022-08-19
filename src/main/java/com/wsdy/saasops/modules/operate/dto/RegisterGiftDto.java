package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "BettingGiftDto", description = "注册送")
public class RegisterGiftDto {

    @ApiModelProperty(value = "流水范围")
    private List<AuditCat> auditCats;
    /**
     * 注册来源
     */
    @ApiModelProperty(value = "注册来源 app true是 false 否")
    private Boolean appClient;
    @ApiModelProperty(value = "注册来源 pc true是 false 否")
    private Boolean pcClient;
    @ApiModelProperty(value = "注册来源 wap true是 false 否")
    private Boolean H5Client;
    @ApiModelProperty(value = "注册来源 管理后台 true是 false 否")
    private Boolean adminManage;
    @ApiModelProperty(value = "注册来源 代理后台 true是 false 否")
    private Boolean agentManage;
    @ApiModelProperty(value = "注册来源 代理后台 true是 false 否")
    private Boolean friendRegister;

    /**
     * 申请条件
     */
    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;
    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;
    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;
    @ApiModelProperty(value = "流水倍数")
    private Double multipleWater;
    @ApiModelProperty(value = "领取期限 自注册成功起drawType天内领取  * 活动期间注册方可领取，0 天代表不限制领取时限")
    private Integer drawType;
}
