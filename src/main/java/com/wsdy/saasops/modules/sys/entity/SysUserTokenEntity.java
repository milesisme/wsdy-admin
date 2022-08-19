package com.wsdy.saasops.modules.sys.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;


/**
 * 系统用户Token
 */
@Setter
@Getter
@ToString
public class SysUserTokenEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//用户ID
	private Long userId;
	//token
	private String token;
	//过期时间
	private Date expireTime;
	//更新时间
	private Date updateTime;
	@Transient
	private String domainUrl;
	@Transient
	private Integer roleId;
}
