package com.wsdy.saasops.modules.mbrRebateAgent.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "全民代理返回dto")
public class MbrRebateAgentRespChildListDto {
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyflag;
    @ApiModelProperty(value = "代理会员级别id")
    private Integer agyLevelId;
    @ApiModelProperty(value = "下级人数")
    private Integer childNum;
    @ApiModelProperty(value = "自身的有效派彩=自身游戏派彩-自身红利 正为0")
    private BigDecimal validPayoutForSelf;
    @ApiModelProperty(value = "下级贡献的有效派彩=validpayoutfromchildmember+validpayoutfromchildmemberagent ")
    private BigDecimal validPayoutFromChild;
    @ApiModelProperty(value = "自身实发=自身返利rebate + 自身获得下级的奖金总计bonusAmountExfromChildTotal + 自身获得的实际下级佣金实际rebateFromChildActual")
    private BigDecimal rebateTotal;
}
