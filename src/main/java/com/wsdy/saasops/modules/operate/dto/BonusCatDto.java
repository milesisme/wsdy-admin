package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class BonusCatDto {

    @ApiModelProperty(value = "类型ID")
    private Integer catId;

    @ApiModelProperty(value = "类型")
    private String catName;

    @ApiModelProperty(value = "适用范围")
    private List<BonusDepotDto> depotDtos;
}
