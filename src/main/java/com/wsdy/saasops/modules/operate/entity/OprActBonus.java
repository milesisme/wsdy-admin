package com.wsdy.saasops.modules.operate.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.wsdy.saasops.modules.operate.dto.AuditCat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "OprActBonus", description = "活动审核表（红利）")
@Table(name = "opr_act_bonus")
public class OprActBonus implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "申请时间")
    private String applicationTime;
    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;
    @ApiModelProperty(value = "存款ID")
    private Integer depositId;
    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;
    @ApiModelProperty(value = "活动ID")
    private Integer activityId;
    @ApiModelProperty(value = "审核人")
    private String auditUser;
    @ApiModelProperty(value = "审核时间")
    private String auditTime;
    @ApiModelProperty(value = "0 拒绝 or 未通过 1成功 or 已使用 2待处理 ( 3 可使用 4已失效 )")
    private Integer status;
    @ApiModelProperty(value = "奖励红利")
    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "审核备注")
    private String memo;
    @ApiModelProperty(value = "1 显示有效投注额 2显示存款金额 3隐藏该字段")
    private Integer isShow;
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositedAmount;
    @ApiModelProperty(value = "活动规则ID")
    private Integer ruleId;
    @ApiModelProperty(value = "稽核ID")
    private Integer auditId;
    @ApiModelProperty(value = "优惠流水倍数")
    private BigDecimal discountAudit;
    @ApiModelProperty(value = "有效投注流水范围 0输 1赢 2全部 null 全部")
    private Integer scope;
    @ApiModelProperty(value = "IP")
    private String ip;
    @ApiModelProperty(value = "设备来源; PC:0 H5:3")
    private Byte devSource;
    @ApiModelProperty(value = "转账金额")
    private BigDecimal transferAmount;
    @ApiModelProperty(value = "平台ID")
    private Integer depotId;
    @ApiModelProperty(value = "分类ID")
    private Integer catId;
    @ApiModelProperty(value = "稽核")
    private BigDecimal auditAmount;
    @ApiModelProperty(value = "创建人")
    private String createUser;
    @ApiModelProperty(value = "备注")
    private String applicationMemo;
    @ApiModelProperty(value = "waterdateid")
    private Integer waterdateid;
    @ApiModelProperty(value = "派彩")
    private BigDecimal payout;
    @ApiModelProperty(value = "活动层级id")
    private Integer actLevelId;
    @ApiModelProperty(value = "0 前台申请  1后台添加 2 人工增加 3 人工减少")
    private Integer source;
    @ApiModelProperty(value = "抽奖记录id")
    private Integer lotteryId;
    @ApiModelProperty(value = "抽奖名称")
    private String prizename;
    @ApiModelProperty(value = "类型 默认0彩金  1实物")
    private Integer prizetype;
    @ApiModelProperty(value = "订单前缀")
    private String orderPrefix;
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "financialCode")
    private String financialCode;
    @ApiModelProperty(value = "调整金额人员")
    private String modifyAmountUser;
    @ApiModelProperty(value = "调整金额时间")
    private String modifyAmountTime;
    @ApiModelProperty(value = "调整金额备注")
    private String modifyAmountMemo;
    @ApiModelProperty(value = "混合活动子规则code  存就送AQ0000003 投就送AQ0000012 救援金AQ0000004 其他AQ0000015")
    private String subRuleTmplCode;

    //查询使用
    @Transient
    @ApiModelProperty(value = "设备来源; PC:0 H5:3")
    private String devSourceList;
    @Transient
    @ApiModelProperty(value = "开始时间")
    private String startTime;
    @Transient
    @ApiModelProperty(value = "结束时间")
    private String endTime;
    @Transient
    @ApiModelProperty(value = "同一真实姓名对应的会员数量")
    private Integer realNameCount;
    @Transient
    @ApiModelProperty(value = "同一IP对应的会员数量")
    private Integer registerIpCount;
    @Transient
    @ApiModelProperty(value = "注册IP")
    private String registerIp;
    @Transient
    @ApiModelProperty(value = "注册时间")
    private String registerTime;
    @Transient
    @ApiModelProperty(value = "活动分类编号")
    private String tmplCode;
    @Transient
    @ApiModelProperty(value = "活动名称")
    private String activityName;
    @Transient
    @ApiModelProperty(value = "活动类型")
    private String tmplName;
    @Transient
    @ApiModelProperty(value = "活动分类")
    private String catName;
    @Transient
    @ApiModelProperty(value = "直属代理")
    private String agyAccount;
    @Transient
    @ApiModelProperty(value = "总代理")
    private String topAgyAccount;
    @Transient
    @ApiModelProperty(value = "直属代理id")
    private Integer cagencyId;
    @Transient
    @ApiModelProperty(value = "总代理id")
    private Integer tagencyId;
    @Transient
    @ApiModelProperty(value = "分组id")
    private Integer groupId;
    @Transient
    @ApiModelProperty(value = "活动类型ID")
    private Integer actTmplId;
    @Transient
    @ApiModelProperty(value = "活动分类Id")
    private Integer actCatId;
    @Transient
    @ApiModelProperty(value = "是否勾选活动类型 true是")
    private Boolean groupTmpl;
    @Transient
    @ApiModelProperty(value = "是否勾活动分类 true是")
    private Boolean groupCat;
    @Transient
    @ApiModelProperty(value = "是否勾总代 true是")
    private Boolean groupTagency;
    @Transient
    @ApiModelProperty(value = "是否勾代理 true是")
    private Boolean groupCagency;
    @Transient
    @ApiModelProperty(value = "是否勾会员名 true是")
    private Boolean groupAccount;
    @Transient
    @ApiModelProperty(value = "数量")
    private Integer countNum;
    @Transient
    @ApiModelProperty(value = "sql标志")
    private int isStatus;
    @Transient
    @ApiModelProperty(value = "审核时间开始")
    private String auditTimeFrom;
    @Transient
    @ApiModelProperty(value = "审核时间结束")
    private String auditTimeTo;
    @Transient
    @ApiModelProperty(value = "活动状态")
    private int useState;
    @Transient
    @ApiModelProperty(value = "总代理id 查询使用")
    private List<Integer> tagencyIds;
    @Transient
    @ApiModelProperty(value = "直属代理id 查询使用")
    private List<Integer> cagencyIds;
    @Transient
    @ApiModelProperty(value = "活动类型ID 查询使用")
    private List<Integer> actTmplIds;
    @Transient
    @ApiModelProperty(value = "活动ID 查询使用")
    private List<Integer> activityIds;
    @Transient
    @ApiModelProperty(value = "活动ID 查询使用")
    private List<Integer> accountIds;
    @Transient
    @ApiModelProperty(value = "0 拒绝 1通过 2待处理 查询使用")
    private List<Integer> statuss;
    @Transient
    @ApiModelProperty(value = "会员组名称")
    private String groupName;
    @Transient
    @ApiModelProperty(value = "总代")
    private String generalAccount;
    @Transient
    @ApiModelProperty(value = "稽核规则")
    private List<AuditCat> auditRule;
    @Transient
    @ApiModelProperty(value = "会员等级")
    private String tierName;
    @Transient
    @ApiModelProperty(value = "返水注单统计时间开始")
    private String waterStart;
    @Transient
    @ApiModelProperty(value = "返水注单统计时间结束")
    private String waterEnd;
    @Transient
    @ApiModelProperty(value = "返水批处理时间开始")
    private String applicationTimeStart;
    @Transient
    @ApiModelProperty(value = "返水批处理时间结束")
    private String applicationTimeEnd;
    @Transient
    @ApiModelProperty(value = "生日年")
    private String applicationTimeYear;
    @Transient
    @ApiModelProperty(value = "分类名称")
    private String gmCatName;
    @Transient
    @ApiModelProperty(value = "活动id集合,导出活动集合")
    List<Integer> ids;

    @Transient
    @ApiModelProperty(value = "是否黑名单")
    private String isBlack;
}