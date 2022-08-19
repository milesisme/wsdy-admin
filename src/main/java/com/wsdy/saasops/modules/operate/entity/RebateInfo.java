package com.wsdy.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class RebateInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "返点时间")
    private String reportTime;

    @ApiModelProperty(value = "会员名称")
    private String loginName;

    @ApiModelProperty(value = "会员组名称")
    private String groupName;

    @ApiModelProperty(value = "会员id")
    private Integer accountId;

    @ApiModelProperty(value = "分类id")
    private Integer catId;

    @ApiModelProperty(value = "来源好友")
    private Integer count;

    @ApiModelProperty(value = "本人返点")
    private BigDecimal rebateAmount;

    @ApiModelProperty(value = "贡献上级返点")
    private BigDecimal contributeAmount;

    @ApiModelProperty(value = "总输赢")
    private BigDecimal totalResult;

    @ApiModelProperty(value = "发放开始时间")
    private String startTime;

    @ApiModelProperty(value = "发放结束时间")
    private String endTime;

    @ApiModelProperty(value = "统计时间")
    private String startday;

    @ApiModelProperty(value = "发放时间")
    private String createTime;

    @ApiModelProperty(value = "会员组id")
    private Integer groupId;

    @ApiModelProperty(value = "返利种类")
    private String financialCode;

    @ApiModelProperty(value = "0 拒绝 1 通过 2待处理")
    private Integer status;

    @ApiModelProperty(value = "推荐人")
    private String referrer;

    @ApiModelProperty(value = "有效投注字符串")
    private List<RebateCat> rebateCatList;

    @Transient
    private String catIds;

    @Transient
    private String validbets;
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;
    @Transient
    @ApiModelProperty(value = "总代 top")
    private Integer tagencyId;

    @Transient
    @ApiModelProperty(value = "会员层级")
    private Integer depth;

    @ApiModelProperty(value = "fund_audit表ID")
    private String auditId;
}