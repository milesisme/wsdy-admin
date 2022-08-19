package com.wsdy.saasops.modules.base.controller;

import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import org.apache.shiro.SecurityUtils;

/**
 * Controller公共组件
 */
public abstract class AbstractController {

	protected SysUserEntity getUser() {
		return (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
	}

	protected Long getUserId() {
		return getUser().getUserId();
	}
}
