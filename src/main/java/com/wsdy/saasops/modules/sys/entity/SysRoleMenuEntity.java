package com.wsdy.saasops.modules.sys.entity;


import java.io.Serializable;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色与菜单对应关系
 */
@Setter
@Getter
@ApiModel(value = "SysRoleMenuEntity", description = "角色菜单")
@ToString
public class SysRoleMenuEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 */
    @ApiModelProperty(value = "角色ID")
	private Long roleId;

	/**
	 * 菜单ID
	 */
    @ApiModelProperty(value = "菜单ID")
	private Long menuId;

    @ApiModelProperty(value = "是否全选")
	private Boolean isTotalChecked;

	@JsonIgnore
	@Transient
	private Long parentId;

	@Transient
	private Integer type;
	
	public SysRoleMenuEntity(Long roleId, Long menuId) {  
        super();  
        this.roleId = roleId;  
        this.menuId = menuId;  
    }
    public SysRoleMenuEntity() {  
        // TODO Auto-generated constructor stub  
    }  
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false; 
        }
        if (this == obj) {
            return true;
        }
        
        SysRoleMenuEntity user = (SysRoleMenuEntity) obj;
        //多重逻辑处理，去除年龄、姓名相同的记录  
        if (this.getRoleId().compareTo(user.getRoleId())==0 
                && this.getMenuId().equals(user.getMenuId()) && this.getType().equals(user.getType())) {
            return true;    
        }
		return false;
	}

	@Override
	public int hashCode()
	{
		// 需重写hashCode
		int hash = 7;
		hash = 31*hash+roleId.intValue();
		hash = 31*hash+menuId.hashCode();
		return hash;
	}
}
