package com.wsdy.saasops.agapi.modules.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DataTrendParamDto {

    @ApiModelProperty(value = "日期")
    private String time;

    @ApiModelProperty(value = "1净输赢 2总存款 3首存金额 4投注金额" +
            "5 总取款 6新注册人数 7首存人数 8存款人数 9取款人数 10投注人数")
    private int type;

    private Integer agentId;

    private Integer subcagencyId;

    @ApiModelProperty(value = "日期")
    private String startTime;

    @ApiModelProperty(value = "日期")
    private String endTime;
}
