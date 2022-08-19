package com.wsdy.saasops.modules.agent.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AgentComMbrDto {

    /**
     *  总计信息(列表字段)：12个信息
     */
    // 资金报表 8个
    @ApiModelProperty(value = "净盈利")
    private BigDecimal totalProfit=BigDecimal.ZERO;
    @ApiModelProperty(value = "存款人数")
    private BigDecimal totalDepositBalanceNum=BigDecimal.ZERO;
    @ApiModelProperty(value = "存款金额：总存款")
    private BigDecimal totalDepositBalance=BigDecimal.ZERO;
    @ApiModelProperty(value = "提款人数")
    private BigDecimal totalDrawAmountNum=BigDecimal.ZERO;
    @ApiModelProperty(value = "提款金额：总提款")
    private BigDecimal totalDrawAmount=BigDecimal.ZERO;
    @ApiModelProperty(value = "优惠人数  优惠：任务+红利(活动)")
    private BigDecimal totalBonusAmountNum=BigDecimal.ZERO;
    @ApiModelProperty(value = "优惠金额  优惠：任务+红利(活动)")
    private BigDecimal totalBonusAmount=BigDecimal.ZERO;
    @ApiModelProperty(value = "派彩金额：总派彩")
    private BigDecimal totalPayout=BigDecimal.ZERO;
    // 总览 4个
    @ApiModelProperty(value = "活跃人数:活跃会员")
    private BigDecimal totalActiveMbrs=BigDecimal.ZERO;
    @ApiModelProperty(value = "新增会员")
    private BigDecimal totalNewMbrs=BigDecimal.ZERO;
    @ApiModelProperty(value = "首存人数：时间范围内首存人数")
    private BigDecimal totalNewDeposits=BigDecimal.ZERO;
    @ApiModelProperty(value = "有效投注额：总有效投注")
    private BigDecimal totalValidBets=BigDecimal.ZERO;

    @ApiModelProperty(value = "存提比例:取款金额/存款金额")
    private BigDecimal ctRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "流水比例:总有效流水/（总红利+存款）")
    private BigDecimal lsRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "输赢比例:输赢值（派彩）/投注额")
    private BigDecimal syRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "优惠比例:优惠金额/存款金额")
    private BigDecimal yhRatio=BigDecimal.ZERO;
    @ApiModelProperty(value = "存提差:存款总额-提款总额")
    private BigDecimal ctDiffer=BigDecimal.ZERO;
    @ApiModelProperty(value = "投注最多的平台")
    private String mostBetsPlat;
    /**
     * 列表字段
     */
    @ApiModelProperty(value = "日期")
    private String createTime;

    @ApiModelProperty(value = "代理id")
    private Integer agyId;
    @ApiModelProperty(value = "代理名称")
    private String agyAccount;
    @ApiModelProperty(value = "上级代理id")
    private Integer parentId;
    @ApiModelProperty(value = "部门")
    private Integer departmentid;
    @ApiModelProperty(value = "总代id")
    private Integer tagencyId;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "会员id")
    private Integer accountId;
    @ApiModelProperty(value = "净输赢冲销")
    private BigDecimal netwinlose;
    @ApiModelProperty(value = "平台费用")
    private BigDecimal cost;



}