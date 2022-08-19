package com.wsdy.saasops.api.modules.pay.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;


@Data
@ApiModel(value = "PayParams", description = "盘子支付支付入参")
public class PayParams {

    @ApiModelProperty(value = "充值数值")
    private BigDecimal fee;

    @ApiModelProperty(value = "银行代码")
    private String bankCode;

    @ApiModelProperty(value = "银行卡ID")
    private Integer bankCardId;

    @ApiModelProperty(value = "0代表PC端,1代表手机")
    private Integer terminal;

    @ApiModelProperty(value = "onlinePayId 以前payType")
    private Integer onlinePayId;

    @ApiModelProperty(value = "depositId 线下支付ID （存款ID）")
    private Integer depositId;

    @ApiModelProperty(value = "项目", hidden = true)
    private String subject = "product";

    @ApiModelProperty(value = "备注", hidden = true)
    private String extra = "product";

    @ApiModelProperty(value = "ip", hidden = true)
    private String ip;

    @JsonIgnore
    @ApiModelProperty(value = "会员Id", hidden = true)
    private Integer accountId;

    @JsonIgnore
    @ApiModelProperty(value = "会员名", hidden = true)
    private String loginName;

    @ApiModelProperty(value = "订单号", hidden = true)
    private Long outTradeNo;

    @ApiModelProperty(value = "客户端来源", hidden = true)
    private Byte fundSource;

    @ApiModelProperty(value = "siteCode", hidden = true)
    private String siteCode;

    @ApiModelProperty(value = "转账人姓名", hidden = true)
    private String userName;

    // 加密货币相关
    @ApiModelProperty(value = "加密货币支付渠道id")
    private Integer crId;
    @ApiModelProperty(value = "参考汇率")
    private BigDecimal exchangeRate;
    @ApiModelProperty(value = "货币类型 USDT")
    private String currencyCode;
    @ApiModelProperty(value = "协议类型 ERC20 TRC20")
    private String currencyProtocol;
    @ApiModelProperty(value = "hash")
    private String hash;
    @ApiModelProperty(value = "订单创建时间")
    private String createTime;

    @ApiModelProperty(value = "存款人账号 LBT")
    private String depositUserAcc;

    @ApiModelProperty(value = "极速存款凭证图片")
    private List<String> pictureList;
    @ApiModelProperty(value = "存款订单Id")
    private Integer orderId;
    @ApiModelProperty(value = "多语言存款")
    private String verifyCode;
    @ApiModelProperty(value = "充值代理推广码")
    private String  spreadCode;
}
