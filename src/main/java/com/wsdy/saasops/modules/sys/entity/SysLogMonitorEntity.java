package com.wsdy.saasops.modules.sys.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 系统用户
 */
@Setter
@Getter
@ToString
public class SysLogMonitorEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;

	/**
	 * 用户ID
	 */
	private Long accountId;

	/**
	 * 用户名
	 */
	private String loginName;

	/**
	 * 创建时间
	 */
	private String createTime;
	private String createStartTime;
	private String createEndTime;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 模块名称
	 */
	private String moduleName;

	/**
	 * 操作ip
	 */
	private String ip;

	/**
	 * 分页信息
	 */
	private Integer pageNo;
	private Integer pageSize;
}
