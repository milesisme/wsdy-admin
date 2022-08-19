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
@ApiModel(value = "sys_role_menu_extend", description = "sys_role_menu_extend")
@Table(name = "sys_role_menu_extend")
public class SysRoleMenuExtend {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;


    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID")
    private Long roleId;


    /**
     * 扩展权限1
     */
    @ApiModelProperty(value = "菜单关联ID")
    private Long menuId;



}
