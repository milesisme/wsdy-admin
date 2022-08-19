package com.wsdy.saasops.api.modules.user.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FriendRebateInfoDto {
    @ApiModelProperty("是否显示好友推荐")
    private Boolean isShowFriendRebate;

    @ApiModelProperty("推广码")
    private String codeId;

}
