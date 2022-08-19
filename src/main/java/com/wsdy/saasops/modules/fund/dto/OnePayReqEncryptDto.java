package com.wsdy.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "OnePayReqEncryptDto", description = "遨游支付代付参数")
public class OnePayReqEncryptDto extends OnePayReqBaseDto {


    private String notify_url;

    private String money;

    private String card_num;

    private Integer bank_code;

    private String card_name;

}
