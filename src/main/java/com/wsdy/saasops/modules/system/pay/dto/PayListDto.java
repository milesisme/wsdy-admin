package com.wsdy.saasops.modules.system.pay.dto;

import com.wsdy.saasops.modules.system.pay.entity.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@ApiModel(value = "PayGroupDto", description = "支付分配")
public class PayListDto {

    @ApiModelProperty(value = "银行卡转账")
    private List<SysDeposit> leftDeposits;

    @ApiModelProperty(value = "银行卡转账")
    private List<SysDeposit> rightDeposits;

    @ApiModelProperty(value = "自动入款平台")
    private List<SetBacicFastPay> leftFastPays;

    @ApiModelProperty(value = "自动入款平台")
    private List<SetBacicFastPay> rightFastPay;

    @ApiModelProperty(value = "线上支付 网银")
    private List<SetBacicOnlinepay> leftOnlinepays;

    @ApiModelProperty(value = "线上支付 网银")
    private List<SetBacicOnlinepay> rightOnlinepays;

    @ApiModelProperty(value = "个人二维码")
    private List<SysQrCode> leftQrCodes;

    @ApiModelProperty(value = "个人二维码")
    private List<SysQrCode> rightQrCodes;

    @ApiModelProperty(value = "电子货币")
    private List<SetBasicSysCryptoCurrencies> leftCr;

    @ApiModelProperty(value = "电子货币")
    private List<SetBasicSysCryptoCurrencies> rightCr;

   /* @ApiModelProperty(value = "线上支付 快捷支付")
    private List<SetBacicOnlinepay> leftShortcut;

    @ApiModelProperty(value = "线上支付 快捷支付")
    private List<SetBacicOnlinepay> rightShortcut;

    @ApiModelProperty(value = "线上支付 银联扫码")
    private List<SetBacicOnlinepay> leftUnionPay;

    @ApiModelProperty(value = "线上支付 银联扫码")
    private List<SetBacicOnlinepay> rightUnionPay;*/
}
