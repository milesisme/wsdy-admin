package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import com.wsdy.saasops.common.utils.PageUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "全民代理返回dto")
public class MbrRebateAgentRespDto {
    // 基本信息
    @ApiModelProperty(value = "是否显示全民代理 true 显示  fale 不显示")
    private Boolean isShowMbrAgent;
    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyflag;
    @ApiModelProperty(value = "是否满足申请条件 true 是   fale 否")
    private Boolean isApply;
    @ApiModelProperty(value="推广码")
    private String codeId;
    @ApiModelProperty(value="APP推广域名")
    private String appDomain;

    // 全民代理数据-自身
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
    @ApiModelProperty(value = "自身获得的实际下级佣金实际=初算-下级代理实发总计")
    private BigDecimal rebateFromChildActual;
    @ApiModelProperty(value = "自身实发=自身返利rebate + 自身获得下级的奖金总计bonusAmountExfromChildTotal + 自身获得的实际下级佣金实际rebateFromChildActual")
    private BigDecimal rebateTotal;

    // 全民代理数据-下级
    @ApiModelProperty(value = "下级数据")
    PageUtils childList;
}
