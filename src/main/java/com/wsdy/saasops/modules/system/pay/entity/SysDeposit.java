package com.wsdy.saasops.modules.system.pay.entity;

import com.wsdy.saasops.modules.member.entity.MbrGroup;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.*;

@Setter
@Getter
@ApiModel(value = "SysDeposit", description = "公司入款设置")
@Table(name = "set_basic_sys_deposit")
public class SysDeposit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "状态 :0：禁用，1：启用")
    private Integer available;
    @ApiModelProperty(value = "开户账号")
    private String bankAccount;
    @ApiModelProperty(value = "开户姓名")
    private String realName;
    @ApiModelProperty(value = "银行名称")
    private String bankName;
    @ApiModelProperty(value = "开户银行")
    private Integer bankId;
    @ApiModelProperty(value = "开户支行")
    private String bankBranch;
    @ApiModelProperty(value = "手续费上限金额CNY")
    private BigDecimal feeTop;
    @ApiModelProperty(value = "手续费 按比例收费")
    private BigDecimal feeScale;
    @ApiModelProperty(value = "固定收费")
    private BigDecimal feeFixed;
    @ApiModelProperty(value = "收费的方式 (0-按比例收费 1固定收费)")
    private Integer feeWay;
    @ApiModelProperty(value = "每日最大存款金额")
    private BigDecimal dayMaxAmout;
    @ApiModelProperty(value = "")
    private BigDecimal dayDepAmt;
    @ApiModelProperty(value = "")
    private String createUser;
    @ApiModelProperty(value = "")
    private String createTime;
    @ApiModelProperty(value = "")
    private String modifyUser;
    @ApiModelProperty(value = "")
    private String modifyTime;
    @ApiModelProperty(value = "日存款金额")
    private BigDecimal depositAmount;
    @ApiModelProperty(value = "是否删除 逻辑删除 0否 1是")
    private Integer isDelete;
    @ApiModelProperty(value = "单笔存款最小值")
    private BigDecimal minAmout;
    @ApiModelProperty(value = "单笔存款最大值")
    private BigDecimal maxAmout;
    @ApiModelProperty(value = "单笔固定金额")
    private String fixedAmount;
    @ApiModelProperty(value = "线上支付id")
    private Integer fastPayId;
    @ApiModelProperty(value = "0 银行卡 1自动入款银行卡")
    private Integer type;
    @ApiModelProperty(value = "0，null 不显示，1显示")
    private Integer isShow;
    
    @ApiModelProperty(value = "是否热门：true 是 false 不是")
    private Boolean isHot;
    @ApiModelProperty(value = "是否推荐：true 是 false 不是")
    private Boolean isRecommend;
    
    @Transient
    @ApiModelProperty(value = "会员组")
    private List<MbrGroup> groupList;
    @Transient
    @ApiModelProperty(value = "排序号")
    private Integer sort;
    @Transient
    private String devSource;
    @Transient
    @ApiModelProperty(value = "银行卡log")
    private String bankLog;
    @Transient
    @ApiModelProperty(value = "层数")
    private Integer tier;
    @Transient
    @ApiModelProperty(value = "1 按银行名称排序，2 按限红排序 3 按时间排序")
    private Integer sortItem;
    @Transient
    @ApiModelProperty(value = "asc,desc")
    private String sortBy;
    @Transient
    private String bankLogo;
    @Transient
    @ApiModelProperty(value = "银行卡code")
    private String bankCode;
    @Transient
    @ApiModelProperty(value = "payUrl")
    private String payUrl;
    @Transient
    @ApiModelProperty(value = "密钥")
    private String password;
    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<Integer> groupIds;
    @Transient
    @ApiModelProperty(value = "接入商公司id")
    private String cid;
    @Transient
    @ApiModelProperty(value = "支付渠道唯一标识")
    private String evebBankId;
    @Transient
    @ApiModelProperty(value = "存款方式，remit: 银行卡转账 ,   qrcode: 二维码存款")
    private String category;
    @Transient
    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;
}