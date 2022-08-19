package com.wsdy.saasops.modules.member.entity;

import com.wsdy.saasops.modules.member.dto.AccountLogDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "MbrAccountLog", description = "OprActRule")
@Table(name = "mbr_account_log")
public class MbrAccountLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACCOUNT_NAME_ONE = "修改会员";
    public static final String ACCOUNT_NAME_TWO = "真实姓名为";
    public static final String ACCOUNT_NAME_TREE = "真实姓名:";
    public static final String ACCOUNT_BANK_ONE = "修改会员";
    public static final String ACCOUNT_BANK_DELETE = "删除会员";
    public static final String ACCOUNT_TO_ACCOUNT = "向会员";
    public static final String ACCOUNT_TO_AGENT = "向代理";
    public static final String ACCOUNT_TO_SENDMAIL = "发送站内信";
    public static final String ACCOUNT_BANK_ADD_ONE = "新增会员";
    public static final String ACCOUNT_BANK_TWO = "银行卡";
    public static final String ACCOUNT_BANK_STATUS = "银行卡状态为";
    public static final String ACCOUNT_MOBILE_ONE = "修改会员";
    public static final String ACCOUNT_MOBILE_TWO = "手机号为";
    public static final String ACCOUNT_EMAIL_ONE = "修改会员";
    public static final String ACCOUNT_EMAIL_TWO = "邮箱为";
    public static final String ACCOUNT_DRAW_PWD = "的提款密码";
    public static final String ACCOUNT_LOGIN_PWD = "的登录密码";
    public static final String ACCOUNT_QQ = "修改QQ为";
    public static final String ACCOUNT_QQ_EX = "QQ:";
    public static final String ACCOUNT_WECHAT = "修改微信为";
    public static final String ACCOUNT_WECHAT_EX = "微信:";
    public static final String ACCOUNT_GROUP = "修改会员组为";
    public static final String ACCOUNT_AGENT = "修改代理为";
    public static final String ACCOUNT_STATUS_ONE = "修改会员";
    public static final String ACCOUNT_STATUS_TWO = "状态为";
    public static final String ACCOUNT_STATUS_THREE = "禁用";
    public static final String ACCOUNT_STATUS_FOUR = "启用";
    public static final String ACCOUNT_STATUS_FIVE = "删除";
    public static final String ACCOUNT_MEMO_ONE = "修改会员";
    public static final String ACCOUNT_MEMO_TWO = "备注为";
    public static final String ACCOUNT_INFO_ONE = "修改会员";
    public static final String ACCOUNT_INFO_WALLET = "钱包[";
    public static final String ACCOUNT_INFO_ONE_DELETE = "删除会员";
    public static final String ACCOUNT_INFO_TWO = "信息为";
    public static final String ACCOUNT_INFO = "会员信息修改";
    public static final String ACCOUNT_MOBILE = "手机";
    public static final String ACCOUNT_MOBILE_EX = "电话:";
    public static final String ACCOUNT_EMAIL = "邮箱";
    public static final String ACCOUNT_EMAIL_EX = "邮箱:";
    public static final String ACCOUNT_DEPOSIT_INFO_ONE = "修改";
    public static final String ACCOUNT_DEPOSIT_INFO_TWO = "审核状态为";
    public static final String ACCOUNT_WITHDRAW_INFO_ONE = "修改";
    public static final String ACCOUNT_WITHDRAW_INFO_TWO = "状态为";
    public static final String ACCOUNT_AUDIT_INFO_ONE = "修改";
    public static final String ACCOUNT_AUDIT_INFO_TWO = "审核状态为";
    public static final String ACCOUNT_BONUS_INFO_ONW = "审核";
    public static final String ACCOUNT_BONUS_ADD_INFO_ONW = "新增了活动";
    public static final String ACCOUNT_BONUS_ADD_INFO_TWO = "信息为";
    public static final String ACCOUNT_BONUS_EDIT_INFO_ONE = "编辑了活动";
    public static final String ACCOUNT_BONUS_EDIT_INFO_TWO = "信息为";
    public static final String ACCOUNT_BONUS_STATUS_INFO_ONE = "编辑了活动";
    public static final String ACCOUNT_BONUS_STATUS_INFO_TWO = "状态为";
    public static final String ACCOUNT_INFO_OT_ONE = "修改会员";
    public static final String ACCOUNT_INFO_OT_TWO = "其他资料为";
    public static final String ACCOUNT_SUCCEED = "成功";
    public static final String ACCOUNT_AUDIT_UPDATE = "修改会员";
    public static final String ACCOUNT_UPDATE_ACCOUNT = "调整会员";
    public static final String ACCOUNT_CLEAN_AUDIT = "清除会员";
    public static final String ACCOUNT_CHARGE_AUDIT = "扣除会员";
    public static final String ACCOUNT_AUDIT = "稽核";
    public static final String ACCOUNT_ILLEGAL_AUDIT = "违规稽核";
    public static final String ACCOUNT_ADD_AUDIT = "增加稽核";
    public static final String ACCOUNT_ILLEGAL_AUDIT_TWO = "违规稽核为";
    public static final String ACCOUNT_ILLEGAL_AMOUNT = "违规稽核金额";
    public static final String ACCOUNT_ILLEGAL_AUDIT_THREE = "违规稽核为'正常'";
    public static final String ACTIVITY_CONFIG = "活动设置";
    public static final String AUDIT_REPORT = "稽核报表";
    public static final String ACCOUNT_TRANSFER = "户内转账";
    public static final String ACCOUNT_UPDATETRANSFER = "户内转编辑状态";
    public static final String ACCOUNT_TRANSFER_ADD = "新增";
    public static final String GAME_LIST = "游戏列表";
    public static final String GAME_LIST_UPDATE = "修改";
    public static final String GAME_LIST_STATUS = "状态为";
    public static final String GAME_LIST_JUMP = "跳转方式为";
    public static final String MESSAGE_TEMPLATE = "信息模板";
    public static final String NOTICE_MESSAGE = "公告通知";
    public static final String OPR_ADV_MANAGER = "广告管理";
    public static final String OPR_ADV = "广告";
    public static final String OPR_HELP_CATEGORY = "帮助中心分类";
    public static final String OPR_HELP_TITLE = "帮助中心标题";
    public static final String OPR_HELP_CONTENT = "帮助中心内容";
    public static final String OPR_HELP_MANAGER = "帮助中心";
    public static final String ACCOUNT_TRANSFER_DEPOSIT = "转入订单：";
    public static final String ACCOUNT_TRANSFER_WISDRAW = "转出订单：";
    public static final String BONUS_LIST = "红利列表";
    public static final String FUND_ADJUST = "资金调整";
    public static final String MEMBER_WITHDRAW = "会员提款";
    public static final String MEMBER_DEPOSIT = "会员入款";
    public static final String PAY_LIST = "支付列表";
    public static final String DEPOSIT_MANAGER = "出款管理";
    public static final String SYSTEM_COMFIG = "系统设置";
    public static final String ROLE_LIST = "角色权限";
    public static final String USER_MANAGER = "用户管理";
    public static final String MEMBER_LIST = "会员列表";
    public static final String MEMBER_LIST_DETAIL = "会员详情";
    public static final String OPR_REC_MSG = "站内信";
    public static final String MEMBER_GROUP = "会员组";
    public static final String MEMBER_GROUP_AUTO = "会员组自动升级";
    public static final String ACCOUNT_AUDIT_ADD = "申请人工增加";
    public static final String UPDATE_ADJUSTMENT = "资金核对调整";
    public static final String ACCOUNT_AUDIT_REDUCE = "申请人工减少";
    public static final String ACCOUNT_LIMIT = "额度";
    public static final String USER_LOGIN = "登录";
    public static final String USER_LOGOUT = "登出";
    // add 2019.6.29
    public static final String ACCOUNT_LEVEL = "活动等级";
    public static final String ACCOUNT_LEVEL_UPDATE_BATCH = "会员批量调级";
    public static final String ACCOUNT_LEVEL_LOCK_STATUS = "星级锁定状态";
    public static final String ACCOUNT_LEVEL_STATICS_RULE = "周期统计规则";
    public static final String ACCOUNT_LEVEL_AUTOMATI = "自动晋升";
    public static final String ACCOUNT_LEVEL_EDIT = "活动等级编辑";

    public static final String ACTIVITY_RULE = "活动规则";
    public static final String ACTIVITY_RULE_DELETE = "删除活动规则";
    public static final String ACTIVITY_RULE_SAVE = "新增活动规则";
    public static final String ACTIVITY_RULE_UPDATE = "编辑活动规则";
    public static final String ACTIVITY_RULE_UPDATE_STATUS = "变更活动规则状态";

    public static final String ACTIVITY_CAT = "活动类别";
    public static final String ACTIVITY_CAT_SAVE = "新增活动类别";
    public static final String ACTIVITY_CAT_UPDATE = "编辑活动类别";

    public static final String OPR_BONUS = "红利列表";
    public static final String OPR_BONUS_SAVE = "新增红利";
    public static final String OPR_BONUS_AUDIT = "红利审核";
    public static final String OPR_BONUS_MODIFY_AMOUNT = "红利金额调整";

    public static final String OPR_BONUS_WATER = "返水列表";
    public static final String OPR_BONUS_WATER_AUDIT = "返水审核";

    public static final String SYSTEM_COMFIG_REG = "注册设置";
    public static final String SYSTEM_COMFIG_REG_WEBREGSET = "是否允许前台注册";

    public static final String UPDATE_PAY_SET = "支付分配";

    // add 2019.7.29
    public static final String AGENT_LIST = "代理列表";
    public static final String AGENT_LIST_SAVE_PARENT = "新增总代";
    public static final String AGENT_LIST_SAVE_CHILD = "新增子代理";

    public static final String AGENT_LIST_UPDATE = "编辑代理";

    public static final String AGENT_LIST_UPDATE_STATUS_PARENT = "编辑总代";
    public static final String AGENT_LIST_UPDATE_STATUS_CHILD = "编辑子代理";

    public static final String AGENT_LIST_CHANGE = "重置代理";
    public static final String AGENT_LIST_DELETE = "删除代理";

    public static final String AGENT_DOMAIN = "代理域名";
    public static final String AGENT_DOMAIN_ADD = "新增推广域名";
    public static final String AGENT_DOMAIN_AUDIT = "审核推广域名";

    public static final String DOWNLOAD_FILE = "导出";

    public static final String ACCOUNT_MASSTEXTING = "批量发送短信";
    public static final String ACCOUNT_AGRNT = "修改会员代理";
    public static final String ACCOUNT_SUPLOGNANAME = "修改会员推荐人";
    public static final String SYSTEM_OUT_CALLPLATE_SET = "语音线路设置";

    public static final String TASK_ACCOUNT = "任务中心";

    public static final String ACCOUNT_AUTO= "自动升级";
    
    public static final String ACCOUNT_AUTO_RECOVER= "等级自动恢复";

    public static final String ACCOUNT_AUTO_DOWNGRADE = "自动降级";

    public static final String ACCOUNT_CR_WALLET = "出款管理";

    public static final String ACCOUNT_LOGIN_LOCK = "登录锁定状态";

    // 返利列表(全民代理)
    public static final String ACCOUNT_REBATE_AGENT = "返利列表(全民代理)";


    public static final String ACCOUNT_REBATE_FRIEND = "好友推荐返利";

  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "createTime")
    private String createTime;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "模块名")
    private String moduleName;

    @ApiModelProperty(value = "操作ip")
    private String ip;

    @ApiModelProperty(value = "检测ip")
    private String checkiP;

    @Transient
    private AccountLogDto logDto;


}