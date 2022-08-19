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
@ApiModel(value = "SetBacicOnlinepay", description = "")
@Table(name = "set_bacic_onlinePay")
public class SetBacicOnlinepay implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "支付名称")
    private String name;

    @ApiModelProperty(value = "商户号")
    private String merNo;

    @ApiModelProperty(value = "支付平台Id")
    private Integer payId;

    @ApiModelProperty(value = "是否启用 1启用 0禁言")
    private Integer available;

    @ApiModelProperty(value = "创建者")
    private String createUser;

    @ApiModelProperty(value = "前端展示名称")
    private String showName;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新人")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次更新时间")
    private String modifyTime;

    @ApiModelProperty(value = "密钥")
    private String password;

    @ApiModelProperty(value = "限额模式 0区间限额 1固定金额")
    private Integer amountType;

    @ApiModelProperty(value = "单笔存款最小值")
    private BigDecimal minAmout;

    @ApiModelProperty(value = "单笔存款最大值")
    private BigDecimal maxAmout;

    @ApiModelProperty(value = "单笔固定金额")
    private String fixedAmount;

    @ApiModelProperty(value = "单日最大限额")
    private BigDecimal dayMaxAmout;

    @ApiModelProperty(value = "收款账号 id")
    private String bankId;

    @ApiModelProperty(value = "是否删除 逻辑删除 0否 1是")
    private Integer isDelete;

    @ApiModelProperty(value = "日存款金额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "设备来源;PC:0 H5:3")
    private String devSource;
    
    @ApiModelProperty(value = "是否热门：true 是 false 不是")
    private Boolean isHot;
    
    @ApiModelProperty(value = "是否推荐：true 是 false 不是")
    private Boolean isRecommend;

    @ApiModelProperty(value = "三方支付跳转方式 0内跳 1外跳")
    private Integer isJump;
	

    @Transient
    @ApiModelProperty(value = "支付所属:1 QQ 2微信 3京东 4网银 5支付宝 6同略云 7快捷支付 8银联扫码 9风云聚合 10BTP 11银行卡跳转 12个人二维码 13LBT 14卡转卡")
    private Integer paymentType;

    @Transient
    @ApiModelProperty(value = "会员组")
    private List<MbrGroup> groupList;

    @Transient
    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @Transient
    @ApiModelProperty(value = "支付平台CODE")
    private String platfromCode;

    @Transient
    @ApiModelProperty(value = "名称")
    private String payName;

    @Transient
    @ApiModelProperty(value = "支付域名")
    private String payUrl;

    @Transient
    @ApiModelProperty(value = "回调URL")
    private String callbackUrl;

    @Transient
    @ApiModelProperty(value = "是否二维码 1是 0否")
    private Integer urlMethod;

    @Transient
    @ApiModelProperty(value = "支付code")
    private String code;

    @Transient
    @ApiModelProperty(value = "1 按类型排序，2 按渠道排序 3 按限红排序 4 按时间排序")
    private Integer sortItem;

    @Transient
    @ApiModelProperty(value = "asc,desc")
    private String sortBy;

    @Transient
    private Integer terminalType;

    @Transient
    private String bankLogo;

    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<Integer> groupIds;

    @Transient
    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;
}