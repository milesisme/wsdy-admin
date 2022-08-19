package com.wsdy.saasops.modules.member.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wsdy.saasops.common.utils.AccountEncryption;
import com.wsdy.saasops.modules.base.entity.BaseAuth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ApiModel(value = "MbrAccount", description = "")
@Table(name = "mbr_account")
@ToString
public class MbrAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    //批量操作的时候
    @Transient
    private Integer[] Ids;

    @ApiModelProperty(value = "会员账号")
    private String loginName;

    //@JsonIgnore
    @ApiModelProperty(value = "会员密码")
    private String loginPwd;

    //@JsonIgnore
    @ApiModelProperty(value = "提款密码")
    private String securePwd;

    @ApiModelProperty(value = "会员组")
    private Integer groupId;

    @ApiModelProperty(value = "总代 top 冗余字段")
    private Integer tagencyId;

    @ApiModelProperty(value = "直属代理 direct")
    private Integer cagencyId;

    @ApiModelProperty(value = "分线代理id")
    private Integer subCagencyId;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "联系电话号码")
    private String mobile;

    public void setMobile(String mobile) {
        this.mobile = new AccountEncryption().accountMobieEncrypt(mobile);
    }

    @ApiModelProperty(value = "备注")
    private String memo;

    @Transient
    @ApiModelProperty(value = "加密手机号")
    private List<String> mobileEncrypt;

    @Transient
    @ApiModelProperty(value = "新备注")
    private String newMemo;

    @Transient
    @ApiModelProperty(value = "代理备注【用于前端显示】")
    private String agyMemo;

    @ApiModelProperty(value = "是否黑名称(1,是，0否)")
    private Byte isLock;

    @ApiModelProperty(value = "1开启，0禁用,2余额冻结")
    private Byte available;

    @ApiModelProperty(value = "最后登录时间")
    private String loginTime;

    @Transient
    @ApiModelProperty(value = "最后登录ip")
    private String loginIp;

    @Transient
    @ApiModelProperty(value = "最后登录ip")
    private String checkip;

    @ApiModelProperty(value = "")
    private String registerTime;

    @ApiModelProperty(value = "离线时间")
    private String offLineTime;

    @ApiModelProperty(value = "修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "1在线，0离线")
    private Byte isOnline;

    @ApiModelProperty(value = "是否验证手机(1验证，0未验证)")
    private Byte isVerifyMoblie;

    @ApiModelProperty(value = "是否验证邮箱(1验证，0未验证)")
    private Byte isVerifyEmail;

    @ApiModelProperty(value = "手机否接收消息(1允许，0不允许)")
    private Byte isAllowMsg;

    @ApiModelProperty(value = "邮箱否接收消息(1允许，0不允许)")
    private Byte isAllowEmail;

    @ApiModelProperty(value = "QQ号码")
    private String qq;

    @ApiModelProperty(value = "微信号码")
    private String weChat;

    @ApiModelProperty(value = "地区ID")
    private Integer areaId;

    @ApiModelProperty(value = "")
    private String address;

    @ApiModelProperty(value = "免转钱包开关 :0 关  1 开")
    private Integer freeWalletSwitch;

    @ApiModelProperty(value = "活动层级id")
    private Integer actLevelId;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "生日")
    private String birthday;

    @ApiModelProperty(value = "推广类型 0好友，1呼朋")
    private Integer  promoteType;
    
    @ApiModelProperty(value = "会员标签id")
    private Integer labelid;

    @Transient
    @ApiModelProperty(value = "活动层级ID 查询使用")
    private List<String> actLevelIdList;

    @Transient
    @ApiModelProperty(value = "活动层级 默认1级")
    private String tierName;

    @ApiModelProperty(value = "是否锁定活动等级 0否 1是 默认否")
    private Integer isActivityLock;

    @JsonIgnore
    @ApiModelProperty(value = "加密专用")
    private String salt;
    /**
     * 注册IP
     */
    @Transient
    private String registerIp;

    /**
     * 注册设备号
     */
    @Transient
    private String registerDevice;
    /**
     * 注册来源url
     */
    @Transient
    private String registerUrl;
    /**
     * 登陆来源 登陆来源(0 PC,3 H5 4APP)
     */
    private Byte loginSource;
    /**
     * 注册来源 注册来源( 0 PC 1管理后端(v2) 3 wap(h5) 4 APP   5 代理后台  6 帮好友注册)
     */
    @Transient
    private Byte registerSource;
    /**
     * 钱包余额
     */
    @Transient
    private BigDecimal balance;
    /**
     * 总金额
     */
    @Transient
    private BigDecimal totalBalance;
    /**
     * 总存款
     */
    @Transient
    private BigDecimal totalDeposit;
    /**
     * 总取款
     */
    @Transient
    private BigDecimal totalWithdrawal;

    /**
     * 总输赢
     */
    @Transient
    private BigDecimal totalPayout;
    /**
     * 代理账号
     */
    @Transient
    @ApiModelProperty(value = "")
    private String agyAccount;

    /**
     * 总代账号
     */
    @Transient
    private String tagyAccount;
    /**
     * 最后登录时间结束 表 : mbr_account 对应字段 : loginTimeEnd
     */
    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String loginTimeEnd;

    /**
     * 注册时间结束 表 : mbr_account 对应字段 : registerTimeEnd
     */

    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String registerTimeEnd;


    /**
     * 修改时间结束 表 : mbr_account 对应字段 : modifyTimeEnd
     */

    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String modifyTimeEnd;

    /**
     * 会员组名称
     */
    @Transient
    private String groupName;

    @Transient
    private BaseAuth baseAuth;
    @Transient
    private Set<String> columnSets;

    //会员组ids
    @Transient
    private List<Integer> groupIds;

    @Transient
    private List<Integer> accountIds;
    //总代组ids
    @Transient
    private List<Integer> tagencyIds;
    //代理组ids
    @Transient
    private List<Integer> cagencyIds;

    @ApiModelProperty(value = "代理推广代码,可选")
    @Transient
    private String spreadCode;

    @ApiModelProperty(value = "验证码")
    @Transient
    private String captchareg;

    @ApiModelProperty(value="行为验证码")
    @Transient
    private String captchaVerification;
    @ApiModelProperty(value = "手机验证码")
    @Transient
    private String phoneCaptchareg;

    @ApiModelProperty(value = "站点前缀")
    @Transient
    private String websiteTitle;

    @ApiModelProperty(value = "站点名称")
    @Transient
    private String siteFore;

    @ApiModelProperty(value = "首存时间")
    @Transient
    private String depositTime;
    
    @Transient
    @ApiModelProperty(value = "首存时间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String depositTimeStart;
    
    @Transient
    @ApiModelProperty(value = "首存时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String depositTimeEnd;

    // 0禁用,1开启 ,2余额冻结
    public interface Status {
        int DISABLED = 0;
        int VALID = 1;
        int LOCKED = 2;
    }

    @Transient
    @ApiModelProperty(value = "注册来源：  0 PC 1管理后端(v2) 3 wap(h5) 4 APP   5 代理后台  6 帮好友注册")
    private String registerSourceList;

    @Transient
    @ApiModelProperty(value = "登陆来源：0 PC，3 H5")
    private String loginSourceList;

    @Transient
    @ApiModelProperty(value = "总代 查询使用")
    private String tagencyIdList;

    @Transient
    @ApiModelProperty(value = "直属代理 查询使用")
    private String cagencyIdList;

    @Transient
    @ApiModelProperty(value = "会员组 查询使用")
    private List<String> groupIdList;

    @Transient
    @ApiModelProperty(value = "状态 查询使用")
    private String availableList;

    @Transient
    @ApiModelProperty(value = "1在线，0离线 查询使用")
    private String isOnlineList;

    @Transient
    @ApiModelProperty(value = "推广链接")
    private String promotionUrl;

    @Transient
    @ApiModelProperty(value = "H5生成二维码链接")
    private String promotionH5Url;

    @Transient
    @ApiModelProperty(value = "推广人数: 直属下级")
    private Integer promotionNum;

    @Transient
    @ApiModelProperty(value = "推荐人")
    private String referrer;

    @Transient
    private Integer codeId;

    @ApiModelProperty(value = "推广code")
    private String domainCode;

    @Transient
    @ApiModelProperty(value = "上级会员")
    private String parentId;

    @Transient
    @ApiModelProperty(value = "会员推广树深度")
    private Integer depth;

    @ApiModelProperty(value = "等级")
    @Transient
    private Integer accountLevel;

    @Transient
    private String codeSign;

    @ApiModelProperty(value = "返利比例")
    private BigDecimal rebateRatio;

    @Transient
    private String content;

    // 群发短信统计返回数据
    @Transient
    @ApiModelProperty(value = "选中会员")
    private Integer totalAcc;
    @Transient
    @ApiModelProperty(value = "无手机号会员")
    private Integer accWithoutMobile;
    @Transient
    @ApiModelProperty(value = "实发数量")
    private Integer accWithMobile;
    @Transient
    @ApiModelProperty(value = "推荐人")
    private String supLoginName;
    @Transient
    @ApiModelProperty(value = "上级会员loginName")
    private String parentLoginName;
    @Transient
    @ApiModelProperty(value = "银行卡号")
    private String cardNo;


    @Transient
    @ApiModelProperty(value = "推广人数")
    private Integer minPromotionNum;

    @Transient
    @ApiModelProperty(value = "推广人数")
    private Integer maxPromotionNum;

    @Transient
    private BigDecimal minBalance;

    @Transient
    private BigDecimal maxBalance;


    @Transient
    @ApiModelProperty(value = "会员名多选 查询使用")
    private String userNames;
    
    @Transient
    @ApiModelProperty(value = "会员名多选 查询使用")
    private List<String> loginNameList;

    // 外围系统新增数据字段
    @Transient
    @ApiModelProperty(value = "代理类别： 0 公司(总代)/1股东/2总代/ >2 代理 -1会员  -2汇总")
    public Integer agentType;
    @Transient
    @ApiModelProperty(value = "投注状态 1开启，0关闭")
    private Integer bettingStatus;
    @Transient
    @ApiModelProperty(value = "真人洗码佣金比例")
    private BigDecimal realpeoplewash;
    @Transient
    @ApiModelProperty(value = "电子洗码佣金比例")
    private BigDecimal electronicwash;

    @Transient
    @ApiModelProperty(value = "是否是代理列表查询会员： 1是  null为不是")
    private Integer isAgentQry;

    @Transient
    @ApiModelProperty(value = "登录锁定状态 0未锁定，1已锁定")
    private Integer loginLock;

    @Transient
    @ApiModelProperty(value = "投注大于100的最近一天的时间")
    private String betDate;

    @Transient
    @ApiModelProperty(value = "最后登录地区")
    private String loginArea;

    @Transient
    private Boolean addAgent = Boolean.FALSE;

    @ApiModelProperty(value = "昵称")
    private String nickName;
    @ApiModelProperty(value = "昵称修改时间")
    private String nickNameTime;

    @ApiModelProperty(value="手机区号：86中国，886台湾")
    private String mobileAreaCode;

    @ApiModelProperty(value = "存款锁定状态  0正常 1冻结")
    private Integer depositLock;

    @Transient
    @ApiModelProperty(value = "导出：卡-省份-市")
    private String bankArea;
    @Transient
    @ApiModelProperty(value = "导出：注册IP地址")
    private String regArea;
    @Transient
    @ApiModelProperty(value = "导出：线上入款次数")
    private String onlineDepositCount;
    @Transient
    @ApiModelProperty(value = "导出：线上入款金额")
    private String onlineDepositAmount;
    @Transient
    @ApiModelProperty(value = "导出：公司入款次数")
    private String conDepositCount;
    @Transient
    @ApiModelProperty(value = "导出：公司入款金额")
    private String conDepositAmount;
    @Transient
    @ApiModelProperty(value = "导出：提款次数")
    private String withdrawCount;
    @Transient
    @ApiModelProperty(value = "导出：提款总额")
    private String withdrawAmount;
    @Transient
    @ApiModelProperty(value = "导出：红利次数")
    private String bonusCount;
    @Transient
    @ApiModelProperty(value = "导出：红利金额")
    private String bonusAmount;
    @Transient
    @ApiModelProperty(value = "导出：1开启，0禁用,2余额冻结")
    private String availableStr;

    @Transient
    @ApiModelProperty(value = "总红利")
    private BigDecimal totalProfit;
    @ApiModelProperty(value = "代理会员级别id")
    private Integer agyLevelId;
    @Transient
    @ApiModelProperty(value = "代理会员级别id组")
    private List<Integer> agyLevelIds;
    @ApiModelProperty(value = "全民代理标志 0非代理会员 1代理会员")
    private Integer agyflag;
    @Transient
    @ApiModelProperty(value = "全民代理标志组 0非代理会员 1代理会员")
    private List<Integer> agyflags;
    @ApiModelProperty(value = "成为全民代理的时间")
    private String agyTime;

    @Transient
    @ApiModelProperty(value = "投注比")
    private BigDecimal betPoint;

    /**
     * 登录类型0 PC、1 wap、2 移动端-IOS、3移动端-Android
     */
    @Transient
    @ApiModelProperty(value = "登录类型")
    private String loginType;
    @Transient
    @ApiModelProperty(value = "登录域名")
    private String loginUrl;
}