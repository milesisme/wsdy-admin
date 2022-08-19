package com.wsdy.saasops.modules.system.pay.entity;

import com.wsdy.saasops.modules.member.entity.MbrGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "SetBacicFastPay", description = "")
@Table(name = "set_basic_fastpay")
public class SetBacicFastPay implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "支付名称")
    private String name;

    @ApiModelProperty(value = "是否启用 1启用 0禁言")
    private Integer available;

    @ApiModelProperty(value = "手续费上限金额CNY")
    private BigDecimal feeTop;

    @ApiModelProperty(value = "手续费 按比例收费")
    private BigDecimal feeScale;

    @ApiModelProperty(value = "固定收费")
    private BigDecimal feeFixed;

    @ApiModelProperty(value = "收费的方式 (0-按比例收费 1固定收费)")
    private Integer feeWay;

    @ApiModelProperty(value = "限额模式 0区间限额 1固定金额")
    private Integer amountType;

    @ApiModelProperty(value = "创建者")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新人")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次更新时间")
    private String modifyTime;

    @ApiModelProperty(value = "是否删除 逻辑删除 0否 1是")
    private Integer isDelete;

    @ApiModelProperty(value = "支付平台Id")
    private Integer payId;

    @ApiModelProperty(value = "密钥")
    private String password;
    
    @ApiModelProperty(value = "是否热门：true 是 false 不是")
    private Boolean isHot;
    
    @ApiModelProperty(value = "是否推荐：true 是 false 不是")
    private Boolean isRecommend;
    
    @Transient
    private Integer fastPayId;

    @Transient
    @ApiModelProperty(value = "groupId")
    private Integer groupId;

    @Transient
    private Boolean isSign;

    @ApiModelProperty(value = "showName")
    private String showName;

    @ApiModelProperty(value = "支付渠道唯一标识")
    private String evebBankId;

    @Transient
    @ApiModelProperty(value = "银行卡")
    private List<SysDeposit> deposits;

    @Transient
    @ApiModelProperty(value = "会员组")
    private List<MbrGroup> groupList;

    @Transient
    @ApiModelProperty(value = "1 按类型排序，2 按限红排序 3 按时间排序")
    private Integer sortItem;

    @Transient
    @ApiModelProperty(value = "asc,desc")
    private String sortBy;

    @Transient
    @ApiModelProperty(value = "每日最大存款金额")
    private BigDecimal dayMaxAmout;

    @Transient
    @ApiModelProperty(value = "日存款金额")
    private BigDecimal depositAmount;

    @Transient
    private String bankLogo;

    @Transient
    @ApiModelProperty(value = "如果是支付分配查询银行卡逻辑不一样")
    private Boolean isAllocation = Boolean.TRUE;

    @Transient
    @ApiModelProperty(value = "asc,desc")
    private Integer sort;

    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<Integer> groupIds;

    @ApiModelProperty(value = "风云聚合接入商id --> 商户号")
    private String cid;

    @Transient
    @ApiModelProperty(value = "支付code")
    private String platfromCode;

    @Transient
    @ApiModelProperty(value = "支付平台code t_pay表中的code")
    private String payCode;

    @Transient
    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;

    @Transient
    @ApiModelProperty(value = "支付所属:1 QQ 2微信 3京东 4网银 5支付宝 6同略云 7快捷支付 8银联扫码 9风云聚合 10BTP 11银行卡跳转 12个人二维码 13LBT 14卡转卡 15极速存取款")
    private Integer paymentType;


    @ApiModelProperty(value = "支付宝转卡标志 0普通银行卡 1支付宝转卡")
    private String alipayFlg;
    @ApiModelProperty(value = "极速取款每日最大限额")
    private BigDecimal fastDWDayMaxAmout;
    @ApiModelProperty(value = "极速存取款固定金额")
    private String fastDWAmount;
    @Transient
    @ApiModelProperty(value = "支付地址")
    private String payUrl;

}