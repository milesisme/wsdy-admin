package com.wsdy.saasops.api.modules.user.dto;

import com.wsdy.saasops.modules.system.systemsetting.dto.WithdrawLimitTimeDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class WithdrawalLimitDto {
    @ApiModelProperty("提款限时 0警用  1启用")
    private Integer isWithdrawLimitTimeOpen;

    @ApiModelProperty("限制时间")
    private List<WithdrawLimitTimeDto> withdrawLimitTimeDtoList;

    @ApiModelProperty("当前系统时间")
    private String currentTime;
}
