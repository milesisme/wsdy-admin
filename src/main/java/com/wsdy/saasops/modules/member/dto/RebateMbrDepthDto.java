package com.wsdy.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RebateMbrDepthDto {

    @ApiModelProperty(value = "会员层级;仅1,2,3")
    private Integer depth;
    @ApiModelProperty(value = "游戏类别规则")
    private List<RebateCatDto> catDtoList;
}
