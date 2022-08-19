package com.wsdy.saasops.modules.sys.entity;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 系统配置信息
 */
@Setter
@Getter
public class SysConfigEntity {
	private Long id;
	@NotBlank(message="参数名不能为空")
	private String key;
	@NotBlank(message="参数值不能为空")
	private String value; 
	private String remark;
}
