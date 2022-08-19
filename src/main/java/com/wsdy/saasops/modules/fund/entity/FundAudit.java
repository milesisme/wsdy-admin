package com.wsdy.saasops.modules.fund.entity;

import com.wsdy.saasops.modules.base.entity.BaseAuth;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "FundAudit", description = "")
@Table(name = "fund_audit")
public class FundAudit implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "活动ID")
    private Integer activityId;
    @ApiModelProperty(value = "会员登陆名称")
    private String loginName;
    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;
    @ApiModelProperty(value = "稽核ID")
    private Integer auditId;
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "订单前缀")
    private String orderPrefix;
    @ApiModelProperty(value = "调整金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "存款类型 1人工调整 3推广存入（好友返利）  2优惠活动(废弃)  0其他(废弃)  4.人工增加返利(废弃)")
    private Integer depositType;
    @ApiModelProperty(value = "人工增加类型 0 优惠添加 1 额度补回 3 其他 4代理充值")
    private Integer auditAddType;
    @ApiModelProperty(value = "人工减少类型 0 风控扣款、1优惠冲销、2额度冲销、3入款冲销、4其他")
    private Integer reduceType;
    @ApiModelProperty(value = "0 免稽核 1稽核")
    private Integer auditType;
    @ApiModelProperty(value = "稽核倍数")
    private Integer auditMultiple;
    @ApiModelProperty(value = " 1 清除稽核点")
    private Integer isClear;
    @ApiModelProperty(value = "清除稽核点时间")
    private String auditClearTime;
    @ApiModelProperty(value = "备注")
    private String memo;
    @ApiModelProperty(value = "")
    private String createUser;
    @ApiModelProperty(value = "")
    private String createTime;
    @ApiModelProperty(value = "")
    private String modifyUser;
    @ApiModelProperty(value = "")
    private String modifyTime;
    @ApiModelProperty(value = "0 拒绝 1 通过 2待处理")
    private Integer status;
    @ApiModelProperty(value = "审核人")
    private String auditUser;
    @ApiModelProperty(value = "审核时间")
    private String auditTime;
    @ApiModelProperty(value = "调整类别code AA 人工增加 AM人工减少 FA返利增加")
    private String financialCode;
    
    @ApiModelProperty(value = "是否计算到代理净盈利")
    private Boolean isCalculateProfit;
    
    @Transient
    @ApiModelProperty(value = "是否计算到代理净盈利")
    private String isCalculateProfitStr;
    @Transient
    @ApiModelProperty(value = "人工增加类型 导出报表使用")
    private String auditAddTypeStr;
    @Transient
    @ApiModelProperty(value = "状态 导出报表使用")
    private String statusStr;
    @Transient
    @ApiModelProperty(value = "会员ID")
    private List<Integer> ids;
    @Transient
    @ApiModelProperty(value = "代理账号")
    private String agyAccount;
    @Transient
    @ApiModelProperty(value = "上级代理")
    private String topAgyAccount;
    @Transient
    @ApiModelProperty(value = "申请时间开始")
    private String createTimeFrom;
    @Transient
    @ApiModelProperty(value = "申请时间结束")
    private String createTimeTo;
    @Transient
    @ApiModelProperty(value = "会员总数")
    private Integer accCount;
    @Transient
    @ApiModelProperty(value = "调整总额")
    private BigDecimal sumAmount;
    @Transient
    @ApiModelProperty(value = "调整后主账户余额")
    private BigDecimal afterBalance;
    @Transient
    @ApiModelProperty(value = "调整前主账户余额")
    private BigDecimal beforeBalance;
    @Transient
    @ApiModelProperty(value = "总代 or 代理")
    private Integer parentId;
    @Transient
    @ApiModelProperty(value = "真实姓名")
    private String realName;
    @Transient
    @ApiModelProperty(value = "直属代理iD")
    private Integer cagencyId;
    @Transient
    @ApiModelProperty(value = "总代iD")
    private Integer tagencyId;
    @Transient
    @ApiModelProperty(value = "类型")
    private String causeName;
    @Transient
    private BaseAuth baseAuth;
    @Transient
    @ApiModelProperty(value = "登录用户的名字")
    private String loginSysUserName;
    @Transient
    @ApiModelProperty(value = "总代iD 查询使用")
    private List<Integer> tagencyIds;
    @Transient
    @ApiModelProperty(value = "直属代理iD 查询使用")
    private List<Integer> cagencyIds;
    @Transient
    @ApiModelProperty(value = "调整类别code AA 人工增加 AM人工减少 查询使用")
    private List<String> financialCodes;
    @Transient
    @ApiModelProperty(value = "存款类型0其他 1人工存款 2优惠活动 查询使用")
    private List<Integer> depositTypes;
    @Transient
    @ApiModelProperty(value = "0 拒绝 1 通过 2待处理 查询使用")
    private List<Integer> statuss;
    @Transient
    @ApiModelProperty(value = "查询使用：人工增加类型 0 优惠添加 1 额度补回  3其他 4代理充值")
    private List<Integer> auditAddTypes;
    @Transient
    @ApiModelProperty(value = "查询使用：人工减少类型 0 风控扣款、1优惠冲销、2额度冲销、3入款冲销、4其他")
    private List<Integer> reduceTypes;
    @Transient
    @ApiModelProperty(value = "查询使用：调整类型，根据financialCode变化 ")
    private Integer adjustType;
}