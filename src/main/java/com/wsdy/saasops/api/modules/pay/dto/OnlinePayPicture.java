package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OnlinePayPicture {
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "onlinePayId")
    private Integer onlinePayId;
    @ApiModelProperty(value = "限额模式 0区间限额 1固定金额")
    private Integer amountType;
    @ApiModelProperty(value = "单笔存款最小值")
    private BigDecimal minAmout;
    @ApiModelProperty(value = "单笔存款最大值")
    private BigDecimal maxAmout;
    @ApiModelProperty(value = "单笔固定金额")
    private String fixedAmount;
    @ApiModelProperty(value = "前端展示名称")
    private String showName;
    @ApiModelProperty(value = "扫码logo")
    private String ewmLogo;
    @ApiModelProperty(value = "bankLogo")
    private String bankLogo;
    @ApiModelProperty(value = "不可用logo")
    private String disableLogo;
    @ApiModelProperty(value = "urlMethod")
    private Integer urlMethod;
    private List<PayPictureData> payData;
    @ApiModelProperty(value = "类型payType 1 QQ 2微信 3京东 4网银 5支付宝 6同略云 7快捷支付 8银联扫码 (9风云聚合 10BTP)  11 银行卡(跳转)  12个人二维码 13 LBT 14 卡转卡 17 EBPAY", hidden = true)
    private Integer paymentType;
    @ApiModelProperty(value = "payID", hidden = true)
    private Integer payId;
    @ApiModelProperty(value = "单日最大限额", hidden = true)
    private BigDecimal dayMaxAmout;
    @ApiModelProperty(value = "日存款金额", hidden = true)
    private BigDecimal depositAmount;
    @ApiModelProperty(value = "是否排队 0否 1是;不排队前端展示根据sort排序取第一个")
    @Transient
    private Integer isQueue;
    
    @ApiModelProperty(value = "是否热门：true 是 false 不是")
    private Boolean isHot;
    @ApiModelProperty(value = "是否推荐：true 是 false 不是")
    private Boolean isRecommend;
    @ApiModelProperty(value = "跳转方式 0内跳 1外跳")
    private Integer isJump;
}
