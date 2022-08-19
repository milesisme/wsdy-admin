package com.wsdy.saasops.modules.sys.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统用户传输类
 */
@Data
public class SysUserLastLoginTimeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userName;
	private Date lastLoginTime;


}
