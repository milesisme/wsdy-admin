package com.wsdy.saasops.api.modules.pay.dto;

import com.wsdy.saasops.modules.operate.dto.ActivityRuleDto;
import com.wsdy.saasops.modules.system.pay.entity.SysDeposit;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BankResponseDto implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "银行卡")
    private List<SysDeposit> bankCards;
    @ApiModelProperty(value = "支付名称")
    private String bankName;
    @ApiModelProperty(value = "扫码logo")
    private String ewmLogo;
    @ApiModelProperty(value = "logo")
    private String bankLogo;
    @ApiModelProperty(value = "不可用logo")
    private String disableLogo;
    @ApiModelProperty(value = "单笔存款最大值")
    private BigDecimal maxAmout;
    @ApiModelProperty(value = "单笔存款最小值")
    private BigDecimal minAmout;
    @ApiModelProperty(value = "0同略云 1风云支付")
    private Integer urlMethod;
    @ApiModelProperty(value = "前台支付名称")
    private String showName;
    @ApiModelProperty(value = "支付所属:1 QQ 2微信 3京东 4网银 5支付宝 6同略云 7快捷支付 8银联扫码 9风云聚合 10BTP 11银行卡跳转 12个人二维码 13LBT 14卡转卡 15极速存取款")
    private Integer paymentType;
    @ApiModelProperty(value = "支付宝转卡标志 0普通银行卡 1支付宝转卡")
    private String alipayFlg;
    @ApiModelProperty(value = "限额模式 0区间限额 1固定金额")
    private Integer amountType;
    
    @ApiModelProperty(value = "是否热门：true 是 false 不是")
    private Boolean isHot;
    
    @ApiModelProperty(value = "是否推荐：true 是 false 不是")
    private Boolean isRecommend;

    @ApiModelProperty(value = "是否可以传存款姓名")
    private Integer depositName;

    @ApiModelProperty(value = "极速存取款可选金额列表")
    private String fastDWAmount;
    @ApiModelProperty(value = "极速存取款ID")
    private Integer fastDWPayId;

}
