package com.wsdy.saasops.modules.lottery.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LotteryAreaDto {

    @ApiModelProperty(value = "剩余次数")
    private Integer remainingTimes;


    @ApiModelProperty(value = "奖区")
    private Integer prizeArea;

    @ApiModelProperty(value = "奖区明细")
    private List<LotteryPrizeAreaDto> prizeAreaDtos;

    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;

    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;

    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    @ApiModelProperty(value = "已验证邮箱 true是 false 否")
    private Boolean isMail;

    @ApiModelProperty(value = "等级id集合")
    private List<Integer> actLevelIds;

    @ApiModelProperty(value = "域名集合注册来源")
    private String domains;


    @ApiModelProperty(value = "注册时间 0不限制  1多少天内注册  2日期之后注册")
    private Integer registerCondition;
    @ApiModelProperty(value = "1多少天内注册")
    private Integer registerNum;
    @ApiModelProperty(value = "2日期之后注册")
    private String registerDate;


    @ApiModelProperty(value = "抽奖次数统计赠送周期： 0 每日  1周  2活动期间")
    private Integer cycle;

    // 从域名注册条件及赠送次数
    @ApiModelProperty(value = "域名是否选择 true选择 false未选择")
    private Boolean domainsCondition;
    @ApiModelProperty(value = "从域名注册")
    private String registerDomains;
    @ApiModelProperty(value = "域名注册送多少次数")
    private Integer num;

    // 累计充值、首次单笔充值、有效投注条件及赠送次数
    @ApiModelProperty(value = "存款和有效投注条件")
    private List<LotteryDepositDto> depositDtos;

    // App注册登录赠送次数
    @ApiModelProperty(value = "是否勾选 true选择 false未选择")
    private Boolean isSelectedFirstLogin;
    @ApiModelProperty(value = "首次登录赠送次数")
    private Integer numFirstLogin;
    // 每日首次登录赠送次数
    @ApiModelProperty(value = "是否勾选 true选择 false未选择")
    private Boolean isSelectedFirstLoginDay;
    @ApiModelProperty(value = "每日首次登录赠送次数")
    private Integer numFirstLoginDay;
    // 首次绑定银行卡
    @ApiModelProperty(value = "是否勾选 true选择 false未选择")
    private Boolean isSelectedFirstBindBank;
    @ApiModelProperty(value = "首次登录赠送次数")
    private Integer numFirstBindBank;

    @ApiModelProperty(value = "活动id")
    private Integer activityId;
}
