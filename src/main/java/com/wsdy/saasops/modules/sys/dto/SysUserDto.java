package com.wsdy.saasops.modules.sys.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 系统用户传输类
 */
@Data
public class SysUserDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * 用户名List
	 */
	private List<String> usernameList;

    /**
     * 角色ID
     */
    private Long roleId;

	/**
	 * 修改者id
	 */
//	private Long modifyUserId;
	
	/**
	 * 修改时间
	 */
//	private Date modifyTime;
	
}
