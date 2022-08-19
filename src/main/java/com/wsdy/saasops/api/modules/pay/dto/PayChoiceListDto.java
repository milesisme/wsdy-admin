package com.wsdy.saasops.api.modules.pay.dto;

import com.wsdy.saasops.modules.operate.dto.ActivityRuleDto;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysCryptoCurrencies;
import com.wsdy.saasops.modules.system.pay.entity.SysQrCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PayChoiceListDto {

    @ApiModelProperty(value = "所有QQ支付")
    private List<OnlinePayPicture> qqList;

    @ApiModelProperty(value = "所有weChat支付")
    private List<OnlinePayPicture> weChatList;

    @ApiModelProperty(value = "所有JD支付")
    private List<OnlinePayPicture> jdList;

    @ApiModelProperty(value = "所有Alipay支付")
    private List<OnlinePayPicture> alipayList;

    @ApiModelProperty(value = "所有网银支付")
    private List<OnlinePayPicture> wyList;

    @ApiModelProperty(value = "所有在线银行卡支付")
    private List<OnlinePayPicture> bankList;

    @ApiModelProperty(value = "所有个人二维码")
    private List<SysQrCode> qrCodeList;

    @ApiModelProperty(value = "所有数字货币")
    private List<SetBasicSysCryptoCurrencies> crList;

    @ApiModelProperty(value = "所有银行卡-卡转卡支付")
    private List<OnlinePayPicture> bankTransferList;

    @ApiModelProperty(value = "所有聚合支付")
    private List<OnlinePayPicture> aggregationPayList;


    @ApiModelProperty(value = "所有急速存")
    private List<OnlinePayPicture> jscPayList;

    @ApiModelProperty(value = "所有其他支付")
    private List<OnlinePayPicture> otherPayList;

    @ApiModelProperty(value = "ebpay支付")
    private List<OnlinePayPicture> ebpayList;

    @ApiModelProperty(value = "topay支付")
    private List<OnlinePayPicture> topayList;





}
