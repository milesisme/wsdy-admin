package com.wsdy.saasops.api.modules.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TLYCallBackContent {

    @ApiModelProperty(value = "匹配成功的订单 ID")
    private String order_id;

    @ApiModelProperty(value = "该笔订单匹配的员工ID，如果ID为0，则该笔订单是自动匹配的")
    private String verified_by_user_id;

    @ApiModelProperty(value = "如果该笔订单是自动匹配的，则值固定为“Automatic”，如果是人工匹配的，则值为查帐系统进行匹配的员工账号名")
    private String verified_by_username;

    @ApiModelProperty(value = "订单匹配的 Unix Timestamp")
    private String verified_time;

}
