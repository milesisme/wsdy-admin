package com.wsdy.saasops.api.modules.user.dto.SdyActivity;

import com.wsdy.saasops.modules.operate.dto.RedPacketRainRuleDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "RedPacketRainRespDto", description = "红包雨dto")
public class RedPacketRainRespDto {
    // 查询返回
    @ApiModelProperty(value = "当日开始时间")
    private String startTime;
    @ApiModelProperty(value = "当日结束时间")
    private String endTime;
    @ApiModelProperty(value = "活动日期 按周1-7 ,如1,5,7  周一周五周日 ")
    private List<Integer> validDates;

    // 申请返回
    @ApiModelProperty(value = "此次点击获取的红包金额")
    private BigDecimal bonusAmount;
    @ApiModelProperty(value = "红包总可领取次数")
    private int total;
    @ApiModelProperty(value = "红包总已领取次数")
    private int redPacketNum;
    @ApiModelProperty(value = "红包雨次数档位规则")
    private List<RedPacketRainRuleDto> redPacketRainRuleDtos;
}
