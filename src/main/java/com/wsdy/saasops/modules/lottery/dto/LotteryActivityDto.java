package com.wsdy.saasops.modules.lottery.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LotteryActivityDto {

    @ApiModelProperty(value = "抽奖区")
    private List<LotteryAreaDto> lotteryAreaDtos;
}
