package com.wsdy.saasops.api.modules.user.dto;

import com.wsdy.saasops.modules.operate.entity.TGmGame;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "不反水游戏列表dto", description = "不反水游戏列表dto")
public class GameWithoutRebateDto {
    @ApiModelProperty(value = "平台名称")
    private String depotName;
    @ApiModelProperty(value = "平台下不反水游戏列表")
    private List<TGmGame> gameList;
}
