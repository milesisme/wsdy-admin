package com.wsdy.saasops.modules.sys.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.wsdy.saasops.modules.sys.entity.SysRoleMenuEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysRoleMenuEntities implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 *  角色与菜单对应关系
	 */
    @ApiModelProperty(value = "角色")
	private List<SysRoleMenuEntity> sysRoleMenuEntitys;

	/**
	 * 菜单ID
	 */
    @ApiModelProperty(value = "菜单ID")
	private Set<Long> menuIds;
}
