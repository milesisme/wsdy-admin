package com.wsdy.saasops.modules.member.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;



@Setter
@Getter
@ApiModel(value = "MbrFreindTransDetail", description = "好友转账")
@Table(name = "mbr_friend_trans_detail")
public class MbrFriendTransDetail implements Serializable{
private static final long serialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "转账会员名称")
    private String transLoginName;

    @ApiModelProperty(value = "转账会员ID")
    private Integer transAccountId;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal transAmount;

    @ApiModelProperty(value = "转账前余额")
    private BigDecimal transBeforeBalance;

    @ApiModelProperty(value = "转账后余额")
    private BigDecimal transAfterBalance;

    @ApiModelProperty(value = "收款账户名称")
    private String receiptLoginName;

    @ApiModelProperty(value = "收款人会员ID ")
    private Integer receiptAccountId;

    @ApiModelProperty(value = "收款前余额")
    private BigDecimal receiptBeforeBalance;

    @ApiModelProperty(value = "收款后余额")
    private BigDecimal receiptAfterBalance;

    @ApiModelProperty(value = "客户段来源")
    private Byte transferSource;

    @ApiModelProperty(value = "创建时间")
    private String createTime;
    @Transient
    private String amount;

    @ApiModelProperty(value = "转出记录ID")
    private Integer mbrBillDetailTransId;
    @ApiModelProperty(value = "转入记录ID")
    private Integer mbrBillDetailReceipId;

    @Transient
    @ApiModelProperty(value = "转账类型 0转入 1转出")
    private Integer transType;

    @Transient
    @ApiModelProperty(value = "转账状态 1成功")
    private Integer status;

    @Transient
    @ApiModelProperty(value = "转入总计")
    private BigDecimal transInAmount;
    @Transient
    @ApiModelProperty(value = "转出总计")
    private BigDecimal transOutAmount;

}