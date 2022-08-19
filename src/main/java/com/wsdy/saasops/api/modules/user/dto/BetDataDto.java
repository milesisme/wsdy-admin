package com.wsdy.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BetDataDto {
    @ApiModelProperty(value = "注单号")
    private String betNum;
    @ApiModelProperty(value = "结算状态：已结算、未结算")
    private String status;
    @ApiModelProperty(value = "结果：输、赢")
    private String result;
    @ApiModelProperty(value = "站点code")
    private String siteCode;
    @ApiModelProperty(value = "站点名称")
    private String siteName;
    @ApiModelProperty(value = "站点名称")
    private String websiteName;
    @ApiModelProperty(value = "桌号")
    private String tableNo;
    @ApiModelProperty(value = "平台ID")
    private String depotId;
    @ApiModelProperty(value = "局号")
    private String serialId;
    @ApiModelProperty(value = "会员名")
    private String loginName;
    @ApiModelProperty(value = "游戏名称")
    private String gameName;
    @ApiModelProperty(value = "平台名称")
    private String depotName;
    @ApiModelProperty(value = "API线路")
    private String apiPrefix;
    @ApiModelProperty(value = "游戏分类ID")
    private Integer gameCatId;
    @ApiModelProperty(value = "投注开始时间")
    private String betStrTime;
    @ApiModelProperty(value = "投注结束时间")
    private String betEndTime;
    @ApiModelProperty(value = "游戏类型下拉框")
    private String gameCatValue;
    @ApiModelProperty(value = "游戏分类名称")
    private String gameCatName;
    @ApiModelProperty(value = "游戏平台下拉框")
    private String gameDepotValue;
    @ApiModelProperty(value = "账务开始时间")
    private String orderStrTime;
    @ApiModelProperty(value = "账务结束时间")
    private String orderEndTime;
    @ApiModelProperty(value = "下载开始时间")
    private String downloadStrTime;
    @ApiModelProperty(value = "下载结束时间")
    private String downloadEndTime;
    @ApiModelProperty(value = "商户名称")
    private String companyFname;
}
