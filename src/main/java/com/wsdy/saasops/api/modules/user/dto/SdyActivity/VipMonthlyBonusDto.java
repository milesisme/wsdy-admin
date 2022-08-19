package com.wsdy.saasops.api.modules.user.dto.SdyActivity;

import com.wsdy.saasops.modules.operate.dto.MemDayRuleScopeDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VipMonthlyBonusDto {

    @ApiModelProperty(value = "会员范围 0全部会员 1层级会员")
    private Integer scope;

    @ApiModelProperty(value = "层级 活动规则")
    private List<MemDayRuleScopeDto> ruleScopeDtoList;
}
