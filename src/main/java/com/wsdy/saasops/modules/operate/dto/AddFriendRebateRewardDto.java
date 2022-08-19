package com.wsdy.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AddFriendRebateRewardDto {

        @ApiModelProperty(value = "发送给")
        private String loginName;

        @ApiModelProperty(value = "金额")
        private BigDecimal amount;

        @ApiModelProperty(value = "返利类型")
        private Integer rewardType;

        @ApiModelProperty(value = "好友账号")
        private  String subLoginName;

        @ApiModelProperty(value = "稽核")
        private Integer audit;

        @ApiModelProperty(value = "稽核倍数")
        private Integer auditMultiple;

        @ApiModelProperty(value = "备注")
        private String memo;


}
