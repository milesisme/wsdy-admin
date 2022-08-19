package com.wsdy.saasops.modules.system.pay.entity;

import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.operate.dto.ActivityRuleDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@ApiModel(value = "SetBasicSysCryptoCurrencies", description = "站点加密货币")
@Table(name = "set_basic_sys_cryptocurrencies")
public class SetBasicSysCryptoCurrencies implements Serializable{

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "状态 :0：禁用，1：启用")
    private Integer available;

    @ApiModelProperty(value = "支付名称")
    private String name;

    @ApiModelProperty(value = "前台展示名称")
    private String showName;

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

    @ApiModelProperty(value = "商户号")
    private String merNo;

    @ApiModelProperty(value = "密钥")
    private String password;

    @ApiModelProperty(value = "")
    private String createUser;

    @ApiModelProperty(value = "")
    private String createTime;

    @ApiModelProperty(value = "")
    private String modifyUser;

    @ApiModelProperty(value = "")
    private String modifyTime;

    @ApiModelProperty(value = "日已存款金额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "是否删除 逻辑删除 0否 1是")
    private Integer isDelete;

    @ApiModelProperty(value = "单笔存款最小值")
    private BigDecimal minAmout;

    @ApiModelProperty(value = "单笔存款最大值")
    private BigDecimal maxAmout;
    
    @ApiModelProperty(value = "是否热门：true 是 false 不是")
    private Boolean isHot;
    
    @ApiModelProperty(value = "是否推荐：true 是 false 不是")
    private Boolean isRecommend;

    @Transient
    @ApiModelProperty(value = "会员组")
    private List<MbrGroup> groupList;
    @Transient
    @ApiModelProperty(value = "会员组id")
    private Integer groupId;
    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<Integer> groupIds;

    @Transient
    @ApiModelProperty(value = "开户银行id,t_bs_bank主键id")
    private List<Integer> bankIds;


    @Transient
    @ApiModelProperty(value = "1 按银行名称排序，2 按限红排序 3 按时间排序")
    private Integer sortItem;
    @Transient
    @ApiModelProperty(value = "排序号")
    private Integer sort;
    @Transient
    @ApiModelProperty(value = "asc,desc")
    private String sortBy;

    @Transient
    private String devSource;

    @Transient
    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    private Integer isQueue;

    @Transient
    @ApiModelProperty(value = "会员组ids")
    private List<BaseBank> bankList;

    @Transient
    @ApiModelProperty(value = "1 已分配到会员组，0 未分配到会员组")
    private Integer selected;

    @Transient
    @ApiModelProperty(value = "0 跳转，1不跳转（前端处理生成二维码）")
    private Integer urlMethod = 1;

    @Transient
    @ApiModelProperty(value = "币种 = t_bs_bank bankname")
    private String crName;

    @Transient
    @ApiModelProperty(value = "加密货币 币种")
    private String bankCode;
    @Transient
    @ApiModelProperty(value = "协议类型")
    private String category;
    @Transient
    @ApiModelProperty(value = "t_bs_bank id")
    private Integer bankCardId;

    @Transient
    @ApiModelProperty(value = "支付图标")
    private String bankLog;
    @Transient
    @ApiModelProperty(value = "支付图标")
    private String backBankImg;

    @ApiModelProperty(value = "货币类型 USDT")
    private String currencyCode;
    @ApiModelProperty(value = "协议类型 ERC20 TRC20")
    private String currencyProtocol;

    @Transient
    @ApiModelProperty(value = "支付分配图标")
    private String bankLogo;

}
