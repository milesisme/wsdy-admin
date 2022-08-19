package com.wsdy.saasops.modules.lottery.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "OprActLottery", description = "抽奖")
@Table(name = "opr_act_lottery")
public class OprActLottery implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "批次")
    private Integer batchnumber;

    @ApiModelProperty(value = "奖区")
    private Integer prizeArea;

    @ApiModelProperty(value = "奖品类型 1谢谢参与,2彩金,3实物奖品")
    private Integer prizetype;

    @ApiModelProperty(value = "奖品名称")
    private String prizename;

    @ApiModelProperty(value = "奖品金额")
    private BigDecimal donateamount;

    @ApiModelProperty(value = "随机数")
    private Integer random;

    @ApiModelProperty(value = "活动id")
    private Integer activityid;

    @ApiModelProperty(value = "创建时间")
    private String createtime;

    @ApiModelProperty(value = "修改时间")
    private String updatetime;

    @ApiModelProperty(value = "优惠流水倍数")
    private Integer discountAudit;
}