package com.wsdy.saasops.modules.sys.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import com.wsdy.saasops.modules.sys.dto.TreeMenuDto;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色
 */
@Setter
@Getter
public class SysRoleEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 */
	private Long roleId;

	/**
	 * 角色名称
	 */
	@NotBlank(message = "角色名称不能为空")
	private String roleName;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 创建者ID
	 */
	private Long createUserId;

	private List<Long> menuIdList;

	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 显示名称
	 */
	private String roleNickName;
	/**
	 * 用户数量
	 */
	private Integer userNum;

	private Integer deptId;
	
	private String createUser;

	private Integer isEnable;
	
	private Long[] roleIds;

	private List<SysMenuEntity> menuList;

	private List<TreeMenuDto> treeMenuList;

    @Transient
	private String isEnableList;
    
    @Transient
	private Integer page;
	@Transient
	private Integer limit;
	@Transient
	private Integer offset;
	@Transient
	private String sidx;
	@Transient
	private String order;
	
	@Transient
	private Integer pageNo;
	@Transient
	private Integer pageSize;
}
