package com.wsdy.saasops.modules.sys.dto;


import lombok.Data;

import java.util.List;

@Data
public class SysRoleMenuExtendDto {

    /**
     * ID
     */
    private Integer id;


    /**
     * 角色ID
     */
    private Long roleId;


    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 菜单类型
     */
    private Integer type;


    /**
     * 角色
     */
    private List<Long> roleIds;



}
