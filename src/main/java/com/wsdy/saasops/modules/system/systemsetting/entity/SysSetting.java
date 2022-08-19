package com.wsdy.saasops.modules.system.systemsetting.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name = "set_basic_set_sys_setting")
public class SysSetting implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "sysKey")
	private String syskey;

	@ApiModelProperty(value = "sysValue")
	private String sysvalue;
	
	@ApiModelProperty(value = "websiteTerms")
	private String websiteTerms;
	
	public interface SysValueConst {
		String none = "0";
		String visible = "1";//可见
		String require = "2";//必填
	}
}