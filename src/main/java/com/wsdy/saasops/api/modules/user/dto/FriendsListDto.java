package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "好友的列表",description = "好友的列表")
public class FriendsListDto {
    @ApiModelProperty(value = "好友ID")
    private Integer id;
    @ApiModelProperty(value = "好友名称")
    private String loginName;
}
