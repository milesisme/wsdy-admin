package com.wsdy.saasops.modules.system.pay.dto;

import com.wsdy.saasops.modules.system.pay.entity.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AllotDto {
    @ApiModelProperty(value = "会员组id")
    private Integer groupId;
    @ApiModelProperty(value = "银行卡")
    private List<SetBasicSysDepMbr> sysDepMbrs;
    @ApiModelProperty(value = "线上支付")
    private List<SetBasicPaymbrGroupRelation> onlineGroups;
    @ApiModelProperty(value = "自动入款")
    private List<SetBasicFastPayGroup> fastPayGroups;
    @ApiModelProperty(value = "二维码")
    private List<SetBasicQrCodeGroup> qrCodePayGroups;
    @ApiModelProperty(value = "加密货币")
    private List<SetBasicCryptoCurrenciesGroup> crPayGroups;
}
