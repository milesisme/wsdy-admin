package com.wsdy.saasops.modules.operate.dto;

import com.wsdy.saasops.modules.operate.entity.OprGame;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
@ApiModel(value = "DepotListDto", description = "平台DTO")
public class DepotListDto {

    @ApiModelProperty(value = "平台Id")
    private Integer depotId;

    @ApiModelProperty(value = "平台名称")
    private String depotName;

    @ApiModelProperty(value = "平台LOGO")
    private String depotLogo;

    @ApiModelProperty(value = "平台游戏信息 ")
    private List<OprGame> depotGameList;

}