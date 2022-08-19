package com.wsdy.saasops.modules.system.systemsetting.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebTerms {
	
	// 用户注册是否强制显示网站服务条款    0：否  ，1 是
	private Integer memberDisplayTermsOfWebsite;
	//用户注册网站服务条款
	private String memberServiceTermsOfWebsite;
	
	// 代理注册是否强制显示网站服务条款    0：否  ，1 是
	private Integer agentDisplayTermsOfWebsite;
	//代理网站服务条款
	private String agentServiceTermsOfWebsite;

}
