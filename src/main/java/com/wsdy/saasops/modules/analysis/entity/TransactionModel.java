package com.wsdy.saasops.modules.analysis.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransactionModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**类型**/
    private String transactionName;
    private String orderNo;
    /**时间**/
    private Date createTime;
    /***帐号**/
    private String accountName;
    /***金额**/
    private BigDecimal amount;
    /***备注***/
    private String memo;
    /***添加时备注***/
    private String applicationMemo;
    /***审核人**/
    private String auditUser;
    /***方式**/
    private String transactionType;
    /***活动字段***/
    /**活动名称*/
    private String activityName;
    /**活动分类*/
    private String catName;
    /**申请时间*/
    private String applicationTime;
    /**红利金额**/
    private BigDecimal bonusAmount;
    /**银行代码*/
    private String bankcode;
    /**入款类别 0：线上 1：公司*/
    private String mark;
    /**实际出款*/
    private Double withdraw;

    @ApiModelProperty(value="操作人")
    private String modifyamountuser;
    @ApiModelProperty(value="会员名")
    private String userName;
    @ApiModelProperty(value = "会员组名")
    private String mbrGroup;
    @ApiModelProperty(value = "总代")
    private String topAgent;
    @ApiModelProperty(value = "代理")
    private String agent;

    /** 是否线上活动，true：线上 false 线下  */
    private String isOnlineStr;
    
    @ApiModelProperty(value = "是否线上活动，true：线上 false 线下 ")
    private Boolean isOnline;
    
    @ApiModelProperty(value = "0 前台申请 1后台添加 2人工增加 3人工减少")
    private String source;
}
