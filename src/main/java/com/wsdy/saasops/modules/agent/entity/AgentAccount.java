package com.wsdy.saasops.modules.agent.entity;

import com.wsdy.saasops.modules.base.entity.BaseAuth;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Table(name = "agy_account")
public class AgentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "代理密码")
    private String agyPwd;

    @Transient
    @ApiModelProperty(value = "老代理密码")
    private String oldAgyPwd;

    @ApiModelProperty(value = "安全密码")
    private String securePwd;

    @ApiModelProperty(value = "salt")
    private String salt;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "注册网址")
    private String registerUrl;

    @ApiModelProperty(value = "1开启，0禁用")
    private Integer available;

    @ApiModelProperty(value = "0 拒绝，1 成功 2 待处理")
    private Integer status;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "代理推广代码必须唯一")
    private String spreadCode;

    @ApiModelProperty(value = "佣金ID")
    private Integer commissionId;

    @ApiModelProperty(value = "qq")
    private String qq;

    @ApiModelProperty(value = "微信")
    private String weChat;

    @ApiModelProperty(value = "0 官网注册  1后台注册")
    private Integer registerSign;

    @ApiModelProperty(value = "上级ID 没有上级为0")
    private Integer parentId;

    @ApiModelProperty(value = "代理关联会员组")
    private Integer groupId;

    @ApiModelProperty(value = "0 直线代理 1分线代理  2 推广员工 3招商员工 4子账号代理前端使用")
    private Integer attributes;

    @ApiModelProperty(value = "skype")
    private String skype;

    @ApiModelProperty(value = "telegram")
    private String telegram;

    @ApiModelProperty(value = "telegram")
    private String flyGram;

    @ApiModelProperty(value = "部门id")
    private Integer departmentid;

    @ApiModelProperty(value = "分线上级代理id，代理id")
    private Integer superiorCloneId;

    @ApiModelProperty(value = "佣金id")
    private Integer contractId;

    @ApiModelProperty(value = "契约有效期开始")
    private String contractStart;

    @ApiModelProperty(value = "契约有效期结束")
    private String contractEnd;

    @ApiModelProperty(value = "结算费模式  1，平台费  2，服务费 3全部")
    private Integer feeModel;
    
    @ApiModelProperty(value = "服务费存款比例")
    private BigDecimal depositServicerate;
    
    @ApiModelProperty(value = "服务费取款比例")
    private BigDecimal withdrawServicerate;
    
    @ApiModelProperty(value = "平台费额外比例")
    private BigDecimal additionalServicerate;
    
    @ApiModelProperty(value = "下级会员默认分配的会员组，对应mbr_group.id")
    private Integer defaultGroupId;
    
    @ApiModelProperty(value = "设置下级会员默认分配的会员组，对应mbr_group.id")
    private Integer setGroupId;

    @ApiModelProperty(value = "上级返利比率，如果不为空则替代合约返利比率")
    private Integer upRate;
    
    @ApiModelProperty(value = "总代返佣比例")
    private BigDecimal rebateratio;

    @ApiModelProperty(value = "一级代理返佣")
    private BigDecimal firstagentratio;

    @Transient
    @ApiModelProperty(value = "上级返利比率，如果不为空则替代合约返利比率")
    private Integer rate;

    @Transient
    @ApiModelProperty(value = "契约名称")
    private String contractname;
    @ApiModelProperty(value = "审核时间")
    private String reviewTime;

    @Transient
    private Boolean reviewStatus;

    @Transient
    @ApiModelProperty(value = "平台名称")
    private String departmentName;

    @Transient
    @ApiModelProperty(value = "银行卡数量")
    private Integer bankNum;

    @Transient
    @ApiModelProperty(value = "所属等级")
    private Integer grade;

    @Transient
    @ApiModelProperty(value = "直属会员数")
    public Integer accountNum;

    @Transient
    @ApiModelProperty(value = "上级帐号")
    private String agyTopAccount;

    @Transient
    @ApiModelProperty(value = "上级id")
    private String agyTopAccountId;

    @Transient
    @ApiModelProperty(value = "上级佣金id")
    private String agyTopCommissionId;

    @Transient
    @ApiModelProperty(value = "开始时间开始")
    private String createTimeFrom;

    @Transient
    @ApiModelProperty(value = "开始时间结束")
    private String createTimeTo;

    @Transient
    @ApiModelProperty(value = "直属代理")
    private Integer directAgentCount;

    @Transient
    @ApiModelProperty(value = "下线代理数")
    private Integer offlineAgentCount;

    @Transient
    @ApiModelProperty(value = "下线会员数")
    private Integer offlineMemberCount;

    @Transient
    @ApiModelProperty(value = "历史净盈利额")
    private BigDecimal netProfitBalance;

    @Transient
    @ApiModelProperty(value = "代理详情 佣金标识 0模版佣金只可看  1自定义佣金可修改所有" +
            "（不能下级小）  2只看编辑百分比（不能比上级大比下级小） 3只可看")
    private Integer commissionSign;

    @Transient
    @ApiModelProperty(value = "佣金余额,代理列表新增")
    private BigDecimal walletBalance;
    @Transient
    @ApiModelProperty(value = "代充钱包，代理列表新增")
    private BigDecimal rechargeWallet;
    @Transient
    @ApiModelProperty(value = "彩金钱包，代理列表新增")
    private BigDecimal payoffWallet = BigDecimal.ZERO;
    @Transient
    @ApiModelProperty(value = "钱包类型，0 佣金钱包1 代充钱包2彩金钱包")
    private Integer walletType;

    @Transient
    private BaseAuth baseAuth;

    @Transient
    private String parentIds;

    @Transient
    private String captcha;

    @Transient
    @ApiModelProperty(value = "代理域名")
    private String domainUrl;

    @Transient
    @ApiModelProperty(value = "域名ids")
    private List<Integer> domains;

    // 外围系统新增数据字段
    @Transient
