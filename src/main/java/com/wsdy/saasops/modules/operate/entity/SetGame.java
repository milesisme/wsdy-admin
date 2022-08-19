package com.wsdy.saasops.modules.operate.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;


@Setter
@Getter
@ApiModel(value = "SetGame", description = "游戏中间表")
@Table(name = "set_game")
public class SetGame implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @ApiModelProperty(value = "game ID")
    private Integer gameId;

    @ApiModelProperty(value = "平台ID")
    private Integer depotId;

    @ApiModelProperty(value = "游戏开关 (开启PC端 1->开启，0－>禁用)")
    private Integer enableGmaePc;

    @ApiModelProperty(value = "游戏开关 (开启移动端 1->开启，0－>禁用)")
    private Integer enableGmaeMb;

    @ApiModelProperty(value = "游戏开关 (开启APP端 1->开启，0－>禁用)")
    private Integer enableGmaeApp;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "人气值")
    private Integer popularity;

    @ApiModelProperty(value = "平台流水费率")
    private BigDecimal waterrate;
}