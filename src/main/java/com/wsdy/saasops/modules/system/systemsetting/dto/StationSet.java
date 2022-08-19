package com.wsdy.saasops.modules.system.systemsetting.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StationSet {
	
	//网站数据默认查询天数
	private Integer defaultQueryDays;
	//会员数据查询天数
	private Integer memberQueryDays;
	//管理员密码过期天数
	private Integer passwordExpireDays;
	//站点logo图片
	private String logoPath;
	//页面Title图片
	private String titlePath;
	//网站Title
	private String websiteTitle;
	//网站关键字
	private String websiteKeywords;
	//网站描述
	private String websiteDescription;
	//客服配置代码（pc版）
	private String configCodePc;
	//客服配置代码（移动版）
	private String configCodeMb;
	//客服配置代码1（pc版）
	private String configCodePc1;
	//客服配置代码1（移动版）
	private String configCodeMb1;
	//自动删除已读站内信天数
	private Integer autoDeleteDays;
	//网站统计代码(pc版)
	private String websiteCodePc;
	//网站统计代码(移动版)
	private String websiteCodeMb;
	//会员数据查询开始时间
	private String createTimeFrom;
	//会员数据查询结束时间
	private String createTimeTo;
	//代理域名解析地址配置
	private String agentDomainAnalysisSite;

	// 是否允许前台注册 0不允许 1允许
	private Boolean accWebRegister = Boolean.TRUE;

	// 合营部TELEGRAM
	private String configTelegram;
	// 合营部SKYPE
	private String configSkype;
	// 合营部FLYGRAM
	private String configFlygram;

	// PC展示域名
	private String showWebSite;

	// android下载链接
	private String androidDownloadUrl;
	// ios下载链接
	private String iosDownloadUrl;
	
	/** USDT购买超链接 */
	private String usdtBuyUrl;
}
