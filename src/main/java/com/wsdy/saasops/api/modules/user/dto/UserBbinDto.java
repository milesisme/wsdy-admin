package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserBbinDto {

	//网站名称
	private String website;
	//会员账号长度(4-20)
	private String username;
	//上层账号
	private String uppername;
	//密码6-12英文及数字
	private String password;
	/**
	 * 验证码(需全小字)
	 * key=a+b+c
	 * a=无意义字串长度8码
	 * b=md5(website+username+keyB+yyyymmdd)
	 * c=无意义字串长度9码
	 * YYYYMMDD为美东时间 比北京时间晚8小时
	 */
	private String key;
	/**
	 * 语系：zh-cn(简中) zh-tw(繁中) en-us(英文)  euc-jp(日文) ko(韩文) th(泰文) es(西班牙文) vi(越南) khm(谏铺寨) lao(捞国文)
	 */
	//private String lang;
	/**
	 *  体育 ball 视讯 live 电子游艺 game 彩票Ltlottery 若为空白则导入整合页  
	 */
	//private String page_site;
	
	/**
	 * 视讯：live 进入视读大厅页面，需同时带入page_site
	 */
	//private String page_present; 
	/**
	 * 0.维护时回传讯息 1:维护时导入整合页(预设为0)
	 */
	private String maintenance_page;
	/**
	 * 游戏种类(3.BB视讯 5.BB电子 15.3D电子 19.AG视讯 20.PT电子 22.欧博视讯 23.MG电子 24.OG视讯 27.GD视讯 28.GNS电子 29.ISB电子 30.BB捕鱼机 )
	 */
	private String gamekind ;
	/**
	 * 略
	 */
	private String gametype;
	/**
	 * 略
	 */
	private String gamecode;
	//尝试次数
	private int times;


	@Override
    public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("website=").append(website);
		buffer.append("&username=").append(username);
		buffer.append("&uppername=").append(uppername);
		buffer.append("&key=").append(key);
/*		if (!StringUtils.isEmpty(page_site))
			buffer.append("&page_site=").append(page_site);
		if (!StringUtils.isEmpty(page_present))
			buffer.append("&page_present=").append(page_present);*/
		return buffer.toString();
	}
	
	
}
