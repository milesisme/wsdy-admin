package com.wsdy.saasops.listener;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Data
public class BizEvent extends ApplicationEvent {
    @ApiModelProperty(value = "站点code")
    private String siteCode;
    @ApiModelProperty(value = "会员Id")
    private Integer userId;
    @ApiModelProperty(value = "会员名")
    private String userName;
    @ApiModelProperty(value = "代理Id")
    private Integer agencyId;
    @ApiModelProperty(value = "")
    private BizEventType eventType;
    @ApiModelProperty(value = "")
    private String oldPassword;
    @ApiModelProperty(value = "")
    private String newPassword;
    @ApiModelProperty(value = "存款金额")
    private BigDecimal despoitMoney;
    @ApiModelProperty(value = "订单号")
    private String orderNum;
    @ApiModelProperty(value = "优惠(活动)金额")
    private BigDecimal acvitityMoney;
    @ApiModelProperty(value = "优惠活动名称")
    private String acvitityName;
    @ApiModelProperty(value = "提款金额")
    private BigDecimal withdrawMoney;
    @ApiModelProperty(value = "佣金")
    private BigDecimal commssion;
    @ApiModelProperty(value = "")
    private String term;
    @ApiModelProperty(value = "会员登陆名称")
    private String loginName;
    @ApiModelProperty(value = "好友转账金额")
    private BigDecimal transAmount;
    @ApiModelProperty(value = "银行卡号/加密货币钱包地址")
    private String cardNo;
    @ApiModelProperty(value = "")
    private String realName;
    @ApiModelProperty(value = "")
    private String email;
    @ApiModelProperty(value = "")
    private String mobile;

    // 通用
    public BizEvent(Object source, String siteCode, Integer userId, BizEventType eventType) {
        super(source);
        this.eventType = eventType ;
        this.siteCode = siteCode;
        this.userId =userId ;
    }

    // 银行卡/钱包绑定解绑
    public BizEvent(Object source, String siteCode, Integer userId, String cardNo, BizEventType eventType) {
        super(source);
        this.eventType = eventType;
        this.siteCode = siteCode;
        this.userId =userId ;
        this.cardNo = cardNo;
    }
    public BizEvent(Object source, String siteCode, Integer userId, BizEventType eventType,String oldPassword,String newPassword) {
        super(source);
        this.eventType = eventType ;
        this.siteCode = siteCode;
        this.userId =userId ;
        this.oldPassword=oldPassword;
        this.newPassword = newPassword ;
    }
    public BizEvent(Object source, String siteCode, Integer userId, BizEventType eventType, BigDecimal despoitMoney, String orderNum) {
        super(source);
        this.siteCode = siteCode;
        this.userId = userId;
        this.eventType = eventType;
        this.despoitMoney = despoitMoney;
        this.orderNum = orderNum;
    }
}