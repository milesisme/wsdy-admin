package com.wsdy.saasops.modules.fund.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "FundDeposit", description = "FundDeposit")
@Table(name = "fund_deposit")
public class FundDeposit implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    @NotNull
    private Integer id;
    @ApiModelProperty(value = "会员ID(mbr_account）")
    private Integer accountId;
    @ApiModelProperty(value = "公司入款设置ID(set_basic_sys_deposit 主键)")
    private Integer companyPayId;
    @ApiModelProperty(value = "收款账户（线上支付ID）")
    private Integer onlinePayId;
    @ApiModelProperty(value = "收款账户（个人二维码ID）")
    private Integer qrCodeId;
    @ApiModelProperty(value = "加密货币支付渠道id")
    private Integer crId;
    @ApiModelProperty(value = "加密货币支付 hash")
    private String hash;
    @ApiModelProperty(value = "0 线上入款 ,1 公司入款 ,2 个人二维码  3加密货币 4代理充值 5极速入款")
    private Integer mark;
    @ApiModelProperty(value = "0 失败 1 成功 2待处理")
    private Integer status;
    @ApiModelProperty(value = "false 未支付 true 支付")
    private Boolean isPayment;
    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;
    @ApiModelProperty(value = "存款金额 加密货币")
    private BigDecimal depositAmountCr;
    @ApiModelProperty(value = "存款人姓名")
    private String depositUser;
    @ApiModelProperty(value = "手续费")
    private BigDecimal handlingCharge;
    @ApiModelProperty(value = "实际到账")
    private BigDecimal actualArrival;
    @ApiModelProperty(value = "审核人")
    private String auditUser;
    @ApiModelProperty(value = "审核时间")
    private String auditTime;
    @ApiModelProperty(value = "ip")
    private String ip;
    @ApiModelProperty(value = "活动ID")
    private Integer activityId;
    @ApiModelProperty(value = "备注")
    private String memo;
    @ApiModelProperty(value = "")
    private String createUser;
    @ApiModelProperty(value = "")
    private String createTime;
    @ApiModelProperty(value = "")
    private String modifyUser;
    @ApiModelProperty(value = "")
    private String modifyTime;
    @ApiModelProperty(value = "加密货币汇率")
    private BigDecimal exchangeRate;
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "订单前缀")
    private String orderPrefix;
    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;
    @ApiModelProperty(value = "存款附言")
    private String depositPostscript;
    @ApiModelProperty(value = "手续费还返默认(为1 扣（减少） ，为0 手续费已处理（增加）)")
    private Byte handingback;
    @ApiModelProperty(value = "存款来源：0 PC，3 H5")
    private Byte fundSource;
    @ApiModelProperty(value = "0扣自己 1扣代理")
    private Integer feeType;
    @ApiModelProperty(value = "第三方支付订单or 订单id")
    private String payOrderNo;
    @ApiModelProperty(value = "多语言站点使用code")
    private String verifyCode;
    @ApiModelProperty(value = "代理推广码，蜜桃使用")
    private String spreadCode;

    @Transient
    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount;
    @Transient
    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String modifyTimeEnd;
    @Transient
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @Transient
    private String groupName;
    @Transient
    @ApiModelProperty(value = "代理名")
    private String agyAccount;
    @Transient
    private String agyAccountStr;
    // 是否为测试账号
    @Transient
    private Integer isTest; // 0正式 1测试
    @Transient
    private Integer agyTopAccountId;
    @Transient
    private Integer agyAccountId;
    @Transient
    private Integer depositId;
    @Transient
    @ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String createTimeFrom;
    @Transient
    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String createTimeTo;
    @Transient
    @ApiModelProperty(value = "会员组ID")
    private Integer groupId;
    @Transient
    @ApiModelProperty(value = "支付次数")
    private String depositCount;
    @Transient
    @ApiModelProperty(value = "线上支付ID名称")
    private String onlinePayName;
    @Transient
    @ApiModelProperty(value = "公司入款类型")
    private String depositType;
    @Transient
    @ApiModelProperty(value = "是否提款 0否 1是")
    private Boolean isDrawings;
    @Transient
    @ApiModelProperty(value = "真实姓名")
    private String realName;
    @Transient
    private int isSign;
    @Transient
    @ApiModelProperty(value = "失败 成功  待处理")
    private String statusStr;
    @Transient
    @ApiModelProperty(value = "审核开始时间 yyyy-MM-dd HH:mm:ss")
    private String auditTimeFrom;
    @Transient
    @ApiModelProperty(value = "审核结束时间 yyyy-MM-dd HH:mm:ss")
    private String auditTimeTo;
    @Transient
    @ApiModelProperty(value = "审核开始时间 yyyy-MM-dd HH:mm:ss")
    private String auditTimeFromStr;
    @Transient
    @ApiModelProperty(value = "审核结束时间 yyyy-MM-dd HH:mm:ss")
    private String auditTimeToStr;
    @Transient
    @ApiModelProperty(value = "登录用户的名字")
    private String loginSysUserName;

    public interface Mark {
        Integer onlinePay = 0;// 0 线上入款
        Integer offlinePay = 1;// 1 公司入款
        Integer qrCodePay = 2;// 2 个人二维码
        Integer crPay = 3; // 3加密货币
        Integer affPay = 4; //代理充值
    }
    public interface Status {
        int fail = 0;// 0 失败
        int suc = 1;// 1 成功
        int apply = 2;// 2待处理
    }
    public interface PaymentStatus {
        boolean unPay = false;
        boolean pay = true;
    }

    @Transient
    @ApiModelProperty(value = "总代 查询接口使用")
    private List<Integer> agyTopAccountIds;
    @Transient
    @ApiModelProperty(value = "会员ID(mbr_account） 查询接口使用")
    private List<Integer> accountIds;
    @Transient
    @ApiModelProperty(value = "存款渠道集合  查询接口使用")
    private List<Integer> companyPayIds;
    @Transient
    @ApiModelProperty(value = "代理方式集合  查询接口使用")
    private List<Integer> agyAccountIds;
    @Transient
    @ApiModelProperty(value = "加密货币渠道稽核  查询接口使用")
    private List<Integer> crIds;
    @Transient
    @ApiModelProperty(value = "会员组 查询接口使用")
    private List<Integer> groupIds;
    @Transient
    @ApiModelProperty(value = "公司入款类型 查询接口使用")
    private List<String> depositTypes;
    @Transient
    @ApiModelProperty(value = "状态 查询接口使用")
    private String statuss;
    @Transient
    @ApiModelProperty(value = "收款账户（线上支付ID） 查询接口使用")
    private List<Integer> onlinePayIds;
    @Transient
    @ApiModelProperty(value = "存款来源：0 PC，3 H5")
    private String fundSourceList;
    @Transient
    @ApiModelProperty(value = "支付机构：盘子支付。。。")
    private String platfromName;
    //收款渠道查询使用
    @Transient
    @ApiModelProperty(value = "0 线上入款 ,1 公司入款 ,2 个人二维码  3加密货币 4代理充值 5极速存款")
    private String markStr;
    //订单号查询使用
    @Transient
    @ApiModelProperty(value = "订单号模糊查询使用")
    private String orderNoStr;
    //收款渠道
    @Transient
    @ApiModelProperty(value = "收款渠道,给前端使用")
    private String payType;
    //充值记录类型返回
    @Transient
    @ApiModelProperty(value = "收款渠道,给前端使用")
    private String depositTypeName;
    @Transient
    @ApiModelProperty(value = "总代 查询使用")
    private Integer tagencyId;
    @Transient
    @ApiModelProperty(value = "是否通过 0不 1通过 2已经申请")
    private Integer isActivityPass;
    @Transient
    @ApiModelProperty(value = "今日入款")
    private BigDecimal todayDeposit;
    @Transient
    @ApiModelProperty(value = "存款渠道集合  普通扫码支付 查询接口使用")
    private List<Integer> qrCodePayIds;
    @Transient
    @ApiModelProperty(value = "货币类型 USDT")
    private String currencyCode;
    @Transient
    @ApiModelProperty(value = "加密货币渠道稽核  查询接口使用 货币类型 USDT")
    private List<String> currencyCodes;
    @Transient
    @ApiModelProperty(value = "协议类型 ERC20 TRC20")
    private String currencyProtocol;
    @Transient
    @ApiModelProperty(value = "加密货币渠道稽核  查询接口使用 协议类型 ERC20 TRC20")
    private List<String>  currencyProtocols;
    @Transient
    @ApiModelProperty(value = "存款人账号 LBT")
    private String depositUserAcc;
    @Transient
    @ApiModelProperty(value = "人工增加类型 0 优惠添加 1 额度补回 3 其他 4代理充值")
    private Integer auditAddType;

    @Transient
    @ApiModelProperty(value = "审核开始时间:前端查询")
    private String startAuditTime;
    @Transient
    @ApiModelProperty(value = "审核结束时间:前端查询")
    private String endAuditTime;

    @Transient
    @ApiModelProperty(value = "VIP等级")
    private String tierName;

    @Transient
    @ApiModelProperty(value = "是否极速存款订单")
    private Integer fastDeposit;
    @Transient
    @ApiModelProperty(value = "极速存款凭证图片列表")
    private List<String> fastDepositPicture;

    @Transient
    private String orderBy;

    @Transient
    @ApiModelProperty(value = "开始存款金额")
    private BigDecimal minDepositAmount;

    @Transient
    @ApiModelProperty(value = "结束存款金额")
    private BigDecimal maxDepositAmount;


}