package com.wsdy.saasops.modules.fund.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "FundMerchantDetail", description = "流水表")
@Table(name = "fund_merchant_detail")
public class FundMerchantDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "商户id fund_merchant_pay")
    private Integer merchantId;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "商户号")
    private String merchantNo;

    @ApiModelProperty(value = "商户唯一订单号")
    private String transId;

    @ApiModelProperty(value = "支付平台订单号")
    private String orderId;

    @ApiModelProperty(value = "银行打款状态 0未处理 1银行处理中 2已打款 3失败")
    private String bankStatus;

    @ApiModelProperty(value = "提款ID fund_acc_withdraw")
    private Integer accWithdrawId;

}