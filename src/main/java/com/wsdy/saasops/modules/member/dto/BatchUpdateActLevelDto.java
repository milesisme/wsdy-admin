package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class BatchUpdateActLevelDto {
    @ApiModelProperty(value = "新等级id")
    private Integer newLevelId;
    @ApiModelProperty(value = "按等级调整时的旧的等级")
    private List<Integer> oldLevelIds;
    @ApiModelProperty(value = "按会员调整时的会员ids")
    private List<Integer> accountIds;

    @ApiModelProperty(value = "备注")
    private String memo;
    @ApiModelProperty(value = "强制更新标志 1强制更新 0/null为不更新")
    private Integer forceUpdate;
}
