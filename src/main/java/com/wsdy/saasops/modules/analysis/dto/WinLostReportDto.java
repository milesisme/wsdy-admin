package com.wsdy.saasops.modules.analysis.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.math.BigDecimal;

@Data
public class WinLostReportDto {

    @ApiModelProperty(value = "null厅主 0股东 1总代 2代理  account 会员")
    private String level;

    @ApiModelProperty(value = "投注人数")
    private Long total;

    @ApiModelProperty(value = "投注金额")
    private BigDecimal betTotal;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validbetTotal;

    @ApiModelProperty(value = "派彩")
    private BigDecimal payoutTotal;

    @ApiModelProperty(value = "类别")
    private String categoryTotal;

 
    @ApiModelProperty(value = "代理账户 or 会员账户")
    private String username;

    @ApiModelProperty(value = "投注数")
    private Integer quantity;

    @ApiModelProperty(value = "首存时间")
    private String depositTime;
}
