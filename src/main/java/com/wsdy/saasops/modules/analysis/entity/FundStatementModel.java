package com.wsdy.saasops.modules.analysis.entity;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FundStatementModel {

    private String createTime;   //时间
    private BigDecimal totalProfit;    //净盈利
    
    private BigDecimal totalDepositBalance;   //总存款

    private BigDecimal fundAdjust;     //资金调整
    private BigDecimal totalRebate;     //总返点
    
    private BigDecimal totalDrawAmount;    //总提款

    private BigDecimal totalBonusAmount;    //总红利

    @ApiModelProperty("线上活动总红利")
    private BigDecimal totalBonusAmountOnline;
    
    @ApiModelProperty("线下活动总红利")
    private BigDecimal totalBonusAmountOffline;

    private BigDecimal totalPayout;      //总派彩
    
    @ApiModelProperty(value = "投注总额")
    private BigDecimal totalValidBet;
    private BigDecimal totalJackpotPayout;    //奖池彩金
    
    private BigDecimal totalTaskBonus;  // 总任务返利

    private BigDecimal friendsTransAmountTotal;//好友转出
    private BigDecimal friendsRecepitAmountTotal;//好友

    private BigDecimal totalActualReward; // 好友返利
    
    private BigDecimal calculateProfit; // fundAdjust 

    private BigDecimal totalHuPengReward; // 呼朋换友返利

    private Integer pageNo;
    private Integer pageSize;

    private String platform;    //平台名称
    private Integer agyId;     //代理id
    private Integer tagencyId;     //总代id
    private Integer parentId;     //总代id
    private List<Integer> agyIds;
    private Integer isCagency;  //是否是子代 1是 0不是
    private Boolean isTest;  //是否是测试总代 true是 false不是

    private String agyAccount;
    
    private String loginName;   //会员名
    private Integer groupId;
    private String groupName;

    @ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;
    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @ApiModelProperty(value = "排序条件")
    private String orderBy;

    // 全民代理
    @ApiModelProperty(value = "是否全民代理查询标志 1是 0 否")
    private Integer isMbrAgyQry;
    
    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyFlag;

    private String  module;

    @ApiModelProperty(value = "注册时间")
    private String registerTime;
    
    @ApiModelProperty(value = "首存时间")
    private String depositTime;
    @ApiModelProperty(value = "代理类型 1，总代， 2一级代理， 3二级代理")
    private  String agentType;
}
