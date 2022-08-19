package com.wsdy.saasops.modules.fund.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.wsdy.saasops.modules.base.entity.BaseAuth;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "FundAccWithdraw", description = "FundAccWithdraw")
@Table(name = "fund_acc_withdraw")
public class AccWithdraw implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "订单前缀")
    private String orderPrefix;
    @ApiModelProperty(value = "会员Id")
    private Integer accountId;
    @ApiModelProperty(value = "提款状态(0 拒绝 1 通过 2待处理 3 出款中 4自动出款人工审核 5自动出款中 6初审待定 7失败订单)")    //0.1,为前端传来的动作， 2初审，3复审，6初审待定
    private Integer status;
    @ApiModelProperty(value = "0 手动出款 1自动出款 3处理中")
    private Integer type;
    @ApiModelProperty(value = "提款金额")
    private BigDecimal drawingAmount;
    @ApiModelProperty(value = "转账手续费")
    private BigDecimal handlingCharge;
    @ApiModelProperty(value = "行政扣款")
    private BigDecimal cutAmount;
    @ApiModelProperty(value = "扣除优惠")
    private BigDecimal discountAmount;
    @ApiModelProperty(value = "实际出款")
    private BigDecimal actualArrival;
    @ApiModelProperty(value = "实际出款: 加密货币")
    private BigDecimal actualArrivalCr;
    @ApiModelProperty(value = "参考汇率")
    private BigDecimal exchangeRate;
    @ApiModelProperty(value = "审核人")
    private String auditUser;
    @ApiModelProperty(value = "审核时间")
    private String auditTime;
    @ApiModelProperty(value = "出款人")
    private String passUser;
    @ApiModelProperty(value = "出款时间")
    private String passTime;
    @ApiModelProperty(value = "ip")
    private String ip;
    @ApiModelProperty(value = "初审备注")
    private String memo;
    @ApiModelProperty(value = "")
    private String createUser;
    @ApiModelProperty(value = "")
    private String createTime;
    @ApiModelProperty(value = "")
    private String modifyUser;
    @ApiModelProperty(value = "")
    private String modifyTime;
    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;
    @ApiModelProperty(value = "取款申请来源：0 PC，3 H5")
    private Byte withdrawSource;
    @ApiModelProperty(value = "0扣自己 1扣代理")
    private Integer feeType;
    @ApiModelProperty(value = "复审备注")
    private String memoWithdraw;
    @ApiModelProperty(value = "会员提款银行卡信息(bankcardId)")
    private Integer bankCardId;
    @ApiModelProperty(value = "会员提款加密货币钱包信息(MbrCryptoCurrencies id)")
    private Integer cryptoCurrenciesId;
    @ApiModelProperty(value = "提现方式：0银行卡， 1加密货币钱包，2支付宝，5极速提款，6人工提款")
    private Integer methodType;
    // 取款操作锁定相关
    @ApiModelProperty(value = "状态 0未锁定；1已锁定")
    private Integer lockStatus;
    @ApiModelProperty(value = "当前锁定操作人")
    private String lockOperator;
    @ApiModelProperty(value = "最近一次的锁定时间")
    private String lastLockTime;
    @ApiModelProperty(value = "催单次数")
    private Integer cuiCount;
    @Transient
    @ApiModelProperty(value = "是否当前用户锁定 0 未锁定 1当前用户锁定 2 非当前用户锁定")
    private Integer isCurrentUserLock;
    
    @ApiModelProperty(value = "USDT hash")
    private String hash;

    @ApiModelProperty(value = "变更备注")
    private String changeMemo;

    @ApiModelProperty(value = "提款来源")
    private Integer source;
    
    @ApiModelProperty(value = "是否免审")
    private Boolean isExempt;
    
    @ApiModelProperty(value = "免审人")
    private String exemptUser;
    
    @ApiModelProperty(value = "免审时间")
    private Date exemptTime;
    
    @ApiModelProperty(value = "免审备注")
    private String exemptMemo;
    
    @ApiModelProperty(value = "人工提款的关联订单号")
    private String relatedOrderno;

    // ---------------- Transient字段 -------------
    @Transient
    @ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String modifyTimeEnd;
    @Transient
    @ApiModelProperty(value = "出款完成之后会员姓名")
    private String realName;
    @Transient
    @ApiModelProperty(value = "会员组ID")
    private Integer groupId;
    @Transient
    @ApiModelProperty(value = "会员组名字")
    private String groupName;
    @Transient
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @Transient
    @ApiModelProperty(value = "VIP等级")
    private String tierName;
    @Transient
    @ApiModelProperty(value = "直属代理")
    private String agyAccount;
    @Transient
    @ApiModelProperty(value = "总代理")
    private String topAgyAccount;
    @Transient
    private Integer agyTopAccountId;
    @Transient
    private Integer agyAccountId;
    @Transient
    @ApiModelProperty(value = "申请时间开始")
    private String createTimeFrom;
    @Transient
    @ApiModelProperty(value = "申请时间结束")
    private String createTimeTo;
    @Transient
    @ApiModelProperty(value = "提款信息")
    private MbrBankcard mbrBankcard;
    @Transient
    @ApiModelProperty(value = "提款次数")
    private Integer withdrawCount;
    @Transient
    @ApiModelProperty(value = "登录用户的名字")
    private String loginSysUserName;
    @Transient
    @ApiModelProperty(value = "提款银行")
    private String bankName;
    @Transient
    @ApiModelProperty(value = "分行信息地址")
    private String address;
    @Transient
    private Integer notStatus;
    @Transient
    private BaseAuth baseAuth;
    @Transient
    private Integer merchantId;
    @Transient
    private String transId;
    @Transient
    @ApiModelProperty(value = "代付平台：第三方订单号")
    private String orderId;
    @Transient
    private Integer merchantDetailId;
    @Transient
    private List<Integer> topAgyAccounts;
    @Transient
    private List<Integer> agyAccountIds;
    @Transient
    private List<Integer> groupIds;
    @Transient
    private List<Integer> statuss;
    @Transient
    @ApiModelProperty(value = "取款申请来源：0 PC，3 H5")
    private String withdrawSourceList;
    @Transient
    @ApiModelProperty(value = "总代 top")
    private Integer tagencyId;
    @Transient
    @ApiModelProperty(value = "导出：状态名称")
    private String statusStr;
    @Transient
    @ApiModelProperty(value = "导出：出款方式名称")
    private String typeStr;
    @Transient
    @ApiModelProperty(value = "导出：提现方式：0银行卡， 1加密货币钱包")
    private String methodTypeStr;
    @Transient
    @ApiModelProperty(value = "今日取款")
    private BigDecimal todayWithdraw;
    // 加密钱包相关
    @Transient
    @ApiModelProperty(value = "加密钱包类型")
    private String walletName;
    @Transient
    @ApiModelProperty(value = "钱包地址")
    private String walletAddress;
    @Transient
    @ApiModelProperty(value = "货币类型 USDT")
    private String currencyCode;
    @Transient
    @ApiModelProperty(value = "协议类型 ERC20 TRC20")
    private String currencyProtocol;
    // 拒绝时新增的稽核
    @Transient
    @ApiModelProperty(value = "本金：存款金额")
    private BigDecimal depositAmount;
    @Transient
    @ApiModelProperty(value = "流水需求：存款稽核")
    private BigDecimal auditAmount;

    @Transient
    @ApiModelProperty(value = "站内性类型 1流水不足，2违规下注，3优惠套利，4审核备注，空 默认")
    private Integer inMailType;

    @Transient
    @ApiModelProperty(value = "出款开始时间:前端查询")
    private String startPassTime;
    @Transient
    @ApiModelProperty(value = "出款结束时间:前端查询")
    private String endPassTime;

    @Transient
    @ApiModelProperty(value = "是否可以变更状态")
    private Integer canChangeStatus;

    @Transient
    @ApiModelProperty(value = "siteCode")
    private String siteCode;
    @Transient
    @ApiModelProperty(value = "verifyCode")
    private String verifyCode;

    @Transient
    private String orderBy;

    @Transient
    @ApiModelProperty(value = "最小提款金额")
    private BigDecimal minDrawingAmount;

    @Transient
    @ApiModelProperty(value = "最大提款金额")
    private BigDecimal maxDrawingAmount;


    public interface Status {
        byte rejective=0;//拒绝
        byte suc = 1;//取款成功
        byte apply = 2;//待处理
        byte process = 3;//出款中
    }
}