//  @ApiModelProperty(value = "代理类别： 0 公司(总代)/1股东/2总代/ >2 代理   -1会员  -2汇总")
    @ApiModelProperty(value = "代理类别： 0：股东，1：总代，2：一级代理，3：二级代理")
    public Integer agentType;
    @Transient
    @ApiModelProperty(value = "代理点数")
    private BigDecimal balance;
    @Transient
    @ApiModelProperty(value = "投注状态 1开启，0关闭")
    private Integer bettingStatus;
    @Transient
    @ApiModelProperty(value = "真人分成")
    private BigDecimal realpeople;
    @Transient
    @ApiModelProperty(value = "电子分成")
    private BigDecimal electronic;
    @Transient
    @ApiModelProperty(value = "真人洗码佣金比例")
    private BigDecimal realpeoplewash;
    @Transient
    @ApiModelProperty(value = "电子洗码佣金比例")
    private BigDecimal electronicwash;

    // 搜寻用字段
    @Transient
    @ApiModelProperty(value = "搜索用户名")
    private String searchName;

    // 登入日志字段
    @Transient
    @ApiModelProperty(value = "最后登录ip")
    private String loginIp;

    @Transient
    private Integer depth;

    // 查询相关字段
    @Transient
    @ApiModelProperty(value = "面包屑类型 agent 代理 mbr会员")
    private String bannerType;
    @Transient
    @ApiModelProperty(value = "会员账号")
    private String loginName;

    @Transient
    @ApiModelProperty(value = "验证码标志")
    private String codeSign;

    @Transient
    @ApiModelProperty(value="图形验证码 可选")
    private String kaptcha;

    @Transient
    @ApiModelProperty(value="验证码 可选")
    private String mobileCaptchareg;

    @Transient
    private Integer agentId;

    @Transient
    @ApiModelProperty(value = "新的密码密码")
    private String newAgyPwd;

    @Transient
    @ApiModelProperty(value = "安全密码 1设置过  0未设置")
    private Integer isSecurePwd;

    @Transient
    @ApiModelProperty(value = "排序")
    private String orderBy;

    @Transient
    @ApiModelProperty(value = "agentToken")
    private String agentToken;

    @Transient
    @ApiModelProperty(value = "isSign")
    private Boolean isSign = Boolean.TRUE;

    @Transient
    @ApiModelProperty(value = "0 拒绝，1 成功 2 待处理")
    private String statusStr;

    @Transient
    @ApiModelProperty(value = "导出代理等级")
    private String agentTypeStr;

    @Transient
    @ApiModelProperty(value = "导出属性")
    private String attributesStr;

    @Transient
    @ApiModelProperty(value = "代理id数组")
    private List<Integer> ids;

    @Transient
    @ApiModelProperty(value = "属性列表")
    private List<Long> attributesList;

    @Transient
    @ApiModelProperty(value = "属性列表")
    private List<Long> departmentIdList;

    @Transient
    @ApiModelProperty(value = "是否包含空部门")
    private boolean departmentIdIsNull = false;

    
    @Transient
    @ApiModelProperty(value = "是否展示平台费")
    private Boolean isShowAdditional = false;
    
    @Transient
    @ApiModelProperty(value = "是否展示手机号")
    private Boolean isShowMobile = false;
    
	@Transient
	@ApiModelProperty(value = "是否展示服务费")
	private Boolean isShowServicerate = false;
}