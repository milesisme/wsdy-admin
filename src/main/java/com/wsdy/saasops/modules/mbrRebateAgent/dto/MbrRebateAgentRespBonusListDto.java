package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "全民代理返回审核列表")
public class MbrRebateAgentRespBonusListDto {
    @ApiModelProperty(value = "mbr_rebate_agent_bonus表id")
    private Integer id;
    @ApiModelProperty(value = "会员ID")
    private Integer accountId;
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyflag;
    @ApiModelProperty(value = "代理会员级别id")
    private Integer agyLevelId;
    @ApiModelProperty(value = "代理会员级别名称")
    private String tierName;
    @ApiModelProperty(value = "成为全民代理的时间")
    private String agyTime;
    @ApiModelProperty(value = "计算时间")
    private String createTime;
    @ApiModelProperty(value = "统计时间 yyyy-MM")
    private String createTimeEx;
    @ApiModelProperty(value = "审核时间")
    private String auditTime;

    @ApiModelProperty(value = "自身的有效派彩=自身游戏派彩-自身红利 正为0")
    private BigDecimal validPayoutForSelf;
    @ApiModelProperty(value = "下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent ")
    private BigDecimal validPayoutFromChild;

    @ApiModelProperty(value = "自身返利比例")
    private BigDecimal commissionRatio;
    @ApiModelProperty(value = "自身返利=自身的有效派彩 * 自身返利比例")
    private BigDecimal rebate;

    @ApiModelProperty(value = "下级提成比例")
    private BigDecimal commissionRatioSub;
    @ApiModelProperty(value = "自身获得下级的奖金总计")
    private BigDecimal bonusAmountExfromChildTotal;

    @ApiModelProperty(value = "下级代理实发总计")
    private BigDecimal rebateChildTotal;
    @ApiModelProperty(value = "自身获得的实际下级佣金初算=下级贡献的有效派彩*(下级提成比例/100)")
    private BigDecimal rebateFromChild;
    @ApiModelProperty(value = "自身获得的实际下级佣金实际=初算-下级代理实发总计")
    private BigDecimal rebateFromChildActual;

    @ApiModelProperty(value = "自身实发=自身返利rebate + 自身获得下级的奖金总计bonusAmountExfromChildTotal + 自身获得的实际下级佣金实际rebateFromChildActual")
    private BigDecimal rebateTotal;

    // 其他
    @ApiModelProperty(value = "审核状态 0 失败 1成功 2待审核 ")
    private Integer status;
    @ApiModelProperty(value = "审核备注")
    private String memo;
    @ApiModelProperty(value = "下级人数")
    private Integer childNum;
    @ApiModelProperty(value = "会员组名称")
    private String groupName;

    // 导出
    @ApiModelProperty(value = "导出报表 审核状态 0 失败 1成功 2待审核 ")
    private String statusStr;
    @ApiModelProperty(value = "导出报表 自身返利比例")
    private String commissionRatioStr;
    @ApiModelProperty(value = "导出报表 下级提成比例")
    private String commissionRatioSubStr;
}
