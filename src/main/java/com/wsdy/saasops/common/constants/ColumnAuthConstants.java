package com.wsdy.saasops.common.constants;

public class ColumnAuthConstants {
	
	
	public static final Long MEMBER_LIST_MENU_ID=106L; //会员列表menuId
	public static final Long MEMBER_LIST_VIEW_MENU_ID=1020L; //会员列表-会员列表查看menuId
	public static final Long MEMBER_DATA_MENU_ID=722L; //会员资料menuId
	public static final Long MEMBER_OTHER_DATA_MENU_ID=723L; //其他资料menuId
	public static final Long MEMBER_RISK_MENU_ID=724L; //风控审核menuId
	public static final Long MEMBER_ASSET_DATA_MENU_ID=727L; //资产信息menuId
	public static final Long MEMBER_FULL_DATA_MENU_ID=849L; //完整资料menuId

	public static final Long MEMBER_DEPOSIT_MENU_ID=71L; //会员入款权限menuId
	public static final Long MEMBER_WITHDRAW_FIRST_MENU_ID=149L; //提款初审查权限menuId
	public static final Long MEMBER_WITHDRAW_REVIEW_MENU_ID=697L; //提款初复审权限menuId
	public static final Long MEMBER_BOUNS_MENU_ID=699L; //红利申请查看menuId
	
	
	//系统菜单类型
	public static final Long COLUMN_MENU_TYPE_THREE=3L; //列字段
	public static final Long COLUMN_MENU_TYPE_FOUR=4L; //列表查询字段
	public static final Long COLUMN_MENU_TYPE_FIVE=5L; //搜索查询字段


	//真实姓名
	public static final Long MEMBER__MENUNAME_ID=1022L; //查询真实姓名
	
	
	/** 代理列表 */
	public static final Long AGENT_LIST_ID=151L;
	
	/** 代理列表-查看完整电话 */
	public static final Long AGENT_LIST_FULLY_MOBILE=1240L;
	
	/** 服务费设置设置  */
	public static final String AGENT_ACCOUNT_UPDATESERVICERATE ="agent:account:updateServicerate";
	
	/** 平台费设置  */
	public static final String AGENT_ACCOUNT_UPDATEADDITIONAL ="agent:account:updateadditional";

	public static final String AGENT_MOBILE_CONTACT = "agent:contact:mobile";
	public static final String AGENT_NAME_CONTACT = "agent:contact:name";

	public static final String AGENT_PASSWORD_CONTACT = "agent:account:password";
	public static final String AGENT_SECUREPWD_CONTACT = "agent:account:securePwd";
}
