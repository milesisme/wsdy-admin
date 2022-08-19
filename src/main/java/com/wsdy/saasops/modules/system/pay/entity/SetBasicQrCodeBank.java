package com.wsdy.saasops.modules.system.pay.entity;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;

@Getter
@Setter
@ApiModel(value = "SetBasicQrCodeBank", description = "二维码银行关联表")
@Table(name = "set_basic_qrcode_bank")
public class SetBasicQrCodeBank {

    private Integer qrCodeId;

    private Integer bankId;

}
