package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepotHitsDto {
    @ApiModelProperty(value = "站点编码")
    private String website;
    @ApiModelProperty(value = "站点名称")
    private String websiteName;
    @ApiModelProperty(value = "站点总共投注")
    private Integer sitedocCount;
    @ApiModelProperty(value = "orderDate")
    private String orderDate;
    @ApiModelProperty(value = "投注总额")
    private BigDecimal bet;
    @ApiModelProperty(value = "有效投注总额")
    private BigDecimal validBet;
    @ApiModelProperty(value = "活跃人数")
    private String userNameCount;
    @ApiModelProperty(value = "彩金总额")
    private BigDecimal jackpotPayout;
    @ApiModelProperty(value = "派彩总额")
    private BigDecimal payout;
    @ApiModelProperty(value = "key")
    private String key;
    @ApiModelProperty(value = "注单总数")
    private Integer docCount;
    @ApiModelProperty(value = "投注时间")
    private String betDate;
    @ApiModelProperty(value = "平台")
    private String platform;
    @ApiModelProperty(value = "商户")
    private String companyFname;
    @ApiModelProperty(value = "线路")
    private String apiPrefix;
}
