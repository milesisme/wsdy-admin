package com.wsdy.saasops.modules.agent.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.wsdy.saasops.modules.agent.dto.CommDetailsDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Table(name = "agy_commission")
@ApiModel("代理报表")
public class AgyCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "代理id")
    private Integer agentId;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "周期开始")
    private String cycleStart;

    @ApiModelProperty(value = "周期结束")
    private String cycleEnd;

    @ApiModelProperty(value = "活跃人数")
    private Integer activeNum;

    @ApiModelProperty(value = "周期投注额")
    private BigDecimal cycleBet;

    @ApiModelProperty(value = "净输赢")
    private BigDecimal netwinlose;

    @ApiModelProperty(value = "净输赢冲销")
    private BigDecimal writeOff;

    @ApiModelProperty(value = "佣金比例")
    private BigDecimal rate;

    @ApiModelProperty(value = "佣金发放额")
    private BigDecimal commission;

    @ApiModelProperty(value = "审核状态 1通过 0不通过  2待处理")
    private Integer reviewStatus;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "财务备注")
    private String financeMemo;

    @ApiModelProperty(value = "发放状态 1通过 0不通过  2待处理")
    private Integer issuestatus;

    @ApiModelProperty(value = "月")
    private String time;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "1下级返佣 0会员返佣")
    private Integer type;

    @ApiModelProperty(value = "下级代理账号")
    private String subAgyaccount;

    @ApiModelProperty(value = "代理id")
    private Integer subAgentId;

    @ApiModelProperty(value = "审核人")
    private String auditUser;

    @ApiModelProperty(value = "审核时间")
    private String auditTime;

    @ApiModelProperty(value = "通过人")
    private String passUser;

    @ApiModelProperty(value = "通过时间")
    private String passTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;

    @ApiModelProperty(value = "费用调整")
    private BigDecimal adjustedAmount;

    @ApiModelProperty(value = "奖励红利")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "任务返利")
    private BigDecimal taskAmount;

    @ApiModelProperty(value = "返水优惠")
    private BigDecimal rebateAmount;

    @ApiModelProperty(value = "输赢")
    private BigDecimal payout;

    @ApiModelProperty(value = "平台费")
    private BigDecimal cost;
    
    @ApiModelProperty(value = "服务费")
    private BigDecimal serviceCost;

    @ApiModelProperty(value = "结算费模式  1，平台费  2，服务费 3全部")
    private Integer feeModel;
    
    @ApiModelProperty(value = "服务费存款比例")
    private BigDecimal depositServicerate;
    
    @ApiModelProperty(value = "服务费取款比例")
    private BigDecimal withdrawServicerate;
    
    @ApiModelProperty(value = "平台费额外比例")
    private BigDecimal  additionalServicerate;

    @Transient
    @ApiModelProperty(value = "下级返佣")
    private BigDecimal subCommission;

    @Transient
    @ApiModelProperty(value = "开始时间开始")
    private String startTime;

    @Transient
    @ApiModelProperty(value = "开始时间结束")
    private String endTime;

    @Transient
    @ApiModelProperty(value = "清除净输赢冲销 1是  0否")
    private Integer isClean;


    @Transient
    @ApiModelProperty(value = "详情上级返佣")
    private List<CommDetailsDto> detailsDtos;

    @Transient
    @ApiModelProperty(value = "平台费用")
    private List<AgyCommissionDepot> depotList;

    @Transient
    @ApiModelProperty(value = "1下级返佣 0会员返佣")
    private String typeStr;

    @Transient
    @ApiModelProperty(value = "周期导出")
    private String cycleTime;

    @Transient
    @ApiModelProperty(value = "审核状态 1通过 0不通过  2待处理")
    private String reviewStatusStr;

    @Transient
    @ApiModelProperty(value = "发放状态 1通过 0不通过  2待处理")
    private String issuestatusStr;
    
    @Transient
    @ApiModelProperty(value = "代理类别： 0：股东，1：总代，2：一级代理，3：二级代理")
    private Integer agentType;
    
    @Transient
    @ApiModelProperty(value = "代理类别： 0：股东，1：总代，2：一级代理，3：二级代理")
    private String agentTypeStr;
    
}