package com.wsdy.saasops.modules.agent.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Table(name = "agy_sub_menu")
public class AgySubMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理")
    private String agyaccount;

    @ApiModelProperty(value = "id")
    private Integer subagentid;

    @ApiModelProperty(value = "菜单")
    private Long menu_id;

    @Transient
    @ApiModelProperty(value = "菜单")
    private Long menuId;
}