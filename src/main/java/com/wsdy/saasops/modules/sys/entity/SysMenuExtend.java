package com.wsdy.saasops.modules.sys.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@ApiModel(value = "sys_menu_extend", description = "sys_menu_extend")
@Table(name = "sys_menu_extend")
public class SysMenuExtend {

    /**
     * 菜单ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    /**
     * 父菜单ID，一级菜单为0
     */
    private Long parentId;


    private Long refId;

    private String name;

    private String url;

    private Integer type;

    private Integer isInner;
}
